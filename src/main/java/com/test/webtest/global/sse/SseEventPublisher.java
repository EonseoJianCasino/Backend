package com.test.webtest.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseEventPublisher {
    private final SseEmitterManager manager;

    public void ping(String testId) {
        manager.sendTo(testId, "ping", "ok");
    }
    public void publishTestPayload(String testId, Object dto) {
        manager.sendTo(testId, "t2", dto);
    }
    public void publishAiAnalysisResult(String testId, Object dto) {
        manager.sendTo(testId, "t3_ai", dto);
    }
    public void done(String testId) {
        manager.sendTo(testId, "done", "END");
    }
}
