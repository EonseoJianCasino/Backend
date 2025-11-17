package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;


@Getter
public class AiMetricImprovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advice_id", nullable = false)
    private AiMetricAdvice advice;

//    @Getter
    @Column(nullable = false)
    private int ord;

//    @Getter
    @Column(columnDefinition = "text", nullable = false)
    private String text;

    protected AiMetricImprovement() {}

    public AiMetricImprovement(AiMetricAdvice advice, int ord, String text) {
        this.advice = advice;
        this.ord = ord;
        this.text = text;
    }

}
