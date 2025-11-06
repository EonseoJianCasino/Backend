package com.test.webtest.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WebTest API")
                        .description("웹 성능/보안 진단 API 문서")
                        .version("v1.0.0")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api.example.com").description("Prod")
                ));
    }
}
