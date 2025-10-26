package com.test.webtest.domain.scores.service;

import java.util.UUID;

public interface ScoresService {
    void calcAndSaveAsync(UUID testId);

    void calcAndSave(UUID testId);
}
