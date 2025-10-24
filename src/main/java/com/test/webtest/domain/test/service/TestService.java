package com.test.webtest.domain.test.service;

import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;

import java.util.UUID;

public interface TestService {
    TestResponse createTest(CreateTestRequest request);
    TestResponse getTest(UUID testId);
}
