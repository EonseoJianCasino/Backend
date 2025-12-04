package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 404: AI 분석 결과 없음 */
public class AiResultNotFoundException extends BusinessException {
    public AiResultNotFoundException() {
        super(ErrorCode.AI_RESULT_NOT_FOUND);
    }

    public static AiResultNotFoundException of() {
        return new AiResultNotFoundException();
    }
}

