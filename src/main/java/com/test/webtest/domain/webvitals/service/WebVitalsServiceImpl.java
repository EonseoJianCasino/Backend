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

import java.math.BigDecimal;
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
        // 1) 입력값 검증 (단위/범위 등 체크)
        validateOrThrow(cmd);

        // 2) ms 단위 지표들만 소수 둘째 자리까지 "버림" 정규화
        Double normLcp  = truncateMillis(cmd.lcp());   // ms
        Double normCls  = cmd.cls();                   // 그대로 (0~1 스코어)
        Double normInp  = truncateMillis(cmd.inp());   // ms
        Double normFcp  = truncateMillis(cmd.fcp());   // ms
        Double normTtfb = truncateMillis(cmd.ttfb());  // ms

        // 3) 테스트 존재 보장(실조회)
        TestEntity test = testRepository.findById(testId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TEST_NOT_FOUND, "test not found: " + testId));

        // 4) upsert (정규화된 값으로 저장)
        webVitalsRepository.findByTest_Id(testId).ifPresentOrElse(
            found -> found.updateFrom(normLcp, normCls, normInp, normFcp, normTtfb),
            () -> webVitalsRepository.saveAndFlush(
                WebVitalsEntity.create(test, normLcp, normCls, normInp, normFcp, normTtfb)
            )
        );

        // 5) 같은 트랜잭션에서 상태 플래그 갱신
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

    // ====== 여기부터 검증/도우미 메서드 ======

    // ms 값 소수 둘째 자리까지 "버림" (반올림 X)
    private Double truncateMillis(Double v) {
        if (v == null) {
            return null;
        }
        // 모든 값은 0 이상이므로 floor 기반 버림 사용
        return BigDecimal.valueOf(v).setScale(2, java.math.RoundingMode.DOWN).doubleValue();
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
        if (v == null) {
            return;
        }
        if (v < min || v > max) {
            throw new InvalidRequestException(
                name + " 값이 허용 범위를 벗어났습니다. (" + min + " ~ " + max + " " + unit + ")\n" +
                    "단위 안내: LCP/FCP/TTFB=밀리초(ms), INP=밀리초(ms), CLS=0~1");
        }
    }
}
