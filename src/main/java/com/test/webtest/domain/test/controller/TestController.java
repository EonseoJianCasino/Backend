package com.test.webtest.domain.test.controller;

import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;
import com.test.webtest.domain.test.service.TestService;
import com.test.webtest.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Tests", description = "테스트 생성/조회 API")
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @Operation(summary = "테스트 생성", description = "URL을 입력받아 테스트를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<TestResponse>> createTest(@Valid @RequestBody CreateTestRequest request) {
        TestResponse data = testService.createTest(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("테스트가 생성되었습니다.", data));
    }

    @Operation(summary = "테스트 조회", description = "testId로 테스트 엔티티를 조회합니다.")
    @GetMapping("/{testId}")
    public ResponseEntity<ApiResponse<TestResponse>> getTest(@PathVariable("testId") UUID testId) {
        TestResponse data = testService.getTest(testId);
        return ResponseEntity
                .ok(ApiResponse.ok("test url 조회를 성공했습니다.", data));
    }


}
