package com.test.webtest.domain.webvitals.sub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebVitalsSubRequest {
    @JsonProperty("FCP")
    private FcpDto fcp;

    @JsonProperty("TTFB")
    private TtfbDto ttfb;

    @JsonProperty("LCP")
    private LcpDto lcp;

    @JsonProperty("INP")
    private InpDto inp;

    @JsonProperty("CLS")
    private ClsDto cls;
}

