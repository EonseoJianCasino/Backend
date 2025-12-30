package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.webvitals.dto.WebVitalsSubRequest;
import com.test.webtest.domain.webvitals.dto.WebVitalsSubResponse;

import java.util.UUID;

public interface WebVitalsSubService {
    void saveWebVitalsSub(UUID testId, WebVitalsSubRequest request);
    WebVitalsSubResponse getWebVitalsSub(UUID testId);
}
