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
public class WebVitalsEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", unique = true, nullable = false)
    private TestEntity test;

    @Column(name = "lcp")
    private Double lcp;

    @Column(name = "cls")
    private Double cls;

    @Column(name = "inp")
    private Double inp;

    @Column(name = "fcp")
    private Double fcp;

    @Column(name = "ttfb")
    private Double ttfb;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 팩토리 메서드 - WebVitals 생성
    public static WebVitalsEntity create(TestEntity test, Double lcp, Double cls, Double inp, Double fcp, Double ttfb) {
        validateMetricValue(lcp, "LCP");
        validateMetricValue(cls, "CLS");
        validateMetricValue(inp, "INP");
        validateMetricValue(fcp, "FCP");
        validateMetricValue(ttfb, "TTFB");

        return WebVitalsEntity.builder()
                .id(UUID.randomUUID())
                .test(test)  // @MapsId를 사용하므로 test만 설정하면 testId는 자동으로 매핑됨
                .lcp(lcp)
                .cls(cls)
                .inp(inp)
                .fcp(fcp)
                .ttfb(ttfb)
                .build();
    }

    public void updateFrom(Double lcp, Double cls, Double inp,
                           Double fcp, Double ttfb) {
        this.lcp = lcp;
        this.cls = cls;
        this.fcp = fcp;
        this.ttfb = ttfb;
        this.inp = inp;
    }

    // 지표값 검증 메서드 (음수 불가, NaN 불가)
    private static void validateMetricValue(Double v, String name) {
        if (v == null) return;           // null 허용
        if (Double.isNaN(v)) throw new IllegalArgumentException(name + " 값은 NaN일 수 없습니다.");
        if (v < 0)           throw new IllegalArgumentException(name + " 값은 음수일 수 없습니다. 입력값: " + v);
    }
}
