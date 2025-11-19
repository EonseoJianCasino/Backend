package com.test.webtest.domain.securityvitals.scan;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity.SaveCommand;
import com.test.webtest.domain.securityvitals.scan.model.FetchResult;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 보안 스캐닝 퍼사드
 * - HttpFetcher로 헤더/쿠키 수집
 * - CSP/HSTS 등 전용 파서로 분석
 * - JdkTlsInspector로 TLS 인증서 점검
 * - SecurityVitalsEntity.SaveCommand로 변환해 서비스 계층에 전달
 */

@Component
public class SecurityScanner {

    private final HttpFetcher httpFetcher;
    private final SslInspector sslInspector;

    public SecurityScanner() {
        this.httpFetcher = new HttpFetcher();
        this.sslInspector = new JdkTlsInspector(); // 기본
    }

    public SecurityScanner(HttpFetcher httpFetcher, SslInspector sslInspector) {
        this.httpFetcher = httpFetcher;
        this.sslInspector = sslInspector;
    }

    public SaveCommand scan(String url) {
        // 1) HTTP 요청(리다이렉트 추적)
        FetchResult fr = httpFetcher.fetch(url);

        Map<String, List<String>> h = fr.headers(); // 소문자 키
        List<String> setCookies = fr.setCookies();

        // 2) 개별 지표 파싱
        // X-Content-Type-Options
        String xcto = HeaderUtil.first(h, "x-content-type-options");

        // Referrer-Policy
        String rpRaw = HeaderUtil.first(h, "referrer-policy");
        String rp = ReferrerPolicyNormalizer.normalize(rpRaw);

        // HSTS
        String hstsRaw = HeaderUtil.first(h, "strict-transport-security");
        HstsParser.Hsts hsts = HstsParser.parse(hstsRaw);

        // CSP
        String cspRaw = HeaderUtil.first(h, "content-security-policy");
        CspParser.Csp csp = CspParser.parse(cspRaw);

        // X-Frame-Options
        String xfo = HeaderUtil.first(h, "x-frame-options");

        // Cookies
        boolean hasCookies = !setCookies.isEmpty();
        CookieParser.Summary cookieSum = CookieParser.summarize(setCookies);

        // 3) SSL/TLS 인증서 검사 (최종 도메인 기준)
        URI finalUri = fr.finalUri();
        SslInspector.Result ssl = sslInspector.inspect(finalUri.getHost(), finalUri.getPort() > 0 ? finalUri.getPort() : 443);

        // 4) SaveCommand 생성
        return SaveCommand.builder()
                .hasCsp(csp.present())
                .hasHsts(hsts.present())
                .xFrameOptions(xfo)
                .xContentTypeOptions(xcto)
                .referrerPolicy(rp)
                .hstsMaxAge(hsts.maxAge())
                .hstsIncludeSubdomains(hsts.includeSubDomains())
                .hstsPreload(hsts.preload())
                .cspHasUnsafeInline(csp.unsafeInline())
                .cspHasUnsafeEval(csp.unsafeEval())
                .cspFrameAncestors(csp.frameAncestors())
                .hasCookies(hasCookies)
                .cookieSecureAll(cookieSum.allSecure())
                .cookieHttpOnlyAll(cookieSum.allHttpOnly())
                .cookieSameSitePolicy(cookieSum.sameSitePolicy())
                .sslValid(ssl.valid())
                .sslChainValid(ssl.chainValid())
                .sslDaysRemaining(ssl.daysRemaining())
                .sslIssuer(ssl.issuer())
                .sslSubject(ssl.subject())
                .cspRaw(cspRaw)
                .hstsRaw(hstsRaw)
                .build();
    }
}
