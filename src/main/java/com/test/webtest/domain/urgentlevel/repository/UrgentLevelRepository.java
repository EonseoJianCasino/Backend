package com.test.webtest.domain.urgentlevel.repository;

import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UrgentLevelRepository extends JpaRepository<UrgentLevelEntity, UUID> {
    Optional<UrgentLevelEntity> findByTestId(UUID testId);

    boolean existsByTestId(UUID testId);
}
