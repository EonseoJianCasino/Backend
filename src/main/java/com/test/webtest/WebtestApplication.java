package com.test.webtest;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class WebtestApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebtestApplication.class, args);
	}
    @Bean
    ApplicationRunner printDs(DataSource ds) {
        return args -> System.out.println("[DS] url = " + ds.getConnection().getMetaData().getURL());
    }
}
