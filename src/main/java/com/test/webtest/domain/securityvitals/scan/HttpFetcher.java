package com.test.webtest.domain.securityvitals.scan;

import com.test.webtest.domain.securityvitals.scan.model.FetchResult;
import org.springframework.stereotype.Component;

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

    public HttpFetcher(Duration timeout) {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(timeout)
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

            // 헤더 맵을 소문자 키로 통일
            Map<String, List<String>> headersLower = resp.headers().map().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toLowerCase(Locale.ROOT),
                            Map.Entry::getValue));

            List<String> setCookies = headersLower.getOrDefault("set-cookie", List.of());

            // 최종 URI (리다이렉트 후)
            URI finalUri = resp.uri();

            return new FetchResult(finalUri, headersLower, setCookies);
        } catch (Exception e) {
            // 실패한 경우 최소한의 객체라도 반환
            return new FetchResult(URI.create(url), Collections.emptyMap(), List.of());
        }
    }
}
