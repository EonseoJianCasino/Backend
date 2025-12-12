package com.test.webtest.domain.ai.converter;

import com.test.webtest.domain.ai.dto.*;
import com.test.webtest.domain.ai.entity.*;
import com.test.webtest.domain.ai.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AiDtoConverter {

    private final AiAnalysisSummaryRepository summaryRepo;
    private final AiMajorImprovementRepository majorImprovementRepo;
    private final AiTopPriorityRepository topPriorityRepo;
    private final AiWebElementRepository webElementRepo;
    private final AiSecurityMetricRepository securityMetricRepo;

    public AiDtoConverter(
            AiAnalysisSummaryRepository summaryRepo,
            AiMajorImprovementRepository majorImprovementRepo,
            AiTopPriorityRepository topPriorityRepo,
            AiWebElementRepository webElementRepo,
            AiSecurityMetricRepository securityMetricRepo) {
        this.summaryRepo = summaryRepo;
        this.majorImprovementRepo = majorImprovementRepo;
        this.topPriorityRepo = topPriorityRepo;
        this.webElementRepo = webElementRepo;
        this.securityMetricRepo = securityMetricRepo;
    }

    @Transactional(readOnly = true)
    public AiAnalysisSummaryResponse getAnalysisSummary(UUID testId) {
        Optional<AiAnalysisSummary> summaryOpt = summaryRepo.findByTestId(testId);

        if (summaryOpt.isEmpty()) {
            return new AiAnalysisSummaryResponse(
                    null, null,
                    List.of(),
                    List.of());
        }

        AiAnalysisSummary summary = summaryOpt.get();

        List<AiAnalysisSummaryResponse.MajorImprovementDto> majorImprovements =
                majorImprovementRepo.findByTestIdOrderByRankAsc(testId).stream()
                        .map(m -> new AiAnalysisSummaryResponse.MajorImprovementDto(
                                m.getRank(),
                                m.getMetric(),
                                m.getTitle(),
                                m.getDescription()))
                        .toList();

        List<AiAnalysisSummaryResponse.TopPriorityDto> topPriorities =
                topPriorityRepo.findByTestIdOrderByRankAsc(testId).stream()
                        .map(p -> new AiAnalysisSummaryResponse.TopPriorityDto(
                                p.getRank(),
                                p.getStatus(),
                                p.getTargetType(),
                                p.getTargetName(),
                                p.getReason()))
                        .toList();

        return new AiAnalysisSummaryResponse(
                summary.getOverallExpectedImprovement(),
                summary.getOverallTotalAfter(),
                majorImprovements,
                topPriorities);
    }

    @Transactional(readOnly = true)
    public AiAnalysisResponse getAnalysis(UUID testId) {
        Optional<AiAnalysisSummary> summaryOpt = summaryRepo.findByTestId(testId);

        // web_elements 변환
        List<AiAnalysisResponse.WebElementDto> webElements = webElementRepo.findByTestId(testId).stream()
                .map(this::toWebElementDto)
                .toList();

        // security_metrics 변환
        List<AiAnalysisResponse.SecurityMetricDto> securityMetrics = securityMetricRepo.findByTestId(testId).stream()
                .map(this::toSecurityMetricDto)
                .toList();

        if (summaryOpt.isEmpty()) {
            return new AiAnalysisResponse(
                    webElements,
                    securityMetrics,
                    null, null, null,
                    List.of());
        }

        AiAnalysisSummary summary = summaryOpt.get();

        // major_improvements 변환
        List<AiAnalysisResponse.MajorImprovementDto> majorImprovements =
                majorImprovementRepo.findByTestIdOrderByRankAsc(testId).stream()
                        .map(m -> new AiAnalysisResponse.MajorImprovementDto(
                                m.getRank(),
                                m.getMetric(),
                                m.getTitle(),
                                m.getDescription()))
                        .toList();

        return new AiAnalysisResponse(
                webElements,
                securityMetrics,
                summary.getOverallExpectedImprovement(),
                summary.getOverallTotalAfter(),
                null,  // overallTotalBefore는 컨트롤러에서 설정
                majorImprovements);
    }

    private AiAnalysisResponse.WebElementDto toWebElementDto(AiWebElement entity) {
        List<AiAnalysisResponse.MetricDeltaDto> metricDeltas = entity.getMetricDeltas().stream()
                .map(d -> new AiAnalysisResponse.MetricDeltaDto(
                        d.getMetric(),
                        d.getCurrentScore(),
                        d.getAchievableScore(),
                        d.getDelta()))
                .toList();

        List<String> relatedMetrics = entity.getRelatedMetrics().stream()
                .map(AiWebElementRelatedMetric::getMetricText)
                .toList();

        return new AiAnalysisResponse.WebElementDto(
                entity.getElementName(),  // name
                entity.getStatus(),
                entity.getBenefitSummary(),
                entity.getExpectedScoreGain(),
                metricDeltas,
                relatedMetrics,
                entity.getBenefitDetail());
    }

    private AiAnalysisResponse.SecurityMetricDto toSecurityMetricDto(AiSecurityMetric entity) {
        return new AiAnalysisResponse.SecurityMetricDto(
                entity.getMetricName(),  // name
                entity.getStatus(),
                entity.getBenefitSummary(),
                entity.getDelta(),
                entity.getExpectedScoreGain(),
                entity.getBenefitDetail());
    }

    @Transactional(readOnly = true)
    public TopPrioritiesResponse getTopPriorities(UUID testId) {
        List<TopPrioritiesResponse.TopPriorityDto> topPriorities =
                topPriorityRepo.findByTestIdOrderByRankAsc(testId).stream()
                        .map(p -> new TopPrioritiesResponse.TopPriorityDto(
                                p.getRank(),
                                p.getStatus(),
                                p.getTargetType(),
                                p.getTargetName(),
                                p.getReason()))
                        .toList();

        return new TopPrioritiesResponse(topPriorities);
    }
}
