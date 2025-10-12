package com.test.webtest.domain.test.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTestRequest (
        @NotBlank String url
) {}
