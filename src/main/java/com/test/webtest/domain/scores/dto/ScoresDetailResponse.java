package com.test.webtest.domain.scores.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;

public record ScoresDetailResponse(
        int total,
        MetricScore lcp,
        MetricScore cls,
        MetricScore inp,
        MetricScore fcp,
        MetricScore ttfb,
        @JsonProperty("security_total_score") int securityTotal
) {
    public record MetricScore(
            int score,
            String urgentStatus
    ) {}
    public static ScoresDetailResponse from(ScoresEntity se, UrgentLevelEntity ue) {
        return new ScoresDetailResponse(
                n(se.getTotal()),
                metric(n(se.getLcpScore()), ue.getLcpStatus()),
                metric(n(se.getClsScore()), ue.getClsStatus()),
                metric(n(se.getInpScore()), ue.getInpStatus()),
                metric(n(se.getFcpScore()), ue.getFcpStatus()),
                metric(n(se.getTtfbScore()), ue.getFcpStatus()),
                n(se.getSecurityTotal())
        );
    }
    private static int n(Integer v) { return v == null ? 0 : v; }

    private static MetricScore metric(int score, String urgentStatus) {
        return new MetricScore(score, urgentStatus);
    }
}