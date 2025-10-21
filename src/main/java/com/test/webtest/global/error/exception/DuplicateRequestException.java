package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 429: 10분 중복 차단 */
public class DuplicateRequestException extends BusinessException{
    public DuplicateRequestException() { super(ErrorCode.DUPLICATE_REQUEST);}
}
