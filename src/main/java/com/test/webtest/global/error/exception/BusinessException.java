package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;
import lombok.Getter;

/**
 * 모든 도메인 예외의 부모 (런타임 예외로 정의해 서비스단에서 throws 없이도 전파 가능)
 */

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.defaultMessage);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.defaultMessage, cause);
        this.errorCode = errorCode;
    }
}
