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

    /** Status 계산: good 이하면 GOOD, poor 이상이면 URGENT, 나머지는 WARNING */
    public String calculateStatus(Double value, WebVitalsThreshold metric) {
        if (value == null)
            return null;

        double good = metric.getGood();
        double poor = metric.getPoor();

        if (value <= good) {
            return "GOOD";
        } else if (value >= poor) {
            return "URGENT";
        } else {
            return "WARNING";
        }
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

    public int toWebHalfScore(@Nullable WebVitalsEntity web) {
        return toWebHalfScore(toWebScores(web));
    }

    /** 보안 지표를 0~50점(가중치 포함)으로 환산 */
    public int toSecurityHalfScore(@Nullable SecurityVitalsEntity sec) {
        if (sec == null)
            return 0;

        int hstsRaw = securityRules.scoreHsts(sec); // 0/50/100
        int xfoRaw = securityRules.scoreXfoOrFrameAncestors(sec); // 0/100
        int sslRaw = securityRules.scoreSsl(sec); // 0/70/100
        int xctoRaw = securityRules.scoreXContentTypeOptions(sec); // 0/100
        int rpRaw = securityRules.scoreReferrerPolicy(sec); // 0/50/100
        int cookieRaw = securityRules.scoreCookies(sec); // 0/40/70/100
        int cspRaw = securityRules.scoreCsp(sec); // 0/50/100

        double total = 7 * (hstsRaw / 100.0) +
                7 * (xfoRaw / 100.0) +
                8 * (sslRaw / 100.0) +
                7 * (xctoRaw / 100.0) +
                7 * (rpRaw / 100.0) +
                7 * (cookieRaw / 100.0) +
                7 * (cspRaw / 100.0);

        return (int) Math.round(total); // 0~50
    }

    /** 총점 = 웹(평균 0~100 → 0~50 환산) + 보안(0~50) */
    public int total(WebScores webScores, int securityHalf) {
        int webHalf = toWebHalfScore(webScores);
        return Math.min(100, webHalf + securityHalf);
    }

    /** 최저 3개 지표명 반환 - ScoresEntity에서 저장된 점수 사용 */
    public List<String> bottom3(@Nullable ScoresEntity scoresEntity, @Nullable SecurityVitalsEntity sec) {
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

        return map.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue)) // 오름차순(점수 낮은게 먼저)
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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