package com.test.webtest.global.error.model;

import java.time.Instant;

public record ErrorResponse(
        int status,        // HTTP 숫자 코드
        String message,    // 사용자/클라 표시용 메시지
        Instant timestamp  // UTC 타임스탬프
) {
    public static ErrorResponse of(ErrorCode ec, String messageOverride) {
        return new ErrorResponse(
                ec.httpStatus.value(),
                (messageOverride != null && !messageOverride.isBlank())
                        ? messageOverride
                        : ec.defaultMessage,
                Instant.now()
        );
    }

    public static ErrorResponse of(ErrorCode ec) {
        return of(ec, null);
    }
}
