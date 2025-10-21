package com.test.webtest.global.error.exception;

import com.test.webtest.global.error.model.ErrorCode;

/** 404: 엔티티 없음*/
public class EntityNotFoundException extends BusinessException{
    public EntityNotFoundException(ErrorCode code) { super(code);}
    public static EntityNotFoundException test() { return new EntityNotFoundException((ErrorCode.TEST_NOT_FOUND));}
    public static EntityNotFoundException webVitals() { return new EntityNotFoundException(ErrorCode.WEB_VITALS_NOT_FOUND);}
    public static EntityNotFoundException securityVitals() { return new EntityNotFoundException(ErrorCode.SECURITY_VITALS_NOT_FOUND);}
}
