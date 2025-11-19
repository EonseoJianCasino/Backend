package com.test.webtest.domain.ai.dto;

import java.util.List;

public record AiAnalysisResponse(
        // 지표별 조언
        AiMetricAdviceBundleResponse metrics,
        // 요약 정보 (top_priorities 제외)
        Integer overallExpectedImprovement,
        Integer webTotalAfter,
        Integer securityTotalAfter,
        Integer overallTotalAfter,
        List<MajorImprovementDto> majorImprovements) {
    public record MajorImprovementDto(
            String metric,
            String title,
            String description) {
    }
}
