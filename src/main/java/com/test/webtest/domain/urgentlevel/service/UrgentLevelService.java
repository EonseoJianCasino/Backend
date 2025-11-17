package com.test.webtest.domain.urgentlevel.service;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import com.test.webtest.global.common.util.ScoreCalculator.UrgentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrgentLevelService {

    private final UrgentLevelRepository urgentLevelRepository;
    private final TestRepository testRepository;
    private final WebVitalsRepository webVitalsRepository;
    private final SecurityVitalsRepository securityVitalsRepository;
    private final ScoreCalculator scoreCalculator;

    @Transactional
    public void calcAndSave(UUID testId) {
        TestEntity test = testRepository.getReferenceById(testId);
        WebVitalsEntity web = webVitalsRepository.findByTest_Id(testId).orElse(null);
        SecurityVitalsEntity sec = securityVitalsRepository.findByTest_Id(testId).orElse(null);

        Map<String, UrgentStatus> webStatuses = scoreCalculator.webUrgentStatuses(web);
        Map<String, UrgentStatus> secStatuses = scoreCalculator.securityUrgentStatuses(sec);

        String lcpStatus  = toString(webStatuses.get("LCP"));
        String clsStatus  = toString(webStatuses.get("CLS"));
        String inpStatus  = toString(webStatuses.get("INP"));
        String fcpStatus  = toString(webStatuses.get("FCP"));
        String ttfbStatus = toString(webStatuses.get("TTFB"));

        String hstsStatus         = toString(secStatuses.get("HSTS"));
        String frameAncestorsStat = toString(secStatuses.get("FRAME-ANCESTORS/XFO"));
        String sslStatus          = toString(secStatuses.get("SSL"));
        String xctoStatus         = toString(secStatuses.get("XCTO"));
        String rpStatus           = toString(secStatuses.get("REFERRER-POLICY"));
        String cookiesStatus      = toString(secStatuses.get("COOKIES"));
        String cspStatus          = toString(secStatuses.get("CSP"));

        urgentLevelRepository.findByTestId(testId).ifPresentOrElse(
                found -> {
                    found.update(
                            lcpStatus, clsStatus, inpStatus, fcpStatus, ttfbStatus,
                            hstsStatus, frameAncestorsStat, sslStatus,
                            xctoStatus, rpStatus, cookiesStatus, cspStatus
                    );
                    log.info("[URGENT_LEVEL] updated testId={}", testId);
                },
                () -> {
                    UrgentLevelEntity created = UrgentLevelEntity.create(
                            test,
                            lcpStatus, clsStatus, inpStatus, fcpStatus, ttfbStatus,
                            hstsStatus, frameAncestorsStat, sslStatus,
                            xctoStatus, rpStatus, cookiesStatus, cspStatus
                    );
                    urgentLevelRepository.save(created);
                    log.info("[URGENT_LEVEL] inserted testId={}", testId);
                }
        );
    }

    private String toString(UrgentStatus status) {
        return status == null ? null : status.name();
    }
}
