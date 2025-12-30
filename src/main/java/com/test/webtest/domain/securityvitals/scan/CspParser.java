package com.test.webtest.domain.securityvitals.scan;

/**
 * Content-Security-Policy 헤더를 파싱
 * - 'unsafe-inline' / 'unsafe-eval' 사용 여부
 * - frame-ancestors 지시어 원문 추출
 * - 헤더 존재 여부
 */

public class CspParser {

    public record Csp(boolean present, boolean unsafeInline, boolean unsafeEval, String frameAncestors) {}

    public static Csp parse(String raw) {
        if (raw == null || raw.isBlank()) return new Csp(false, false, false, null);

        boolean unsafeInline = false, unsafeEval = false;
        String frameAncestors = null;

        String[] directives = raw.split(";");
        for (String d : directives) {
            String s = d.trim().toLowerCase();
            if (s.contains("'unsafe-inline'")) unsafeInline = true;
            if (s.contains("'unsafe-eval'")) unsafeEval = true;

            if (s.startsWith("frame-ancestors")) {
                frameAncestors = d.trim().substring("frame-ancestors".length()).trim(); // 원문 보존
            }
        }
        return new Csp(true, unsafeInline, unsafeEval, frameAncestors);
    }
}
