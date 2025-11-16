package com.test.webtest.domain.priorities.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * 우선순위 엔티티
 */

@Entity
@Table(name = "priorities")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrioritiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "test_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID testId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "metric", nullable = false, length = 50)
    private String metric;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
