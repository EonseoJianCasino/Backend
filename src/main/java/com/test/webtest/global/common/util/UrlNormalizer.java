package com.test.webtest.global.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * URL,도메인 정규화 유틸
 */

public final class UrlNormalizer {
    private UrlNormalizer(){}

    // 엔티티 저장용 (domainName)
    public static String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return url; // 완전 비정상 문자열이면 원본 반환
            host = host.toLowerCase(Locale.ROOT);
            if (host.startsWith("www.")) host = host.substring(4);
            return host;
        } catch(URISyntaxException e) {
            return url;
        }
    }

    // 중복요청 키 생성용
    public static String normalizeUrlForKey(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return fallbackRegexNormalize(url);

            host = host.toLowerCase(Locale.ROOT);
            if (host.startsWith("www.")) host = host.substring(4);

            int port = uri.getPort();
            // 기본 포트 제거
            boolean isHttp = "http".equalsIgnoreCase(uri.getScheme());
            boolean isHttps = "https".equalsIgnoreCase(uri.getScheme());
            String portPart = "";
            if (port > 0 && !((isHttp && port == 80) || (isHttps && port == 443))) {
                portPart = ":" + port;
            }

            // 패스만 취함, 쿼리/프래그먼트 제거
            String path = uri.getPath();
            if (path == null) path = "";
            // 트레일링 슬래시 제거(루트 "/"는 제거)
            if (path.endsWith("/") && path.length() > 1) {
                path = path.replaceAll("/+$", "");
            }

            // host + port + path
            if (path.isEmpty() || "/".equals(path)) {
                return host + portPart;
            }
            return host + portPart + path;
        } catch (URISyntaxException e) {
            return fallbackRegexNormalize(url);
        }
    }

    // 파서 실패 시 최소한의 정규화(스킴/WWW/슬래시)만 적용
    private static String fallbackRegexNormalize(String url) {
        String s = url.toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "")
                .replaceAll("/+$", "");
        // 쿼리/프래그먼트 제거
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(0, q);
        int h = s.indexOf('#');
        if (h >= 0) s = s.substring(0, h);
        return s;
    }
}
