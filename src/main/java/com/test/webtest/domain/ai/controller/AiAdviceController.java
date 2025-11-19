package com.test.webtest.domain.ai.controller;

import com.test.webtest.domain.ai.dto.AiAnalysisResponse;
import com.test.webtest.domain.ai.dto.TopPrioritiesResponse;
import com.test.webtest.domain.ai.service.AiPersistService;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class AiAdviceController {

    private final AiPersistService aiPersistService;

    @GetMapping("/{testId}/ai/recommendations")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> getRecommendations(
            @PathVariable UUID testId) {
        AiAnalysisResponse data = aiPersistService.getAnalysis(testId);
        return ResponseEntity.ok(ApiResponse.ok("AI 분석 결과를 조회했습니다.", data));
    }

    @GetMapping("/{testId}/ai/priorities")
    public ResponseEntity<ApiResponse<TopPrioritiesResponse>> getTopPriorities(
            @PathVariable UUID testId) {
        TopPrioritiesResponse data = aiPersistService.getTopPriorities(testId);
        return ResponseEntity.ok(ApiResponse.ok("AI 우선순위 정보를 조회했습니다.", data));
    }

}
