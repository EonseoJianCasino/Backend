package com.test.webtest.domain.ai.dto;

import java.util.List;

/**
 * AI 결과 번들 DTO: 우선순위 Top3 + 개선안들 + 기대효과
 */
public record AiAnalysisBundleDto(
        List<PriorityItem> priorities,
        List<RecommendationItem> recommendations,
        List<ExpectationItem> expectations
) {
    public record PriorityItem(String type, String metric, String reason, int rank) {}

    public record RecommendationItem(String type, String title, String content, String metric) {}

    public record ExpectationItem(String metric, String content) {}
}
