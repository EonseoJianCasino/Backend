package com.test.webtest.global.sse;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/tests")
public class SseEventController {
    @GetMapping("/{id}/events")
    public SseEmitter stream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(60L * 60 * 1000);
        CompletableFuture.runAsync(() -> {
            try{
                emitter.send(SseEmitter.event().name("ping").data("ok"));

            } catch (Exception ignore){}
        });
        return emitter;
    }
}
