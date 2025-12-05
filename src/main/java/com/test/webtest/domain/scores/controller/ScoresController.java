package com.test.webtest.domain.scores.controller;

import com.test.webtest.domain.scores.dto.ScoresDetailResponse;
import com.test.webtest.domain.scores.dto.TotalScoreResponse;
import com.test.webtest.domain.scores.service.ScoresService;
import com.test.webtest.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{testId}/scores/recalc")
    public ResponseEntity<ApiResponse<ScoresDetailResponse>> reCalcScore(
            @PathVariable("testId") UUID testId
    ) {
        scoresService.calcAndSave(testId);
        var data = scoresService.getDetail(testId);
        return ResponseEntity.ok(ApiResponse.ok("세부 점수 재계산을 성공했습니다.", data));
    }

    @PostMapping("/{testId}/scores/total/recalc")
    public ResponseEntity<ApiResponse<TotalScoreResponse>> reCalcTotalScore(
            @PathVariable("testId") UUID testId
    ) {
        scoresService.calcAndSave(testId);
        TotalScoreResponse total = scoresService.getTotal(testId);
        return ResponseEntity.ok(ApiResponse.ok("총점 재계산을 성공했습니다.", total));
    }
}
