package com.test.webtest.global.common.constants;

import lombok.Getter;

@Getter
public enum WebMetricThreshold {
    LCP(2.5, 4.0),     // seconds
    CLS(0.10, 0.25),   // unitless
    INP(200.0, 500.0), // milliseconds
    FCP(1.8, 3.0),     // seconds
    TBT(200.0, 600.0), // milliseconds
    TTFB(0.8, 1.8);    // seconds

    private final double good;
    private final double poor;

    WebMetricThreshold(double good, double poor) {
        this.good = good;
        this.poor = poor;
    }
}
