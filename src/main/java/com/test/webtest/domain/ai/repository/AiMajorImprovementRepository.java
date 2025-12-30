package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiMajorImprovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiMajorImprovementRepository extends JpaRepository<AiMajorImprovement, Long> {
    List<AiMajorImprovement> findByTestId(UUID testId);
    List<AiMajorImprovement> findByTestIdOrderByRankAsc(UUID testId);

    @Modifying
    @Query("DELETE FROM AiMajorImprovement m WHERE m.testId = :testId")
    void deleteByTestId(UUID testId);
}
