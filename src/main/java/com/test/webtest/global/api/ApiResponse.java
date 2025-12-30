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

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(ApiSuccess.<T>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .build());
    }

    // 200 OK
    public static <T> ApiResponse<T> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    // 201 CREATED
    public static <T> ApiResponse<T> created(String message, T data) {
        return success(HttpStatus.CREATED, message, data);
    }

}
