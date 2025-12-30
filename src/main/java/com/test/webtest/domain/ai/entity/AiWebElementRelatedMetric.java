package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ai_web_element_related_metric")
public class AiWebElementRelatedMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "web_element_id", nullable = false)
    private AiWebElement webElement;

    @Column(nullable = false)
    private int ord;

    @Column(name = "metric_text", length = 50)
    private String metricText;

    protected AiWebElementRelatedMetric() {}

    public AiWebElementRelatedMetric(AiWebElement webElement, int ord, String metricText) {
        this.webElement = webElement;
        this.ord = ord;
        this.metricText = metricText;
    }
}

