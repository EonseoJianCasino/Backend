package com.test.webtest.domain.webvitals.controller;

import com.test.webtest.domain.webvitals.dto.WebVitalsSubRequest;
import com.test.webtest.domain.webvitals.dto.WebVitalsSubResponse;
import com.test.webtest.domain.webvitals.dto.WebVitalsSavedResponse;
import com.test.webtest.domain.webvitals.service.WebVitalsSubService;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class WebVitalsSubController {

    private final WebVitalsSubService webVitalsSubService;

    @PostMapping("/{testId}/web-vitals/sub")
    public ResponseEntity<ApiResponse<WebVitalsSavedResponse>> saveWebVitalsSub(
            @PathVariable("testId") UUID testId,
            @RequestBody WebVitalsSubRequest request) {
        
        webVitalsSubService.saveWebVitalsSub(testId, request);
        var data = new WebVitalsSavedResponse(testId, Instant.now());
        return ResponseEntity.ok(ApiResponse.ok("웹 바이탈 보조 지표가 저장되었습니다.", data));
    }

    @GetMapping("/{testId}/web-vitals/sub")
    public ResponseEntity<ApiResponse<WebVitalsSubResponse>> getWebVitalsSub(
            @PathVariable("testId") UUID testId) {
        
        WebVitalsSubResponse response = webVitalsSubService.getWebVitalsSub(testId);
        return ResponseEntity.ok(ApiResponse.ok("웹 바이탈 보조 지표를 조회했습니다.", response));
    }
}
