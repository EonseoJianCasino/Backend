package com.test.webtest.domain.webvitals.repository;

//import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.entity.LcpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * WebVitalsEntity 전용 Repository
 * - DB와 연동하여 WebVitals 데이터를 조회, 저장, 삭제 등 CRUD 처리
 * - testId 기준 조회 및 존재 여부 확인 기능 제공
 */


public interface LcpRepository extends JpaRepository<LcpEntity, UUID> {
    Optional<LcpEntity> findByTest_Id(UUID testId); // test 연관 필드 경유
    boolean existsByTest_Id(UUID testId);
}
