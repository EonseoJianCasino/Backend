package com.test.webtest.global.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {
    private static final long DEFAULT_TIMEOUT_MS = Duration.ofHours(1).toMillis();

    // testId → 연결들
    private final Map<String, SseEmitter> emitterByTest = new ConcurrentHashMap<>();

    public SseEmitter register(String testId) {
        SseEmitter next = new SseEmitter(DEFAULT_TIMEOUT_MS);
        // 기존 연결이 있으면 정리 후 교체
        SseEmitter prev = emitterByTest.put(testId, next);
        if (prev != null) {
            try { prev.complete(); } catch (Exception ignore) {}
        }

        // 종료·에러 시 맵에서 제거
        Runnable cleanup = () -> remove(testId, next);
        next.onTimeout(cleanup);
        next.onCompletion(cleanup);
        next.onError(e -> cleanup.run());

        return next;
    }

    public void sendTo(String testId, String eventName, Object payload) {
        SseEmitter em = emitterByTest.get(testId);
        if (em == null) return; // 구독 없음
        try {
            em.send(SseEmitter.event().name(eventName).data(payload));
        } catch (Exception ex) {
            remove(testId, em); // 끊어졌으면 즉시 정리
        }
    }

    public void remove(String testId, SseEmitter candidate) {
        emitterByTest.compute(testId, (id, current) -> (current == candidate) ? null : current);
    }
}
