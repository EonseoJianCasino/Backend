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
import com.test.webtest.global.sse.SseEventPublisher;
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
    @Transactional
    public void scanAndSave(UUID testId) {
        TestEntity test = testRepository.getReferenceById(testId);

        // 대상 URL 보안 스캔
        SaveCommand result = securityScanner.scan(test.getUrl());

        // upsert
        securityVitalsRepository.findByTestId(testId).ifPresentOrElse(
                found -> {
                    found.updateFrom(result);
                    log.info("[SEC] updated testId={}", testId);
                },
                () -> {
                    SecurityVitalsEntity created = SecurityVitalsEntity.create(test, result);
                    securityVitalsRepository.save(created);
                    log.info("[SEC] inserted testId={}", testId);
                }
        );

        // 커밋 이후 후속 로직
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                logicStatusService.onPartialUpdate(testId, Channel.SECURITY);
            }
        });
    }

    @Transactional(readOnly = true)
    public SecurityVitalsView getView(UUID testId) {
        SecurityVitalsEntity entity = securityVitalsRepository.findByTestId(testId)
                .orElseThrow(() -> new IllegalArgumentException("security vitals not found " + testId));
        return messageService.toView(entity);
    }
}
