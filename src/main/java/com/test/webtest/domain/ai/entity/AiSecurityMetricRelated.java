package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ai_security_metric_related")
public class AiSecurityMetricRelated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_metric_id", nullable = false)
    private AiSecurityMetric securityMetric;

    @Column(nullable = false)
    private int ord;

    @Column(name = "metric_text", length = 50)
    private String metricText;

    protected AiSecurityMetricRelated() {}

    public AiSecurityMetricRelated(AiSecurityMetric securityMetric, int ord, String metricText) {
        this.securityMetric = securityMetric;
        this.ord = ord;
        this.metricText = metricText;
    }
}

