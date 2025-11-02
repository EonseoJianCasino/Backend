package com.test.webtest.domain.priorities.repository;

import com.test.webtest.domain.priorities.entity.PrioritiesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// JpaRepository를 상속받아 기본적인 CRUD 기능(save, findById, findAll 등)을 자동으로 제공받음
// <엔티티 타입, ID 타입>
@Repository
public interface PrioritiesRepository extends JpaRepository<PrioritiesEntity, UUID> {

    List<PrioritiesEntity> findAllByTestIdOrderByRankAsc(UUID testId);

    void deleteAllByTestId(UUID testId);
}