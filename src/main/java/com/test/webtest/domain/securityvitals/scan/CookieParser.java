package com.test.webtest.domain.securityvitals.scan;

import java.util.*;

/**
 * Set-Cookie 헤더들을 파싱해 보안 속성 요약을 생성
 * - Secure / HttpOnly 전체 적용 여부
 * - SameSite 정책(Strict/Lax/None/Unspecified)
 */

public class CookieParser {

    public record Summary(boolean allSecure, boolean allHttpOnly, String sameSitePolicy) {}

    public static Summary summarize(List<String> setCookies) {
        if (setCookies == null || setCookies.isEmpty()) {
            return new Summary(false, false, "Unspecified");
        }
        boolean allSecure = true;
        boolean allHttpOnly = true;
        String mergedSameSite = detectSameSite(setCookies);

        for (String c : setCookies) {
            String lower = c.toLowerCase(Locale.ROOT);
            if (!lower.contains("secure")) allSecure = false;
            if (!lower.contains("httponly")) allHttpOnly = false;
        }
        return new Summary(allSecure, allHttpOnly, mergedSameSite);
    }

    private static String detectSameSite(List<String> setCookies) {
        // 하나라도 Strict이면 Strict, 아니면 Lax, 아니면 None, 없으면 Unspecified
        boolean hasStrict = false, hasLax = false, hasNone = false;
        for (String c : setCookies) {
            String lower = c.toLowerCase(Locale.ROOT);
            int idx = lower.indexOf("samesite=");
            if (idx >= 0) {
                String tail = lower.substring(idx + 9);
                if (tail.startsWith("strict")) hasStrict = true;
                else if (tail.startsWith("lax")) hasLax = true;
                else if (tail.startsWith("none")) hasNone = true;
            }
        }
        if (hasStrict) return "Strict";
        if (hasLax) return "Lax";
        if (hasNone) return "None";
        return "Unspecified";
    }
}
