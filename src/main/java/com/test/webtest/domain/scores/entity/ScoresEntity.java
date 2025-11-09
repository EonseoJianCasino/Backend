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
    private Integer total;        // 웹50 + 보안50

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static ScoresEntity create(TestEntity test,
                                      int total, Integer lcp, Integer cls, Integer inp,
                                      Integer fcp, Integer ttfb) {
        return ScoresEntity.builder()
                .id(UUID.randomUUID())
                .test(test)
                .total(total)
                .lcpScore(lcp)
                .clsScore(cls)
                .inpScore(inp)
                .fcpScore(fcp)
                .ttfbScore(ttfb)
                .build();
    }

    public void update(int total, Integer lcp, Integer cls, Integer inp,
                       Integer fcp, Integer ttfb) {
        this.total = total;
        this.lcpScore = lcp;
        this.clsScore = cls;
        this.inpScore = inp;
        this.fcpScore = fcp;
        this.ttfbScore = ttfb;
    }
}
