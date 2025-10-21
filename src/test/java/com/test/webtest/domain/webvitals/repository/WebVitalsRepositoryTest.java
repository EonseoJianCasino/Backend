package com.test.webtest.domain.webvitals.repository;

import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebVitalsRepository 통합 테스트
 * @DataJpaTest를 사용하여 실제 DB 작업 테스트
 */
@DataJpaTest
@DisplayName("WebVitalsRepository 통합 테스트")
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class WebVitalsRepositoryTest {

    @Autowired
    private WebVitalsRepository webVitalsRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("WebVitals 저장 및 조회 - 정상 케이스")
    void save_AndFindById_ShouldWork() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2000.0)
                .cls(0.05)
                .inp(150.0)
                .fcp(1500.0)
                .tbt(150.0)
                .ttfb(600.0)
                .build();

        // when
        WebVitalsEntity saved = webVitalsRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTestId()).isEqualTo(testId);
        assertThat(found.get().getLcp()).isEqualTo(2000.0);
    }

    @Test
    @DisplayName("testId로 WebVitals 조회 - 성공")
    void findByTestId_WhenExists_ShouldReturnEntity() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2500.0)
                .cls(0.15)
                .inp(250.0)
                .fcp(1800.0)
                .tbt(300.0)
                .ttfb(900.0)
                .build();

        webVitalsRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<WebVitalsEntity> result = webVitalsRepository.findByTestId(testId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTestId()).isEqualTo(testId);
        assertThat(result.get().getLcp()).isEqualTo(2500.0);
        assertThat(result.get().getCls()).isEqualTo(0.15);
        assertThat(result.get().getInp()).isEqualTo(250.0);
        assertThat(result.get().getFcp()).isEqualTo(1800.0);
        assertThat(result.get().getTbt()).isEqualTo(300.0);
        assertThat(result.get().getTtfb()).isEqualTo(900.0);
    }

    @Test
    @DisplayName("testId로 WebVitals 조회 - 존재하지 않음")
    void findByTestId_WhenNotExists_ShouldReturnEmpty() {
        // given
        UUID nonExistentTestId = UUID.randomUUID();

        // when
        Optional<WebVitalsEntity> result = webVitalsRepository.findByTestId(nonExistentTestId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("testId로 존재 여부 확인 - 존재함")
    void existsByTestId_WhenExists_ShouldReturnTrue() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2000.0)
                .cls(0.05)
                .inp(150.0)
                .fcp(1500.0)
                .tbt(150.0)
                .ttfb(600.0)
                .build();

        webVitalsRepository.save(entity);
        entityManager.flush();

        // when
        boolean exists = webVitalsRepository.existsByTestId(testId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("testId로 존재 여부 확인 - 존재하지 않음")
    void existsByTestId_WhenNotExists_ShouldReturnFalse() {
        // given
        UUID nonExistentTestId = UUID.randomUUID();

        // when
        boolean exists = webVitalsRepository.existsByTestId(nonExistentTestId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("testId로 WebVitals 삭제 - 성공")
    void deleteByTestId_ShouldRemoveEntity() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2000.0)
                .cls(0.05)
                .build();

        webVitalsRepository.save(entity);
        entityManager.flush();

        // 삭제 전 존재 확인
        assertThat(webVitalsRepository.existsByTestId(testId)).isTrue();

        // when
        webVitalsRepository.deleteByTestId(testId);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(webVitalsRepository.existsByTestId(testId)).isFalse();
    }

    @Test
    @DisplayName("WebVitals 삭제 (delete) - 정상 케이스")
    void delete_ShouldRemoveEntity() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(3000.0)
                .cls(0.25)
                .inp(500.0)
                .fcp(3000.0)
                .tbt(600.0)
                .ttfb(1800.0)
                .build();

        WebVitalsEntity saved = webVitalsRepository.save(entity);
        entityManager.flush();

        // when
        webVitalsRepository.delete(saved);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<WebVitalsEntity> result = webVitalsRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 값을 포함한 WebVitals 저장 (일부 지표만 있는 경우)")
    void save_WithNullMetrics_ShouldWork() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2000.0)    // LCP만 있음
                .cls(null)      // null
                .inp(null)      // null
                .fcp(null)      // null
                .tbt(null)      // null
                .ttfb(null)     // null
                .build();

        // when
        WebVitalsEntity saved = webVitalsRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLcp()).isEqualTo(2000.0);
        assertThat(found.get().getCls()).isNull();
        assertThat(found.get().getInp()).isNull();
        assertThat(found.get().getFcp()).isNull();
        assertThat(found.get().getTbt()).isNull();
        assertThat(found.get().getTtfb()).isNull();
    }

    @Test
    @DisplayName("모든 Web Vitals가 Good 기준인 경우")
    void save_AllMetricsGood_ShouldWork() {
        // given - Google Lighthouse Good 기준
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2000.0)    // Good: < 2500ms
                .cls(0.05)      // Good: < 0.1
                .inp(150.0)     // Good: < 200ms
                .fcp(1500.0)    // Good: < 1800ms
                .tbt(150.0)     // Good: < 200ms
                .ttfb(600.0)    // Good: < 800ms
                .build();

        // when
        WebVitalsEntity saved = webVitalsRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLcp()).isLessThan(2500.0);
        assertThat(found.get().getCls()).isLessThan(0.1);
        assertThat(found.get().getInp()).isLessThan(200.0);
        assertThat(found.get().getFcp()).isLessThan(1800.0);
        assertThat(found.get().getTbt()).isLessThan(200.0);
        assertThat(found.get().getTtfb()).isLessThan(800.0);
    }

    @Test
    @DisplayName("모든 Web Vitals가 Poor 기준인 경우")
    void save_AllMetricsPoor_ShouldWork() {
        // given - Google Lighthouse Poor 기준
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(5000.0)    // Poor: > 4000ms
                .cls(0.30)      // Poor: > 0.25
                .inp(600.0)     // Poor: > 500ms
                .fcp(3500.0)    // Poor: > 3000ms
                .tbt(700.0)     // Poor: > 600ms
                .ttfb(2000.0)   // Poor: > 1800ms
                .build();

        // when
        WebVitalsEntity saved = webVitalsRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLcp()).isGreaterThan(4000.0);
        assertThat(found.get().getCls()).isGreaterThan(0.25);
        assertThat(found.get().getInp()).isGreaterThan(500.0);
    }

    @Test
    @DisplayName("여러 개의 WebVitals 조회")
    void findAll_ShouldReturnAllEntities() {
        // given
        WebVitalsEntity entity1 = WebVitalsEntity.builder()
                .testId(UUID.randomUUID())
                .lcp(2000.0)
                .cls(0.05)
                .inp(150.0)
                .fcp(1500.0)
                .tbt(150.0)
                .ttfb(600.0)
                .build();

        WebVitalsEntity entity2 = WebVitalsEntity.builder()
                .testId(UUID.randomUUID())
                .lcp(3000.0)
                .cls(0.15)
                .inp(300.0)
                .fcp(2000.0)
                .tbt(400.0)
                .ttfb(1000.0)
                .build();

        webVitalsRepository.save(entity1);
        webVitalsRepository.save(entity2);
        entityManager.flush();

        // when
        long count = webVitalsRepository.count();

        // then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("소수점 정밀도 테스트")
    void save_WithPreciseDecimalValues_ShouldWork() {
        // given
        UUID testId = UUID.randomUUID();
        WebVitalsEntity entity = WebVitalsEntity.builder()
                .testId(testId)
                .lcp(2345.6789)    // 소수점 4자리
                .cls(0.123456)     // 소수점 6자리
                .inp(234.56)       // 소수점 2자리
                .fcp(1789.12)
                .tbt(234.56)
                .ttfb(789.01)
                .build();

        // when
        WebVitalsEntity saved = webVitalsRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getId());
        assertThat(found).isPresent();
        // Double의 정밀도 범위 내에서 비교
        assertThat(found.get().getLcp()).isCloseTo(2345.6789, org.assertj.core.data.Offset.offset(0.0001));
        assertThat(found.get().getCls()).isCloseTo(0.123456, org.assertj.core.data.Offset.offset(0.000001));
    }
}

