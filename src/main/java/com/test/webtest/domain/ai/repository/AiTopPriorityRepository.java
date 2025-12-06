package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiTopPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AiTopPriorityRepository extends JpaRepository<AiTopPriority, Long> {

    List<AiTopPriority> findByTestIdOrderByRankAsc(UUID testId);

    @Modifying
    @Query("DELETE FROM AiTopPriority p WHERE p.testId = :testId")
    void deleteByTestId(UUID testId);
}

