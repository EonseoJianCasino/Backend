package com.test.webtest.domain.priorities.controller;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.priorities.service.PrioritiesService;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class PrioritiesController {
    
    private final PrioritiesService prioritiesService;
    
    @GetMapping("/{testId}/priorities")
    public ResponseEntity<ApiResponse<List<PriorityDto>>> getBottom3(@PathVariable UUID testId) {
        List<PriorityDto> response = prioritiesService.getBottom3(testId);
        return ResponseEntity.ok(
            ApiResponse.ok("하위 3개 우선순위 조회 성공", response)
        );
    }
}
