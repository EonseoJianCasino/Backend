package com.test.webtest.global.longpoll;

import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.error.model.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LongPollingManager {
    private final Map<WaitKey, Set<DeferredResult<ResponseEntity<?>>>> waiters = new ConcurrentHashMap<>();

    public DeferredResult<ResponseEntity<?>> register(WaitKey key, long timeoutMillis) {
        DeferredResult<ResponseEntity<?>> dr = new DeferredResult<>(timeoutMillis);
        waiters.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(dr);

        Runnable cleanup = () -> {
            Set<DeferredResult<ResponseEntity<?>>> set = waiters.getOrDefault(key, Collections.emptySet());
            set.remove(dr);
            if (set.isEmpty()) waiters.remove(key);
        };

        dr.onTimeout(() -> {dr.setResult(ResponseEntity.noContent().build()); cleanup.run();});
        dr.onError(ex -> {dr.setResult(ResponseEntity.internalServerError().body(ex.getMessage())); cleanup.run();});
        dr.onCompletion(cleanup);

        return dr;
    }

    public void complete(WaitKey key, Object payLoad) {
        Set<DeferredResult<ResponseEntity<?>>> set = waiters.remove(key); // 다음 요청으로 동일한 key 가 들어올 경우 중복되는 것을 방지
        if(set == null) return;
        for (DeferredResult<ResponseEntity<?>> dr : set) {
            if (!dr.hasResult()) dr.setResult(ResponseEntity.ok(payLoad)); // waiter 이 비었다면 응답 값을 설정함
        }
    }

    public void completeError(WaitKey key, ErrorCode ec, String messageOverride) {
        Set<DeferredResult<ResponseEntity<?>>> set = waiters.remove(key);
        if (set == null) return;

        String traceId = MDC.get("traceId");
        ErrorResponse body = ErrorResponse.of(ec, messageOverride, traceId);

        for (DeferredResult<ResponseEntity<?>> dr : set) {
            if (!dr.hasResult()) {
                dr.setResult(ResponseEntity
                        .status(ec.httpStatus)
                        .body(body));
            }
        }
    }
}
