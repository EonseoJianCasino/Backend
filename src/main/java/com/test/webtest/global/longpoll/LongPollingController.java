package com.test.webtest.global.longpoll;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
public class LongPollingController {
    private final LongPollingManager manager;
    public LongPollingController(LongPollingManager manger) { this.manager = manger;}

    @GetMapping("/{testId}/wait")
    public DeferredResult<ResponseEntity<?>> waitFor(
      @PathVariable UUID testId,
      @RequestParam LongPollingTopic topic,
      @RequestParam(defaultValue = "60") int timeoutSec
    ){
        long timeoutMillis = timeoutSec * 1000L;
        return manager.register(new WaitKey(testId, topic), timeoutMillis);
    }
}
