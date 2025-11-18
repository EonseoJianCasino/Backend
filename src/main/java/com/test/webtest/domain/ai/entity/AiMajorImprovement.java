package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ai_major_improvement")
public class AiMajorImprovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private AiAnalysisSummary summary;

    @Column(nullable = false)
    private int ord;

    @Column(name = "metric", length = 50)
    private String metric;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "description", length = 100)
    private String description;

    protected AiMajorImprovement() {}

    public AiMajorImprovement(AiAnalysisSummary summary, int ord, String metric, String title, String description) {
        this.summary = summary;
        this.ord = ord;
        this.metric = metric;
        this.title = title;
        this.description = description;
    }
}

