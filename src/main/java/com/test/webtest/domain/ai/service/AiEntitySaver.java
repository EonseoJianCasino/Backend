package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiSavePayload;
import com.test.webtest.domain.ai.entity.AiAnalysisSummary;
import com.test.webtest.domain.ai.entity.AiMajorImprovement;
import com.test.webtest.domain.ai.entity.AiMetricAdvice;
import com.test.webtest.domain.ai.entity.AiTopPriority;
import com.test.webtest.domain.ai.entity.Metric;
import com.test.webtest.domain.ai.repository.AiAnalysisSummaryRepository;
import com.test.webtest.domain.ai.repository.AiMajorImprovementRepository;
import com.test.webtest.domain.ai.repository.AiMetricAdviceRepository;
import com.test.webtest.domain.ai.repository.AiTopPriorityRepository;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AiEntitySaver {

    private final AiMetricAdviceRepository adviceRepo;
    private final AiAnalysisSummaryRepository summaryRepo;
    private final AiMajorImprovementRepository majorImprovementRepo;
    private final AiTopPriorityRepository topPriorityRepo;
    private final LogicStatusRepository logicStatusRepo;

    public AiEntitySaver(
            AiMetricAdviceRepository adviceRepo,
            AiAnalysisSummaryRepository summaryRepo,
            AiMajorImprovementRepository majorImprovementRepo,
            AiTopPriorityRepository topPriorityRepo,
            LogicStatusRepository logicStatusRepo) {
        this.adviceRepo = adviceRepo;
        this.summaryRepo = summaryRepo;
        this.majorImprovementRepo = majorImprovementRepo;
        this.topPriorityRepo = topPriorityRepo;
        this.logicStatusRepo = logicStatusRepo;
    }

    @Transactional
    public void saveAll(UUID testId, AiSavePayload payload) {
        if (payload.web_elements != null) {
            for (AiSavePayload.WebElement element : payload.web_elements) {
                saveWebElement(testId, element);
            }
        }

        if (payload.security_metrics != null) {
            for (AiSavePayload.SecurityMetric secMetric : payload.security_metrics) {
                saveSecurityMetric(testId, secMetric);
            }
        }

        saveAnalysisSummary(testId, payload);
        logicStatusRepo.markAiTriggered(testId);
    }

    private void saveWebElement(UUID testId, AiSavePayload.WebElement element) {
        if (element == null || element.metric_deltas == null || element.metric_deltas.isEmpty()) {
            return;
        }

        Map<String, List<AiSavePayload.MetricDelta>> metricGroups = element.metric_deltas.stream()
                .collect(Collectors.groupingBy(d -> d.metric));

        for (Map.Entry<String, List<AiSavePayload.MetricDelta>> entry : metricGroups.entrySet()) {
            String metricName = entry.getKey();
            Metric metric = Metric.fromExternalName(metricName);
            if (metric == null)
                continue;

            int totalDelta = entry.getValue().stream()
                    .mapToInt(d -> d.delta)
                    .sum();

            String summary = String.format("%s: %s", element.element_name, element.detailed_plan);

            AiMetricAdvice advice = AiMetricAdvice.of(
                    testId,
                    metric,
                    summary,
                    String.valueOf(totalDelta));

            advice.addImprovement(0, element.detailed_plan);
            advice.addBenefit(0, element.benefit_summary);

            if (element.related_metrics != null) {
                int i = 0;
                for (String related : element.related_metrics) {
                    advice.addRelatedMetric(i++, related);
                }
            }

            adviceRepo.save(advice);
        }
    }

    private void saveSecurityMetric(UUID testId, AiSavePayload.SecurityMetric secMetric) {
        if (secMetric == null)
            return;

        Metric metric = Metric.fromExternalName(secMetric.metric);
        if (metric == null)
            return;

        AiMetricAdvice advice = AiMetricAdvice.of(
                testId,
                metric,
                secMetric.improvement_plan,
                String.valueOf(secMetric.expected_gain));

        advice.addImprovement(0, secMetric.improvement_plan);
        advice.addBenefit(0, secMetric.expected_benefit);

        String impactInfo = String.format("%s: %s", secMetric.impact_title, secMetric.impact_description);
        advice.addRelatedMetric(0, impactInfo);

        adviceRepo.save(advice);
    }

    private void saveAnalysisSummary(UUID testId, AiSavePayload payload) {
        summaryRepo.deleteByTestId(testId);

        AiAnalysisSummary summary = AiAnalysisSummary.of(
                testId,
                payload.overall_expected_improvement,
                payload.overall_total_after);

        summaryRepo.save(summary);

        // major_improvements는 별도 테이블에 저장
        saveMajorImprovements(testId, payload);

        // top_priorities는 별도 테이블에 저장
        saveTopPriorities(testId, payload);
    }

    private void saveMajorImprovements(UUID testId, AiSavePayload payload) {
        majorImprovementRepo.deleteByTestId(testId);

        if (payload.major_improvements != null) {
            int ord = 0;
            for (AiSavePayload.MajorImprovement improvement : payload.major_improvements) {
                AiMajorImprovement entity = AiMajorImprovement.of(
                        testId,
                        ord++,
                        improvement.metric,
                        improvement.title,
                        improvement.description);
                majorImprovementRepo.save(entity);
            }
        }
    }

    private void saveTopPriorities(UUID testId, AiSavePayload payload) {
        topPriorityRepo.deleteByTestId(testId);

        if (payload.top_priorities != null) {
            for (AiSavePayload.TopPriority priority : payload.top_priorities) {
                AiTopPriority entity = AiTopPriority.of(
                        testId,
                        priority.rank,
                        priority.target_type,
                        priority.target_name,
                        priority.expected_gain,
                        priority.reason);
                topPriorityRepo.save(entity);
            }
        }
    }
}
