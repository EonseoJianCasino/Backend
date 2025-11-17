package com.test.webtest.domain.ai.entity;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Entity
@Table(name = "ai_metric_advice")
public class AiMetricAdvice {

    @Id
    private UUID id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

//    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Metric metric; // LCP / CLS / ...

//    @Getter
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

//    @Getter
    @Column(name = "estimated_label", length = 255)
    private String estimatedLabel; // estimated_score_improvement 원문

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

//    @Getter
    @OneToMany(mappedBy = "advice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiMetricImprovement> improvements = new ArrayList<>();
//
//    @Getter
    @OneToMany(mappedBy = "advice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiMetricBenefit> benefits = new ArrayList<>();

//    @Getter
    @OneToMany(mappedBy = "advice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiMetricRelatedMetric> relatedMetrics = new ArrayList<>();

    public AiMetricAdvice() {}

    public static AiMetricAdvice of(UUID testId, Metric metric,
                                    String summary, String estimatedLabel) {
        AiMetricAdvice a = new AiMetricAdvice();
        a.id = UUID.randomUUID();
        a.testId = testId;
        a.metric = metric;
        a.summary = summary;
        a.estimatedLabel = estimatedLabel;
        return a;
    }

    // getter/setter들...
    public void addImprovement(int ord, String text) {
        AiMetricImprovement i = new AiMetricImprovement(this, ord, text);
        improvements.add(i);
    }

    public void addBenefit(int ord, String text) {
        AiMetricBenefit b = new AiMetricBenefit(this, ord, text);
        benefits.add(b);
    }

    public void addRelatedMetric(int ord, String metricText) {
        AiMetricRelatedMetric r = new AiMetricRelatedMetric(this, ord, metricText);
        relatedMetrics.add(r);
    }

//    public Metric getMetric() {
//        return metric;
//    }
//
//    public String getSummary() {
//        return summary;
//    }
//
//    public String getEstimatedLabel() {
//        return estimatedLabel;
//    }
//
//    public List<AiMetricImprovement> getImprovements() {
//        return improvements;
//    }
//
//    public List<AiMetricBenefit> getBenefits() {
//        return benefits;
//    }
//
//    public List<AiMetricRelatedMetric> getRelatedMetrics() {
//        return relatedMetrics;
//    }

}
