package com.test.webtest.domain.securityvitals.service;

import com.test.webtest.domain.securityvitals.dto.SecurityVitalsView;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.rules.SecurityRulesLoader;
import com.test.webtest.domain.securityvitals.rules.SecurityRulesLoader.ConditionRule;
import com.test.webtest.domain.securityvitals.rules.SecurityRulesLoader.MetricRule;
import com.test.webtest.domain.securityvitals.rules.SecurityRulesLoader.RuleSet;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.global.common.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SecurityMessageService {

    private final SecurityRulesLoader loader;
    private final ScoreCalculator scoreCalculator;

    // Entity -> View 변환
    public SecurityVitalsView toView(SecurityVitalsEntity s,
                                     @Nullable UrgentLevelEntity urgent) {

        Map<String, Object> ctx = buildContext(s);

        List<SecurityVitalsView.Item> items = new ArrayList<>();
        RuleSet ruleSet = loader.load();

        for (MetricRule mr : ruleSet.rulesForAllMetrics()) {
            String msg = firstMatchedMessage(mr, ctx);

            // 내부 metric 키 (예: "ssl", "hsts" ...) 를 사람이 읽기 좋은 코드로 바꿈
            String display = toDisplayName(mr.getMetric()); // "SSL", "HSTS", "XCTO" ...

            // 여기서 urgentLevel 매핑
            String urgentStatus = resolveUrgentStatus(display, urgent);

            items.add(new SecurityVitalsView.Item(display, msg, urgentStatus));
        }

        return new SecurityVitalsView(
                items,
                s.getCreatedAt()
        );
    }

    private String resolveUrgentStatus(String metric, @Nullable UrgentLevelEntity urgent) {
        if (urgent == null) return null;

        return switch (metric) {
            case "HSTS" ->
                    urgent.getHstsStatus();
            case "FRAME-ANCESTORS/XFO" ->
                    urgent.getFrameAncestorsStatus();
            case "SSL" ->
                    urgent.getSslStatus();
            case "XCTO" ->
                    urgent.getXctoStatus();
            case "REFERRER-POLICY" ->
                    urgent.getReferrerPolicyStatus();
            case "COOKIES" ->
                    urgent.getCookiesStatus();
            case "CSP" ->
                    urgent.getCspStatus();
            default ->
                    null;
        };
    }

    // === 룰 매칭 ===
    private String firstMatchedMessage(MetricRule metricRule, Map<String, Object> ctx) {
        for (ConditionRule cr : metricRule.getConditions()) {
            if (matchAll(cr.getWhen(), ctx)) {
                return render(cr.getMessage(), ctx);
            }
        }
        // 안전망
        return "정보 없음";
    }

    // 모든 조건 키 평가
    private boolean matchAll(Map<String, Object> cond, Map<String, Object> ctx) {
        for (Map.Entry<String, Object> e : cond.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (!matchOne(k, v, ctx)) return false;
        }
        return true;
    }

    private boolean matchOne(String key, Object expected, Map<String, Object> ctx) {
        // 1) 기본값
        String field = key;
        String op = "equals";

        // 2) 알려진 연산자 접미사만 인식
        String[] ops = { "equals", "in", "lt", "lte", "gt", "gte", "present" };
        for (String candidate : ops) {
            String suffix = "_" + candidate;
            if (key.endsWith(suffix)) {
                field = key.substring(0, key.length() - suffix.length());
                op = candidate;
                break;
            }
        }

        Object actual = ctx.get(field);

        switch (op) {
            case "equals":
                return Objects.equals(normalize(actual), normalize(expected));
            case "in":
                if (expected instanceof Collection<?> col) {
                    Object n = normalize(actual);
                    for (Object o : col) if (Objects.equals(n, normalize(o))) return true;
                }
                return false;
            case "lt":
                return asLong(actual) < asLong(expected);
            case "lte":
                return asLong(actual) <= asLong(expected);
            case "gt":
                return asLong(actual) > asLong(expected);
            case "gte":
                return asLong(actual) >= asLong(expected);
            case "present":
                boolean want = toBool(expected);
                boolean isPresent = (actual != null) && !String.valueOf(actual).isBlank();
                return want == isPresent;
            default:
                return false;
        }
    }

    // === 템플릿 렌더링 ({{var}}, {{#flag}}...{{/flag}})
    private static final Pattern VAR = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");
    private static final Pattern SEC = Pattern.compile("\\{\\{#([a-zA-Z0-9_]+)}}([\\s\\S]*?)\\{\\{/\\1}}");

    private String render(String template, Map<String, Object> ctx) {
        if (template == null) return "";
        String out = template;

        // section
        Matcher secM = SEC.matcher(out);
        StringBuffer sb = new StringBuffer();
        while (secM.find()) {
            String key = secM.group(1);
            boolean flag = toBool(ctx.get(key));
            String body = flag ? secM.group(2) : "";
            secM.appendReplacement(sb, Matcher.quoteReplacement(body));
        }
        secM.appendTail(sb);
        out = sb.toString();

        // variables
        Matcher m = VAR.matcher(out);
        sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String val = String.valueOf(Objects.requireNonNullElse(ctx.get(key), ""));
            m.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static boolean nz(Boolean b) { return b != null && b; }
    private static long nzl(Long v, long def) { return v != null ? v : def; }
    private static int nzi(Integer v, int def) { return v != null ? v : def; }

    // === Entity -> Map ===
    private Map<String, Object> buildContext(SecurityVitalsEntity s) {
        Map<String, Object> m = new HashMap<>();
        // SSL
        m.put("ssl_valid", nz(s.getSslValid()));
        m.put("ssl_chain_valid", nz(s.getSslChainValid()));
        m.put("ssl_days_remaining", nzi(s.getSslDaysRemaining(), -1)); // 미수집이면 -1
        m.put("ssl_issuer", s.getSslIssuer());
        m.put("ssl_subject", s.getSslSubject());
        m.put("ssl_present", s.getSslValid()!=null || s.getSslChainValid()!=null || s.getSslDaysRemaining()!=null);

        // HSTS
        m.put("has_hsts", nz(s.getHasHsts())); // null -> false
        m.put("hsts_max_age", nzl(s.getHstsMaxAge(), 0L));
        m.put("hsts_include_subdomains", nz(s.getHstsIncludeSubdomains()));
        m.put("hsts_preload", nz(s.getHstsPreload()));
        m.put("hsts_present", s.getHasHsts()!=null || s.getHstsRaw()!=null);

        // X-Content-Type-Options / Referrer-Policy
        m.put("x_content_type_options", nullSafeLower(s.getXContentTypeOptions()));
        m.put("referrer_policy", nullSafeLower(s.getReferrerPolicy()));

        // Cookies
        m.put("has_cookies", nz(s.getHasCookies()));
        m.put("cookie_secure_all", nz(s.getCookieSecureAll()));
        m.put("cookie_httponly_all", nz(s.getCookieHttpOnlyAll()));
        m.put("cookie_samesite_policy", s.getCookieSameSitePolicy()==null ? "unspecified" : s.getCookieSameSitePolicy());

        // CSP
        m.put("has_csp", nz(s.getHasCsp())); // null -> false
        m.put("csp_has_unsafe_inline", nz(s.getCspHasUnsafeInline()));
        m.put("csp_has_unsafe_eval", nz(s.getCspHasUnsafeEval()));
        m.put("csp_frame_ancestors", s.getCspFrameAncestors());
        m.put("csp_frame_ancestors_present", notBlank(s.getCspFrameAncestors()));
        m.put("csp_present", s.getHasCsp()!=null || s.getCspRaw()!=null); // 존재/미수집 판단

        // XFO
        m.put("x_frame_options", s.getXFrameOptions());

        return m;
    }

    // === bottom3용: metric명으로 메시지만 추출 ===
    public String getMessageByMetric(SecurityVitalsEntity s, String metricName) {
        if (s == null) return "정보 없음";
        Map<String, Object> ctx = buildContext(s);
        RuleSet ruleSet = loader.load();
        
        for (MetricRule mr : ruleSet.rulesForAllMetrics()) {
            String display = toDisplayName(mr.getMetric());
            if (display.equalsIgnoreCase(metricName) || 
                mr.getMetric().equalsIgnoreCase(metricName)) {
                return firstMatchedMessage(mr, ctx);
            }
        }
        return "정보 없음";
    }

    private String toDisplayName(String metric) {
        return switch (metric) {
            case "SSL_VALIDITY" -> "SSL";
            case "HSTS" -> "HSTS";
            case "X_CONTENT_TYPE_OPTIONS" -> "XCTO";
            case "REFERRER_POLICY" -> "REFERRER-POLICY";
            case "COOKIES" -> "COOKIES";
            case "CSP" -> "CSP";
            case "X_FRAME_OPTIONS_OR_FRAME_ANCESTORS" -> "FRAME-ANCESTORS/XFO";
            default -> metric;
        };
    }

    // === helpers ===
    private static Object normalize(Object v) {
        if (v == null) return null;
        if (v instanceof String s) return s.trim().toLowerCase(Locale.ROOT);
        return v;
    }

    private static long asLong(Object v) {
        if (v == null) return Long.MIN_VALUE;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return Long.MIN_VALUE; }
    }

    private static boolean toBool(Object v) {
        if (v instanceof Boolean b) return b;
        if (v == null) return false;
        String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT);
        return s.equals("true") || s.equals("1") || s.equals("yes");
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static String nullSafeLower(String s) { return s == null ? null : s.trim().toLowerCase(Locale.ROOT); }
}
