package com.test.webtest.domain.webvitals.dto;

import java.time.Instant;
import java.util.UUID;

public record WebVitalsSavedResponse(
        UUID testId,
        Instant savedAt
) {}