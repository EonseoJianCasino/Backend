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
        Integer overallTotalBefore,
        List<MajorImprovementDto> majorImprovements) {

    public AiAnalysisResponse withOverallTotalBefore(Integer overallTotalBefore) {
        return new AiAnalysisResponse(
                metrics,
                overallExpectedImprovement,
                webTotalAfter,
                securityTotalAfter,
                overallTotalAfter,
                overallTotalBefore,
                majorImprovements
        );
    }

    public record MajorImprovementDto(
            String metric,
            String title,
            String description) {
    }
}
