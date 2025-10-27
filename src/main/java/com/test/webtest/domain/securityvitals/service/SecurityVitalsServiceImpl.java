package com.test.webtest.domain.securityvitals.service;

import com.test.webtest.domain.logicstatus.service.LogicStatusServiceImpl;
import com.test.webtest.domain.securityvitals.dto.SecurityVitalsView;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity.SaveCommand;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.scan.SecurityScanner;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.global.common.constants.Channel;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.exception.EntityNotFoundException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.sse.SseEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityVitalsServiceImpl implements SecurityVitalsService {
    private final TestRepository testRepository;
    private final SecurityVitalsRepository securityVitalsRepository;
    private final LogicStatusServiceImpl logicStatusService;
    private final SecurityScanner securityScanner;
    private final SecurityMessageService messageService;
    private final SseEventPublisher sseEventPublisher;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // ← 커밋 보장
    public void scanAndSave(UUID testId) {
        // 1) 테스트 존재 보장 (프록시 대신 실조회)
        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SECURITY_VITALS_NOT_FOUND, "test not found: " + testId));

        // 2) 스캔 (실패해도 저장은 시도)
        SaveCommand result;
        try {
            result = securityScanner.scan(test.getUrl());
        } catch (Exception e) {
            log.warn("[SEC][SCAN][FAIL] url={}, ex={}", test.getUrl(), e.toString());
            result = SaveCommand.failed(); // 실패 플래그/기본값 채우는 팩토리(없다면 만들어도 됨)
        }

        // 3) upsert
        SaveCommand finalResult = result;
        SaveCommand finalResult1 = result;
        securityVitalsRepository.findByTest_Id(testId).ifPresentOrElse(
                found -> {
                    found.updateFrom(finalResult);
                    log.info("[SEC] updated testId={}", testId);
                    // 변경감지에만 의존하지 말고 확실히
                    securityVitalsRepository.flush();
                },
                () -> {
                    SecurityVitalsEntity created = SecurityVitalsEntity.create(test, finalResult1);
                    securityVitalsRepository.saveAndFlush(created); // ← 즉시 flush
                    log.info("[SEC] inserted testId={}", testId);
                }
        );

        // 4) 같은 트랜잭션 안에서 즉시 검증
        long cnt = securityVitalsRepository.count();
        boolean exists = securityVitalsRepository.existsByTest_Id(testId);
        log.info("[SEC][TX] count={}, exists(testId)={}", cnt, exists);

        if (!exists) {
            // 방어적: 매핑/제약 문제면 바로 알기 위해 예외
            throw new IllegalStateException("[SEC] save failed (no row visible in same tx) testId=" + testId);
        }

        // 5) 상태 플래그 업데이트 (같은 트랜잭션에서)
        logicStatusService.onPartialUpdate(testId, Channel.SECURITY);

        // 6) 커밋 이후 SSE 전송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                securityVitalsRepository.findByTest_Id(testId).ifPresent(entity -> {
                    var view = messageService.toView(entity);
                    sseEventPublisher.publishSecuritySnapshot(testId.toString(), view);
                });
            }
        });
    }

    @Transactional(readOnly = true)
    public SecurityVitalsView getView(UUID testId) {
        // 아직 수집 전이면 202/204로 매핑하는 커스텀 예외 권장(선택)
        var entity = securityVitalsRepository.findByTest_Id(testId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TEST_NOT_FOUND)); // 404 매핑
        return messageService.toView(entity);
    }
}
