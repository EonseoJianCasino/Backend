package com.test.webtest.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptRequest {

    @NotBlank
    private String prompt;     // 사용자 프롬프트

    private String system;     // (선택) 시스템 지시문
    private String model;      // (선택) 모델명 (미지정 시 기본값 사용)
    private Boolean jsonMode;  // (선택) JSON Mode 강제 여부

}
