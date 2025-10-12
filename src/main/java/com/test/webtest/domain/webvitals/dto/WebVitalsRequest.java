package com.test.webtest.domain.webvitals.dto;

import jakarta.validation.constraints.NotBlank;

public record WebVitalsRequest(
        @NotBlank String url
) {}
