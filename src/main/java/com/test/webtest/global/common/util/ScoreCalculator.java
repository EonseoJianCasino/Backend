package com.test.webtest.global.common.util;

import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScoreCalculator {

    private final SecurityScoringRules securityRules;

    public ScoreCalculator(SecurityScoringRules securityRules) {
        this.securityRules = securityRules;
    }

    public enum UrgentStatus { GOOD, WARNING, POOR }

    /** 웹 성능 지표(LCP, CLS, INP, FCP, TTFB)를 0~100 점수로 환산하여 묶어서 반환 */
    public WebScores toWebScores(@Nullable WebVitalsEntity web) {
        if (web == null) {
            return new WebScores(0, 0, 0, 0, 0);
        }
        double lcp = linearScore(web.getLcp(), WebVitalsThreshold.LCP);
        double cls = linearScore(web.getCls(), WebVitalsThreshold.CLS);
        double inp = linearScore(web.getInp(), WebVitalsThreshold.INP);
        double fcp = linearScore(web.getFcp(), WebVitalsThreshold.FCP);
        double ttfb = linearScore(web.getTtfb(), WebVitalsThreshold.TTFB);

        return new WebScores(
                (int) Math.round(lcp),
                (int) Math.round(cls),
                (int) Math.round(inp),
                (int) Math.round(fcp),
                (int) Math.round(ttfb));
    }

    /** 개별 지표: good=100, poor=0으로 선형 환산(사이 값 선형 보간) */
    private double linearScore(Double value, WebVitalsThreshold metric) {
        if (value == null)
            return 0.0;

        double good = metric.getGood();
        double poor = metric.getPoor();

        if (value <= good)
            return 100.0;
        if (value > poor)
            return 0.0;

        double ratio = (value - good) / (poor - good); // 0~1
        double score = 100.0 - (ratio * 100.0);
        return Math.max(0.0, Math.min(100.0, score));
    }

    // ---- Web 긴급도 ----
    public UrgentStatus calculateUrgentStatus(Double value, WebVitalsThreshold metric) {
        if (value == null)
            return null;

        double good = metric.getGood();
        double poor = metric.getPoor();

        if (value <= good) {
            return UrgentStatus.GOOD;
        } else if (value >= poor) {
            return UrgentStatus.POOR;
        } else {
            return UrgentStatus.WARNING;
        }
    }

    public String calculateStatus(Double value, WebVitalsThreshold metric) {
        UrgentStatus s = calculateUrgentStatus(value, metric);
        return (s == null) ? null : s.name();
    }

    public Map<String, UrgentStatus> webUrgentStatuses(@Nullable WebVitalsEntity web) {
        Map<String, UrgentStatus> map = new LinkedHashMap<>();

        if (web == null) {
            map.put("LCP", UrgentStatus.POOR);
            map.put("CLS", UrgentStatus.POOR);
            map.put("INP", UrgentStatus.POOR);
            map.put("FCP", UrgentStatus.POOR);
            map.put("TTFB", UrgentStatus.POOR);
            return map;
        }

        map.put("LCP",  calculateUrgentStatus(web.getLcp(),  WebVitalsThreshold.LCP));
        map.put("CLS",  calculateUrgentStatus(web.getCls(),  WebVitalsThreshold.CLS));
        map.put("INP",  calculateUrgentStatus(web.getInp(),  WebVitalsThreshold.INP));
        map.put("FCP",  calculateUrgentStatus(web.getFcp(),  WebVitalsThreshold.FCP));
        map.put("TTFB", calculateUrgentStatus(web.getTtfb(), WebVitalsThreshold.TTFB));

        return map;
    }

    /** 웹 지표를 0~50점(가중치 포함)으로 환산 */
    public int toWebHalfScore(WebScores w) {
        double total = 9 * (w.lcp / 100.0) +
                9 * (w.cls / 100.0) +
                8 * (w.inp / 100.0) +
                8 * (w.fcp / 100.0) +
                8 * (w.ttfb / 100.0);
        return (int) Math.round(total); // 0~50
    }

    public record SecurityScores(
            int hsts,
            int frameAncestorsOrXfo,
            int ssl,
            int xcto,
            int referrerPolicy,
            int cookies,
            int csp
    ) {}

    /** 보안 지표별 원시 점수(0~100)를 그대로 DTO로 묶어서 반환 */
    public SecurityScores toSecurityScores(@Nullable SecurityVitalsEntity sec) {
        if (sec == null) {
            // 보안 데이터 자체가 없으면 전부 0점
            return new SecurityScores(0, 0, 0, 0, 0, 0, 0);
        }

        int hstsRaw   = securityRules.scoreHsts(sec);                  // 0 / 50 / 100
        int xfoFaRaw  = securityRules.scoreXfoOrFrameAncestors(sec);   // 0 / 100
        int sslRaw    = securityRules.scoreSsl(sec);                   // 0 / 70 / 100
        int xctoRaw   = securityRules.scoreXContentTypeOptions(sec);   // 0 / 100
        int rpRaw     = securityRules.scoreReferrerPolicy(sec);        // 0 / 50 / 100
        int cookieRaw = securityRules.scoreCookies(sec);               // 0 / 40 / 70 / 100
        int cspRaw    = securityRules.scoreCsp(sec);                   // 0 / 50 / 100

        return new SecurityScores(
                hstsRaw,
                xfoFaRaw,
                sslRaw,
                xctoRaw,
                rpRaw,
                cookieRaw,
                cspRaw
        );
    }

    // ---- Security 긴급도 (기존) ----
    public Map<String, UrgentStatus> securityUrgentStatuses(@Nullable SecurityVitalsEntity sec) {
        Map<String, UrgentStatus> map = new LinkedHashMap<>();

        // 1) 아직 보안 스캔이 안 된 경우: "정보 없음"이지 "최악"은 아니다.
        if (sec == null) {
            map.put("HSTS",                UrgentStatus.WARNING);
            map.put("FRAME-ANCESTORS/XFO", UrgentStatus.WARNING);
            map.put("SSL",                 UrgentStatus.WARNING);
            map.put("XCTO",                UrgentStatus.WARNING);
            map.put("REFERRER-POLICY",     UrgentStatus.WARNING);
            map.put("COOKIES",             UrgentStatus.WARNING);
            map.put("CSP",                 UrgentStatus.WARNING);
            return map;
        }

        SecurityScores s = toSecurityScores(sec);

        // 2) 일반 지표들은 점수 band만 완화해서 그대로 사용
        map.put("HSTS",                bandToUrgentStatus(s.hsts()));
        map.put("FRAME-ANCESTORS/XFO", bandToUrgentStatus(s.frameAncestorsOrXfo()));
        map.put("SSL",                 bandToUrgentStatus(s.ssl()));
        map.put("XCTO",                bandToUrgentStatus(s.xcto()));
        map.put("REFERRER-POLICY",     bandToUrgentStatus(s.referrerPolicy()));

        // 3) COOKIES는 has_cookies 플래그를 반영해서 좀 더 유연하게
        UrgentStatus cookiesStatus = bandToUrgentStatus(s.cookies());

        // 서비스가 아예 쿠키를 사용하지 않는 경우: 공격 면 자체가 거의 없으니 GOOD로 완화
        if (Boolean.FALSE.equals(sec.getHasCookies())) {
            cookiesStatus = UrgentStatus.GOOD;
        }

        map.put("COOKIES", cookiesStatus);

        map.put("CSP",                 bandToUrgentStatus(s.csp()));

        return map;
    }

    // 점수 band 완화: 70 이상 GOOD, 40 이상 WARNING, 그 미만만 POOR
    private UrgentStatus bandToUrgentStatus(int raw) {
        if (raw >= 70) return UrgentStatus.GOOD;     // 70, 100
        if (raw >= 40) return UrgentStatus.WARNING;  // 40
        return UrgentStatus.POOR;                    // 0
    }

    /** 보안 지표를 0~50점(가중치 포함)으로 환산 */
    public int toSecurityHalfScore(@Nullable SecurityVitalsEntity sec) {
        SecurityScores s = toSecurityScores(sec);

        double total = 7 * (s.hsts() / 100.0) +
                7 * (s.frameAncestorsOrXfo() / 100.0) +
                8 * (s.ssl() / 100.0) +
                7 * (s.xcto() / 100.0) +
                7 * (s.referrerPolicy() / 100.0) +
                7 * (s.cookies() / 100.0) +
                7 * (s.csp() / 100.0);

        return (int) Math.round(total); // 0~50
    }

    /** 총점 = 웹(0~42 → 0~50 스케일링) + 보안(0~50) */
    public int total(WebScores webScores, int securityHalf) {
        int webHalf = toWebHalfScore(webScores);
        // 가중치 합 42점 만점을 50점 만점으로 스케일링
        int webHalfScaled = (int) Math.round(webHalf * (50.0 / 42.0));
        return Math.min(100, webHalfScaled + securityHalf);
    }

    /** 최저 3개 지표명 반환 - ScoresEntity에서 저장된 점수 사용 */
    public Map<String,Integer> bottom3(@Nullable ScoresEntity scoresEntity, @Nullable SecurityVitalsEntity sec) {
        Map<String, Integer> map = new LinkedHashMap<>();

        // Web 지표는 ScoresEntity에서 조회
        if (scoresEntity != null) {
            map.put("LCP", scoresEntity.getLcpScore() != null ? scoresEntity.getLcpScore() : 0);
            map.put("CLS", scoresEntity.getClsScore() != null ? scoresEntity.getClsScore() : 0);
            map.put("INP", scoresEntity.getInpScore() != null ? scoresEntity.getInpScore() : 0);
            map.put("FCP", scoresEntity.getFcpScore() != null ? scoresEntity.getFcpScore() : 0);
            map.put("TTFB", scoresEntity.getTtfbScore() != null ? scoresEntity.getTtfbScore() : 0);
        } else {
            map.put("LCP", 0);
            map.put("CLS", 0);
            map.put("INP", 0);
            map.put("FCP", 0);
            map.put("TTFB", 0);
        }

        // Security (7) — sec == null이면 전부 0 처리
        if (sec != null) {
            map.put("HSTS", securityRules.scoreHsts(sec));
            map.put("FRAME-ANCESTORS/XFO", securityRules.scoreXfoOrFrameAncestors(sec));
            map.put("SSL", securityRules.scoreSsl(sec));
            map.put("XCTO", securityRules.scoreXContentTypeOptions(sec));
            map.put("REFERRER-POLICY", securityRules.scoreReferrerPolicy(sec));
            map.put("COOKIES", securityRules.scoreCookies(sec));
            map.put("CSP", securityRules.scoreCsp(sec));
        } else {
            map.put("HSTS", 0);
            map.put("FRAME-ANCESTORS/XFO", 0);
            map.put("SSL", 0);
            map.put("XCTO", 0);
            map.put("REFERRER-POLICY", 0);
            map.put("COOKIES", 0);
            map.put("CSP", 0);
        }

       return map;
    }

    /** -------------------- DTO -------------------- */
    public record WebScores(
            int lcp,
            int cls,
            int inp,
            int fcp,
            int ttfb) {
    }
}