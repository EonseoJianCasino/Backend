package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.webvitals.dto.WebVitalsView;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.global.common.constants.WebMetricThreshold;
import org.springframework.stereotype.Component;

@Component
public class WebVitalsMessageService {

    public WebVitalsView toView(WebVitalsEntity e) {
        return new WebVitalsView(
                e.getLcp(),  msg(e.getLcp(),  WebMetricThreshold.LCP),
                e.getCls(),  msg(e.getCls(),  WebMetricThreshold.CLS),
                e.getInp(),  msg(e.getInp(),  WebMetricThreshold.INP),
                e.getFcp(),  msg(e.getFcp(),  WebMetricThreshold.FCP),
                e.getTbt(),  msg(e.getTbt(),  WebMetricThreshold.TBT),
                e.getTtfb(), msg(e.getTtfb(), WebMetricThreshold.TTFB),
                e.getCreatedAt()
        );
    }

    private String msg(Double value, WebMetricThreshold th) {
        if (value == null) return null;
        if (th.getGood() <= 0 || th.getPoor() <= 0) return null;

        double pct;
        if (value <= th.getGood()) {
            pct = 100.0;
        } else if (value >= th.getPoor()) {
            pct = 0.0;
        } else {
            pct = ((value - th.getGood()) / (th.getPoor() - th.getGood())) * 100.0;
        }

        long pctRounded = Math.round(pct);
        return "좋은 지표의 " + pctRounded + "% 수준입니다";
    }
}