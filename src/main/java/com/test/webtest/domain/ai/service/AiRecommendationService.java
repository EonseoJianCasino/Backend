package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiResponse;

import java.util.Map;
import java.util.UUID;

public interface AiRecommendationService {
    void invokeAsync(UUID testId);

    AiResponse generate(String userPrompt, String system, String model, boolean jsonMode);

    AiResponse generateWithSchema(String prompt, Map<String, Object> jsonSchema);
}
