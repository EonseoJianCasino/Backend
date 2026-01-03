package com.test.webtest.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {

    // 웹 프론트(운영/테스트용 도메인 + 로컬 개발용)
    // 프로퍼티 없으면 로컬 개발만 열리게(안전한 기본값)
    @Value("${web.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    // 크롬 확장 Origin (id 확정 전엔 dev에서 * 패턴)
    // prod에서는 반드시 실제 id로 고정 권장
    @Value("${web.cors.allowed-extension-origins:}")
    private List<String> allowedExtensionOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1) 정확 일치 Origin (웹 프론트)
        if (allowedOrigins != null) {
            allowedOrigins.stream()
                .filter(s -> s != null && !s.isBlank())
                .forEach(config::addAllowedOrigin);
        }

        // 2) 패턴 Origin (확장프로그램)
        if (allowedExtensionOrigins != null) {
            allowedExtensionOrigins.stream()
                .filter(s -> s != null && !s.isBlank())
                .forEach(config::addAllowedOriginPattern);
        }

        // 3) 공통 설정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}