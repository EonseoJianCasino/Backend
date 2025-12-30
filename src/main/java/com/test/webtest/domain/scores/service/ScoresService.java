package com.test.webtest.domain.scores.service;

import com.test.webtest.domain.scores.dto.ScoresDetailResponse;
import com.test.webtest.domain.scores.dto.TotalScoreResponse;

import java.util.UUID;

public interface ScoresService {
    void calcAndSave(UUID testId);

    ScoresDetailResponse getDetail(UUID testId);
    TotalScoreResponse getTotal(UUID testId);
}
