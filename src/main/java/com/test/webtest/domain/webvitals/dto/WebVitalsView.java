package com.test.webtest.domain.webvitals.dto;

import java.time.Instant;

public record WebVitalsView(
        Double lcp,   String lcpMessage,
        Double cls,   String clsMessage,
        Double inp,   String inpMessage,
        Double fcp,   String fcpMessage,
        Double ttfb,  String ttfbMessage,
        Instant createdAt
) {}