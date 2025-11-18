package com.test.webtest.domain.urgentlevel.entity;

import com.test.webtest.domain.test.entity.TestEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "urgent_level")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UrgentLevelEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private TestEntity test;

    // ----- Performance -----
    @Column(name = "lcp_status", length = 10)
    private String lcpStatus; // GOOD, POOR, WARNING

    @Column(name = "cls_status", length = 10)
    private String clsStatus;

    @Column(name = "inp_status", length = 10)
    private String inpStatus;

    @Column(name = "fcp_status", length = 10)
    private String fcpStatus;

    @Column(name = "ttfb_status", length = 10)
    private String ttfbStatus;

    // ----- Security -----
    @Column(name = "hsts_status", length = 10)
    private String hstsStatus;

    @Column(name = "frame_ancestors_status", length = 20)
    private String frameAncestorsStatus;

    @Column(name = "ssl_status", length = 10)
    private String sslStatus;

    @Column(name = "xcto_status", length = 10)
    private String xctoStatus;

    @Column(name = "referrer_policy_status", length = 20)
    private String referrerPolicyStatus;

    @Column(name = "cookies_status", length = 10)
    private String cookiesStatus;

    @Column(name = "csp_status", length = 10)
    private String cspStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static UrgentLevelEntity create(
            TestEntity test,
            String lcpStatus, String clsStatus, String inpStatus,
            String fcpStatus, String ttfbStatus,
            String hstsStatus, String frameAncestorsStatus, String sslStatus,
            String xctoStatus, String referrerPolicyStatus,
            String cookiesStatus, String cspStatus
    ) {
        return UrgentLevelEntity.builder()
                .id(UUID.randomUUID())
                .test(test)
                .lcpStatus(lcpStatus)
                .clsStatus(clsStatus)
                .inpStatus(inpStatus)
                .fcpStatus(fcpStatus)
                .ttfbStatus(ttfbStatus)
                .hstsStatus(hstsStatus)
                .frameAncestorsStatus(frameAncestorsStatus)
                .sslStatus(sslStatus)
                .xctoStatus(xctoStatus)
                .referrerPolicyStatus(referrerPolicyStatus)
                .cookiesStatus(cookiesStatus)
                .cspStatus(cspStatus)
                .build();
    }

    public void update(
            String lcpStatus, String clsStatus, String inpStatus,
            String fcpStatus, String ttfbStatus,
            String hstsStatus, String frameAncestorsStatus, String sslStatus,
            String xctoStatus, String referrerPolicyStatus,
            String cookiesStatus, String cspStatus
    ) {
        this.lcpStatus = lcpStatus;
        this.clsStatus = clsStatus;
        this.inpStatus = inpStatus;
        this.fcpStatus = fcpStatus;
        this.ttfbStatus = ttfbStatus;
        this.hstsStatus = hstsStatus;
        this.frameAncestorsStatus = frameAncestorsStatus;
        this.sslStatus = sslStatus;
        this.xctoStatus = xctoStatus;
        this.referrerPolicyStatus = referrerPolicyStatus;
        this.cookiesStatus = cookiesStatus;
        this.cspStatus = cspStatus;
    }
}
