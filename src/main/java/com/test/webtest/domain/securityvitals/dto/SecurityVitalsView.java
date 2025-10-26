package com.test.webtest.domain.securityvitals.dto;

import java.time.Instant;
import java.util.List;

public record SecurityVitalsView(
        List<Item> items,
        Instant createdAt,
        Integer securityScore
) {
    public record Item(String metric, String message) {}
}
