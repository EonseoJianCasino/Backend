package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.priorities.entity.PrioritiesEntity;
import com.test.webtest.domain.priorities.repository.PrioritiesRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
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
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PrioritiesServiceImpl implements PrioritiesService {

    private final ScoreCalculator scoreCalculator;
    private final PrioritiesRepository prioritiesRepository;
    private final WebVitalsRepository webRepository;
    private final SecurityVitalsRepository securityRepository;
    private final SecurityMessageService securityMessageService;

    private static final Set<String> WEB_METRICS = Set.of("LCP", "CLS", "INP", "FCP", "TTFB");

    @Override
    @Transactional
    public PrioritiesResponse calculateAndGetPriorities(UUID testId) {

        WebVitalsEntity webEntity = webRepository.findByTest_Id(testId).orElse(null);
        SecurityVitalsEntity securityEntity = securityRepository.findByTest_Id(testId).orElse(null);

        List<String> bottom3Metrics = scoreCalculator.bottom3(webEntity, securityEntity);

        ScoreCalculator.WebScores webScores = scoreCalculator.toWebScores(webEntity);

        int webVitalCount = 0;
        List<PrioritiesEntity> newPriorities = new java.util.ArrayList<>();

        for (int i = 0; i < bottom3Metrics.size(); i++) {
            String metricName = bottom3Metrics.get(i);
            String type;
            String message;

            if (WEB_METRICS.contains(metricName)) {
                type = "PERFORMANCE";
                webVitalCount++;

                int score = getWebScoreByName(webScores, metricName);
                message = String.format("좋은 지표의 %d%% 수준입니다", score); // score가 높을수록 좋음 (0~100)
            } else {
                type = "SECURITY";
                message = securityMessageService.getMessageByMetric(securityEntity, metricName);
            }

            PrioritiesEntity entity = PrioritiesEntity.builder()
                    .testId(testId)
                    .type(type)
                    .metric(metricName)
                    .reason(message)
                    .rank(i + 1)
                    .build();
            newPriorities.add(entity);
        }

        prioritiesRepository.saveAll(newPriorities);

        return getPriorities(testId);
    }

    @Override
    public PrioritiesResponse getPriorities(UUID testId) {
        List<PrioritiesEntity> entities = prioritiesRepository.findAllByTestIdOrderByRankAsc(testId);

        List<PriorityDto> dtos = entities.stream()
                .map(this::toPriorityDto)
                .collect(Collectors.toList());

        long webVitalCount = dtos.stream()
                .filter(dto -> "PERFORMANCE".equals(dto.getType()))
                .count();

        return PrioritiesResponse.builder()
                .testId(testId)
                .priorities(dtos)
                .totalCount(dtos.size())
                .webVitalCount((int) webVitalCount)
                .build();
    }

    private PriorityDto toPriorityDto(PrioritiesEntity entity) {
        return PriorityDto.builder()
                .type(entity.getType())
                .metric(entity.getMetric())
                .reason(entity.getReason())
                .rank(entity.getRank())
                .build();
    }

    private int getWebScoreByName(ScoreCalculator.WebScores scores, String metricName) {
        return switch (metricName) {
            case "LCP" -> scores.lcp();
            case "CLS" -> scores.cls();
            case "INP" -> scores.inp();
            case "FCP" -> scores.fcp();
            case "TTFB" -> scores.ttfb();
            default -> 0;
        };
    }
    
    @Override
    public PrioritiesResponse getBottom3(UUID testId) {
        var web = webRepository.findByTest_Id(testId).orElse(null);
        var sec = securityRepository.findByTest_Id(testId).orElse(null);
        
        var webScores = scoreCalculator.toWebScores(web);
        var bottom3Metrics = scoreCalculator.bottom3(webScores, sec);
        
        List<PriorityDto> items = new ArrayList<>();
        int webVitalCount = 0;
        int rank = 1;
        
        for (String metric : bottom3Metrics) {
            if (WEB_METRICS.contains(metric)) {
                webVitalCount++;
                items.add(getWebVitalDummy(metric, rank++));
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
    
    private PriorityDto getWebVitalDummy(String metric, int rank) {
        return switch(metric) {
            case "LCP" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("LCP")
                    .reason("좋은 지표의 40% 수준입니다")
                    .rank(rank)
                    .value(3.1)
                    .build();
            case "CLS" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("CLS")
                    .reason("좋은 지표의 53% 수준입니다")
                    .rank(rank)
                    .value(0.18)
                    .build();
            case "INP" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("INP")
                    .reason("좋은 지표의 27% 수준입니다")
                    .rank(rank)
                    .value(280.0)
                    .build();
            case "FCP" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("FCP")
                    .reason("좋은 지표의 33% 수준입니다")
                    .rank(rank)
                    .value(2.2)
                    .build();
            case "TTFB" -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric("TTFB")
                    .reason("좋은 지표의 20% 수준입니다")
                    .rank(rank)
                    .value(1.0)
                    .build();
            default -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason("정보 없음")
                    .rank(rank)
                    .value(null)
                    .build();
        };
    }
}