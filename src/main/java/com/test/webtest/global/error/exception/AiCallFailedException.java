package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

public class AiCallFailedException extends BusinessException{
    public AiCallFailedException() { super(ErrorCode.AI_CALL_FAILED);}
    public AiCallFailedException(String msg) { super(ErrorCode.AI_CALL_FAILED, msg);}
}
