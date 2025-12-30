package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 429: 10초 중복 차단 */
public class DuplicateRequestException extends BusinessException{
    public DuplicateRequestException() {
        super(ErrorCode.DUPLICATE_REQUEST);
    }

    public DuplicateRequestException(String msg) {
        super(ErrorCode.DUPLICATE_REQUEST, msg);
    }
}
