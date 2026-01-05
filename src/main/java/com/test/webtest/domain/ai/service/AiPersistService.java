package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.converter.AiDtoConverter;
import com.test.webtest.domain.ai.dto.AiAnalysisResponse;
import com.test.webtest.domain.ai.dto.AiAnalysisSummaryResponse;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.dto.TopPrioritiesResponse;
import com.test.webtest.domain.ai.repository.AiAnalysisSummaryRepository;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.global.error.exception.AiCallFailedException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.error.exception.AiResultNotFoundException;
import com.test.webtest.global.logging.Monitored;
import com.test.webtest.global.longpoll.LongPollingManager;
import com.test.webtest.global.longpoll.LongPollingTopic;
import com.test.webtest.global.longpoll.TxAfterCommit;
import com.test.webtest.global.longpoll.WaitKey;
import com.test.webtest.global.longpoll.payload.PhaseReadyPayload;
import com.test.webtest.global.monitoring.PipelineMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.test.webtest.domain.ai.schema.AiSchemas.buildPerfAdviceSchema;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiPersistService {

  private final AiGeminiService geminiService;
  private final AiPromptBuilder promptBuilder;
  private final AiResponseParser responseParser;
  private final AiEntitySaver entitySaver;
  private final AiDtoConverter dtoConverter;
  private final AiAnalysisSummaryRepository summaryRepository;
  private final LogicStatusRepository logicStatusRepository;
  private final LongPollingManager longPollingManager;
  private final PipelineMetrics pipelineMetrics;
  private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(2);
  
  @Autowired
  private ApplicationContext applicationContext;

  /**
   * 비동기로 AI 생성 (LogicStatusServiceImpl에서 호출)
   */
  @Async("logicExecutor")
  @Monitored("ai.invokeAsync")
  @Transactional
  public void invokeAsync(UUID testId) {
    invokeAsyncWithRetry(testId, false);
  }

  @Async("logicExecutor")
  @Transactional
  public void invokeAsyncWithRetry(UUID testId, boolean isRetry) {
    try{
      // 재시도인 경우 기존 데이터를 무시하고 강제로 재생성
      generateAndSave(testId, isRetry);
    } catch(AiCallFailedException ex) {
      // 타임아웃인 경우에만 재시도
      if (ex.isTimeout() && !isRetry) {
        log.warn("[AI][TIMEOUT] 60초 타임아웃 발생 testId={}, 60초 후 재시도 예약", testId);
        retryExecutor.schedule(() -> {
          log.info("[AI][RETRY] 타임아웃 재시도 시작 testId={}", testId);
          // Spring 프록시를 통해 호출하여 @Async와 @Transactional이 동작하도록 함
          AiPersistService self = applicationContext.getBean(AiPersistService.class);
          self.invokeAsyncWithRetry(testId, true);
        }, 60, TimeUnit.SECONDS);
      } else {
        // 즉시 실패 또는 재시도도 실패한 경우 에러 반환
        if (ex.isTimeout() && isRetry) {
          log.error("[AI][RETRY][FAIL] 타임아웃 재시도도 실패 testId={}", testId);
        } else {
          log.warn("[AI][ASYNC][FAIL] 즉시 실패 testId={} msg={}", testId, ex.getMessage(), ex);
        }
        longPollingManager.completeError(
                new WaitKey(testId, LongPollingTopic.AI_READY),
                ErrorCode.AI_CALL_FAILED,
                ex.getMessage() != null ? ex.getMessage() : "AI 서비스 호출에 실패했습니다."
        );
      }
    } catch (Exception ex) {
      log.error("[AI][ASYNC][FAIL] 예기치 못한 오류 testId={} isRetry={} ex={}", testId, isRetry, ex.toString());

      longPollingManager.completeError(
              new WaitKey(testId, LongPollingTopic.AI_READY),
              ErrorCode.INTERNAL_ERROR,
              "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
      );
    }
  }

  @Monitored("ai.generateAndSave")
  void generateAndSave(UUID testId) {
    generateAndSave(testId, false);
  }

  @Monitored("ai.generateAndSave")
  void generateAndSave(UUID testId, boolean forceRegenerate) {
    // 재시도가 아닌 경우에만 기존 데이터 체크
    if (!forceRegenerate && summaryRepository.findByTestId(testId).isPresent()) {
      log.info("[AI] 이미 데이터 존재, 생성 스킵: testId={}", testId);
      return;
    }

    String prompt = promptBuilder.buildPrompt(testId);
    AiResponse response = geminiService.generateWithSchema(prompt, buildPerfAdviceSchema());
    var payload = responseParser.parseResponse(response);
    // 1. AI 결과 저장 (entitySaver.saveAll에서 자동으로 기존 데이터 삭제 후 저장)
    entitySaver.saveAll(testId, payload);

    // 2. logic_status.ai_ready TRUE
    var rows = logicStatusRepository.markAiReady(testId);
    if (!rows.isEmpty()) {
      log.info("[AI][SAVE][SUCCESS] ai_ready marked TRUE for testId={}", testId);

      // 3. 커밋 후 AI_READY 롱폴 알림
      TxAfterCommit.run(()-> {
          try {
              longPollingManager.complete(
                      new WaitKey(testId, LongPollingTopic.AI_READY),
                      new PhaseReadyPayload(LongPollingTopic.AI_READY, testId, java.time.Instant.now())
              );
              pipelineMetrics.incAiReadySuccess();
          } catch (Exception e) {
              pipelineMetrics.incAiReadyFailure();
              log.warn("[AI][METRICS_AI_READY][FAIL] AI_READY long-poll complete 실패 testId={}", testId, e);
          }
      });
    } else {

        log.info("[AI][SAVE][FAIL] markAiReady returned empty rows (already ready or not triggered), testId={}", testId);
    }
  }

  @Transactional(readOnly = true)
  @Monitored("ai.getAnalysis")
  public AiAnalysisResponse getAnalysis(UUID testId) {
    // 데이터가 존재하지 않으면 예외 발생
    if (summaryRepository.findByTestId(testId).isEmpty()) {
      throw AiResultNotFoundException.of();
    }
    // DB에서 조회만 수행 (AI 호출은 invokeAsync에서만 수행)
    return dtoConverter.getAnalysis(testId);
  }

  @Transactional(readOnly = true)
  public AiAnalysisSummaryResponse getAnalysisSummary(UUID testId) {
    return dtoConverter.getAnalysisSummary(testId);
  }

  @Transactional(readOnly = true)
  @Monitored("ai.getTopPriorities")
  public TopPrioritiesResponse getTopPriorities(UUID testId) {
    // 데이터가 존재하지 않으면 예외 발생
    if (summaryRepository.findByTestId(testId).isEmpty()) {
      throw AiResultNotFoundException.of();
    }
    // DB에서 조회만 수행 (AI 호출은 invokeAsync에서만 수행)
    return dtoConverter.getTopPriorities(testId);
  }
}
