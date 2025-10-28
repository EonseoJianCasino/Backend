package com.test.webtest.domain.webvitals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class WebVitalsRequest {
    // 대문자 키 그대로 받음 (API 명세 준수)
    @JsonProperty("LCP") @PositiveOrZero private Double LCP;
    @JsonProperty("CLS") @PositiveOrZero private Double CLS;
    @JsonProperty("INP") @PositiveOrZero private Double INP;
    @JsonProperty("FCP") @PositiveOrZero private Double FCP;
    @JsonProperty("TBT") @PositiveOrZero private Double TBT;
    @JsonProperty("TTFB") @PositiveOrZero private Double TTFB;
}