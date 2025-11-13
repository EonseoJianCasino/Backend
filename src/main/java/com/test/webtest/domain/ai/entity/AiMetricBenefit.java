package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_metric_benefit")

public class AiMetricBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advice_id", nullable = false)
    private AiMetricAdvice advice;

    @Column(nullable = false)
    private int ord;

    @Column(columnDefinition = "text", nullable = false)
    private String text;

    protected AiMetricBenefit() {}

    public AiMetricBenefit(AiMetricAdvice advice, int ord, String text) {
        this.advice = advice;
        this.ord = ord;
        this.text = text;
    }

}
