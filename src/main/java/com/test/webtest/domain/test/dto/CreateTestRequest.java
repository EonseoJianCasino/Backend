package com.test.webtest.domain.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CreateTestRequest {
    @NotBlank(message = "URL은 필수입니다.")
    private String url;

    @NotNull(message = "시작 시간은 필수입니다.")
    private Instant startedAt;
}