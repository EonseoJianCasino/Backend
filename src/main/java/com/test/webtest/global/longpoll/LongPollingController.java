package com.test.webtest.global.longpoll;

import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.global.longpoll.payload.PhaseReadyPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class LongPollingController {
    private final LongPollingManager manager;
    private final LogicStatusRepository logicStatusRepository;

    @GetMapping("/{testId}/wait")
    public DeferredResult<ResponseEntity<?>> waitFor(
      @PathVariable UUID testId,
      @RequestParam LongPollingTopic topic,
      @RequestParam(defaultValue = "60") int timeoutSec
    ){
        if (isAlreadyReady(testId, topic)) {
            var payload = new PhaseReadyPayload(topic, testId, java.time.Instant.now());
            DeferredResult<ResponseEntity<?>> dr = new DeferredResult<>();
            dr.setResult(ResponseEntity.ok(payload));
            return dr;
        }
        long timeoutMillis = timeoutSec * 1000L;
        return manager.register(new WaitKey(testId, topic), timeoutMillis);
    }

    private boolean isAlreadyReady(UUID testId, LongPollingTopic topic) {
        return logicStatusRepository.findById(testId)
                .map(status -> switch (topic) {
                    case CORE_READY -> status.isScoresReady();
                    case AI_READY -> status.isAiReady();
                })
                .orElse(false);
    }
}
