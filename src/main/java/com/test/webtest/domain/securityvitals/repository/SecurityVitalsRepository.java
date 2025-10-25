package com.test.webtest.domain.securityvitals.repository;

import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SecurityVitalsRepository extends JpaRepository<SecurityVitalsEntity, UUID> {
    Optional<SecurityVitalsEntity> findByTestId(UUID testID);
    boolean existsByTestId(UUID testId);
}
