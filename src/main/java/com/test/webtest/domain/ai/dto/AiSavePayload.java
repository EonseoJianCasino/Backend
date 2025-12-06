package com.test.webtest.domain.ai.dto;

import java.util.List;

public class AiSavePayload {

    public int overall_expected_improvement;
    public int overall_total_after;
    public List<TopPriority> top_priorities;
    public List<WebElement> web_elements;
    public List<SecurityMetric> security_metrics;
    public List<MajorImprovement> major_improvements;

    public static class TopPriority {
        public int rank;
        public String status;  // good|warning|poor
        public String target_type;
        public String target_name;
        public String reason;
    }

    public static class WebElement {
        public String element_name;
        public String status;  // 양호|주의|긴급
        public String benefit_summary;
        public int expected_score_gain;
        public List<MetricDelta> metric_deltas;
        public List<String> related_metrics;
        public String benefit_detail;
    }

    public static class MetricDelta {
        public String metric;
        public int current_score;
        public int achievable_score;
        public int delta;
    }

    public static class SecurityMetric {
        public String metric_name;
        public String status;  // 양호|주의|긴급
        public String benefit_summary;
        public int delta;
        public int expected_score_gain;
        public List<String> related_metrics;
        public String benefit_detail;
    }

    public static class MajorImprovement {
        public int rank;
        public String metric;
        public String title;
        public String description;
    }
}
