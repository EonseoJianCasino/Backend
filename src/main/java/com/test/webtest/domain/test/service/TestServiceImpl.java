package com.test.webtest.domain.test.service;

import com.test.webtest.domain.logicstatus.entity.LogicStatusEntity;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.domain.securityvitals.service.SecurityVitalsServiceImpl;
import com.test.webtest.domain.test.dto.CreateTestRequest;
import com.test.webtest.domain.test.dto.TestResponse;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.global.common.util.UrlNormalizer;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.exception.DuplicateRequestException;
import com.test.webtest.global.error.exception.InvalidRequestException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.monitoring.DuplicateRequestMetrics;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService{

    private final TestRepository testRepository;
    private final LogicStatusRepository logicStatusRepository;
    private final RateLimitService rateLimitService;
    private final SecurityVitalsServiceImpl securityVitalsService;
    private final DuplicateRequestMetrics duplicateRequestMetrics;

    @Override
    @Transactional
    public TestResponse createTest(CreateTestRequest request) {
         var normalizedKey = UrlNormalizer.normalizeUrlForKey(request.getUrl());
         if (normalizedKey == null || normalizedKey.isBlank()) {
             // 정규화 결과 자체가 이상하면 400
             throw new InvalidRequestException("URL 정규화에 실패했습니다. 입력값을 확인하세요.");
         }

         var result = rateLimitService.checkAndMark(normalizedKey);
         if (!result.allowed()) {
             duplicateRequestMetrics.increment();
            throw new DuplicateRequestException(
                    "동일 URL로 10초 내 중복 요청이 차단되었습니다. 남은 대기(ms): " + result.remainingMillis()
            );
         }

        TestEntity entity = TestEntity.create(request.getUrl());
        testRepository.save(entity);

        logicStatusRepository.save(LogicStatusEntity.create(entity.getId()));

        UUID testId = entity.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                // 네트워크 호출이므로 트랜잭션 밖에서 실행
                securityVitalsService.scanAndSave(testId);
            }
        });

        return TestResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public TestResponse getTest(UUID testId) {
        TestEntity entity = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TEST_NOT_FOUND,
                        "요청한 테스트가 존재하지 않습니다. id=" + testId
                ));
        return TestResponse.fromEntity(entity);
    }

    @Service
    @RequiredArgsConstructor
    public static class RateLimitService {
        private final InMemoryRateLimit rateLimit; // 구성용 래퍼(테스트/운영 교체 포인트)

        public Result checkAndMark(String normalizedUrlKey) {
            return rateLimit.checkAndMark(normalizedUrlKey);
        }

        public record Result(boolean allowed, long remainingMillis) {}
    }

    @Service
    public static class InMemoryRateLimit {
        // 간단 버전: ConcurrentHashMap + 10초 윈도우 (멀티 인스턴스 시 Redis로 교체 권장)
        private final java.time.Duration window = java.time.Duration.ofSeconds(10);
        private final java.util.concurrent.ConcurrentMap<String, java.time.Instant> lastSeen = new java.util.concurrent.ConcurrentHashMap<>();

        public RateLimitService.Result checkAndMark(String normalizedUrlKey) {
            var now = java.time.Instant.now();
            var allowedFlag = new java.util.concurrent.atomic.AtomicBoolean(false);
            var after = lastSeen.compute(normalizedUrlKey, (k, prev) -> {
                if (prev == null || java.time.Duration.between(prev, now).compareTo(window) > 0) {
                    allowedFlag.set(true);
                    return now;
                } else {
                    return prev;
                }
            });
            if (allowedFlag.get()) {
                return new RateLimitService.Result(true, 0);
            } else {
                var remaining = window.minus(java.time.Duration.between(after, now));
                var ms = Math.max(0, remaining.toMillis());
                return new RateLimitService.Result(false, ms);
            }
        }
    }
}



