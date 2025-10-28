package com.test.webtest.domain.logicstatus.service;

import com.test.webtest.domain.ai.service.AiRecommendationService;
import com.test.webtest.domain.logicstatus.dto.T2Payload;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.domain.webvitals.service.WebVitalsMessageService;
import com.test.webtest.global.common.constants.Channel;
import com.test.webtest.global.sse.SseEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogicStatusServiceImpl {
    private final LogicStatusRepository repo;
    private final ScoresRepository scoresRepository;

    private final SecurityVitalsRepository securityVitalsRepository;
    private final WebVitalsRepository webVitalsRepository;

    private final SecurityMessageService securityMessageService;
    private final WebVitalsMessageService webVitalsMessageService;

    private final com.test.webtest.domain.scores.service.ScoresService scoresService;
    private final AiRecommendationService aiService;
    private final SseEventPublisher sse;

    @Transactional
    public void onPartialUpdate(UUID testId, Channel channel) {
        switch (channel) {
            case WEB      -> repo.markWebReceived(testId);
            case SECURITY -> repo.markSecReceived(testId);
        }

        // 점수 계산 (동기)
        boolean scoresMarked = markScoresReadyIfEligible(testId);
        if (scoresMarked) {
            scoresService.calcAndSave(testId);
            // 커밋 이후 t2 발행 (DB 반영 완료 보장)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    var secOpt = securityVitalsRepository.findByTest_Id(testId);
                    var webOpt = webVitalsRepository.findByTest_Id(testId);
                    var scOpt  = scoresRepository.findByTestId(testId);

                    var secView = secOpt.map(securityMessageService::toView).orElse(null);
                    var webView = webOpt.map(webVitalsMessageService::toView).orElse(null);

                    int total   = scOpt.map(ScoresEntity::getTotal).orElse(0);
                    int lcp     = scOpt.map(ScoresEntity::getLcpScore).orElse(0);
                    int cls     = scOpt.map(ScoresEntity::getClsScore).orElse(0);
                    int inp     = scOpt.map(ScoresEntity::getInpScore).orElse(0);
                    int fcp     = scOpt.map(ScoresEntity::getFcpScore).orElse(0);
                    int tbt     = scOpt.map(ScoresEntity::getTbtScore).orElse(0);
                    int ttfb    = scOpt.map(ScoresEntity::getTtfbScore).orElse(0);

                    var payload = new T2Payload(
                            new T2Payload.Scores(total, lcp, cls, inp, fcp, tbt, ttfb),
                            secView,
                            webView
                    );

                    sse.publishTestPayload(testId.toString(), payload); // "t2"
                }
            });
        }

        // Ai 호출 (비동기)
        boolean aiMarked = markAiTriggeredIfEligible(testId);
        if (aiMarked) aiService.invokeAsync(testId);
    }

    private boolean markScoresReadyIfEligible(UUID testId) {
        List<Object[]> rows = repo.markScoresReady(testId);
        return !rows.isEmpty();
    }

    private boolean markAiTriggeredIfEligible(UUID testId) {
        List<Object[]> rows = repo.markAiTriggered(testId);
        return !rows.isEmpty();
    }
}
