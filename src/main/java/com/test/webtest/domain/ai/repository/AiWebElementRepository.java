package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiWebElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiWebElementRepository extends JpaRepository<AiWebElement, Long> {
    List<AiWebElement> findByTestId(UUID testId);
    List<AiWebElement> findByTestIdOrderByRankAsc(UUID testId);

    @Modifying
    @Query("DELETE FROM AiWebElement e WHERE e.testId = :testId")
    void deleteByTestId(UUID testId);
}
