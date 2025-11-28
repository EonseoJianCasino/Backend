package com.test.webtest.global.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "webtest",
                        "env", "local"
                );
    }

    @Bean
    public MeterFilter denyJvmThreadMetrics() {
        return MeterFilter.denyNameStartsWith("jvm.threads");
    }
}
