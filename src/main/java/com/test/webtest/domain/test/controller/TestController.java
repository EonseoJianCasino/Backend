package com.test.webtest.domain.test.controller;

import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;
import com.test.webtest.domain.test.service.TestService;
import com.test.webtest.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestResponse>> createTest(@Valid @RequestBody CreateTestRequest request) {
        TestResponse data = testService.createTest(request);
        var body = ApiResponse.success("TEST_CREATED", "테스트가 생성되었습니다.", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
