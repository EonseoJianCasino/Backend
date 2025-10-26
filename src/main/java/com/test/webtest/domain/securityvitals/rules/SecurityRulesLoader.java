package com.test.webtest.domain.securityvitals.rules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SecurityRulesLoader {

    private final ObjectMapper objectMapper; // Spring Boot 기본 Bean

    private volatile RuleSet cached;

    public RuleSet load() {
        RuleSet local = cached;
        if (local != null) return local;
        synchronized (this) {
            if (cached == null) cached = readFromClasspath();
            return cached;
        }
    }

    private RuleSet readFromClasspath() {
        try (InputStream in = new ClassPathResource("priorities/security.rules.json").getInputStream()) {
            Map<String, Object> root = objectMapper.readValue(in, new TypeReference<>() {});
            String version = String.valueOf(root.getOrDefault("version", "1.0.0"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rulesRaw = (List<Map<String, Object>>) root.get("rules");

            List<MetricRule> metricRules = new ArrayList<>();
            for (Map<String, Object> r : rulesRaw) {
                String metric = String.valueOf(r.get("metric"));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> conds = (List<Map<String, Object>>) r.get("conditions");
                List<ConditionRule> conditionRules = new ArrayList<>();
                for (Map<String, Object> c : conds) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> when = (Map<String, Object>) c.getOrDefault("when", Map.of());
                    String message = String.valueOf(c.get("message"));
                    conditionRules.add(new ConditionRule(when, message));
                }
                metricRules.add(new MetricRule(metric, conditionRules));
            }
            return new RuleSet(version, metricRules);
        } catch (Exception e) {
            throw new IllegalStateException("security.rules.json load error", e);
        }
    }

    // ==== 모델 ====

    @Getter
    public static class RuleSet {
        private final String version;
        private final List<MetricRule> rules;

        public RuleSet(String version, List<MetricRule> rules) {
            this.version = version;
            this.rules = List.copyOf(rules);
        }

        public List<MetricRule> rulesForAllMetrics() { return rules; }
    }

    @Getter
    public static class MetricRule {
        private final String metric;               // 예: SSL_VALIDITY
        private final List<ConditionRule> conditions; // 위에서 아래 순서대로 평가 (첫 매칭 사용)

        public MetricRule(String metric, List<ConditionRule> conditions) {
            this.metric = metric;
            this.conditions = List.copyOf(conditions);
        }
    }

    @Getter
    public static class ConditionRule {
        private final Map<String, Object> when; // 간단 DSL
        private final String message;

        public ConditionRule(Map<String, Object> when, String message) {
            this.when = Map.copyOf(when);
            this.message = message;
        }
    }
}
