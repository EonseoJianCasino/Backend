package com.test.webtest.domain.scores.dto;

public record TotalScoreResponse(int totalScore, int securityTotalScore, int webTotalScore) {
    public static TotalScoreResponse of(
            int total,
            int securityTotal,
            int webTotal
    ) {
        return new TotalScoreResponse(total, securityTotal, webTotal);
    }
}