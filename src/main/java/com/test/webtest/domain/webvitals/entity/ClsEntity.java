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
@Table(name = "cls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ClsEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", unique = true, nullable = false)
    private TestEntity test;

    @Column(name = "entry_type")
    private String entryType;

    @Column(name = "start_time")
    private Double startTime;

    @Column(name = "cls_value")
    private Double clsValue;

    @Column(name = "had_recent_input")
    private Boolean hadRecentInput;

    @Column(name = "sources", columnDefinition = "text")
    private String sources;

    @Column(name = "previous_rect", columnDefinition = "text")
    private String previousRect;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 팩토리 메서드 - WebVitals 생성
    public static ClsEntity create(TestEntity test, String entryType, Double startTime, Double clsValue,
            Boolean hadRecentInput, String sources, String previousRect) {
        validateMetricValue(startTime, "start time");
        validateMetricValue(clsValue, "CLS value");

        return ClsEntity.builder()
                .id(UUID.randomUUID())
                .test(test) // @MapsId를 사용하므로 test만 설정하면 testId는 자동으로 매핑됨
                .entryType(entryType)
                .startTime(startTime)
                .clsValue(clsValue)
                .hadRecentInput(hadRecentInput)
                .sources(sources)
                .previousRect(previousRect)
                .build();
    }

    public void updateFrom(String entryType, Double startTime, Double clsValue, Boolean hadRecentInput, String sources,
            String previousRect) {
        this.entryType = entryType;
        this.startTime = startTime;
        this.clsValue = clsValue;
        this.hadRecentInput = hadRecentInput;
        this.sources = sources;
        this.previousRect = previousRect;
    }

    // 지표값 검증 메서드 (음수 불가, NaN 불가)
    private static void validateMetricValue(Double v, String name) {
        if (v == null)
            return; // null 허용
        if (Double.isNaN(v))
            throw new IllegalArgumentException(name + " 값은 NaN일 수 없습니다.");
        if (v < 0)
            throw new IllegalArgumentException(name + " 값은 음수일 수 없습니다. 입력값: " + v);
    }
}
