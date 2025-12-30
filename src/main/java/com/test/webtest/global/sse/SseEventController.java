package com.test.webtest.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class SseEventController {
    private final SseEmitterManager manager;
    private final SseEventPublisher publisher;

    @GetMapping(value = "/{testId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable UUID testId) {
        // 매니저에 등록
        String Id = String.valueOf(testId);
        SseEmitter emitter = manager.register(Id);

        // ping 을 날려 읽기 활동을 만든다
        publisher.ping(Id);

        return emitter;
    }
}
