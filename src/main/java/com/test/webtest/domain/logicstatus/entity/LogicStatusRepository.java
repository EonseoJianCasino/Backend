//package com.test.webtest.domain.logicstatus.entity;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.UUID;
//
//public interface LogicStatusRepository extends JpaRepository<LogicStatusEntity, UUID> {
//
//    @Modifying(clearAutomatically = true)
//    @Query("""
//        update LogicStatusEntity ls
//           set ls.aiTriggered = true,
//               ls.updatedAt = CURRENT_TIMESTAMP
//         where ls.testId = :testId
//    """)
//    int markAiTriggered(@Param("testId") UUID testId);
//
//}