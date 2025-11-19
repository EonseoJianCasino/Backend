package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.logicstatus.service.LogicStatusServiceImpl;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.domain.webvitals.dto.WebVitalsView;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.constants.Channel;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.exception.InvalidRequestException;
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
    private final UrgentLevelRepository urgentLevelRepository;

    @Override
    @Transactional
    public void saveWebVitals(UUID testId, WebVitalsSaveCommand cmd) {
        // 테스트 존재 보장(실조회)
        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEST_NOT_FOUND, "test not found: " + testId));

        // upsert
        webVitalsRepository.findByTest_Id(testId).ifPresentOrElse(
                found -> found.updateFrom(cmd.lcp(), cmd.cls(), cmd.inp(), cmd.fcp(), cmd.ttfb()),
                () -> webVitalsRepository.saveAndFlush(WebVitalsEntity.create(
                        test, cmd.lcp(), cmd.cls(), cmd.inp(), cmd.fcp(), cmd.ttfb())));

        // 같은 트랜잭션에서 상태 플래그 갱신
        logicStatusService.onPartialUpdate(testId, Channel.WEB);
    }

    @Override
    @Transactional(readOnly = true)
    public WebVitalsView getView(UUID testId) {
        var entity = webVitalsRepository.findByTest_Id(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEB_VITALS_NOT_FOUND));
        var urgent = urgentLevelRepository.findByTestId(testId).orElse(null);

        return messageService.toView(entity, urgent);
    }

//    private void validateOrThrow(WebVitalsSaveCommand c) {
//        // 1) 전부 null이면 저장 무의미
//        if (c.lcp() == null && c.cls() == null && c.inp() == null &&
//                c.fcp() == null && c.ttfb() == null) {
//            throw new InvalidRequestException("최소 하나 이상의 지표가 필요합니다.");
//        }
//
//        // 2) NaN 금지
//        rejectIfNaN(c.lcp(), "LCP");
//        rejectIfNaN(c.cls(), "CLS");
//        rejectIfNaN(c.inp(), "INP");
//        rejectIfNaN(c.fcp(), "FCP");
//        rejectIfNaN(c.ttfb(), "TTFB");
//
//        // 3) 음수는 @PositiveOrZero에서 걸리지만 방어적으로 한 번 더
//        rejectIfNegative(c.lcp(), "LCP");
//        rejectIfNegative(c.cls(), "CLS");
//        rejectIfNegative(c.inp(), "INP");
//        rejectIfNegative(c.fcp(), "FCP");
//        rejectIfNegative(c.ttfb(), "TTFB");
//
//        // 4) 단위/범위 검증
//        // CLS: 0~1
//        if (c.cls() != null && (c.cls() < 0.0 || c.cls() > 1.0)) {
//            throw new InvalidRequestException("CLS는 0~1 범위여야 합니다. (예: 0.07)");
//        }
//        // LCP/FCP (초): 0~60s
//        rejectIfOutOfRange(c.lcp(), 0.0, 60.0, "LCP", "초(s)");
//        rejectIfOutOfRange(c.fcp(), 0.0, 60.0, "FCP", "초(s)");
//        // TTFB (초): 0~30s
//        rejectIfOutOfRange(c.ttfb(), 0.0, 30.0, "TTFB", "초(s)");
//        // INP (ms): 0~10000ms
//        rejectIfOutOfRange(c.inp(), 0.0, 10000.0, "INP", "밀리초(ms)");
//    }
//
//    private void rejectIfNaN(Double v, String name) {
//        if (v != null && v.isNaN()) {
//            throw new InvalidRequestException(name + " 값은 NaN일 수 없습니다.");
//        }
//    }
//
//    private void rejectIfNegative(Double v, String name) {
//        if (v != null && v < 0.0) {
//            throw new InvalidRequestException(name + " 값은 음수일 수 없습니다.");
//        }
//    }
//
//    private void rejectIfOutOfRange(Double v, double min, double max, String name, String unit) {
//        if (v == null)
//            return;
//        if (v < min || v > max) {
//            throw new InvalidRequestException(
//                    name + " 값이 허용 범위를 벗어났습니다. (" + min + " ~ " + max + " " + unit + ")\n" +
//                            "단위 안내: LCP/FCP/TTFB=초(s), INP=밀리초(ms), CLS=0~1");
//        }
//    }
}