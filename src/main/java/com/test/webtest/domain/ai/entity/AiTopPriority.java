package com.test.webtest.domain.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "ai_top_priority")
public class AiTopPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false)
    private int rank;

    @Column(name = "status", length = 20)
    private String status;  // good|warning|poor

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_name", length = 100)
    private String targetName;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    protected AiTopPriority() {}

    public static AiTopPriority of(UUID testId, int rank, String status, String targetType, String targetName, String reason) {
        AiTopPriority p = new AiTopPriority();
        p.testId = testId;
        p.rank = rank;
        p.status = status;
        p.targetType = targetType;
        p.targetName = targetName;
        p.reason = reason;
        return p;
    }
}
