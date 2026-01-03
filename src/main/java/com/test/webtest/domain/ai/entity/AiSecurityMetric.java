package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "ai_security_metric")
public class AiSecurityMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false)
    private int rank;

    @Column(name = "metric_name", length = 50)
    private String metricName;

    @Column(name = "status", length = 20)
    private String status;  // 양호|주의|긴급

    @Column(name = "benefit_summary", columnDefinition = "text")
    private String benefitSummary;

    @Column(name = "delta")
    private Integer delta;

    @Column(name = "expected_score_gain")
    private Integer expectedScoreGain;

    @Column(name = "benefit_detail", columnDefinition = "text")
    private String benefitDetail;

    @OneToMany(mappedBy = "securityMetric", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiSecurityMetricRelated> relatedMetrics = new ArrayList<>();

    protected AiSecurityMetric() {}

    public static AiSecurityMetric of(UUID testId, int rank, String metricName, String status, String benefitSummary, Integer delta, Integer expectedScoreGain, String benefitDetail) {
        AiSecurityMetric m = new AiSecurityMetric();
        m.testId = testId;
        m.rank = rank;
        m.metricName = metricName;
        m.status = status;
        m.benefitSummary = benefitSummary;
        m.delta = delta;
        m.expectedScoreGain = expectedScoreGain;
        m.benefitDetail = benefitDetail;
        return m;
    }

    public void addRelatedMetric(int ord, String metricText) {
        AiSecurityMetricRelated rm = new AiSecurityMetricRelated(this, ord, metricText);
        relatedMetrics.add(rm);
    }
}
