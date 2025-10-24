package com.test.webtest.domain.test.controller;

import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;
import com.test.webtest.domain.test.service.TestService;
import com.test.webtest.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestResponse>> createTest(@Valid @RequestBody CreateTestRequest request) {
        TestResponse data = testService.createTest(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("테스트가 생성되었습니다.", data));
    }

    @GetMapping("/{testId}")
    public ResponseEntity<ApiResponse<TestResponse>> getTest(@PathVariable("testId") UUID testId) {
        TestResponse data = testService.getTest(testId);
        return ResponseEntity
                .ok(ApiResponse.ok("test url 조회를 성공했습니다.", data));
    }


}
