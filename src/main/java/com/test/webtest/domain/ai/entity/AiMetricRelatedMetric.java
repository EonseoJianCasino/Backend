package com.test.webtest.domain.ai.entity;


@Entity
@Table(name = "ai_metric_related_metric")
public class AiMetricRelatedMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advice_id", nullable = false)
    private AiMetricAdvice advice;

    @Column(nullable = false)
    private int ord;

    @Column(name = "metric_text", length = 50, nullable = false)
    private String metricText;

    protected AiMetricRelatedMetric() {}

    public AiMetricRelatedMetric(AiMetricAdvice advice, int ord, String metricText) {
        this.advice = advice;
        this.ord = ord;
        this.metricText = metricText;
    }

}
