package com.test.webtest.global.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class PipelineMetrics {
    private final Counter coreReadySuccess;
    private final Counter coreReadyFailure;
    private final Counter aiReadySuccess;
    private final Counter aiReadyFailure;

    public PipelineMetrics(MeterRegistry registry) {
        this.coreReadySuccess = Counter.builder("webtest.pipeline.core_ready.success")
                .description("CORE_READY 응답 성공 횟수")
                .register(registry);
        this.coreReadyFailure = Counter.builder("webtest.pipeline.core_ready.failure")
                .description("CORE_READY 응답 실패/에러 횟수")
                .register(registry);

        this.aiReadySuccess = Counter.builder("webtest.pipeline.ai_ready.success")
                .description("AI_READY 응답 성공 횟수")
                .register(registry);

        this.aiReadyFailure = Counter.builder("webtest.pipeline.ai_ready.failure")
                .description("AI_READY 응답 실패/에러 횟수")
                .register(registry);
    }
    public void incCoreReadySuccess() {
        coreReadySuccess.increment();
    }

    public void incCoreReadyFailure() {
        coreReadyFailure.increment();
    }

    public void incAiReadySuccess() {
        aiReadySuccess.increment();
    }

    public void incAiReadyFailure() {
        aiReadyFailure.increment();
    }
}
