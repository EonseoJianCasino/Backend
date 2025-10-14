package com.test.webtest.domain.test.service;

import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService{

    private final TestRepository testRepository;

    @Override
    @Transactional
    public TestResponse createTest(CreateTestRequest request) {
        TestEntity entity = TestEntity.create(request.getUrl(), request.getIp());
        testRepository.save(entity);
        return TestResponse.fromEntity(entity);
    }
}
