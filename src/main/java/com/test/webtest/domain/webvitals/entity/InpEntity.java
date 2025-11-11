package com.test.webtest.domain.webvitals.entity;

import com.test.webtest.domain.test.entity.TestEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * WebVitalsEntity - Web Vitals 원본 지표를 저장하는 엔티티입니다.
 * ERD의 'web_vitals' 테이블과 매핑됩니다.
 */

@Entity
@Table(name = "web_vitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class InpEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", unique = true, nullable = false)
    private TestEntity test;

    @Column(name = "entry_type")
    private String entryType;

    @Column(name = "name")
    private String name;

    @Column(name = "start_time")
    private Double startTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "processing_start")
    private Double processingStart;

    @Column(name = "processing_end")
    private Double processingEnd;

    @Column(name = "interaction_id")
    private Integer interactionId;

    @Column(name = "target")
    private String target;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 팩토리 메서드 - WebVitals 생성
    public static InpEntity create(TestEntity test, String entryType, String name, Double startTime, Integer duration, Double processingStart, Double processingEnd, Integer interactionId, String target) {
        validateMetricValue(startTime, "start time");
        validateMetricValue(processingStart, "processing start");
        validateMetricValue(processingEnd, "processing end");

        return InpEntity.builder()
                .id(UUID.randomUUID())
                .test(test)  // @MapsId를 사용하므로 test만 설정하면 testId는 자동으로 매핑됨
                .entryType(entryType)
                .name(name)
                .startTime(startTime)
                .duration(duration)
                .processingStart(processingStart)
                .processingEnd(processingEnd)
                .interactionId(interactionId)
                .target(target)
                .build();
    }

    public void updateFrom(String entryType, String name, Double startTime, Integer duration, Double processingStart, Double processingEnd, Integer interactionId, String target) {
        this.entryType = entryType;
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
        this.processingStart = processingStart;
        this.processingEnd = processingEnd;
        this.interactionId = interactionId;
        this.target = target;
    }

    // 지표값 검증 메서드 (음수 불가, NaN 불가)
    private static void validateMetricValue(Double v, String name) {
        if (v == null) return;           // null 허용
        if (Double.isNaN(v)) throw new IllegalArgumentException(name + " 값은 NaN일 수 없습니다.");
        if (v < 0)           throw new IllegalArgumentException(name + " 값은 음수일 수 없습니다. 입력값: " + v);
    }
}
