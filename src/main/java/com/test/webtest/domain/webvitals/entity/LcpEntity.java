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
@Table(name = "lcp")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class LcpEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", unique = true, nullable = false)
    private TestEntity test;

    @Column(name = "start_time")
    private Integer startTime;

    @Column(name = "render_time")
    private Integer renderTime;

    @Column(name = "rendered_size")
    private Integer renderedSize;

    @Column(name = "element")
    private String element;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 팩토리 메서드 - WebVitals 생성
    public static LcpEntity create(TestEntity test, Integer startTime, Integer renderTime, Integer renderedSize,
            String element) {
        validateMetricValue(startTime, "start time");
        validateMetricValue(renderTime, "render time");
        validateMetricValue(renderedSize, "element");
        // validateMetricValue(element, "rendered size");

        return LcpEntity.builder()
                .id(UUID.randomUUID())
                .test(test) // @MapsId를 사용하므로 test만 설정하면 testId는 자동으로 매핑됨
                .startTime(startTime)
                .renderTime(renderTime)
                .element(element)
                .renderedSize(renderedSize)
                .build();
    }

    public void updateFrom(Integer startTime, Integer renderTime, Integer renderedSize, String element) {
        this.startTime = startTime;
        this.renderTime = renderTime;
        this.renderedSize = renderedSize;
        this.element = element;
    }

    // 지표값 검증 메서드 (음수 불가, NaN 불가)
    private static void validateMetricValue(Integer v, String name) {
        if (v == null)
            return; // null 허용
        if (Double.isNaN(v))
            throw new IllegalArgumentException(name + " 값은 NaN일 수 없습니다.");
        if (v < 0)
            throw new IllegalArgumentException(name + " 값은 음수일 수 없습니다. 입력값: " + v);
    }
}
