package com.test.webtest.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173") // 크롬 확장/프론트 도메인 추가 예정
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .maxAge(3600);
    }
}
