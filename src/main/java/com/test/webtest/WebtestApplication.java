package com.test.webtest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
@Slf4j
public class WebtestApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebtestApplication.class, args);
    }

    @Bean
    ApplicationRunner printDs(DataSource ds) {
        return args -> {
            // try-with-resources 사용으로 Connection 자동 반환
            try (var connection = ds.getConnection()) {
                String url = connection.getMetaData().getURL();
                log.info("[DS] url = {}", url); // System.out.println 대신 로그 사용
            } catch (SQLException e) {
                log.error("[DS] 데이터소스 연결 실패", e);
                throw new RuntimeException("데이터소스 연결 중 오류 발생", e);
            }
        };
    }
}
