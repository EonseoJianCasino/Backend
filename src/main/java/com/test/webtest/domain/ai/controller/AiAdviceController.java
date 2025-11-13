package com.test.webtest.domain.ai.controller;


import com.test.webtest.domain.ai.dto.AiMetricAdviceBundleResponse;
import com.test.webtest.domain.ai.service.AiPersistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiAdviceController {

    private final AiPersistService aiPersistService;

    public AiAdviceController(AiPersistService aiPersistService) {
        this.aiPersistService = aiPersistService;
    }

    @GetMapping("/metrics/{testId}")
    public ResponseEntity<AiMetricAdviceBundleResponse> getMetricAdvice(
            @PathVariable UUID testId
    ) {
        return ResponseEntity.ok(aiPersistService.getMetricAdviceBundle(testId));
    }

}
