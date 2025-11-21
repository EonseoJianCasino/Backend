package com.test.webtest.domain.scores.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.test.webtest.domain.scores.entity.ScoresEntity;

public record TotalScoreResponse(
        @JsonProperty("total_score") int totalScore,
        @JsonProperty("security_score") int securityTotalScore,
        @JsonProperty("web_score") int webTotalScore
) {
    public static TotalScoreResponse from(ScoresEntity e) {
        return new TotalScoreResponse(
            n(e.getTotal()),
            n(e.getSecurityTotal()),
            n(e.getSecurityTotal())
        );
    }
    private static int n(Integer v) {return v == null ? 0 : v;}
}