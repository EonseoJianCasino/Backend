package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiAnalysisSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiAnalysisSummaryRepository extends JpaRepository<AiAnalysisSummary, UUID> {
    Optional<AiAnalysisSummary> findByTestId(UUID testId);
    void deleteByTestId(UUID testId);
}

