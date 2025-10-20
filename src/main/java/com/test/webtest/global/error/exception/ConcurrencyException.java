package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 409: 동시성 충돌/잠금 실패 등 */
public class ConcurrencyException extends BusinessException{
    public ConcurrencyException() { super(ErrorCode.CONCURRENCY_CONFLICT);}
    public ConcurrencyException(String msg) { super(ErrorCode.CONCURRENCY_CONFLICT, msg);}
}
