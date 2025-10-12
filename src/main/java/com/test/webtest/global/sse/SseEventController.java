package com.test.webtest.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class SseEventController {

    private final SseEmitterManager manager;
    private final SseEventPublisher publisher;

    @GetMapping("/{id}/events")
    public SseEmitter stream(@PathVariable("id") String testId) {
        SseEmitter emitter = manager.register(testId);
        // 최초 연결 즉시 핑
        publisher.ping(testId);
        return emitter;
    }
}
