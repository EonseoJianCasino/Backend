package com.test.webtest.domain.ai.dto;

import java.util.List;

public class AiSavePayload {

    public int overall_expected_improvement;
    public List<WebElement> web_elements;
    public List<SecurityMetric> security_metrics;
    public Normalization normalization;
    public List<MajorImprovement> major_improvements;
    public List<TopPriority> top_priorities;

    public static class WebElement {
        public String element_name;
        public int expected_gain;
        public List<String> related_metrics;
        public List<MetricDelta> metric_deltas;
        public String detailed_plan;
        public String benefit_summary;
    }

    public static class MetricDelta {
        public String metric;
        public int current_score;
        public int achievable_score;
        public int delta;
    }

    public static class SecurityMetric {
        public String metric;
        public int current_score;
        public int achievable_score;
        public int delta;
        public int expected_gain;
        public String improvement_plan;
        public String expected_benefit;
        public String impact_title;
        public String impact_description;
    }

    public static class Normalization {
        public int web_total_after;
        public int security_total_after;
        public int overall_total_after;
    }

    public static class MajorImprovement {
        public String metric;
        public String title;
        public String description;
    }

    public static class TopPriority {
        public int rank;
        public String target_type;
        public String target_name;
        public int expected_gain;
        public String reason;
    }
}
