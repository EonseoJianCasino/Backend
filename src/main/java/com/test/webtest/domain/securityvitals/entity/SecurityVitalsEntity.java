package com.test.webtest.domain.securityvitals.entity;

import com.test.webtest.domain.test.entity.TestEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_vitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class SecurityVitalsEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private TestEntity test;

    // --- 기본 헤더 플래그/값 ---
    @Column(name = "has_csp")
    private Boolean hasCsp;

    @Column(name = "has_hsts")
    private Boolean hasHsts;

    @Column(name = "x_frame_options", length = 50)
    private String xFrameOptions;

    @Column(name = "x_content_type_options", length = 50)
    private String xContentTypeOptions;

    // --- 확장 컬럼들 ---
    @Column(name = "referrer_policy", length = 50)
    private String referrerPolicy;

    @Column(name = "hsts_max_age")
    private Long hstsMaxAge;

    @Column(name = "hsts_include_subdomains")
    private Boolean hstsIncludeSubdomains;

    @Column(name = "hsts_preload")
    private Boolean hstsPreload;

    @Column(name = "csp_has_unsafe_inline")
    private Boolean cspHasUnsafeInline;

    @Column(name = "csp_has_unsafe_eval")
    private Boolean cspHasUnsafeEval;

    @Column(name = "csp_frame_ancestors", columnDefinition = "text")
    private String cspFrameAncestors;

    @Column(name = "cookie_secure_all")
    private Boolean cookieSecureAll;

    @Column(name = "cookie_httponly_all")
    private Boolean cookieHttpOnlyAll;

    @Column(name = "cookie_samesite_policy", length = 10)
    private String cookieSameSitePolicy; // Strict/Lax/None/Unspecified

    @Column(name = "ssl_valid")
    private Boolean sslValid;

    @Column(name = "ssl_chain_valid")
    private Boolean sslChainValid;

    @Column(name = "ssl_days_remaining")
    private Integer sslDaysRemaining;

    @Column(name = "ssl_issuer", columnDefinition = "text")
    private String sslIssuer;

    @Column(name = "ssl_subject", columnDefinition = "text")
    private String sslSubject;

    @Column(name = "csp_raw", columnDefinition = "text")
    private String cspRaw;

    @Column(name = "hsts_raw", columnDefinition = "text")
    private String hstsRaw;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- 팩토리 ---
    public static SecurityVitalsEntity create(TestEntity test, SaveCommand c) {
        return SecurityVitalsEntity.builder()
                .id(UUID.randomUUID())
                .test(test)
                .hasCsp(c.hasCsp())
                .hasHsts(c.hasHsts())
                .xFrameOptions(c.xFrameOptions())
                .xContentTypeOptions(c.xContentTypeOptions())
                .referrerPolicy(c.referrerPolicy())
                .hstsMaxAge(c.hstsMaxAge())
                .hstsIncludeSubdomains(c.hstsIncludeSubdomains())
                .hstsPreload(c.hstsPreload())
                .cspHasUnsafeInline(c.cspHasUnsafeInline())
                .cspHasUnsafeEval(c.cspHasUnsafeEval())
                .cspFrameAncestors(c.cspFrameAncestors())
                .cookieSecureAll(c.cookieSecureAll())
                .cookieHttpOnlyAll(c.cookieHttpOnlyAll())
                .cookieSameSitePolicy(c.cookieSameSitePolicy())
                .sslValid(c.sslValid())
                .sslChainValid(c.sslChainValid())
                .sslDaysRemaining(c.sslDaysRemaining())
                .sslIssuer(c.sslIssuer())
                .sslSubject(c.sslSubject())
                .cspRaw(c.cspRaw())
                .hstsRaw(c.hstsRaw())
                .build();
    }

    // --- 업데이트 메서드(Upsert 시 사용) ---
    public void updateFrom(SaveCommand c) {
        this.hasCsp = c.hasCsp();
        this.hasHsts = c.hasHsts();
        this.xFrameOptions = c.xFrameOptions();
        this.xContentTypeOptions = c.xContentTypeOptions();
        this.referrerPolicy = c.referrerPolicy();
        this.hstsMaxAge = c.hstsMaxAge();
        this.hstsIncludeSubdomains = c.hstsIncludeSubdomains();
        this.hstsPreload = c.hstsPreload();
        this.cspHasUnsafeInline = c.cspHasUnsafeInline();
        this.cspHasUnsafeEval = c.cspHasUnsafeEval();
        this.cspFrameAncestors = c.cspFrameAncestors();
        this.cookieSecureAll = c.cookieSecureAll();
        this.cookieHttpOnlyAll = c.cookieHttpOnlyAll();
        this.cookieSameSitePolicy = c.cookieSameSitePolicy();
        this.sslValid = c.sslValid();
        this.sslChainValid = c.sslChainValid();
        this.sslDaysRemaining = c.sslDaysRemaining();
        this.sslIssuer = c.sslIssuer();
        this.sslSubject = c.sslSubject();
        this.cspRaw = c.cspRaw();
        this.hstsRaw = c.hstsRaw();
    }

    /**
     * 서비스에서 사용하기 위한 저장 커맨드(스캔 결과를 그대로 담음)
     */
    @Builder
    public record SaveCommand(
            Boolean hasCsp,
            Boolean hasHsts,
            String  xFrameOptions,
            String  xContentTypeOptions,

            String  referrerPolicy,
            Long    hstsMaxAge,
            Boolean hstsIncludeSubdomains,
            Boolean hstsPreload,
            Boolean cspHasUnsafeInline,
            Boolean cspHasUnsafeEval,
            String  cspFrameAncestors,
            Boolean cookieSecureAll,
            Boolean cookieHttpOnlyAll,
            String  cookieSameSitePolicy,
            Boolean sslValid,
            Boolean sslChainValid,
            Integer sslDaysRemaining,
            String  sslIssuer,
            String  sslSubject,
            String  cspRaw,
            String  hstsRaw
    ) {}
}
