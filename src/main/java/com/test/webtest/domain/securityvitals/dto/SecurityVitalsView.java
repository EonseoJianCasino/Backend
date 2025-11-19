package com.test.webtest.domain.securityvitals.dto;

import java.time.Instant;
import java.util.List;

public record SecurityVitalsView(
        List<Item> items,
        Instant createdAt
) {
    public record Item(
            String metric,
            String message,
            String urgentStatus  // GOOD / WARNING / POOR
    ) {}
}
