package com.test.webtest.domain.scores.dto;

public record TotalScoreResponse(int totalScore) {
    public static TotalScoreResponse of(int total) {
        return new TotalScoreResponse(total);
    }
}