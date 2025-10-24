package com.test.webtest.domain.test.repository;

import com.test.webtest.domain.test.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestRepository extends JpaRepository<TestEntity, UUID> {
}
