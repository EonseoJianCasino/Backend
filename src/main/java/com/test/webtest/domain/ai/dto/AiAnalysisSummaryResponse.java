package com.test.webtest.domain.ai.dto;

import java.util.List;

public record AiAnalysisSummaryResponse(
        Integer overallExpectedImprovement,
        Integer overallTotalAfter,
        List<MajorImprovementDto> majorImprovements,
        List<TopPriorityDto> topPriorities) {
    public record MajorImprovementDto(
            Integer rank,
            String metric,
            String title,
            String description) {
    }

    public record TopPriorityDto(
            Integer rank,
            String status,
            String targetType,
            String targetName,
            String reason) {
    }
}
