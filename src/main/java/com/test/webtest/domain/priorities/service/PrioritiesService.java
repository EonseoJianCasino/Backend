package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;

import java.util.List;
import java.util.UUID;

public interface PrioritiesService {

    List<PriorityDto> getBottom3(UUID testId);
}