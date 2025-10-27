package com.test.webtest.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * 도메인 이벤트명을 붙여 프론트엔드로 전송하는 퍼블리셔
 */
@Component
@RequiredArgsConstructor
public class SseEventPublisher {
    private final SseEmitterManager manager;

    public void ping(String testId) {
        manager.sendTo(testId, "ping", Map.of(
                "ok",true,"ts", Instant.now().toString()
        ));
    }

    public void publishWebSnapshot(String testId, Object dto) {
        manager.sendTo(testId, "t1_web", dto);
    }

    public void publishSecuritySnapshot(String testId, Object dto) {
        manager.sendTo(testId, "t1_sec", dto);
    }

    public void publishAiAnalysisResult(String testId, Object dto) {
        manager.sendTo(testId, "t3_ai", dto);
    }

    public void done(String testId) {
        manager.sendTo(testId, "done", Map.of(
                "end",true,"ts", Instant.now().toString()
        ));
    }
}
