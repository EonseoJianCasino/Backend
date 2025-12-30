package com.test.webtest.domain.securityvitals.scan;

/**
 * Strict-Transport-Security 헤더를 파싱
 * - max-age (초) / includeSubDomains / preload 여부
 * - 헤더 존재 여부
 */

public class HstsParser {

    public record Hsts(boolean present, Long maxAge, boolean includeSubDomains, boolean preload) {}

    public static Hsts parse(String raw) {
        if (raw == null || raw.isBlank()) return new Hsts(false, null, false, false);
        String[] parts = raw.split(";");
        Long maxAge = null;
        boolean sub = false, preload = false;
        for (String p : parts) {
            String s = p.trim().toLowerCase();
            if (s.startsWith("max-age")) {
                int eq = s.indexOf('=');
                if (eq > 0) {
                    try { maxAge = Long.parseLong(s.substring(eq + 1).trim()); } catch (Exception ignored) {}
                }
            } else if (s.equals("includesubdomains")) {
                sub = true;
            } else if (s.equals("preload")) {
                preload = true;
            }
        }
        return new Hsts(true, maxAge, sub, preload);
    }
}
