package com.test.webtest.global.common.util;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;


import com.test.webtest.global.common.constants.WebMetricThreshold;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    private final SecurityHalfScore securityHalfScore;

    public ScoreCalculator(SecurityHalfScore securityHalfScore) {
        this.securityHalfScore = securityHalfScore;
    }

    /**
     * 웹 성능 지표(LCP, CLS, INP 등)를 100점화하여 반환한다.
     */
    public WebScores toWebScores(@Nullable WebVitalsEntity web) {
        // null 체크 추가 (develop 브랜치)
        if (web == null) {
            return new WebScores(0, 0, 0, 0, 0, 0);
        }

        // 실제 계산 로직 (feature/score 브랜치)
        double lcp = calculateLinearScore(web.getLcp(), WebMetricThreshold.LCP);
        double cls = calculateLinearScore(web.getCls(), WebMetricThreshold.CLS);
        double inp = calculateLinearScore(web.getInp(), WebMetricThreshold.INP);
        double fcp = calculateLinearScore(web.getFcp(), WebMetricThreshold.FCP);
        double tbt = calculateLinearScore(web.getTbt(), WebMetricThreshold.TBT);
        double ttfb = calculateLinearScore(web.getTtfb(), WebMetricThreshold.TTFB);

        return new WebScores(
                (int) Math.round(lcp),
                (int) Math.round(cls),
                (int) Math.round(inp),
                (int) Math.round(fcp),
                (int) Math.round(tbt),
                (int) Math.round(ttfb)
        );
    }

    private double calculateLinearScore(Double value, WebMetricThreshold metric) {
        if (value == null) return 0.0;

        double good = metric.getGood();
        double poor = metric.getPoor();

        if (value <= good) return 100.0;
        if (value > poor) return 0.0;

        double ratio = (value - good) / (poor - good);
        double score = 100.0 - (ratio * 100.0);

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * 보안 지표를 50점화하여 반환한다. (내부에서 0~100 → 0~50 환산)
     */
    public int toSecurityHalfScore(@Nullable SecurityVitalsEntity sec) {
        if (sec == null) return 0;

        int hstsRaw   = securityHalfScore.scoreHsts(sec);                    // 0/50/100
        int xfoRaw    = securityHalfScore.scoreXfoOrFrameAncestors(sec);     // 0/100
        int sslRaw    = securityHalfScore.scoreSsl(sec);                     // 0/70/100
        int xctoRaw   = securityHalfScore.scoreXContentTypeOptions(sec);     // 0/100
        int rpRaw     = securityHalfScore.scoreReferrerPolicy(sec);          // 0/50/100
        int cookieRaw = securityHalfScore.scoreCookies(sec);                 // 0/40/70/100
        int cspRaw    = securityHalfScore.scoreCsp(sec);                     // 0/50/100

        double total =
                7 * (hstsRaw   / 100.0) +
                7 * (xfoRaw    / 100.0) +
                8 * (sslRaw    / 100.0) +
                7 * (xctoRaw   / 100.0) +
                7 * (rpRaw     / 100.0) +
                7 * (cookieRaw / 100.0) +
                7 * (cspRaw    / 100.0);

        return (int) Math.round(total); // 0~50
    }

    /**
     * 총점을 계산한다. (웹 50 + 보안 50 구조)
     */
    public int totalFrom(WebScores webScores, int securityHalf) {
        // 실제 계산 로직 추가
        int webAvg = (webScores.lcp + webScores.cls + webScores.inp +
                webScores.fcp + webScores.tbt + webScores.ttfb) / 6;
        int webHalf = (int) (webAvg * 0.5);
        return Math.min(100, webHalf + securityHalf);
    }

    /**
     * 웹 점수 묶음 구조체 (Java 17 record)
     */
    public record WebScores(
            int lcp,
            int cls,
            int inp,
            int fcp,
            int tbt,
            int ttfb
    ) {}
}