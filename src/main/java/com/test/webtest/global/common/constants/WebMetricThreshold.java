package com.test.webtest.global.common.constants;

import lombok.Getter;

@Getter
public enum WebMetricThreshold {
    LCP(2500.0, 4000.0),
    CLS(0.1, 0.25),
    INP(200.0, 500.0),
    FCP(1800.0, 3000.0),
    TBT(200.0, 600.0),
    TTFB(800.0, 1800.0);

    private final double good;
    private final double poor;

    WebMetricThreshold(double good, double poor) {
        this.good = good;
        this.poor = poor;
    }
}
