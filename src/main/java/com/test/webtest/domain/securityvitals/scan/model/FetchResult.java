package com.test.webtest.domain.securityvitals.scan.model;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * 단일 HTTP 요청의 결과를 담는 VO
 * - finalUri: 리다이렉트 적용 후 최종 URI
 * - headers: 응답 헤더 (소문자 키로 통일)
 * - setCookies: Set-Cookie 헤더 전체 목록
 */

public record FetchResult(
        URI finalUri,
        Map<String, List<String>> headers,
        List<String> setCookies
) {}
