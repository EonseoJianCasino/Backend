package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.priorities.entity.PrioritiesEntity;
import com.test.webtest.domain.priorities.repository.PrioritiesRepository;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import com.test.webtest.global.common.util.WebVitalsThreshold;
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
    private final ScoresRepository scoresRepository;

    private static final Set<String> WEB_METRICS = Set.of("LCP", "CLS", "INP", "FCP", "TTFB");

    @Override
    @Transactional
    public PrioritiesResponse calculateAndGetPriorities(UUID testId) {

        ScoresEntity scoresEntity = scoresRepository.findByTestId(testId).orElse(null);
        SecurityVitalsEntity securityEntity = securityRepository.findByTest_Id(testId).orElse(null);
        WebVitalsEntity webEntity = webRepository.findByTest_Id(testId).orElse(null);

        List<String> bottom3Metrics = scoreCalculator.bottom3(scoresEntity, securityEntity);

        // WebScores는 ScoresEntity에서 가져오거나, 없으면 계산
        ScoreCalculator.WebScores webScores;
        if (scoresEntity != null) {
            webScores = new ScoreCalculator.WebScores(
                    scoresEntity.getLcpScore() != null ? scoresEntity.getLcpScore() : 0,
                    scoresEntity.getClsScore() != null ? scoresEntity.getClsScore() : 0,
                    scoresEntity.getInpScore() != null ? scoresEntity.getInpScore() : 0,
                    scoresEntity.getFcpScore() != null ? scoresEntity.getFcpScore() : 0,
                    scoresEntity.getTtfbScore() != null ? scoresEntity.getTtfbScore() : 0);
        } else {
            webScores = scoreCalculator.toWebScores(webEntity);
        }

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
        var scoresEntity = scoresRepository.findByTestId(testId).orElse(null);
        var sec = securityRepository.findByTest_Id(testId).orElse(null);
        var web = webRepository.findByTest_Id(testId).orElse(null);

        var bottom3Metrics = scoreCalculator.bottom3(scoresEntity, sec);

        // WebScores는 ScoresEntity에서 가져오거나, 없으면 계산
        ScoreCalculator.WebScores webScores;
        if (scoresEntity != null) {
            webScores = new ScoreCalculator.WebScores(
                    scoresEntity.getLcpScore() != null ? scoresEntity.getLcpScore() : 0,
                    scoresEntity.getClsScore() != null ? scoresEntity.getClsScore() : 0,
                    scoresEntity.getInpScore() != null ? scoresEntity.getInpScore() : 0,
                    scoresEntity.getFcpScore() != null ? scoresEntity.getFcpScore() : 0,
                    scoresEntity.getTtfbScore() != null ? scoresEntity.getTtfbScore() : 0);
        } else {
            webScores = scoreCalculator.toWebScores(web);
        }

        List<PriorityDto> items = new ArrayList<>();
        int webVitalCount = 0;
        int rank = 1;

        for (String metric : bottom3Metrics) {
            if (WEB_METRICS.contains(metric)) {
                webVitalCount++;
                items.add(getWebVitalDummy(web, webScores, metric, rank++));
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

    private PriorityDto getWebVitalDummy(WebVitalsEntity web, ScoreCalculator.WebScores webScores, String metric,
            int rank) {
        int score = getWebScoreByName(webScores, metric);

        if (web == null) {
            return PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                    .rank(rank)
                    .value(null)
                    .status(null)
                    .build();
        }

        return switch (metric) {
            case "LCP" -> {
                Double value = web.getLcp();
                yield PriorityDto.builder()
                        .type("PERFORMANCE")
                        .metric("LCP")
                        .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                        .rank(rank)
                        .value(value)
                        .status(getStatus(value, WebVitalsThreshold.LCP))
                        .build();
            }
            case "CLS" -> {
                Double value = web.getCls();
                yield PriorityDto.builder()
                        .type("PERFORMANCE")
                        .metric("CLS")
                        .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                        .rank(rank)
                        .value(value)
                        .status(getStatus(value, WebVitalsThreshold.CLS))
                        .build();
            }
            case "INP" -> {
                Double value = web.getInp();
                yield PriorityDto.builder()
                        .type("PERFORMANCE")
                        .metric("INP")
                        .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                        .rank(rank)
                        .value(value)
                        .status(getStatus(value, WebVitalsThreshold.INP))
                        .build();
            }
            case "FCP" -> {
                Double value = web.getFcp();
                yield PriorityDto.builder()
                        .type("PERFORMANCE")
                        .metric("FCP")
                        .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                        .rank(rank)
                        .value(value)
                        .status(getStatus(value, WebVitalsThreshold.FCP))
                        .build();
            }
            case "TTFB" -> {
                Double value = web.getTtfb();
                yield PriorityDto.builder()
                        .type("PERFORMANCE")
                        .metric("TTFB")
                        .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                        .rank(rank)
                        .value(value)
                        .status(getStatus(value, WebVitalsThreshold.TTFB))
                        .build();
            }
            default -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason("정보 없음")
                    .rank(rank)
                    .value(null)
                    .status(null)
                    .build();
        };
    }

    private String getStatus(Double value, WebVitalsThreshold threshold) {
        if (value == null)
            return null;

        if (value <= threshold.getGood()) {
            return "양호";
        } else if (value >= threshold.getPoor()) {
            return "긴급";
        } else {
            return "주의";
        }
    }
}