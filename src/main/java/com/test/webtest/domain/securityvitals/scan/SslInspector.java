package com.test.webtest.domain.securityvitals.scan;

import org.springframework.stereotype.Component;

/**
 * TLS 점검을 위한 전략 인터페이스
 * - 구현체 교체(예: SSL Labs API) 가능하도록 추상화
 */

public interface SslInspector {
    record Result(boolean valid, boolean chainValid, Integer daysRemaining, String issuer, String subject) {}
    Result inspect(String host, int port);
}
