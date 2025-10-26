package com.test.webtest.domain.securityvitals.service;

import com.test.webtest.domain.logicstatus.service.LogicStatusServiceImpl;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity.SaveCommand;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.global.common.constants.Channel;
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

    @Override
    @Transactional
    public void scanAndSave(UUID testId) {
        TestEntity test = testRepository.getReferenceById(testId);

        SaveCommand result = SaveCommand.builder()
                .hasCsp(true)
                .hasHsts(true)
                .xFrameOptions("SAMEORIGIN")
                .xContentTypeOptions("nosniff")
                .referrerPolicy("no-referrer-when-downgrade")
                .hstsMaxAge(31536000L)
                .hstsIncludeSubdomains(true)
                .hstsPreload(false)
                .cspHasUnsafeInline(false)
                .cspHasUnsafeEval(false)
                .cspFrameAncestors("'self'")
                .cookieSecureAll(true)
                .cookieHttpOnlyAll(true)
                .cookieSameSitePolicy("Lax")
                .sslValid(true)
                .sslChainValid(true)
                .sslDaysRemaining(120)
                .sslIssuer("Let's Encrypt")
                .sslSubject("CN=example.com")
                .cspRaw("default-src 'self'; img-src https: data:;")
                .hstsRaw("max-age=31536000; includeSubDomains")
                .build();

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

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                logicStatusService.onPartialUpdate(testId, Channel.SECURITY);
            }
        });
    }
}
