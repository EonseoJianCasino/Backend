package com.test.webtest.global.common.util;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SecurityScoringRules {
    public int scoreHsts(SecurityVitalsEntity s) {
        if (Boolean.TRUE.equals(s.getHasHsts())) {
            Long maxAge = s.getHstsMaxAge();
            boolean sub = Boolean.TRUE.equals(s.getHstsIncludeSubdomains());
            if (maxAge != null && maxAge >= 15_768_000L && sub) return 100;   // ≥ 6개월 + includeSubDomains
            if ((maxAge != null && maxAge < 2_592_000L) || !sub) return 50;   // < 30일 OR 서브도메인 미포함
            return 50; // 중간치
        }
        return 0;
    }

    public int scoreXfoOrFrameAncestors(SecurityVitalsEntity s) {
        String fa = safeLower(s.getCspFrameAncestors());
        if (fa != null) {
            if (fa.contains("'none'") || fa.contains("'self'") || fa.matches(".*https?://.*"))
                return 100;
        }
        String xfo = safeUpper(s.getXFrameOptions());
        if ("DENY".equals(xfo) || "SAMEORIGIN".equals(xfo)) return 100;
        return 0;
    }

    public int scoreSsl(SecurityVitalsEntity s) {
        if (!Boolean.TRUE.equals(s.getSslValid()) || !Boolean.TRUE.equals(s.getSslChainValid()))
            return 0;
        Integer days = s.getSslDaysRemaining();
        if (days == null) return 0;
        if (days >= 90) return 100;
        if (days >= 30) return 70;
        return 0; // <30 또는 만료
    }

    public int scoreXContentTypeOptions(SecurityVitalsEntity s) {
        return "nosniff".equals(safeLower(s.getXContentTypeOptions())) ? 100 : 0;
    }

    public int scoreReferrerPolicy(SecurityVitalsEntity s) {
        String rp = safeLower(s.getReferrerPolicy());
        if (rp == null) return 0;
        switch (rp) {
            case "no-referrer":
            case "strict-origin-when-cross-origin":
                return 100;
            case "origin":
            case "same-origin":
            case "origin-when-cross-origin":
            case "strict-origin":
            case "no-referrer-when-downgrade":
                return 50;
            case "unsafe-url":
            default:
                return 0;
        }
    }

    public int scoreCookies(SecurityVitalsEntity s) {
        boolean sec  = Boolean.TRUE.equals(s.getCookieSecureAll());
        boolean http = Boolean.TRUE.equals(s.getCookieHttpOnlyAll());
        String ss    = safeLower(s.getCookieSameSitePolicy()); // strict/lax/none/unspecified

        int satisfied = 0;
        if (sec) satisfied++;
        if (http) satisfied++;
        if ("strict".equals(ss) || "lax".equals(ss)) satisfied++;

        // SameSite=None 이면서 Secure=false 이면 강등
        if ("none".equals(ss) && !sec) return 0;

        switch (satisfied) {
            case 3:  return 100;
            case 2:  return 70;
            case 1:  return 40;
            default: return 0;
        }
    }

    public int scoreCsp(SecurityVitalsEntity s) {
        if (!Boolean.TRUE.equals(s.getHasCsp())) return 0;
        boolean unsafeInline = Boolean.TRUE.equals(s.getCspHasUnsafeInline());
        boolean unsafeEval   = Boolean.TRUE.equals(s.getCspHasUnsafeEval());
        if (!unsafeInline && !unsafeEval) return 100;
        return 50;
    }

    // -------- helpers --------
    public static String safeLower(String v) { return v == null ? null : v.trim().toLowerCase(Locale.ROOT); }
    public static String safeUpper(String v) { return v == null ? null : v.trim().toUpperCase(Locale.ROOT); }
}
