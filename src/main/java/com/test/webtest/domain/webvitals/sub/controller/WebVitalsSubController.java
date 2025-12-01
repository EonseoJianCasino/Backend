package com.test.webtest.domain.webvitals.sub.controller;

import com.test.webtest.domain.webvitals.sub.dto.WebVitalsSubRequest;
import com.test.webtest.domain.webvitals.sub.dto.WebVitalsSubResponse;
import com.test.webtest.domain.webvitals.sub.service.WebVitalsSubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class WebVitalsSubController {

    private final WebVitalsSubService webVitalsSubService;

    @PostMapping("/{testId}/web-vitals/sub")
    public ResponseEntity<Void> saveWebVitalsSub(
            @PathVariable("testId") UUID testId,
            @RequestBody WebVitalsSubRequest request) {
        
        webVitalsSubService.saveWebVitalsSub(testId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{testId}/web-vitals/sub")
    public ResponseEntity<WebVitalsSubResponse> getWebVitalsSub(
            @PathVariable("testId") UUID testId) {
        
        WebVitalsSubResponse response = webVitalsSubService.getWebVitalsSub(testId);
        return ResponseEntity.ok(response);
    }
}

