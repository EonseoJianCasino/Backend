package com.test.webtest.global.api;

import lombok.Builder;
import lombok.Getter;

/**
 * success 응답 객체 포맷
 */
@Getter
@Builder
public class ApiSuccess<T> {
    private final int status;
    private final String code;
    private final String message;
    private final T data;
}
