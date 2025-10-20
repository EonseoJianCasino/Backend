package com.test.webtest.domain.test.service;

import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;

public interface TestService {
    TestResponse createTest(CreateTestRequest request);
}
