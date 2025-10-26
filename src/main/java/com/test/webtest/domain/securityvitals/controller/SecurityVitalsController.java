package com.test.webtest.domain.securityvitals.controller;

import com.test.webtest.domain.securityvitals.dto.SecurityVitalsView;
import com.test.webtest.domain.securityvitals.service.SecurityVitalsServiceImpl;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests/{testId}/security-vitals")
@RequiredArgsConstructor
public class SecurityVitalsController {
    private final SecurityVitalsServiceImpl securityVitalsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SecurityVitalsView>> get(@PathVariable UUID testId) {
        SecurityVitalsView view = securityVitalsService.getView(testId);
        return ResponseEntity.ok(ApiResponse.ok("보안 바이탈 조회를 성공했습니다.", view));
    }
}
