package com.test.webtest.domain.ai.converter;

import com.test.webtest.domain.ai.dto.*;
import com.test.webtest.domain.ai.entity.AiAnalysisSummary;
import com.test.webtest.domain.ai.entity.AiMetricAdvice;
import com.test.webtest.domain.ai.repository.AiAnalysisSummaryRepository;
import com.test.webtest.domain.ai.repository.AiMajorImprovementRepository;
import com.test.webtest.domain.ai.repository.AiMetricAdviceRepository;
import com.test.webtest.domain.ai.repository.AiTopPriorityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AiDtoConverter {

    private final AiMetricAdviceRepository adviceRepo;
    private final AiAnalysisSummaryRepository summaryRepo;
    private final AiMajorImprovementRepository majorImprovementRepo;
    private final AiTopPriorityRepository topPriorityRepo;

    public AiDtoConverter(
            AiMetricAdviceRepository adviceRepo,
            AiAnalysisSummaryRepository summaryRepo,
            AiMajorImprovementRepository majorImprovementRepo,
            AiTopPriorityRepository topPriorityRepo) {
        this.adviceRepo = adviceRepo;
        this.summaryRepo = summaryRepo;
        this.majorImprovementRepo = majorImprovementRepo;
        this.topPriorityRepo = topPriorityRepo;
    }

    @Transactional(readOnly = true)
    public AiMetricAdviceBundleResponse getMetricAdviceBundle(UUID testId) {
        var list = adviceRepo.findByTestId(testId);
        AiMetricAdviceBundleResponse bundle = new AiMetricAdviceBundleResponse();

        for (AiMetricAdvice advice : list) {
            MetricAdviceResponse dto = toResponseDto(advice);

            switch (advice.getMetric()) {
                case LCP -> bundle.LCP = dto;
                case CLS -> bundle.CLS = dto;
                case INP -> bundle.INP = dto;
                case FCP -> bundle.FCP = dto;
                case TTFB -> bundle.TTFB = dto;
                case HSTS -> bundle.HSTS = dto;
                case FRAME_ANCESTORS -> bundle.FRAME_ANCESTORS = dto;
                case SSL -> bundle.SSL = dto;
                case XCTO -> bundle.XCTO = dto;
                case REFERRER_POLICY -> bundle.REFERRER_POLICY = dto;
                case COOKIES -> bundle.COOKIES = dto;
                case CSP -> bundle.CSP = dto;
            }
        }

        return bundle;
    }

    private MetricAdviceResponse toResponseDto(AiMetricAdvice advice) {
        List<String> improvements = advice.getImprovements().stream()
                .sorted(Comparator.comparingInt(i -> i.getOrd()))
                .map(i -> i.getText())
                .toList();

        List<String> benefits = advice.getBenefits().stream()
                .sorted(Comparator.comparingInt(b -> b.getOrd()))
                .map(b -> b.getText())
                .toList();

        List<String> related = advice.getRelatedMetrics().stream()
                .sorted(Comparator.comparingInt(r -> r.getOrd()))
                .map(r -> r.getMetricText())
                .toList();

        return new MetricAdviceResponse(
                advice.getMetric().name(),
                advice.getSummary(),
                advice.getEstimatedLabel(),
                improvements,
                benefits,
                related);
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

        // major_improvements는 별도 테이블에서 조회
        List<AiAnalysisSummaryResponse.MajorImprovementDto> majorImprovements = majorImprovementRepo.findByTestIdOrderByOrdAsc(testId).stream()
                .map(m -> new AiAnalysisSummaryResponse.MajorImprovementDto(
                        m.getMetric(),
                        m.getTitle(),
                        m.getDescription()))
                .toList();

        // top_priorities는 별도 테이블에서 조회
        List<AiAnalysisSummaryResponse.TopPriorityDto> topPriorities = topPriorityRepo.findByTestIdOrderByRankAsc(testId).stream()
                .map(p -> new AiAnalysisSummaryResponse.TopPriorityDto(
                        p.getRank(),
                        p.getTargetType(),
                        p.getTargetName(),
                        p.getExpectedGain(),
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
        AiMetricAdviceBundleResponse metrics = getMetricAdviceBundle(testId);
        Optional<AiAnalysisSummary> summaryOpt = summaryRepo.findByTestId(testId);

        if (summaryOpt.isEmpty()) {
            return new AiAnalysisResponse(
                    metrics,
                    null, null, null,
                    List.of());
        }

        AiAnalysisSummary summary = summaryOpt.get();

        // major_improvements는 별도 테이블에서 조회
        List<AiAnalysisResponse.MajorImprovementDto> majorImprovements = majorImprovementRepo.findByTestIdOrderByOrdAsc(testId).stream()
                .map(m -> new AiAnalysisResponse.MajorImprovementDto(
                        m.getMetric(),
                        m.getTitle(),
                        m.getDescription()))
                .toList();

        return new AiAnalysisResponse(
                metrics,
                summary.getOverallExpectedImprovement(),
                summary.getOverallTotalAfter(),
                null,  // overallTotalBefore는 컨트롤러에서 설정
                majorImprovements);
    }

    @Transactional(readOnly = true)
    public TopPrioritiesResponse getTopPriorities(UUID testId) {
        // top_priorities는 별도 테이블에서 직접 조회
        List<TopPrioritiesResponse.TopPriorityDto> topPriorities = topPriorityRepo.findByTestIdOrderByRankAsc(testId).stream()
                .map(p -> new TopPrioritiesResponse.TopPriorityDto(
                        p.getRank(),
                        p.getTargetType(),
                        p.getTargetName(),
                        p.getExpectedGain(),
                        p.getReason()))
                .toList();

        return new TopPrioritiesResponse(topPriorities);
    }
}

