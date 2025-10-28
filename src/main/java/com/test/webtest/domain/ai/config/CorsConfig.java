package com.test.webtest.domain.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.allowed-origins:*}")
    private String allowed;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowed.split(","))
                .allowedMethods("GET","POST","OPTIONS")
                .allowCredentials(true);
    }

}
