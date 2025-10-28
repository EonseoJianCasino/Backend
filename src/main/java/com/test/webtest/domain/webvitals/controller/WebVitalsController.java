package com.test.webtest.domain.webvitals.controller;

import com.test.webtest.domain.webvitals.dto.WebVitalsRequest;
import com.test.webtest.domain.webvitals.dto.WebVitalsSavedResponse;
import com.test.webtest.domain.webvitals.dto.WebVitalsView;
import com.test.webtest.domain.webvitals.service.WebVitalsService;
import com.test.webtest.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class WebVitalsController {

    private final WebVitalsService webVitalsService;

    @PostMapping("/{testId}/web-vitals")
    public ResponseEntity<ApiResponse<WebVitalsSavedResponse>> save(
            @PathVariable("testId") UUID testId,
            @Valid @RequestBody WebVitalsRequest body
    ) {
        var cmd = new WebVitalsService.WebVitalsSaveCommand(
                body.getLCP(), body.getCLS(), body.getINP(),
                body.getFCP(), body.getTBT(), body.getTTFB()
        );
        webVitalsService.saveWebVitals(testId, cmd);
        var data = new WebVitalsSavedResponse(testId, Instant.now());
        return ResponseEntity.ok(ApiResponse.ok("웹 바이탈이 저장되었습니다.", data)); // API 문서 준수
    }

    @GetMapping("/{testId}/web-vitals")
    public ResponseEntity<ApiResponse<WebVitalsView>> get(
            @PathVariable("testId") UUID testId
    ) {
        WebVitalsView view = webVitalsService.getView(testId);
        return ResponseEntity.ok(ApiResponse.ok("웹 바이탈 조회를 성공했습니다.", view));
    }
}