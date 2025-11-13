package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 404: 엔티티 없음*/
public class EntityNotFoundException extends BusinessException{
    public EntityNotFoundException(ErrorCode code) { super(code); }

    /** 사용 예: throw EntityNotFoundException.of(ErrorCode.TEST_NOT_FOUND) */
    public static EntityNotFoundException of(ErrorCode code) { return new EntityNotFoundException(code); }
}
