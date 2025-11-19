// src/main/java/com/test/webtest/global/config/SecurityConfig.java
package com.test.webtest.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API는 CSRF 불필요
                .cors(Customizer.withDefaults()) // CORS는 Security 필터에서 켬
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/**").permitAll() // 테스트 단계: 전체 오픈
                        .anyRequest().permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
