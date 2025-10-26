package com.test.webtest.domain.securityvitals.scan;

import java.util.Locale;

/**
 * Referrer-Policy 원문을 소문자/트리밍 규칙으로 정규화
 */

public class ReferrerPolicyNormalizer {
    public static String normalize(String raw) {
        if (raw == null) return null;
        return raw.trim().toLowerCase(Locale.ROOT);
    }
}
