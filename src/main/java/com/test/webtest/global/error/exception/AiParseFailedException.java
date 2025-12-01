package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

public class AiParseFailedException extends BusinessException{
    public AiParseFailedException() { super(ErrorCode.AI_PARSE_FAILED);}
    public AiParseFailedException(String msg) { super(ErrorCode.AI_PARSE_FAILED, msg);}
    public AiParseFailedException(Throwable cause) { super(ErrorCode.AI_PARSE_FAILED, cause);}
}

