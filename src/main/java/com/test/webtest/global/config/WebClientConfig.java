package com.test.webtest.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${app.gemini.base-url}")
    private String baseUrl;

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Bean
    public WebClient geminiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {
                    URI uri = request.url();
                    String query = uri.getQuery();
                    String newQuery = query == null
                            ? "key=" + apiKey
                            : query + "&key=" + apiKey;
                    URI newUri = URI.create(uri.getScheme() + "://" + uri.getAuthority()
                            + uri.getPath() + "?" + newQuery);
                    ClientRequest newRequest = ClientRequest.from(request)
                            .url(newUri)
                            .build();
                    return next.exchange(newRequest);
                })
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
