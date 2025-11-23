package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.domain.webvitals.dto.WebVitalsView;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WebVitalsMessageService {

    public WebVitalsView toView(WebVitalsEntity e, @Nullable UrgentLevelEntity urgent) {

        List<WebVitalsView.Item> items = new ArrayList<>();
        items.add(new WebVitalsView.Item(
                "LCP",
                description("LCP"),
                urgent != null ? urgent.getLcpStatus() : null,
                formatTimeWithUnit("LCP", e.getLcp())
        ));
        items.add(new WebVitalsView.Item(
                "CLS",
                description("CLS"),
                urgent != null ? urgent.getClsStatus() : null,
                formatTimeWithUnit("CLS", e.getCls())));
        items.add(new WebVitalsView.Item(
                "INP",
                description("INP"),
                urgent != null ? urgent.getInpStatus() : null,
                formatTimeWithUnit("INP", e.getInp())));
        items.add(new WebVitalsView.Item(
                "FCP",
                description("FCP"),
                urgent != null ? urgent.getFcpStatus() : null,
                formatTimeWithUnit("FCP", e.getFcp())));
        items.add(new WebVitalsView.Item(
                "TTFB",
                description("TTFB"),
                urgent != null ? urgent.getTtfbStatus() : null,
                formatTimeWithUnit("TTFB", e.getTtfb())));
        log.info("[webVitals][message] items={}", items.toString());
        return new WebVitalsView(items, e.getCreatedAt());
    }

    private String description(String metric) {
        return switch (metric) {
            case "LCP" ->
                    "가장 큰 텍스트 블록/이미지가 화면에 나타나는 시간";
            case "CLS" ->
                    "로딩 중 화면 요소들이 얼마나 예상치 못하게 움직였는지 수치화한 값";
            case "INP" ->
                    "사용자 상호작용 후 화면 반응까지 걸린 시간 중 가장 긴 시간";
            case "FCP" ->
                    "첫 번째 텍스트나 이미지가 보일 때까지의 시간";
            case "TTFB" ->
                    "요청을 보냈을 때 첫 데이터 바이트 도착까지 걸린 시간";
            default ->
                    "웹 성능 지표";
        };
    }

    private String formatTimeWithUnit(String metric, Double value) {
        if (value == null) {
            return "-";
        }
        if (metric.equals("LCP") || metric.equals("INP") || metric.equals("FCP") || metric.equals("TTFB")) {
            return String.format("%.0fms", value);
        } else {
            return String.format("%.2f",value);
        }
    }
}