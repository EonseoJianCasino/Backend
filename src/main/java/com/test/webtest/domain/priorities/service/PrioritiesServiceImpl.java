package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PrioritiesServiceImpl implements PrioritiesService {

    private final ScoreCalculator scoreCalculator;
    private final WebVitalsRepository webRepository;
    private final SecurityVitalsRepository securityRepository;
    private final SecurityMessageService securityMessageService;
    private final ScoresRepository scoresRepository;

    private static final Set<String> WEB_METRICS = Set.of("LCP", "CLS", "INP", "FCP", "TTFB");

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
                items.add(getWebVitalDummy(web, webScores, scoresEntity, metric, rank++));
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

    private PriorityDto getWebVitalDummy(WebVitalsEntity web, ScoreCalculator.WebScores webScores,
            ScoresEntity scoresEntity, String metric, int rank) {
        int score = getWebScoreByName(webScores, metric);

        // ScoresEntity에서 저장된 status 사용 (GOOD, WARNING, URGENT)
        String status = getStatusFromScoresEntity(scoresEntity, metric);

        if (web == null) {
            return PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason(String.format("좋은 지표의 %d%% 수준입니다", score))
                    .rank(rank)
                    .value(null)
                    .status(status)
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
                        .status(status)
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
                        .status(status)
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
                        .status(status)
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
                        .status(status)
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
                        .status(status)
                        .build();
            }
            default -> PriorityDto.builder()
                    .type("PERFORMANCE")
                    .metric(metric)
                    .reason("정보 없음")
                    .rank(rank)
                    .value(null)
                    .status(status)
                    .build();
        };
    }

    private String getStatusFromScoresEntity(ScoresEntity scoresEntity, String metric) {
        if (scoresEntity == null)
            return null;

        return switch (metric) {
            case "LCP" -> scoresEntity.getLcpStatus();
            case "CLS" -> scoresEntity.getClsStatus();
            case "INP" -> scoresEntity.getInpStatus();
            case "FCP" -> scoresEntity.getFcpStatus();
            case "TTFB" -> scoresEntity.getTtfbStatus();
            default -> null;
        };
    }
}