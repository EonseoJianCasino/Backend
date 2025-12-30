package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "ai_analysis_summary")
public class AiAnalysisSummary {

    @Id
    private UUID id;

    @Column(name = "test_id", nullable = false, unique = true)
    private UUID testId;

    @Column(name = "overall_expected_improvement", nullable = false)
    private Integer overallExpectedImprovement;

    @Column(name = "overall_total_after")
    private Integer overallTotalAfter;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public AiAnalysisSummary() {}

    public static AiAnalysisSummary of(
            UUID testId,
            int overallExpectedImprovement,
            Integer overallTotalAfter
    ) {
        AiAnalysisSummary s = new AiAnalysisSummary();
        s.id = UUID.randomUUID();
        s.testId = testId;
        s.overallExpectedImprovement = overallExpectedImprovement;
        s.overallTotalAfter = overallTotalAfter;
        return s;
    }
}

