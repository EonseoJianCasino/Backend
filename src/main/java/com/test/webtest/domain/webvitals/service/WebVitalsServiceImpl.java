package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.logicstatus.service.LogicStatusServiceImpl;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.constants.Channel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebVitalsServiceImpl implements WebVitalsService{
    private final WebVitalsRepository webVitalsRepository;
    private final TestRepository testRepository;
    private final LogicStatusServiceImpl logicStatusService;

    @Override
    @Transactional
    public void saveWebVitals(UUID testId, WebVitalsSaveCommand cmd) {
        TestEntity test = testRepository.getReferenceById(testId);

        WebVitalsEntity entity = WebVitalsEntity.create(
                test, cmd.lcp(), cmd.cls(), cmd.inp(), cmd.fcp(), cmd.tbt(), cmd.ttfb()
        );

        webVitalsRepository.save(entity);

        // 커밋 이후 status 호출
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                logicStatusService.onPartialUpdate(testId, Channel.WEB);
            }
        });
    }
}
