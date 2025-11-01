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
import com.test.webtest.global.error.exception.ConcurrencyException;
import com.test.webtest.global.sse.SseEventPublisher;
import jakarta.persistence.LockTimeoutException;
import org.hibernate.PessimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
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
        try {
            // 1) 채널 플래그 마킹 (조건부 UPDATE)
            switch (channel) {
                case WEB      -> repo.markWebReceived(testId);
                case SECURITY -> repo.markSecReceived(testId);
            }

            // 2) 점수 준비 조건 충족 시 집계
            boolean scoresMarked = markScoresReadyIfEligible(testId);
            if (scoresMarked) {
                scoresService.calcAndSave(testId);

                // 3) 커밋 이후 T2 전송
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

                        sse.publishTestPayload(testId.toString(), payload);
                    }
                });
            }

            // 4) AI 트리거 (조건 충족 시)
            boolean aiMarked = markAiTriggeredIfEligible(testId);
            if (aiMarked) aiService.invokeAsync(testId);

        } catch (PessimisticLockException | LockTimeoutException
                 | PessimisticLockingFailureException e) {
            // DB 락/타임아웃 → 409로 매핑
            throw new ConcurrencyException("동시 처리 충돌: testId=" + testId);
        }
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
