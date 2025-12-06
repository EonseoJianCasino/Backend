package com.test.webtest.domain.ai.dto;

import java.util.List;

public record AiAnalysisResponse(
        // 개선 방안들
        List<WebElementDto> webElements,
        List<SecurityMetricDto> securityMetrics,
        // 요약 정보
        Integer overallExpectedImprovement,
        Integer overallTotalAfter,
        Integer overallTotalBefore,
        List<MajorImprovementDto> majorImprovements) {

    public AiAnalysisResponse withOverallTotalBefore(Integer overallTotalBefore) {
        return new AiAnalysisResponse(
                webElements,
                securityMetrics,
                overallExpectedImprovement,
                overallTotalAfter,
                overallTotalBefore,
                majorImprovements
        );
    }

    public record WebElementDto(
            String name,
            String status,
            String benefitSummary,
            Integer expectedScoreGain,
            List<MetricDeltaDto> metricDeltas,
            List<String> relatedMetrics,
            String benefitDetail) {
    }

    public record MetricDeltaDto(
            String metric,
            Integer currentScore,
            Integer achievableScore,
            Integer delta) {
    }

    public record SecurityMetricDto(
            String name,
            String status,
            String benefitSummary,
            Integer delta,
            Integer expectedScoreGain,
            List<String> relatedMetrics,
            String benefitDetail) {
    }

    public record MajorImprovementDto(
            Integer rank,
            String metric,
            String title,
            String description) {
    }
}
