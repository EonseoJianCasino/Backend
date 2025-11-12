package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_expectations")
public class AiExpectation {
    @Id
    private UUID id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false, length = 50) // LCP, CLS, INP...
    private String metric;

//    @Lob
//    @Column(nullable = false)
//    private String content;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;


    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static AiExpectation of(UUID testId, String metric, String content) {
        AiExpectation e = new AiExpectation();
        e.id = UUID.randomUUID();
        e.testId = testId;
        e.metric = metric;
        e.content = content;
        return e;
    }
    // getters/setters
}

