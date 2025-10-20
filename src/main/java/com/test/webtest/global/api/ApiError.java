package com.test.webtest.global.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * error 응답 객체 포맷
 */

@Getter
@Builder
public class ApiError {
    private final String code;
    private final String message;
    private final List<Detail> details;

    @Getter
    @AllArgsConstructor
    public static class Detail {
        private final String field;
        private final String reason;
    }
}
