package com.test.webtest.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 최상위 래퍼로 success/error 중 하나만 노출
 */

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T>{
    private final ApiSuccess<T> success;

    private ApiResponse(ApiSuccess<T> success) {
        this.success = success;
    }

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(ApiSuccess.<T>builder()
                .status(HttpStatus.OK.value())
                .code(code)
                .message(message)
                .data(data)
                .build());
    }
}
