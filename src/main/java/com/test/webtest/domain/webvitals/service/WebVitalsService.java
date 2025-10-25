package com.test.webtest.domain.webvitals.service;

import java.util.UUID;

public interface WebVitalsService {
    void saveWebVitals(UUID testId, WebVitalsSaveCommand command);

    record WebVitalsSaveCommand(
            Double lcp, Double fid, Double cls, Double fcp, Double ttfb, Double inp, Double tbt
    ){}
}
