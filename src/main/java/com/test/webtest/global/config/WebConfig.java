package com.test.webtest.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 앞으로 인터셉터, 메시지 컨버터, 포맷터 등 MVC 관련 설정만 추가
}
