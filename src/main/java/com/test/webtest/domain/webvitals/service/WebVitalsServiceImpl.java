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
        WebVitalsSaveCommand msCmd = normalizeToMillis(cmd);
        validateOrThrow(msCmd);

        // 테스트 존재 보장(실조회)
        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEST_NOT_FOUND, "test not found: " + testId));

        // upsert
        webVitalsRepository.findByTest_Id(testId).ifPresentOrElse(
                found -> found.updateFrom(msCmd.lcp(), msCmd.cls(), msCmd.inp(), msCmd.fcp(), msCmd.ttfb()),
                () -> webVitalsRepository.saveAndFlush(WebVitalsEntity.create(
                        test, msCmd.lcp(), msCmd.cls(), msCmd.inp(), msCmd.fcp(), msCmd.ttfb())));

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

    // 웹 지표 입력 값 검증
    private void validateOrThrow(WebVitalsSaveCommand c) {
        // 1) 전부 null이면 저장 무의미
        if (c.lcp() == null && c.cls() == null && c.inp() == null &&
                c.fcp() == null && c.ttfb() == null) {
            throw new InvalidRequestException("최소 하나 이상의 지표가 필요합니다.");
        }

        // 2) NaN 금지
        rejectIfNaN(c.lcp(), "LCP");
        rejectIfNaN(c.cls(), "CLS");
        rejectIfNaN(c.inp(), "INP");
        rejectIfNaN(c.fcp(), "FCP");
        rejectIfNaN(c.ttfb(), "TTFB");

        // 3) 음수 방어
        rejectIfNegative(c.lcp(), "LCP");
        rejectIfNegative(c.cls(), "CLS");
        rejectIfNegative(c.inp(), "INP");
        rejectIfNegative(c.fcp(), "FCP");
        rejectIfNegative(c.ttfb(), "TTFB");

        // 4) 단위/범위 검증
        // CLS: 0~1
        if (c.cls() != null && (c.cls() < 0.0 || c.cls() > 1.0)) {
            throw new InvalidRequestException("CLS는 0~1 범위여야 합니다. (예: 0.07)");
        }
        // LCP/FCP (ms): 0~60,000ms
        rejectIfOutOfRange(c.lcp(), 0.0, 60000.0, "LCP", "밀리초(ms)");
        rejectIfOutOfRange(c.fcp(), 0.0, 60000.0, "FCP", "밀리초(ms)");
        // TTFB (ms): 0~30,000ms
        rejectIfOutOfRange(c.ttfb(), 0.0, 30000.0, "TTFB", "밀리초(ms)");
        // INP (ms): 0~10,000ms
        rejectIfOutOfRange(c.inp(), 0.0, 10000.0, "INP", "밀리초(ms)");
    }

    // 초 단위로 들어오는 값을 ms로 통일 (CLS는 비율이므로 제외)
    private WebVitalsSaveCommand normalizeToMillis(WebVitalsSaveCommand c) {
        return new WebVitalsSaveCommand(
                toMillis(c.lcp()),
                c.cls(),
                toMillis(c.inp()),
                toMillis(c.fcp()),
                toMillis(c.ttfb())
        );
    }

    private Double toMillis(Double secondsValue) {
        if (secondsValue == null) return null;
        return secondsValue * 1000.0;
    }

    private void rejectIfNaN(Double v, String name) {
        if (v != null && v.isNaN()) {
            throw new InvalidRequestException(name + " 값은 NaN일 수 없습니다.");
        }
    }

    private void rejectIfNegative(Double v, String name) {
        if (v != null && v < 0.0) {
            throw new InvalidRequestException(name + " 값은 음수일 수 없습니다.");
        }
    }

    private void rejectIfOutOfRange(Double v, double min, double max, String name, String unit) {
        if (v == null)
            return;
        if (v < min || v > max) {
            throw new InvalidRequestException(
                    name + " 값이 허용 범위를 벗어났습니다. (" + min + " ~ " + max + " " + unit + ")\n" +
                            "단위 안내: LCP/FCP/TTFB=밀리초(ms), INP=밀리초(ms), CLS=0~1");
        }
    }
}
