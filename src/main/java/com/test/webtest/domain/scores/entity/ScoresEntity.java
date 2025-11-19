package com.test.webtest.domain.scores.entity;

import com.test.webtest.domain.test.entity.TestEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ScoresEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestEntity test;

    @Column(name = "total", nullable = false)
    private Integer total; // 웹50 + 보안50

    @Column(name = "security_total", nullable = false)
    private Integer securityTotal; // 웹50 + 보안50

    @Column(name = "web_total", nullable = false)
    private Integer webTotal; // 웹50 + 보안50

    @Column(name = "lcp_score")
    private Integer lcpScore;

    @Column(name = "cls_score")
    private Integer clsScore;

    @Column(name = "inp_score")
    private Integer inpScore;

    @Column(name = "fcp_score")
    private Integer fcpScore;

    @Column(name = "ttfb_score")
    private Integer ttfbScore;

    @Column(name = "hsts_score")
    private Integer hstsScore;

    @Column(name = "frame_ancestors_score")
    private Integer frameAncestorsScore;

    @Column(name = "ssl_score")
    private Integer sslScore;

    @Column(name = "xcto_score")
    private Integer xctoScore;

    @Column(name = "referrer_policy_score")
    private Integer referrerPolicyScore;

    @Column(name = "cookies_score")
    private Integer cookiesScore;

    @Column(name = "csp_score")
    private Integer cspScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static ScoresEntity create(
            TestEntity test,
            int total, int  securityTotal, int webTotal,
            Integer lcp, Integer cls, Integer inp,
            Integer fcp, Integer ttfb,
            Integer hsts, Integer frameAncestors, Integer ssl,
            Integer xcto, Integer referrerPolicy, Integer cookies, Integer csp
    ) {
        return ScoresEntity.builder()
                .id(UUID.randomUUID())
                .test(test)
                .total(total)
                .securityTotal(securityTotal)
                .webTotal(webTotal)
                .lcpScore(lcp)
                .clsScore(cls)
                .inpScore(inp)
                .fcpScore(fcp)
                .ttfbScore(ttfb)
                .hstsScore(hsts)
                .frameAncestorsScore(frameAncestors)
                .sslScore(ssl)
                .xctoScore(xcto)
                .referrerPolicyScore(referrerPolicy)
                .cookiesScore(cookies)
                .cspScore(csp)
                .build();
    }

    public void update(
            int total, int securityTotal, int webTotal,
            Integer lcp, Integer cls, Integer inp,
            Integer fcp, Integer ttfb,
            Integer hsts, Integer frameAncestors, Integer ssl,
            Integer xcto, Integer referrerPolicy, Integer cookies, Integer csp
    ) {
        this.total = total;
        this.securityTotal = securityTotal;
        this.webTotal = webTotal;
        this.lcpScore = lcp;
        this.clsScore = cls;
        this.inpScore = inp;
        this.fcpScore = fcp;
        this.ttfbScore = ttfb;
        this.hstsScore = hsts;
        this.frameAncestorsScore = frameAncestors;
        this.sslScore = ssl;
        this.xctoScore = xcto;
        this.referrerPolicyScore = referrerPolicy;
        this.cookiesScore = cookies;
        this.cspScore = csp;
    }
}
