package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PrioritiesServiceImpl implements PrioritiesService {

    private final ScoreCalculator scoreCalculator;
    private final WebVitalsRepository webRepository;
    private final SecurityVitalsRepository securityRepository;
    private final SecurityMessageService securityMessageService;
    private final ScoresRepository scoresRepository;
    private final UrgentLevelRepository urgentLevelRepository;

    private static final Set<String> WEB_METRICS = Set.of("LCP", "CLS", "INP", "FCP", "TTFB");

    @Override
    public PrioritiesResponse getBottom3(UUID testId) {
        var scoresEntity = scoresRepository.findByTestId(testId).orElse(null);
        var sec = securityRepository.findByTest_Id(testId).orElse(null);
        var web = webRepository.findByTest_Id(testId).orElse(null);
        var urgentLevel = urgentLevelRepository.findByTestId(testId).orElse(null);

        var bottom3Metrics = scoreCalculator.bottom3(scoresEntity, sec);

        List<PriorityDto> items = new ArrayList<>();
        int webVitalCount = 0;
        int rank = 1;

        for (String metric : bottom3Metrics) {
            if (WEB_METRICS.contains(metric)) {
                webVitalCount++;
                items.add(getWebVitalDummy(web, urgentLevel, metric, rank++));
            } else {
                String message = securityMessageService.getMessageByMetric(sec, metric);
                items.add(PriorityDto.builder()
                        .type("SECURITY")
                        .metric(metric)
                        .reason(message)
                        .rank(rank++)
                        .value(null)
                        .build());
            }
        }

        return PrioritiesResponse.builder()
                .testId(testId)
                .priorities(items)
                .totalCount(items.size())
                .webVitalCount(webVitalCount)
                .build();
    }

    private PriorityDto getWebVitalDummy(WebVitalsEntity web, UrgentLevelEntity urgentLevel,
            String metric, int rank) {
        // UrgentLevelEntity에서 저장된 status 사용 (GOOD, POOR, WARNING)
        String status = getStatusFromUrgentLevel(urgentLevel, metric);

        if (web == null) {
            return PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
        }

        return switch (metric) {
            case "LCP" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("LCP")
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
            case "CLS" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("CLS")
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
            case "INP" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("INP")
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
            case "FCP" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("FCP")
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
            case "TTFB" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("TTFB")
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
            default -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason(null)
                    .rank(rank)
                    .value(null)
                    .urgentLevel(status)
                    .build();
        };
    }

    private String getStatusFromUrgentLevel(UrgentLevelEntity urgentLevel, String metric) {
        if (urgentLevel == null)
            return null;

        return switch (metric) {
            case "LCP" -> urgentLevel.getLcpStatus();
            case "CLS" -> urgentLevel.getClsStatus();
            case "INP" -> urgentLevel.getInpStatus();
            case "FCP" -> urgentLevel.getFcpStatus();
            case "TTFB" -> urgentLevel.getTtfbStatus();
            default -> null;
        };
    }
}
