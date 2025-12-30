package com.test.webtest.global.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DuplicateRequestMetrics {
    private final Counter duplicateRequestCounter;
    public DuplicateRequestMetrics(MeterRegistry registry) {
        this.duplicateRequestCounter = Counter.builder("webtest.duplicate_requests")
                .description("중복 요청 차단 횟수")
                .tag("type", "test")   // 필요하면 태그 추가
                .register(registry);
    }

    public void increment() {
        duplicateRequestCounter.increment();
    }
}
