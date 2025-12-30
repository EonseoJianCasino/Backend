//package com.test.webtest.domain.webvitals.repository;
//
//import com.test.webtest.domain.test.entity.TestEntity;
//import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
///**
// * WebVitalsRepository 통합 테스트
// * @DataJpaTest를 사용하여 임베디드 H2 DB로 테스트
// */
//@DataJpaTest
//@DisplayName("WebVitalsRepository 통합 테스트")
//@org.springframework.test.context.TestPropertySource(properties = {
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.flyway.enabled=false"
//})
//class WebVitalsRepositoryTest {
//
//    @Autowired
//    private WebVitalsRepository webVitalsRepository;
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Test
//    @DisplayName("WebVitals 저장 및 조회 - 정상 케이스")
//    void save_AndFindById_ShouldWork() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2000.0, 0.05, 150.0, 1500.0, 150.0, 600.0);
//
//        // when
//        WebVitalsEntity saved = webVitalsRepository.save(entity);
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        assertThat(saved.getTestId()).isNotNull();
//        assertThat(saved.getCreatedAt()).isNotNull();
//
//        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getTestId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getTestId()).isEqualTo(test.getId());
//        assertThat(found.get().getLcp()).isEqualTo(2000.0);
//        assertThat(found.get().getCls()).isEqualTo(0.05);
//        assertThat(found.get().getInp()).isEqualTo(150.0);
//        assertThat(found.get().getFcp()).isEqualTo(1500.0);
//        assertThat(found.get().getTbt()).isEqualTo(150.0);
//        assertThat(found.get().getTtfb()).isEqualTo(600.0);
//    }
//
//    @Test
//    @DisplayName("testId로 WebVitals 조회 - 성공")
//    void findByTestId_WhenExists_ShouldReturnEntity() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2500.0, 0.15, 250.0, 1800.0, 300.0, 900.0);
//
//        webVitalsRepository.save(entity);
//        entityManager.flush();
//        entityManager.clear();
//
//        // when
//        Optional<WebVitalsEntity> result = webVitalsRepository.findByTestId(test.getId());
//
//        // then
//        assertThat(result).isPresent();
//        assertThat(result.get().getTestId()).isEqualTo(test.getId());
//        assertThat(result.get().getLcp()).isEqualTo(2500.0);
//        assertThat(result.get().getCls()).isEqualTo(0.15);
//        assertThat(result.get().getInp()).isEqualTo(250.0);
//        assertThat(result.get().getFcp()).isEqualTo(1800.0);
//        assertThat(result.get().getTbt()).isEqualTo(300.0);
//        assertThat(result.get().getTtfb()).isEqualTo(900.0);
//    }
//
//    @Test
//    @DisplayName("testId로 WebVitals 조회 - 존재하지 않음")
//    void findByTestId_WhenNotExists_ShouldReturnEmpty() {
//        // given
//        UUID nonExistentTestId = UUID.randomUUID();
//
//        // when
//        Optional<WebVitalsEntity> result = webVitalsRepository.findByTestId(nonExistentTestId);
//
//        // then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("testId로 존재 여부 확인 - 존재함")
//    void existsByTestId_WhenExists_ShouldReturnTrue() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2000.0, 0.05, 150.0, 1500.0, 150.0, 600.0);
//
//        webVitalsRepository.save(entity);
//        entityManager.flush();
//
//        // when
//        boolean exists = webVitalsRepository.existsByTestId(test.getId());
//
//        // then
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("testId로 존재 여부 확인 - 존재하지 않음")
//    void existsByTestId_WhenNotExists_ShouldReturnFalse() {
//        // given
//        UUID nonExistentTestId = UUID.randomUUID();
//
//        // when
//        boolean exists = webVitalsRepository.existsByTestId(nonExistentTestId);
//
//        // then
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    @DisplayName("testId로 WebVitals 삭제 - 성공")
//    void deleteByTestId_ShouldRemoveEntity() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2000.0, 0.05, null, null, null, null);
//
//        webVitalsRepository.save(entity);
//        entityManager.flush();
//
//        // 삭제 전 존재 확인
//        assertThat(webVitalsRepository.existsByTestId(test.getId())).isTrue();
//
//        // when
//        webVitalsRepository.deleteByTestId(test.getId());
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        assertThat(webVitalsRepository.existsByTestId(test.getId())).isFalse();
//    }
//
//    @Test
//    @DisplayName("WebVitals 삭제 (delete) - 정상 케이스")
//    void delete_ShouldRemoveEntity() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 3000.0, 0.25, 500.0, 3000.0, 600.0, 1800.0);
//
//        WebVitalsEntity saved = webVitalsRepository.save(entity);
//        entityManager.flush();
//
//        // when
//        webVitalsRepository.delete(saved);
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        Optional<WebVitalsEntity> result = webVitalsRepository.findById(saved.getTestId());
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("null 값을 포함한 WebVitals 저장 (일부 지표만 있는 경우)")
//    void save_WithNullMetrics_ShouldWork() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2000.0, null, null, null, null, null);
//
//        // when
//        WebVitalsEntity saved = webVitalsRepository.save(entity);
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getTestId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getLcp()).isEqualTo(2000.0);
//        assertThat(found.get().getCls()).isNull();
//        assertThat(found.get().getInp()).isNull();
//        assertThat(found.get().getFcp()).isNull();
//        assertThat(found.get().getTbt()).isNull();
//        assertThat(found.get().getTtfb()).isNull();
//    }
//
//    @Test
//    @DisplayName("모든 Web Vitals가 Good 기준인 경우")
//    void save_AllMetricsGood_ShouldWork() {
//        // given - Google Lighthouse Good 기준
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2000.0, 0.05, 150.0, 1500.0, 150.0, 600.0);
//
//        // when
//        WebVitalsEntity saved = webVitalsRepository.save(entity);
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getTestId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getLcp()).isLessThan(2500.0);
//        assertThat(found.get().getCls()).isLessThan(0.1);
//        assertThat(found.get().getInp()).isLessThan(200.0);
//        assertThat(found.get().getFcp()).isLessThan(1800.0);
//        assertThat(found.get().getTbt()).isLessThan(200.0);
//        assertThat(found.get().getTtfb()).isLessThan(800.0);
//    }
//
//    @Test
//    @DisplayName("모든 Web Vitals가 Poor 기준인 경우")
//    void save_AllMetricsPoor_ShouldWork() {
//        // given - Google Lighthouse Poor 기준
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 5000.0, 0.30, 600.0, 3500.0, 700.0, 2000.0);
//
//        // when
//        WebVitalsEntity saved = webVitalsRepository.save(entity);
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getTestId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getLcp()).isGreaterThan(4000.0);
//        assertThat(found.get().getCls()).isGreaterThan(0.25);
//        assertThat(found.get().getInp()).isGreaterThan(500.0);
//    }
//
//    @Test
//    @DisplayName("여러 개의 WebVitals 조회")
//    void findAll_ShouldReturnAllEntities() {
//        // given
//        TestEntity test1 = TestEntity.create("https://www.example.com");
//        entityManager.persist(test1);
//
//        TestEntity test2 = TestEntity.create("https://www.test.com");
//        entityManager.persist(test2);
//
//        WebVitalsEntity entity1 = WebVitalsEntity.create(test1, 2000.0, 0.05, 150.0, 1500.0, 150.0, 600.0);
//        WebVitalsEntity entity2 = WebVitalsEntity.create(test2, 3000.0, 0.15, 300.0, 2000.0, 400.0, 1000.0);
//
//        webVitalsRepository.save(entity1);
//        webVitalsRepository.save(entity2);
//        entityManager.flush();
//
//        // when
//        long count = webVitalsRepository.count();
//
//        // then
//        assertThat(count).isGreaterThanOrEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("소수점 정밀도 테스트")
//    void save_WithPreciseDecimalValues_ShouldWork() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        WebVitalsEntity entity = WebVitalsEntity.create(test, 2345.6789, 0.123456, 234.56, 1789.12, 234.56, 789.01);
//
//        // when
//        WebVitalsEntity saved = webVitalsRepository.save(entity);
//        entityManager.flush();
//        entityManager.clear();
//
//        // then
//        Optional<WebVitalsEntity> found = webVitalsRepository.findById(saved.getTestId());
//        assertThat(found).isPresent();
//        // Double의 정밀도 범위 내에서 비교
//        assertThat(found.get().getLcp()).isCloseTo(2345.6789, org.assertj.core.data.Offset.offset(0.0001));
//        assertThat(found.get().getCls()).isCloseTo(0.123456, org.assertj.core.data.Offset.offset(0.000001));
//    }
//
//    @Test
//    @DisplayName("음수 값으로 WebVitals 생성 시도 - 실패")
//    void create_WithNegativeValue_ShouldThrowException() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        // when & then
//        assertThatThrownBy(() -> WebVitalsEntity.create(test, -100.0, 0.05, 150.0, 1500.0, 150.0, 600.0))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("LCP")
//                .hasMessageContaining("음수일 수 없습니다");
//    }
//
//    @Test
//    @DisplayName("NaN 값으로 WebVitals 생성 시도 - 실패")
//    void create_WithNaNValue_ShouldThrowException() {
//        // given
//        TestEntity test = TestEntity.create("https://www.example.com");
//        entityManager.persist(test);
//
//        // when & then
//        assertThatThrownBy(() -> WebVitalsEntity.create(test, 2000.0, Double.NaN, 150.0, 1500.0, 150.0, 600.0))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("CLS")
//                .hasMessageContaining("NaN일 수 없습니다");
//    }
//}
//
