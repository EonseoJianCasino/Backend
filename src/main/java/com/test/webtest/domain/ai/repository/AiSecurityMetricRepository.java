package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiSecurityMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiSecurityMetricRepository extends JpaRepository<AiSecurityMetric, Long> {
    List<AiSecurityMetric> findByTestId(UUID testId);

    @Modifying
    @Query("DELETE FROM AiSecurityMetric m WHERE m.testId = :testId")
    void deleteByTestId(UUID testId);
}
