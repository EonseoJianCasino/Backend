package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "ai_major_improvement")
public class AiMajorImprovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false)
    private int ord;

    @Column(name = "metric", length = 50)
    private String metric;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "description", length = 100)
    private String description;

    protected AiMajorImprovement() {}

    public static AiMajorImprovement of(UUID testId, int ord, String metric, String title, String description) {
        AiMajorImprovement m = new AiMajorImprovement();
        m.testId = testId;
        m.ord = ord;
        m.metric = metric;
        m.title = title;
        m.description = description;
        return m;
    }
}

