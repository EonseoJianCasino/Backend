package com.test.webtest.domain.ai.repository;//package com.test.webtest.domain.ai.repository;
//
//import com.test.webtest.domain.ai.entity.AiExpectation;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.UUID;
//
//public interface AiExpectationRepository extends JpaRepository<AiExpectation, UUID> {
//}

import com.test.webtest.domain.ai.entity.AiMetricAdvice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.UUID;
import java.util.List;

@Repository
public interface AiMetricAdviceRepository  extends JpaRepository<AiMetricAdvice, UUID>{

    @EntityGraph(attributePaths = { "improvements", "benefits", "relatedMetrics" })
    List<AiMetricAdvice> findByTestId(UUID testId);

}