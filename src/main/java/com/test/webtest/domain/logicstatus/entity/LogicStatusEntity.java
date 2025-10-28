package com.test.webtest.domain.logicstatus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

//import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
//import java.time.OffsetDateTime;

@Entity
@Table(name = "logic_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class LogicStatusEntity {
    @Id
    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(name = "web_received", nullable = false)
    private boolean webReceived;

    @Column(name = "sec_received", nullable = false)
    private boolean secReceived;

    @Column(name = "scores_ready", nullable = false)
    private boolean scoresReady;

    @Column(name = "ai_triggered", nullable = false)
    private boolean aiTriggered;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static LogicStatusEntity create(UUID testId) {
        return LogicStatusEntity.builder()
                .testId(testId)
                .webReceived(false)
                .secReceived(false)
                .scoresReady(false)
                .aiTriggered(false)
                .build();
    }
}
