package com.test.webtest.global.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {
    private static final long DEFAULT_TIMEOUT_MS = Duration.ofHours(1).toMillis();

    // testId → 연결들
    private final Map<String, Set<SseEmitter>> emittersByTest = new ConcurrentHashMap<>();

    public SseEmitter register(String testId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        emittersByTest
                .computeIfAbsent(testId, k -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        emitter.onTimeout(() -> remove(testId, emitter));
        emitter.onCompletion(() -> remove(testId, emitter));
        emitter.onError(e -> remove(testId, emitter)); // 네트워크 에러 포함
        return emitter;
    }

    public void sendTo(String testId, String eventName, Object payload) {
        var set = emittersByTest.get(testId);
        if (set == null) return;
        set.forEach(em -> {
            try {
                em.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException ex) {
                // 끊어진 연결 정리
                remove(testId, em);
            }
        });
    }

    public void remove(String testId, SseEmitter emitter) {
        var set = emittersByTest.get(testId);
        if (set == null) return;
        set.remove(emitter);
        if (set.isEmpty()) emittersByTest.remove(testId);
    }
}
