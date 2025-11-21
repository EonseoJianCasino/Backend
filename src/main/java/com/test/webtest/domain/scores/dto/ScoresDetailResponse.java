package com.test.webtest.domain.scores.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.test.webtest.domain.scores.entity.ScoresEntity;

public record ScoresDetailResponse(
        int total,
        @JsonProperty("lcp_score")  int lcpScore,
        @JsonProperty("cls_score")  int clsScore,
        @JsonProperty("inp_score")  int inpScore,
        @JsonProperty("fcp_score")  int fcpScore,
        @JsonProperty("ttfb_score") int ttfbScore,
        @JsonProperty("security_total_score") int securityTotal
) {
    public static ScoresDetailResponse from(ScoresEntity e) {
        return new ScoresDetailResponse(
                n(e.getTotal()),
                n(e.getLcpScore()),
                n(e.getClsScore()),
                n(e.getInpScore()),
                n(e.getFcpScore()),
                n(e.getTtfbScore()),
                n(e.getSecurityTotal())
        );
    }
    private static int n(Integer v) { return v == null ? 0 : v; }
}