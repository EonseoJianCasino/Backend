package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "ai_web_element")
public class AiWebElement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(name = "element_name", length = 50)
    private String elementName;

    @Column(name = "status", length = 20)
    private String status;  // 양호|주의|긴급

    @Column(name = "benefit_summary", columnDefinition = "text")
    private String benefitSummary;

    @Column(name = "expected_score_gain")
    private Integer expectedScoreGain;

    @Column(name = "benefit_detail", columnDefinition = "text")
    private String benefitDetail;

    @OneToMany(mappedBy = "webElement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiWebElementMetricDelta> metricDeltas = new ArrayList<>();

    @OneToMany(mappedBy = "webElement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiWebElementRelatedMetric> relatedMetrics = new ArrayList<>();

    protected AiWebElement() {}

    public static AiWebElement of(UUID testId, String elementName, String status, String benefitSummary, Integer expectedScoreGain, String benefitDetail) {
        AiWebElement e = new AiWebElement();
        e.testId = testId;
        e.elementName = elementName;
        e.status = status;
        e.benefitSummary = benefitSummary;
        e.expectedScoreGain = expectedScoreGain;
        e.benefitDetail = benefitDetail;
        return e;
    }

    public void addMetricDelta(String metric, int currentScore, int achievableScore, int delta) {
        AiWebElementMetricDelta md = new AiWebElementMetricDelta(this, metric, currentScore, achievableScore, delta);
        metricDeltas.add(md);
    }

    public void addRelatedMetric(int ord, String metricText) {
        AiWebElementRelatedMetric rm = new AiWebElementRelatedMetric(this, ord, metricText);
        relatedMetrics.add(rm);
    }
}
