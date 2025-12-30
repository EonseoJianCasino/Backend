package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiSavePayload;
import com.test.webtest.domain.ai.entity.*;
import com.test.webtest.domain.ai.repository.*;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class AiEntitySaver {

    private final AiAnalysisSummaryRepository summaryRepo;
    private final AiTopPriorityRepository topPriorityRepo;
    private final AiMajorImprovementRepository majorImprovementRepo;
    private final AiWebElementRepository webElementRepo;
    private final AiSecurityMetricRepository securityMetricRepo;
    private final LogicStatusRepository logicStatusRepo;

    public AiEntitySaver(
            AiAnalysisSummaryRepository summaryRepo,
            AiTopPriorityRepository topPriorityRepo,
            AiMajorImprovementRepository majorImprovementRepo,
            AiWebElementRepository webElementRepo,
            AiSecurityMetricRepository securityMetricRepo,
            LogicStatusRepository logicStatusRepo) {
        this.summaryRepo = summaryRepo;
        this.topPriorityRepo = topPriorityRepo;
        this.majorImprovementRepo = majorImprovementRepo;
        this.webElementRepo = webElementRepo;
        this.securityMetricRepo = securityMetricRepo;
        this.logicStatusRepo = logicStatusRepo;
    }

    @Transactional
    public void saveAll(UUID testId, AiSavePayload payload) {
        // 1. 분석 요약 저장
        saveAnalysisSummary(testId, payload);

        // 2. top_priorities 저장
        saveTopPriorities(testId, payload);

        // 3. major_improvements 저장
        saveMajorImprovements(testId, payload);

        // 4. web_elements 저장
        saveWebElements(testId, payload);

        // 5. security_metrics 저장
        saveSecurityMetrics(testId, payload);

        // 6. AI triggered 상태 업데이트
        logicStatusRepo.markAiTriggered(testId);
    }

    private void saveAnalysisSummary(UUID testId, AiSavePayload payload) {
        summaryRepo.deleteByTestId(testId);

        AiAnalysisSummary summary = AiAnalysisSummary.of(
                testId,
                payload.overall_expected_improvement,
                payload.overall_total_after);

        summaryRepo.save(summary);
    }

    private void saveTopPriorities(UUID testId, AiSavePayload payload) {
        topPriorityRepo.deleteByTestId(testId);

        if (payload.top_priorities != null) {
            for (AiSavePayload.TopPriority priority : payload.top_priorities) {
                AiTopPriority entity = AiTopPriority.of(
                        testId,
                        priority.rank,
                        priority.status,
                        priority.target_type,
                        priority.target_name,
                        priority.reason);
                topPriorityRepo.save(entity);
            }
        }
    }

    private void saveMajorImprovements(UUID testId, AiSavePayload payload) {
        majorImprovementRepo.deleteByTestId(testId);

        if (payload.major_improvements != null) {
            for (AiSavePayload.MajorImprovement improvement : payload.major_improvements) {
                AiMajorImprovement entity = AiMajorImprovement.of(
                        testId,
                        improvement.rank,
                        improvement.metric,
                        improvement.title,
                        improvement.description);
                majorImprovementRepo.save(entity);
            }
        }
    }

    private void saveWebElements(UUID testId, AiSavePayload payload) {
        webElementRepo.deleteByTestId(testId);

        if (payload.web_elements != null) {
            for (AiSavePayload.WebElement element : payload.web_elements) {
                AiWebElement entity = AiWebElement.of(
                        testId,
                        element.element_name,
                        element.status,
                        element.benefit_summary,
                        element.expected_score_gain,
                        element.benefit_detail);

                // metric_deltas 추가
                if (element.metric_deltas != null) {
                    for (AiSavePayload.MetricDelta delta : element.metric_deltas) {
                        entity.addMetricDelta(
                                delta.metric,
                                delta.current_score,
                                delta.achievable_score,
                                delta.delta);
                    }
                }

                // related_metrics 추가
                if (element.related_metrics != null) {
                    int ord = 0;
                    for (String related : element.related_metrics) {
                        entity.addRelatedMetric(ord++, related);
                    }
                }

                webElementRepo.save(entity);
            }
        }
    }

    private void saveSecurityMetrics(UUID testId, AiSavePayload payload) {
        securityMetricRepo.deleteByTestId(testId);

        if (payload.security_metrics != null) {
            for (AiSavePayload.SecurityMetric metric : payload.security_metrics) {
                AiSecurityMetric entity = AiSecurityMetric.of(
                        testId,
                        metric.metric_name,
                        metric.status,
                        metric.benefit_summary,
                        metric.delta,
                        metric.expected_score_gain,
                        metric.benefit_detail);

                securityMetricRepo.save(entity);
            }
        }
    }
}
