package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import java.util.UUID;

public interface PrioritiesService {

    PrioritiesResponse getBottom3(UUID testId);
}