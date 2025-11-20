package com.test.webtest.domain.ai.dto;

import java.util.List;

public class MetricAdviceResponse {

    private String metric; // "LCP", "CLS", ...
    private String summary;
    private String estimatedLabel;
    private List<String> potentialImprovements;
    private List<String> expectedBenefits;
    private List<String> relatedMetrics;

    // 생성자 / 게터 세터
    public MetricAdviceResponse(String metric,
                                String summary,
                                String estimatedLabel,
                                List<String> potentialImprovements,
                                List<String> expectedBenefits,
                                List<String> relatedMetrics) {
        this.metric = metric;
        this.summary = summary;
        this.estimatedLabel = estimatedLabel;
        this.potentialImprovements = potentialImprovements;
        this.expectedBenefits = expectedBenefits;
        this.relatedMetrics = relatedMetrics;
    }

    public MetricAdviceResponse() {}

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEstimatedLabel() { return estimatedLabel; }
    public void setEstimatedLabel(String estimatedLabel) { this.estimatedLabel = estimatedLabel; }

    public List<String> getPotentialImprovements() { return potentialImprovements; }
    public void setPotentialImprovements(List<String> potentialImprovements) { this.potentialImprovements = potentialImprovements; }

    public List<String> getExpectedBenefits() { return expectedBenefits; }
    public void setExpectedBenefits(List<String> expectedBenefits) { this.expectedBenefits = expectedBenefits; }

    public List<String> getRelatedMetrics() { return relatedMetrics; }
    public void setRelatedMetrics(List<String> relatedMetrics) { this.relatedMetrics = relatedMetrics; }



}
