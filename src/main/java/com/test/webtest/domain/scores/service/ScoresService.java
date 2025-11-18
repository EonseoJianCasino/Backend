package com.test.webtest.domain.scores.service;

import com.test.webtest.domain.scores.dto.ScoresDetailResponse;

import java.util.UUID;

public interface ScoresService {
    void calcAndSave(UUID testId);

    ScoresDetailResponse getDetail(UUID testId);
    int getTotal(UUID testId);
}
