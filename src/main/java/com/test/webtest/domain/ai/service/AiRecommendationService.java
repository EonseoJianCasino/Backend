package com.test.webtest.domain.ai.service;

import java.util.UUID;

public interface AiRecommendationService {
    void invokeAsync(UUID testId);
}
