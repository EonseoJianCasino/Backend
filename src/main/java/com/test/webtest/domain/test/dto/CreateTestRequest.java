package com.test.webtest.domain.test.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateTestRequest {
    @NotBlank(message = "URL은 필수입니다.")
    private String url;
}