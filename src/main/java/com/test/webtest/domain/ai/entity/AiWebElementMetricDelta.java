package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ai_web_element_metric_delta")
public class AiWebElementMetricDelta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "web_element_id", nullable = false)
    private AiWebElement webElement;

    @Column(name = "metric", length = 20)
    private String metric;

    @Column(name = "current_score")
    private Integer currentScore;

    @Column(name = "achievable_score")
    private Integer achievableScore;

    @Column(name = "delta")
    private Integer delta;

    protected AiWebElementMetricDelta() {}

    public AiWebElementMetricDelta(AiWebElement webElement, String metric, int currentScore, int achievableScore, int delta) {
        this.webElement = webElement;
        this.metric = metric;
        this.currentScore = currentScore;
        this.achievableScore = achievableScore;
        this.delta = delta;
    }
}

