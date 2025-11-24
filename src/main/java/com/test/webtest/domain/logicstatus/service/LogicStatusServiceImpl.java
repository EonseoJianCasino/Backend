package com.test.webtest.domain.logicstatus.service;

import com.test.webtest.domain.ai.service.AiRecommendationService;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.domain.webvitals.service.WebVitalsMessageService;
import com.test.webtest.global.common.constants.Channel;
import com.test.webtest.global.error.exception.ConcurrencyException;
import com.test.webtest.global.longpoll.LongPollingManager;
import com.test.webtest.global.longpoll.LongPollingTopic;
import com.test.webtest.global.longpoll.TxAfterCommit;
import com.test.webtest.global.longpoll.WaitKey;
import com.test.webtest.global.longpoll.payload.PhaseReadyPayload;
import jakarta.persistence.LockTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PessimisticLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogicStatusServiceImpl {
    private final LogicStatusRepository repo;
    private final ScoresRepository scoresRepository;
    private final SecurityVitalsRepository securityVitalsRepository;
    private final WebVitalsRepository webVitalsRepository;
    private final SecurityMessageService securityMessageService;
    private final WebVitalsMessageService webVitalsMessageService;
    private final com.test.webtest.domain.scores.service.ScoresService scoresService;
    private final AiRecommendationService aiService;
    private final LongPollingManager longPollingManager;

    @Transactional
    public void onPartialUpdate(UUID testId, Channel channel) {
        try {
            // 1) 채널 플래그 마킹 (조건부 UPDATE)
            switch (channel) {
                case WEB -> repo.markWebReceived(testId);
                case SECURITY -> repo.markSecReceived(testId);
            }

            // 2) 점수 준비 조건 충족 시 집계
            boolean scoresMarked = markScoresReadyIfEligible(testId);
            if (scoresMarked) {
                scoresService.calcAndSave(testId);

                // 커밋 후 CORE_READY 롱폴 알림
                TxAfterCommit.run(() -> {
                    log.info("[LONGPOLL][CORE_READY] triggered for testId={}", testId);
                    longPollingManager.complete(
                            new WaitKey(testId, LongPollingTopic.CORE_READY),
                            new PhaseReadyPayload(LongPollingTopic.CORE_READY, testId, Instant.now()));
                });
            }

            // 3) AI 트리거 (조건 충족 시)

            if (canStartAi(testId)) {
                aiService.invokeAsync(testId);
            }

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

    private boolean canStartAi(UUID testId) {
        return repo.findById(testId)
                .map(s -> s.isWebReceived() && s.isSecReceived() && s.isScoresReady() && !s.isAiTriggered())
                .orElse(false);
    }
}
