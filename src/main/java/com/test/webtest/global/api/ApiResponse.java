package com.test.webtest.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

/**
 * 최상위 래퍼로 success/error 중 하나만 노출
 */

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T>{
    private final ApiSuccess<T> success;
    private final ApiError error;

    private ApiResponse(ApiSuccess<T> success, ApiError error) {
        this.success = success;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(ApiSuccess.<T>builder()
                .code(code).message(message).data(data).build(),null);
    }

    public static ApiResponse<?> error(String code, String message, List<ApiError.Detail> details) {
        return new ApiResponse<>(null, ApiError.builder()
                .code(code).message(message).details(details).build());
    }

    public static ApiResponse<?> error(String code, String message) {
        return error(code, message, null);
    }
}
