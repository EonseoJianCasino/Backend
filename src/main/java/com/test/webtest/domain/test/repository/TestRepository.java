package com.test.webtest.domain.test.repository;

import com.test.webtest.domain.test.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, String> {
}
