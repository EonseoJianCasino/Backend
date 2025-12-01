package com.test.webtest.domain.webvitals.sub.service;

import com.test.webtest.domain.webvitals.sub.dto.WebVitalsSubRequest;
import com.test.webtest.domain.webvitals.sub.dto.WebVitalsSubResponse;

import java.util.UUID;

public interface WebVitalsSubService {
    void saveWebVitalsSub(UUID testId, WebVitalsSubRequest request);
    WebVitalsSubResponse getWebVitalsSub(UUID testId);
}

