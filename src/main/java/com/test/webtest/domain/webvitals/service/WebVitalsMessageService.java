package com.test.webtest.domain.webvitals.service;

import com.test.webtest.domain.webvitals.dto.WebVitalsView;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.global.common.util.WebVitalsThreshold;
import org.springframework.stereotype.Component;

@Component
public class WebVitalsMessageService {

    public WebVitalsView toView(WebVitalsEntity e) {
        return new WebVitalsView(
                e.getLcp(),  msg(e.getLcp(),  WebVitalsThreshold.LCP),
                e.getCls(),  msg(e.getCls(),  WebVitalsThreshold.CLS),
                e.getInp(),  msg(e.getInp(),  WebVitalsThreshold.INP),
                e.getFcp(),  msg(e.getFcp(),  WebVitalsThreshold.FCP),
                e.getTtfb(), msg(e.getTtfb(), WebVitalsThreshold.TTFB),
                e.getCreatedAt()
        );
    }

	public String getStatus(Double value, WebVitalsThreshold th) {
		if (value == null) return null;

		if (value <= th.getGood()) {
			return "양호";
		}
		else if (value >= th.getPoor()) {
			return "긴급";
		}
		return "주의";
	}

    private String msg(Double value, WebVitalsThreshold th) {
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