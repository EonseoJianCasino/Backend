package com.test.webtest.domain.securityvitals.service;

import com.test.webtest.domain.logicstatus.service.LogicStatusServiceImpl;
import com.test.webtest.domain.securityvitals.dto.SecurityVitalsView;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity.SaveCommand;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.scan.SecurityScanner;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.global.common.constants.Channel;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.exception.EntityNotFoundException;
import com.test.webtest.global.error.exception.SecurityScanFailedException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.logging.Monitored;
import com.test.webtest.global.longpoll.LongPollingManager;
import com.test.webtest.global.longpoll.LongPollingTopic;
import com.test.webtest.global.longpoll.WaitKey;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private final UrgentLevelRepository urgentLevelRepository;
    private final LongPollingManager longPollingManager;

    @Override
    @Monitored("security.scanAndSave")
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
            log.warn("[SEC][SCAN][FAIL] testId={} url={}, msg={}",testId, test.getUrl(), e.toString());
            longPollingManager.completeError(
                    new WaitKey(testId, LongPollingTopic.CORE_READY),
                    ErrorCode.SECURITY_SCAN_FAILED,
                    "보안 지표 수집 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
            throw new SecurityScanFailedException(
                    "보안 지표 스캔 실패: testId=" + testId);
        }

        // 3) upsert
        securityVitalsRepository.findByTest_Id(testId).ifPresentOrElse(
                found -> {
                    found.updateFrom(result);
                    // 변경감지에만 의존하지 말고 확실히
                    securityVitalsRepository.flush();
                },
                () -> {
                    SecurityVitalsEntity created = SecurityVitalsEntity.create(test, result);
                    securityVitalsRepository.saveAndFlush(created); // ← 즉시 flush
                }
        );

        // 4) 같은 트랜잭션 안에서 즉시 검증
        boolean exists = securityVitalsRepository.existsByTest_Id(testId);

        if (!exists) {
            // 방어적: 매핑/제약 문제면 바로 알기 위해 예외
            log.error("[SEC][UPSERT][INCONSISTENT]  testId={} url={} msg={}", testId, test.getUrl(), "row not visible in same tx");
            throw new IllegalStateException("[SEC][UPSERT][INCONSISTENT] save failed (no row visible in same tx) testId=" + testId);
        }

        // 5) 상태 플래그 업데이트 (같은 트랜잭션에서)
        logicStatusService.onPartialUpdate(testId, Channel.SECURITY);
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityVitalsView getView(UUID testId) {

        // 아직 수집 전이면 202/204로 매핑하는 커스텀 예외 권장(선택)
        var entity = securityVitalsRepository.findByTest_Id(testId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TEST_NOT_FOUND)); // 404 매핑

        var urgent = urgentLevelRepository.findByTestId(testId).orElse(null);
        return messageService.toView(entity, urgent);
    }
}
