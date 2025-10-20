package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 400: 요청 형식, 내용 오류*/
public class InvalidRequestException extends BusinessException{
    public InvalidRequestException(String msg) { super(ErrorCode.VALIDATION_FAILED, msg);}
}
