package com.test.webtest.domain.webvitals.ai.service;

import com.test.webtest.domain.webvitals.ai.dto.WebVitalsAIRequest;
import com.test.webtest.domain.webvitals.ai.dto.WebVitalsAIResponse;

import java.util.UUID;

public interface WebVitalsAIService {
    void saveWebVitalsAI(UUID testId, WebVitalsAIRequest request);
    WebVitalsAIResponse getWebVitalsAI(UUID testId);
}

