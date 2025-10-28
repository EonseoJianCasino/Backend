package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AiRecommendationService {
    void invokeAsync(UUID testId); // 반환형을 CompletableFuture<Void>로 하는 게 더 좋을까? 추후 체이닝, 결과 대기가 가능하다고 함

    AiResponse generate(String userPrompt, String system, String model, boolean jsonMode);

    Flux<String> stream(String userPrompt, String model);



}

