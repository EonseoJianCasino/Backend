package com.test.webtest.global.error.handler;

import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.error.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리 도메인 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        var code = ex.getErrorCode();
        return ResponseEntity.status(code.httpStatus)
                .body(new ErrorResponse(code.code, ex.getMessage(), Instant.now()));
    }

    //@Valid 바인딩/필드 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        var code = ErrorCode.VALIDATION_FAILED;
        return ResponseEntity.status(code.httpStatus)
                .body(new ErrorResponse(code.code, msg, Instant.now()));
    }

    // 경로변수/쿼리스트링 타입 불일치(UUID 파싱 실패 등)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var code = ErrorCode.INVALID_PATH_VARIABLE;
        return ResponseEntity.status(code.httpStatus)
                .body(new ErrorResponse(code.code, "잘못된 경로 변수/쿼리 파라미터 형식입니다.", Instant.now()));
    }

    // 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        var code = ErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(code.httpStatus)
                .body(new ErrorResponse(code.code, code.defaultMessage, Instant.now()));
    }

    // 지원하지 않는 컨텐츠 타입
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        var code = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        return ResponseEntity.status(code.httpStatus)
                .body(new ErrorResponse(code.code, code.defaultMessage, Instant.now()));
    }

    // 그 밖의 모든 예외(마지막 안전망)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex) {
        var code = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(code.httpStatus)
                .body(new ErrorResponse(code.code, code.defaultMessage, Instant.now()));
    }
}
