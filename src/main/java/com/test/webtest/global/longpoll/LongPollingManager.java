package com.test.webtest.global.longpoll;

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
        Set<DeferredResult<ResponseEntity<?>>> set = waiters.remove(key); // 다음 요청으로 동일한 key가 들어올 경우 중복되는 것을 방지
        if(set == null) return;
        for (DeferredResult<ResponseEntity<?>> dr : set) {
            if (!dr.hasResult()) dr.setResult(ResponseEntity.ok(payLoad));
        }
    }
}
