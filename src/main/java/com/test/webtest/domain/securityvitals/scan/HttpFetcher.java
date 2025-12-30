package com.test.webtest.domain.securityvitals.scan;

import com.test.webtest.domain.securityvitals.scan.model.FetchResult;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Java 11 HttpClient 래퍼
 * - 리다이렉트를 따라가며 GET 수행
 * - 응답 헤더를 소문자 키로 변환
 * - 최종 URI와 Set-Cookie 목록을 포함한 FetchResult를 반환
 */
public class HttpFetcher {

    private final HttpClient client;

    public HttpFetcher() {
        // 여기는 원하는 기본 타임아웃 값으로 세팅하면 됨
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5)) // 예: 5초
                .build();
    }

    public FetchResult fetch(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "WebTestBot/1.0")
                    .build();

            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            Map<String, List<String>> headersLower = resp.headers().map().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toLowerCase(Locale.ROOT),
                            Map.Entry::getValue));

            List<String> setCookies = headersLower.getOrDefault("set-cookie", List.of());
            URI finalUri = resp.uri();

            return new FetchResult(finalUri, headersLower, setCookies);
        } catch (Exception e) {
            return new FetchResult(URI.create(url), Collections.emptyMap(), List.of());
        }
    }
}

