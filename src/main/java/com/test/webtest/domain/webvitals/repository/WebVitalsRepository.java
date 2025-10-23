package com.test.webtest.domain.webvitals.repository;

import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * WebVitalsEntity 전용 Repository
 * - DB와 연동하여 WebVitals 데이터를 조회, 저장, 삭제 등 CRUD 처리
 * - testId 기준 조회 및 존재 여부 확인 기능 제공
 */

@Repository
public interface WebVitalsRepository extends JpaRepository<WebVitalsEntity, UUID> {

    Optional<WebVitalsEntity> findByTestId(UUID testId);

    boolean existsByTestId(UUID testId);
    void deleteByTestId(UUID testId);
}
