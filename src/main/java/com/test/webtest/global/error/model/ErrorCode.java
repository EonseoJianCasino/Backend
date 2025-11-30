package com.test.webtest.global.error.model;

import org.springframework.http.HttpStatus;

/**
 * 전역 에러 코드 표준화
 */
public enum ErrorCode {
    // 공통
    VALIDATION_FAILED        (HttpStatus.BAD_REQUEST,            "VALIDATION_FAILED",        "요청 값 검증에 실패했습니다."),
    INVALID_PATH_VARIABLE    (HttpStatus.BAD_REQUEST,            "INVALID_PATH_VARIABLE",    "경로 변수 형식이 잘못되었습니다."),
    MESSAGE_NOT_READABLE     (HttpStatus.BAD_REQUEST,            "MESSAGE_NOT_READABLE",     "요청 본문을 읽을 수 없습니다(JSON 형식 확인)."),
    MISSING_REQUEST_PARAM    (HttpStatus.BAD_REQUEST,            "MISSING_REQUEST_PARAM",    "필수 요청 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED       (HttpStatus.METHOD_NOT_ALLOWED,     "METHOD_NOT_ALLOWED",       "허용되지 않은 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE   (HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",   "지원하지 않는 콘텐츠 타입입니다."),
    NO_HANDLER_FOUND         (HttpStatus.NOT_FOUND,              "NO_HANDLER_FOUND",         "요청한 경로를 찾을 수 없습니다."),
    INTERNAL_ERROR           (HttpStatus.INTERNAL_SERVER_ERROR,  "INTERNAL_ERROR",           "내부 서버 오류가 발생했습니다."),

    // 보안/권한(선택: 스프링 시큐리티 사용 시)
    ACCESS_DENIED            (HttpStatus.FORBIDDEN,              "ACCESS_DENIED",            "요청 권한이 없습니다."),

    // 테스트/리소스
    TEST_NOT_FOUND           (HttpStatus.NOT_FOUND,              "TEST_NOT_FOUND",           "요청한 테스트가 존재하지 않습니다."),
    WEB_VITALS_NOT_FOUND     (HttpStatus.NOT_FOUND,              "WEB_VITALS_NOT_FOUND",     "웹 바이탈 데이터가 존재하지 않습니다."),
    SECURITY_VITALS_NOT_FOUND(HttpStatus.NOT_FOUND,              "SECURITY_VITALS_NOT_FOUND","보안 바이탈 데이터가 존재하지 않습니다."),
    SCORES_NOT_READY         (HttpStatus.CONFLICT,               "SCORES_NOT_READY",         "점수가 아직 집계되지 않았습니다. 잠시 후 다시 시도하세요."),
    URGENT_LEVEL_NOT_FOUND   (HttpStatus.NOT_FOUND,              "URGENT_LEVEL_NOT_FOUND",   "긴급도 상태가 존재하지 않습니다."),

    PRIORITIES_NOT_READY   (HttpStatus.CONFLICT,                 "PRIORITIES_NOT_READY",     "우선순위가 아직 산출되지 않았습니다. 잠시 후 다시 시도하세요."), // [추가]
    PRIORITIES_NOT_FOUND   (HttpStatus.NOT_FOUND,                "PRIORITIES_NOT_FOUND",     "우선순위 데이터가 존재하지 않습니다."), // [추가]
    AI_NOT_READY             (HttpStatus.CONFLICT,               "AI_NOT_READY",             "AI 분석 결과가 아직 준비되지 않았습니다."),

    // 비즈니스 룰
    DUPLICATE_REQUEST        (HttpStatus.TOO_MANY_REQUESTS,      "DUPLICATE_REQUEST",        "동일 URL로 10초 내 중복 요청이 차단되었습니다."),
    CONCURRENCY_CONFLICT     (HttpStatus.CONFLICT,               "CONCURRENCY_CONFLICT",     "동시 처리 충돌이 발생했습니다. 다시 시도하세요."),

    // 외부 연동
    AI_CALL_FAILED           (HttpStatus.BAD_GATEWAY,            "AI_CALL_FAILED",           "AI 서버 호출에 실패했습니다."),
    AI_PARSE_FAILED          (HttpStatus.INTERNAL_SERVER_ERROR,  "AI_PARSE_FAILED",          "AI 응답 파싱에 실패했습니다."),
    EXTERNAL_SERVICE_TIMEOUT (HttpStatus.GATEWAY_TIMEOUT,        "EXTERNAL_SERVICE_TIMEOUT", "외부 서비스 응답 지연으로 실패했습니다.");

    public final HttpStatus httpStatus;
    public final String code;
    public final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.httpStatus = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
