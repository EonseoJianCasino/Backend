package com.test.webtest.domain.ai.dto;

import java.util.List;

public record AiAnalysisSummaryResponse(
        Integer overallExpectedImprovement,
        Integer overallTotalAfter,
        List<MajorImprovementDto> majorImprovements,
        List<TopPriorityDto> topPriorities) {
    public record MajorImprovementDto(
            String metric,
            String title,
            String description) {
    }

    public record TopPriorityDto(
            Integer rank,
            String targetType,
            String targetName,
            Integer expectedGain,
            String reason) {
    }
}
