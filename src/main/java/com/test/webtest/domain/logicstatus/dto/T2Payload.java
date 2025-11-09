package com.test.webtest.domain.logicstatus.dto;

import com.test.webtest.domain.securityvitals.dto.SecurityVitalsView;
import com.test.webtest.domain.webvitals.dto.WebVitalsView;

public record T2Payload(
        Scores scores,
        SecurityVitalsView security,
        WebVitalsView performance
) {
    public record Scores(
            int total,
            int lcpScore,
            int clsScore,
            int inpScore,
            int fcpScore,
            int ttfbScore
    ) {}
}