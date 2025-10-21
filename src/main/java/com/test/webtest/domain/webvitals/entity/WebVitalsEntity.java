package com.test.webtest.domain.webvitals.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

//WebVitalsEntity - Web Vitals 원본 지표 저장
//ERD 의 web_vitals 테이블과 매핑

@Entity
@Table(name = "web_vitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WebVitalsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_id", nullable = false, unique = true)
    private UUID testId;

    @Column(name = "lcp")
    private Double lcp;

    @Column(name = "cls")
    private Double cls;

    @Column(name = "inp")
    private Double inp;

    @Column(name = "fcp")
    private Double fcp;

    @Column(name = "tbt")
    private Double tbt;

    @Column(name = "ttfb")
    private Double ttfb;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
