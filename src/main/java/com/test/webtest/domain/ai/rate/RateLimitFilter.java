package com.test.webtest.domain.ai.rate;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {
    private final Map<String, Window> buckets = new ConcurrentHashMap<>();
    private static final int LIMIT = 60; // 분당 60회

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) req;
        HttpServletResponse resp = (HttpServletResponse) res;

        String key = http.getRemoteAddr(); // 실제 운영에선 사용자ID/토큰 기반 권장
        Window w = buckets.computeIfAbsent(key, k -> new Window());

        synchronized (w) {
            long now = Instant.now().getEpochSecond();
            if (now - w.start >= 60) { w.start = now; w.count = 0; }
            if (++w.count > LIMIT) {
                resp.setStatus(429);
                resp.getWriter().write("Too Many Requests");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    static class Window { long start = Instant.now().getEpochSecond(); int count = 0; }
}
