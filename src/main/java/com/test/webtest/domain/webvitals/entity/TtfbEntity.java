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
public class TtfbEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", unique = true, nullable = false)
    private TestEntity test;

    @Column(name = "entry_type")
    private String entryType;

    @Column(name = "start_time")
    private Integer startTime;

    @Column(name = "response_start")
    private Double responseStart;

    @Column(name = "request_start")
    private Double reqeustStart;

    @Column(name = "domain_lookup_start")
    private Double domainLookupStart;

    @Column(name = "connect_start")
    private Double connectStart;

    @Column(name = "connect_end")
    private Double connectEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 팩토리 메서드 - WebVitals 생성
    public static TtfbEntity create(TestEntity test, String entryType, Integer startTime, Double responseStart, Double requestStart, Double domainLookupStart, Double connectStart, Double connectEnd) {
        validateMetricValue(responseStart, "response start");
        validateMetricValue(requestStart, "request start");
        validateMetricValue(domainLookupStart, "domain lookup start");
        validateMetricValue(connectStart, "connect start");
        validateMetricValue(connectEnd, "connect end");

        return TtfbEntity.builder()
                .id(UUID.randomUUID())
                .test(test)  // @MapsId를 사용하므로 test만 설정하면 testId는 자동으로 매핑됨
                .entryType(entryType)
                .startTime(startTime)
                .responseStart(responseStart)
                .domainLookupStart(domainLookupStart)
                .connectStart(connectStart)
                .connectEnd(connectEnd)
                .build();
    }

    public void updateFrom(String entryType, Integer startTime, Double responseStart, Double requestStart, Double domainLookupStart, Double connectStart, Double connectEnd) {
        this.entryType = entryType;
        this.startTime = startTime;
        this.responseStart = responseStart;
        this.domainLookupStart = domainLookupStart;
        this.connectStart = connectStart;
        this.connectEnd = connectEnd;
    }

    // 지표값 검증 메서드 (음수 불가, NaN 불가)
    private static void validateMetricValue(Double v, String name) {
        if (v == null) return;           // null 허용
        if (Double.isNaN(v)) throw new IllegalArgumentException(name + " 값은 NaN일 수 없습니다.");
        if (v < 0)           throw new IllegalArgumentException(name + " 값은 음수일 수 없습니다. 입력값: " + v);
    }
}
