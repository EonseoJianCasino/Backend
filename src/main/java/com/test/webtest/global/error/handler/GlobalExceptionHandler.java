package com.test.webtest.global.error.handler;

import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.exception.ConcurrencyException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.error.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===== traceId =====
    private String currentTraceId() {
        return MDC.get("traceId");
    }

    // ===== 도메인 예외 =====
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        var ec = ex.getErrorCode();
        var traceId = currentTraceId();
        log.warn("[BUSINESS] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity
                .status(ec.httpStatus)
                .body(ErrorResponse.of(ec, ex.getMessage(), traceId)); // 도메인 메시지는 그대로
    }

    // ===== 바인딩/검증 =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var msg = ex.getBindingResult().getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        var ec = ErrorCode.VALIDATION_FAILED;
        var traceId = currentTraceId();
        log.warn("[VALIDATION] code={} msg={}", ec.code, msg);
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, msg, traceId));
    }

    // @RequestParam/@PathVariable 제약 위반 (예: @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        var msg = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        var ec = ErrorCode.VALIDATION_FAILED;
        var traceId = currentTraceId();
        log.warn("[CONSTRAINT] code={} msg={}", ec.code, msg);
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, msg, traceId));
    }

    // 경로/쿼리 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var ec = ErrorCode.INVALID_PATH_VARIABLE;
        var traceId = currentTraceId();
        log.warn("[TYPE_MISMATCH] name={} value={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // JSON 파싱 실패/본문 읽기 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException ex) {
        var ec = ErrorCode.MESSAGE_NOT_READABLE;
        var traceId = currentTraceId();
        log.warn("[BAD_JSON] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        var ec = ErrorCode.MISSING_REQUEST_PARAM;
        var msg = "필수 파라미터가 누락되었습니다: " + ex.getParameterName();
        var traceId = currentTraceId();
        log.warn("[MISSING_PARAM] code={} msg={}", ec.code, msg);
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, msg, traceId));
    }

    // ===== HTTP 프로토콜 레벨 =====
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        var ec = ErrorCode.METHOD_NOT_ALLOWED;
        var traceId = currentTraceId();
        log.warn("[METHOD_NOT_ALLOWED] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        var ec = ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        var traceId = currentTraceId();
        log.warn("[UNSUPPORTED_MEDIA_TYPE] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // 존재하지 않는 경로(스프링 설정: throw-exception-if-no-handler-found=true 필요)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        var ec = ErrorCode.NO_HANDLER_FOUND;
        var traceId = currentTraceId();
        log.warn("[NO_HANDLER] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // ===== 보안(선택: 시큐리티 사용 시) =====
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        var ec = ErrorCode.ACCESS_DENIED;
        var traceId = currentTraceId();
        log.warn("[ACCESS_DENIED] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // ===== 데이터/영속성 =====
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        // 중복키/제약 위반 등 → 409로 응답하거나, 케이스에 따라 400
        var ec = ErrorCode.CONCURRENCY_CONFLICT;
        var traceId = currentTraceId();
        log.warn("[DATA_INTEGRITY] code={} msg={}", ec.code, ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // ===== 락/동시성 경로 =====
    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<ErrorResponse> handleConcurrency(ConcurrencyException ex) {
        var ec = ErrorCode.CONCURRENCY_CONFLICT; // httpStatus = 409
        var traceId = currentTraceId();
        log.warn("[CONCURRENCY] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }

    // ===== 마지막 안전망 =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex) {
        var ec = ErrorCode.INTERNAL_ERROR;
        var traceId = currentTraceId();
        log.error("[UNHANDLED] code={} msg={}", ec.code, ex.getMessage());
        return ResponseEntity.status(ec.httpStatus).body(ErrorResponse.of(ec, null, traceId));
    }
}
