package com.test.webtest.domain.scores.service;

import com.test.webtest.domain.scores.dto.ScoresDetailResponse;
import com.test.webtest.domain.scores.dto.TotalScoreResponse;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.domain.urgentlevel.service.UrgentLevelService;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.logging.Monitored;
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
    private final UrgentLevelRepository urgentLevelRepository;

    @Override
    @Monitored("scores.calcAndSave")
    @Transactional
    public void calcAndSave(UUID testId) {
        doCalcAndSave(testId);
    }

    private void doCalcAndSave(UUID testId) {

        WebVitalsEntity web = webVitalsRepository.findByTest_Id(testId).orElse(null);
        SecurityVitalsEntity sec = securityVitalsRepository.findByTest_Id(testId).orElse(null);

        var webScore = scoreCalculator.toWebScores(web); // null 안전 (이전 답변 반영)
        var secScores = scoreCalculator.toSecurityScores(sec);

        int securityHalf = scoreCalculator.toSecurityHalfScore(sec);
        int total = scoreCalculator.total(webScore, securityHalf);
        int securityTotal = securityHalf*2;
        int webTotal = scoreCalculator.toWebHalfScore(webScore)*2;

        TestEntity test = testRepository.getReferenceById(testId);

        // Scores 저장 (점수만 저장)
        scoresRepository.findByTestId(testId).ifPresentOrElse(
                found -> {
                    found.update(
                            total, securityTotal, webTotal,
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
                },
                () -> {
                    ScoresEntity created = ScoresEntity.create(
                            test,
                            total, securityTotal, webTotal,
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
                });

        urgentLevelService.calcAndSave(testId);
    }

    @Override
    public ScoresDetailResponse getDetail(UUID testId) {
        ScoresEntity se = scoresRepository.findByTestId(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCORES_NOT_READY, "scores not found: " + testId));
        UrgentLevelEntity ue = urgentLevelRepository.findByTestId(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.URGENT_LEVEL_NOT_FOUND, "urgent level not found " + testId));
        return ScoresDetailResponse.from(se, ue);
    }

    @Override
    public TotalScoreResponse getTotal(UUID testId) {
        ScoresEntity e = scoresRepository.findByTestId(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCORES_NOT_READY, "totalScores not found: " + testId));
        return TotalScoreResponse.from(e);
    }
}
