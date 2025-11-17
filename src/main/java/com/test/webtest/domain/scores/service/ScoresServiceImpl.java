package com.test.webtest.domain.scores.service;

import com.test.webtest.domain.scores.dto.ScoresDetailResponse;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.urgentlevel.service.UrgentLevelService;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import com.test.webtest.global.common.util.WebVitalsThreshold;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoresServiceImpl implements ScoresService {

    private final ScoresRepository scoresRepository;
    private final WebVitalsRepository webVitalsRepository;
    private final SecurityVitalsRepository securityVitalsRepository;
    private final TestRepository testRepository;
    private final ScoreCalculator scoreCalculator;
    private final UrgentLevelService urgentLevelService; // 새 서비스


    @Override
    @Transactional
    public void calcAndSave(UUID testId) {
        doCalcAndSave(testId);
    }

    private void doCalcAndSave(UUID testId) {
        log.info("[SCORES] Calculating scores for testId={}", testId);

        WebVitalsEntity web = webVitalsRepository.findByTest_Id(testId).orElse(null);
        SecurityVitalsEntity sec = securityVitalsRepository.findByTest_Id(testId).orElse(null);

        var webScore = scoreCalculator.toWebScores(web); // null 안전 (이전 답변 반영)
        var secScores = scoreCalculator.toSecurityScores(sec);

        int securityHalf = scoreCalculator.toSecurityHalfScore(sec);
        int total = scoreCalculator.total(webScore, securityHalf);

        // Status 계산 (WebVitalsEntity의 원본 수치 사용)
        String lcpStatus = scoreCalculator.calculateStatus(web != null ? web.getLcp() : null, WebVitalsThreshold.LCP);
        String clsStatus = scoreCalculator.calculateStatus(web != null ? web.getCls() : null, WebVitalsThreshold.CLS);
        String inpStatus = scoreCalculator.calculateStatus(web != null ? web.getInp() : null, WebVitalsThreshold.INP);
        String fcpStatus = scoreCalculator.calculateStatus(web != null ? web.getFcp() : null, WebVitalsThreshold.FCP);
        String ttfbStatus = scoreCalculator.calculateStatus(web != null ? web.getTtfb() : null,
                WebVitalsThreshold.TTFB);

        TestEntity test = testRepository.getReferenceById(testId);

        // Scores 저장 (점수만 저장)
        scoresRepository.findByTestId(testId).ifPresentOrElse(
                found -> {
                    found.update(
                            total,
                            webScore.lcp(), webScore.cls(), webScore.inp(),
                            webScore.fcp(), webScore.ttfb(),
                            secScores.hsts(),
                            secScores.frameAncestorsOrXfo(),
                            secScores.ssl(),
                            secScores.xcto(),
                            secScores.referrerPolicy(),
                            secScores.cookies(),
                            secScores.csp()
                    );
                    log.info("[SCORES] updated testId={} total={}", testId, total);
                },
                () -> {
                    ScoresEntity created = ScoresEntity.create(
                            test,
                            total,
                            webScore.lcp(), webScore.cls(), webScore.inp(),
                            webScore.fcp(), webScore.ttfb(),
                            secScores.hsts(),
                            secScores.frameAncestorsOrXfo(),
                            secScores.ssl(),
                            secScores.xcto(),
                            secScores.referrerPolicy(),
                            secScores.cookies(),
                            secScores.csp()
                    );
                    scoresRepository.save(created);
                    log.info("[SCORES] inserted testId={} total={}", testId, total);
                });

        urgentLevelService.calcAndSave(testId);
    }

    @Override
    public ScoresDetailResponse getDetail(UUID testId) {
        ScoresEntity e = scoresRepository.findByTestId(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCORES_NOT_READY, "scores not found: " + testId));
        return ScoresDetailResponse.from(e);
    }

    @Override
    public int getTotal(UUID testId) {
        ScoresEntity e = scoresRepository.findByTestId(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCORES_NOT_READY, "scores not found: " + testId));
        return e.getTotal() == null ? 0 : e.getTotal();
    }
}
