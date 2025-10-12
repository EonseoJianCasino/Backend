package com.test.webtest.domain.securityvitals.dto;

import jakarta.validation.constraints.NotBlank;

public record SecurityVitalsRequest(
        @NotBlank String url
) {}
