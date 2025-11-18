package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ai_top_priority")
public class AiTopPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private AiAnalysisSummary summary;

    @Column(nullable = false)
    private int rank;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_name", length = 100)
    private String targetName;

    @Column(name = "expected_gain", nullable = false)
    private Integer expectedGain;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    protected AiTopPriority() {}

    public AiTopPriority(AiAnalysisSummary summary, int rank, String targetType, String targetName, int expectedGain, String reason) {
        this.summary = summary;
        this.rank = rank;
        this.targetType = targetType;
        this.targetName = targetName;
        this.expectedGain = expectedGain;
        this.reason = reason;
    }
}

