package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.logicstatus.service.LogicStatusServiceImpl;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.constants.Channel;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebVitalsServiceImpl implements WebVitalsService {

    private final WebVitalsRepository webVitalsRepository;
    private final TestRepository testRepository;
    private final LogicStatusServiceImpl logicStatusService;
    private final WebVitalsMessageService messageService;

    @Override
    @Transactional
    public void saveWebVitals(UUID testId, WebVitalsSaveCommand cmd) {
        // 테스트 존재 보장(실조회)
        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEST_NOT_FOUND, "test not found: " + testId));

        // upsert
        webVitalsRepository.findByTest_Id(testId).ifPresentOrElse(
                found -> found.updateFrom(cmd.lcp(), cmd.cls(), cmd.inp(), cmd.fcp(), cmd.tbt(), cmd.ttfb()),
                () -> webVitalsRepository.saveAndFlush(WebVitalsEntity.create(
                        test, cmd.lcp(), cmd.cls(), cmd.inp(), cmd.fcp(), cmd.tbt(), cmd.ttfb()
                ))
        );

        // 같은 트랜잭션에서 상태 플래그 갱신
        logicStatusService.onPartialUpdate(testId, Channel.WEB);
    }

    @Override
    @Transactional(readOnly = true)
    public com.test.webtest.domain.webvitals.dto.WebVitalsView getView(UUID testId) {
        var entity = webVitalsRepository.findByTest_Id(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEB_VITALS_NOT_FOUND));
        return messageService.toView(entity);
    }
}