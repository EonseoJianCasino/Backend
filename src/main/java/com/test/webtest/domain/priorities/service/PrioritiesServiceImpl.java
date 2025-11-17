package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final Set<String> WEB_METRICS =
            Set.of("LCP", "CLS", "INP", "FCP", "TTFB");

    @Override
    public List<PriorityDto> getBottom3(UUID testId) {
        ScoresEntity scores = scoresRepository.findByTestId(testId).orElse(null);
        SecurityVitalsEntity sec = securityRepository.findByTest_Id(testId).orElse(null);
        UrgentLevelEntity urgent = urgentLevelRepository.findByTestId(testId).orElse(null);

        // 1) metric → 점수
        Map<String, Integer> metricScoreMap = scoreCalculator.bottom3(scores, sec);

        // 2) 후보 리스트 구성
        List<Candidate> candidates = metricScoreMap.entrySet().stream()
                .map(e -> {
                    String metric = e.getKey();
                    int score = e.getValue();
                    String type = WEB_METRICS.contains(metric) ? "PERFORMANCE" : "SECURITY";
                    String urgentLevel = getStatusFromUrgentLevel(urgent, metric);
                    String reason = "SECURITY".equals(type)
                            ? securityMessageService.getMessageByMetric(sec, metric)
                            : null;
                    return new Candidate(metric, type, score, urgentLevel, reason);
                })
                .collect(Collectors.toList());

        // 3) tie-breaker용 랜덤 셔플
        Collections.shuffle(candidates);

        // 4) 긴급도 > 점수 기준 정렬
        candidates.sort(
                Comparator.comparingInt((Candidate c) -> urgentRank(c.urgentLevel)).reversed() // POOR > WARNING > GOOD > null
                        .thenComparingInt(c -> c.score) // 점수 낮을수록 우선
        );

        // 5) 상위 3개만 PriorityDto 로 변환
        List<PriorityDto> result = new ArrayList<>();

        int rank = 1;
        for (Candidate c : candidates.stream().limit(3).toList()) {
            result.add(PriorityDto.builder()
                    .type(c.type)
                    .metric(c.metric)
                    .reason(c.reason)
                    .rank(rank++)
                    .urgentLevel(c.urgentLevel)
                    .build());
        }

        return result;
    }

    private String getStatusFromUrgentLevel(UrgentLevelEntity urgentLevel, String metric) {
        if (urgentLevel == null) return null;

        return switch (metric) {
            // Web
            case "LCP" -> urgentLevel.getLcpStatus();
            case "CLS" -> urgentLevel.getClsStatus();
            case "INP" -> urgentLevel.getInpStatus();
            case "FCP" -> urgentLevel.getFcpStatus();
            case "TTFB" -> urgentLevel.getTtfbStatus();
            // Security
            case "HSTS" -> urgentLevel.getHstsStatus();
            case "FRAME-ANCESTORS/XFO" -> urgentLevel.getFrameAncestorsStatus();
            case "SSL" -> urgentLevel.getSslStatus();
            case "XCTO" -> urgentLevel.getXctoStatus();
            case "REFERRER-POLICY" -> urgentLevel.getReferrerPolicyStatus();
            case "COOKIES" -> urgentLevel.getCookiesStatus();
            case "CSP" -> urgentLevel.getCspStatus();
            default -> null;
        };
    }

    private int urgentRank(String status) {
        if (status == null) return 0;
        return switch (status.toUpperCase()) {
            case "POOR" -> 3;
            case "WARNING" -> 2;
            case "GOOD" -> 1;
            default -> 0;
        };
    }

    private record Candidate(
            String metric,
            String type,
            int score,
            String urgentLevel,
            String reason
    ) {}
}
