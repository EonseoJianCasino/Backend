package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.webvitals.dto.WebVitalsView;

import java.util.UUID;

public interface WebVitalsService {
    void saveWebVitals(UUID testId, WebVitalsSaveCommand cmd);
    WebVitalsView getView(UUID testId);

    record WebVitalsSaveCommand(
            Double lcp, Double cls, Double inp,
            Double fcp, Double tbt, Double ttfb
    ) {}
}
