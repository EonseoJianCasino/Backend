package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiExpectation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AiExpectationRepository extends JpaRepository<AiExpectation, UUID> {
}