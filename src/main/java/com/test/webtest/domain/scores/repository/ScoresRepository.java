package com.test.webtest.domain.scores.repository;

import com.test.webtest.domain.scores.entity.ScoresEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScoresRepository extends JpaRepository<ScoresEntity, UUID> {
    Optional<ScoresEntity> findByTestId(UUID testId);
    boolean existsByTestId(UUID testId);
}
