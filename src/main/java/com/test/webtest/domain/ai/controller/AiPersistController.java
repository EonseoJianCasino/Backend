package com.test.webtest.domain.ai.controller;

import com.test.webtest.domain.ai.service.AiPersistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai/gemini")
public class AiPersistController {

    private final AiPersistService aiPersistService;

    public AiPersistController(AiPersistService aiPersistService) {
        this.aiPersistService = aiPersistService;
    }

    /**
     * Gemini에게 프롬프트를 보내고, 결과를 DB에 저장하는 엔드포인트
     * 예: POST /api/ai/gemini/save?testId=...
     */
    @PostMapping("/save")
    public ResponseEntity<Void> saveAiResult(
            @RequestParam("testId") UUID testId //,
//            @RequestBody String prompt
    ) {
//        aiPersistService.generateAndSave(testId, prompt);
        aiPersistService.generateAndSave(testId);
        return ResponseEntity.ok().build();
    }
}
