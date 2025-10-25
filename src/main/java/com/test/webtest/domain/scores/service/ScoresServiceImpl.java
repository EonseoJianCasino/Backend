package com.test.webtest.domain.scores.service;

import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoresServiceImpl implements ScoresService{

    private final ScoresRepository scoresRepository;
    private final WebVitalsRepository webVitalsRepository;
    private final SecurityVitalsRepository securityVitalsRepository;
    private final TestRepository testRepository;
    private final ScoreCalculator scoreCalculator;

    @Async("logicExecutor")
    public void calcAndSaveAsync(UUID testId) {
        log.info("[SCORES] Calculating scores for testId = {}", testId);

        WebVitalsEntity web = webVitalsRepository.findByTestId(testId)
                .orElse(null); // 웹 없을 수도 있음(가중치 0으로 처리)
        SecurityVitalsEntity sec = securityVitalsRepository.findByTestId(testId)
                .orElse(null);

        // 웹 지표 100점화 {lcp, cls, inp, fcp, tbt, ttfb}
        var webScore = scoreCalculator.toWebScores(web);

        // 보안 50점화
        int securityHalf = scoreCalculator.toSecurityHalfScore(sec);

        // 총점 = 웹세부 평균 등으로 0~100 만들고 *0.5) + 보안
        int total = scoreCalculator.totalFrom(webScore, securityHalf);

        TestEntity test = testRepository.getReferenceById(testId);
        scoresRepository.findByTestId(testId).ifPresentOrElse(
            found -> {
                found.update(total,
                        webScore.lcp(), webScore.cls(), webScore.inp(),
                        webScore.fcp(), webScore.tbt(), webScore.ttfb());
                log.info("[SCORES] updated testId={} total={}", testId, total);
            },
            ()->{
                ScoresEntity created = ScoresEntity.create(
                        test, total,
                        webScore.lcp(), webScore.cls(), webScore.inp(),
                        webScore.fcp(), webScore.tbt(), webScore.ttfb()
                );
                scoresRepository.save(created);
                log.info("[SCORES] inserted testId={} total={}", testId, total);
            }
        );
    }
}
