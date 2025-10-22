package com.test.webtest.test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("URL 기반 10초 중복 차단 룰 TDD")
class RateLimitServiceTest {

    RateLimitService service;
    FixedTimeProvider time;

    @BeforeEach
    void setUp() {
        // given
        this.time = new FixedTimeProvider(Instant.parse("2025-10-21T00:00:00Z"));

        this.service = new InMemoryRateLimitService(
                time,
                com.test.webtest.global.common.util.UrlNormalizer::normalizeUrlForKey,
                Duration.ofSeconds(10)
        );
    }

    @Test
    @DisplayName("첫 요청은 허용되어야 한다")
    void first_request_should_be_allowed() {
        // given
        String url = "https://example.com/";

        // when
        var res = service.checkAndMark(url);

        // then
        assertThat(res.allowed()).isTrue();
        assertThat(res.remaining()).isZero();
    }

    @Test
    @DisplayName("10초 내 재요청은 차단되고 남은 대기시간을 알려준다")
    void request_within_10s_should_be_blocked() {
        // given
        service.checkAndMark("https://example.com/");

        // when
        time.advance(Duration.ofSeconds(9));
        var res = service.checkAndMark("https://example.com");

        // then
        assertThat(res.allowed()).isFalse();
        assertThat(res.remaining().getSeconds()).isBetween(0L, 1L);
    }

    @Test
    @DisplayName("10초 경과 후 재요청은 허용되어야 한다")
    void request_after_window_should_be_allowed() {
        // given
        service.checkAndMark("https://example.com");

        // when
        time.advance(Duration.ofSeconds(11));
        var res = service.checkAndMark("https://example.com/");

        // then
        assertThat(res.allowed()).isTrue();
        assertThat(res.remaining()).isZero();
    }

    @Test
    @DisplayName("동시에 같은 URL로 여러 요청이 와도 오직 1개만 허용되어야 한다")
    void concurrent_requests_allow_only_one() throws Exception {
        // given
        var pool = Executors.newFixedThreadPool(8);
        var results = Collections.synchronizedList(new ArrayList<Boolean>());
        var latch = new CountDownLatch(1);

        // when
        for (int i = 0; i < 8; i++) {
            pool.submit(() -> {
                latch.await();
                var r = service.checkAndMark("https://example.com").allowed();
                results.add(r);
                return null;
            });
        }
        latch.countDown();
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        // then
        long allowedCount = results.stream().filter(b -> b).count();
        assertThat(allowedCount).isEqualTo(1);
    }

    // ===== 테스트 편의 클래스 =====
    static class FixedTimeProvider implements TimeProvider {
        private Instant now;
        FixedTimeProvider(Instant start) { this.now = start; }
        @Override public Instant now() { return now; }
        void advance(Duration d) { this.now = this.now.plus(d); }
    }
    interface TimeProvider { Instant now(); }

    interface RateLimitService {
        Result checkAndMark(String url);
        record Result(boolean allowed, Duration remaining) {}
    }

    static class InMemoryRateLimitService implements RateLimitService {
        private final TimeProvider time;
        private final Function<String,String> normalizer;
        private final Duration window;
        private final ConcurrentMap<String, Instant> lastSeen = new ConcurrentHashMap<>();

        InMemoryRateLimitService(TimeProvider time, Function<String,String> normalizer, Duration window) {
            this.time = time; this.normalizer = normalizer; this.window = window;
        }

        @Override
        public Result checkAndMark(String url) {
            String key = normalizer.apply(url);
            Instant now = time.now();

            var allowed = new java.util.concurrent.atomic.AtomicBoolean(false);

            Instant after = lastSeen.compute(key, (k, prev) -> {
                if (prev == null || Duration.between(prev, now).compareTo(window) > 0) {
                    allowed.set(true);     // 이번 스레드가 허용 권을 획득
                    return now;            // 갱신
                } else {
                    return prev;           // 유지
                }
            });

            if (allowed.get()) {
                return new Result(true, Duration.ZERO);
            } else {
                // 남은 대기시간 계산 (음수 방지)
                Duration remaining = window.minus(Duration.between(after, now));
                if (remaining.isNegative()) remaining = Duration.ZERO;
                return new Result(false, remaining);
            }
        }
    }

}
