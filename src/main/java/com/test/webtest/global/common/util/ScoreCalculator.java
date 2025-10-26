package com.test.webtest.global.common.util;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.global.common.constants.WebMetricThreshold;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    /**
     * 웹 성능 지표(LCP, CLS, INP 등)를 100점화하여 반환한다.
     */
    public WebScores toWebScores(WebVitalsEntity web) {
        //실제 계산 로직 추가
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
        ); // 임시 값
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
    public int toSecurityHalfScore(SecurityVitalsEntity sec) {
        // 실제 계산 로직 추가
        return 40; // 임시 값
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