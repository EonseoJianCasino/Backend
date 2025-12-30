package com.test.webtest.domain.securityvitals.repository;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SecurityVitalsRepository extends JpaRepository<SecurityVitalsEntity, UUID> {
    Optional<SecurityVitalsEntity> findByTest_Id(UUID testID);
    boolean existsByTest_Id(UUID testId);

    @org.springframework.data.jpa.repository.Query(
            value = "select * from security_vitals where test_id = :testId limit 1",
            nativeQuery = true
    )
    Optional<SecurityVitalsEntity> findByTestIdNative(@org.springframework.data.repository.query.Param("testId") UUID testId);
}
