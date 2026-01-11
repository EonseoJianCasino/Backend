package com.test.webtest.domain.logicstatus.repository;

import com.test.webtest.domain.logicstatus.entity.LogicStatusEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface LogicStatusRepository extends JpaRepository<LogicStatusEntity, UUID> {

    // 1) 웹 플래그 ON
    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET web_received = TRUE, updated_at = now()
         WHERE test_id = :testId
        RETURNING web_received, web_sub_received, sec_received, scores_ready, ai_triggered
        """, nativeQuery = true)
    List<Object[]> markWebReceived(@Param("testId") UUID testId);

    // 1-1) 웹 서브 플래그 ON
    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET web_sub_received = TRUE, updated_at = now()
         WHERE test_id = :testId
        RETURNING web_received, web_sub_received, sec_received, scores_ready, ai_triggered
        """, nativeQuery = true)
    List<Object[]> markWebSubReceived(@Param("testId") UUID testId);

    // 2) 보안 플래그 ON
    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET sec_received = TRUE, updated_at = now()
         WHERE test_id = :testId
        RETURNING web_received, web_sub_received, sec_received, scores_ready, ai_triggered
        """, nativeQuery = true)
    List<Object[]> markSecReceived(@Param("testId") UUID testId);

    // 3) 점수 준비 마킹 (경합 방지) - Web Vitals만 있어도 실행
    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET scores_ready = TRUE, updated_at = now()
         WHERE test_id = :testId
           AND web_received = TRUE
           AND scores_ready = FALSE
        RETURNING scores_ready
        """, nativeQuery = true)
    List<Object[]> markScoresReady(@Param("testId") UUID testId);

    // 4) AI 트리거 마킹 (경합 방지)
    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET ai_triggered = TRUE,
               ai_running = TRUE,
               updated_at = now()
         WHERE test_id = :testId
           AND web_received = TRUE
           AND web_sub_received = TRUE
           AND sec_received = TRUE
           AND scores_ready = TRUE
           AND ai_triggered = FALSE
        RETURNING ai_triggered
        """, nativeQuery = true)
    List<Object[]> markAiTriggered(@Param("testId") UUID testId);

    /**
     * AI 재시도(또는 수동 재생성) 요청 시: 이미 실행 중이면 새 작업을 만들지 않기 위해
     * testId 기준으로 ai_running을 원자적으로 TRUE로 전환한다.
     */
    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET ai_running = TRUE,
               ai_ready = FALSE,
               updated_at = now()
         WHERE test_id = :testId
           AND ai_running = FALSE
        RETURNING ai_running
        """, nativeQuery = true)
    List<Object[]> markAiRunning(@Param("testId") UUID testId);

    @Modifying
    @Query(value = """
        UPDATE logic_status
           SET ai_running = FALSE,
               updated_at = now()
         WHERE test_id = :testId
        RETURNING ai_running
        """, nativeQuery = true)
    List<Object[]> clearAiRunning(@Param("testId") UUID testId);


    // 5) AI 완료 마킹 (결과 준비 완료)
    @Modifying
    @Query(value = """
        UPDATE logic_status
            SET ai_ready = TRUE,
                ai_running = FALSE,
                updated_at = now()
        WHERE test_id = :testId
            AND ai_triggered = TRUE
            AND ai_ready = FALSE
        RETURNING ai_ready
        """, nativeQuery = true)
    List<Object[]> markAiReady(@Param("testId") UUID testId);
}
