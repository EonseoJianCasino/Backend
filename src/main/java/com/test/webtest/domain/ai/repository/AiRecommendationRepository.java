package com.test.webtest.domain.ai.repository;

import com.test.webtest.domain.ai.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// AiRecommendationRepository.java
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {}
