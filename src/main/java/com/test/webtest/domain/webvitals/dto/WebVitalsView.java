package com.test.webtest.domain.webvitals.dto;

import java.time.Instant;
import java.util.List;

public record WebVitalsView(
        List<Item> items,
        Instant createdAt
) {
    // metric: LCP/CLS/INP/FCP/TTFB
    // message: 지표 정의 설명
    // urgentStatus: GOOD / WARNING / POOR (없으면 null)
    public record Item(
            String metric,
            String message,
            String urgentStatus
    ) {}
}