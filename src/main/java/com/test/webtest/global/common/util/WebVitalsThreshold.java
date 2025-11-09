package com.test.webtest.global.common.util;

import lombok.Getter;

@Getter
public enum WebVitalsThreshold {
    LCP(2500, 4000),     // milliseconds
    CLS(0.10, 0.25),   // score
    INP(200.0, 500.0), // milliseconds
    FCP(1800, 3000),     // milliseconds
    TTFB(800, 1800);    // milliseconds

    private final double good;
    private final double poor;

    WebVitalsThreshold(double good, double poor) {
        this.good = good;
        this.poor = poor;
    }
}
