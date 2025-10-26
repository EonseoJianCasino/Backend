package com.test.webtest.domain.securityvitals.scan;

import java.util.List;
import java.util.Map;

/**
 * 응답 헤더 맵(소문자 키)에서 첫 번째 값을 안전하게 꺼내는 유틸
 * - 값이 없거나 공백일 경우 null 반환
 */

public class HeaderUtil {
    public static String first(Map<String, List<String>> headersLower, String keyLower) {
        List<String> v = headersLower.get(keyLower);
        if (v == null || v.isEmpty()) return null;
        String s = v.get(0);
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
