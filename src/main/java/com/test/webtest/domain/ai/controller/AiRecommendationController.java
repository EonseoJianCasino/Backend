package com.test.webtest.domain.ai.controller;

import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.dto.PromptRequest;
import com.test.webtest.domain.ai.service.AiRecommendationService;
import com.test.webtest.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService geminiService;

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<AiResponse>> complete(@Valid @RequestBody PromptRequest req) {
        AiResponse data = geminiService.generate(
                req.getPrompt(),
                req.getSystem(),
                req.getModel(),
                Boolean.TRUE.equals(req.getJsonMode()));
        return ResponseEntity.ok(ApiResponse.ok("AI 응답이 생성되었습니다.", data));
    }
}
