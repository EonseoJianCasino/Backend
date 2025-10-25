package com.test.webtest.domain.airecommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService{
    @Override
    @Async("logicExecutor")
    public void invokeAsync(UUID testId) {
        log.info("[AI] invoke recommendations for testId={}", testId);

        // 프롬프트 생성 및 외부 AI 호출
    }
}
