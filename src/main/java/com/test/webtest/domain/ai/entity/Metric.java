package com.test.webtest.domain.ai.entity;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Metric {
    LCP("LCP"),
    CLS("CLS"),
    INP("INP"),
    FCP("FCP"),
    TTFB("TTFB"),
    HSTS("HSTS"),
    FRAME_ANCESTORS("FRAME-ANCESTORS"),
    SSL("SSL"),
    XCTO("XCTO"),
    REFERRER_POLICY("REFERRER-POLICY"),
    COOKIES("COOKIES"),
    CSP("CSP");

    private static final Map<String, Metric> LOOKUP =
            new ConcurrentHashMap<>();

    static {
        Arrays.stream(values())
                .forEach(metric -> LOOKUP.put(metric.externalName, metric));
    }

    private final String externalName;

    Metric(String externalName) {
        this.externalName = externalName;
    }

    public String externalName() {
        return externalName;
    }

    public static Metric fromExternalName(String name) {
        if (name == null) {
            return null;
        }
        Metric direct = LOOKUP.get(name);
        if (direct != null) {
            return direct;
        }
        return LOOKUP.get(name.toUpperCase(Locale.ROOT));
    }
}
