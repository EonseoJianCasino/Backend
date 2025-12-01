package com.test.webtest.domain.webvitals.ai.controller;

import com.test.webtest.domain.webvitals.ai.dto.WebVitalsAIRequest;
import com.test.webtest.domain.webvitals.ai.dto.WebVitalsAIResponse;
import com.test.webtest.domain.webvitals.ai.service.WebVitalsAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class webvitalsAIController {

    private final WebVitalsAIService webVitalsAIService;

    @PostMapping("/{testId}/web-vitals/ai")
    public ResponseEntity<Void> saveWebVitalsAI(
            @PathVariable("testId") UUID testId,
            @RequestBody WebVitalsAIRequest request) {
        
        webVitalsAIService.saveWebVitalsAI(testId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{testId}/web-vitals/ai")
    public ResponseEntity<WebVitalsAIResponse> getWebVitalsAI(
            @PathVariable("testId") UUID testId) {
        
        WebVitalsAIResponse response = webVitalsAIService.getWebVitalsAI(testId);
        return ResponseEntity.ok(response);
    }
}
