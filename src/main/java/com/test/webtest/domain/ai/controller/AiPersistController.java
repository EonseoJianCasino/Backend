package com.test.webtest.domain.ai.controller;

import com.test.webtest.domain.ai.service.AiPersistService;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class AiPersistController {

    private final AiPersistService aiPersistService;

    /**
     * AI 응답을 생성하고 DB에 저장하는 엔드포인트
     * 프론트엔드는 저장 후 /api/tests/{testId}/ai/recommendations로 별도 조회 필요
     */
    @PostMapping("/{testId}/ai/generate")
    public ResponseEntity<ApiResponse<Void>> generateAiResult(@PathVariable UUID testId) {
        aiPersistService.generateAndSave(testId);
        return ResponseEntity.ok(ApiResponse.ok("AI 응답이 생성되어 DB에 저장되었습니다.", null));
    }
}
