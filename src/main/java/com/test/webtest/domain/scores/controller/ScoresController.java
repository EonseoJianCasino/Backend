package com.test.webtest.domain.scores.controller;

import com.test.webtest.domain.scores.dto.ScoresDetailResponse;
import com.test.webtest.domain.scores.dto.TotalScoreResponse;
import com.test.webtest.domain.scores.service.ScoresService;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class ScoresController {
    private final ScoresService scoresService;

    @GetMapping("/{testId}/scores")
    public ResponseEntity<ApiResponse<ScoresDetailResponse>> getDetail(
            @PathVariable("testId") UUID testId
    ) {
        var data = scoresService.getDetail(testId);
        return ResponseEntity.ok(ApiResponse.ok("세부 점수 조회를 성공했습니다.", data));
    }

    @GetMapping("/{testId}/scores/total")
    public ResponseEntity<ApiResponse<TotalScoreResponse>> getTotal(
            @PathVariable("testId") UUID testId
    ) {
        TotalScoreResponse total = scoresService.getTotal(testId);
        return ResponseEntity.ok(ApiResponse.ok("총점 조회를 성공했습니다.", total));
    }
}
