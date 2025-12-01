package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiAnalysisResponse;
import com.test.webtest.domain.ai.dto.AiAnalysisSummaryResponse;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.dto.TopPrioritiesResponse;
import com.test.webtest.domain.ai.repository.AiAnalysisSummaryRepository;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.global.logging.Monitored;
import com.test.webtest.global.longpoll.LongPollingManager;
import com.test.webtest.global.longpoll.LongPollingTopic;
import com.test.webtest.global.longpoll.TxAfterCommit;
import com.test.webtest.global.longpoll.WaitKey;
import com.test.webtest.global.longpoll.payload.PhaseReadyPayload;
import com.test.webtest.global.monitoring.PipelineMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.test.webtest.domain.ai.schema.AiSchemas.buildPerfAdviceSchema;

import java.util.UUID;

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

  /**
   * 비동기로 AI 생성 (LogicStatusServiceImpl에서 호출)
   */
  @Async("logicExecutor")
  @Monitored("ai.invokeAsync")
  public void invokeAsync(UUID testId) {
    generateAndSave(testId);
  }

  @Transactional
  @Monitored("ai.generateAndSave")
  public void generateAndSave(UUID testId) {
    // 이미 데이터가 있으면 스킵
    if (summaryRepository.findByTestId(testId).isPresent()) {
      log.info("[AI] 이미 데이터 존재, 생성 스킵: testId={}", testId);
      return;
    }

    String prompt = promptBuilder.buildPrompt(testId);
    AiResponse response = geminiService.generateWithSchema(prompt, buildPerfAdviceSchema());
    var payload = responseParser.parseResponse(response);
    // 1. AI 결과 저장
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

  @Transactional
  @Monitored("ai.getAnalysis")
  public AiAnalysisResponse getAnalysis(UUID testId) {
    // 데이터가 없으면 AI 호출 → 저장
    if (summaryRepository.findByTestId(testId).isEmpty()) {
      log.info("[AI][GET_ANALYSIS][FAIL] 데이터 없음, AI 생성 시작: testId={}", testId);
      generateAndSave(testId);
    }
    return dtoConverter.getAnalysis(testId);
  }

  @Transactional(readOnly = true)
  public AiAnalysisSummaryResponse getAnalysisSummary(UUID testId) {
    return dtoConverter.getAnalysisSummary(testId);
  }

  @Transactional
  @Monitored("ai.getTopPriorities")
  public TopPrioritiesResponse getTopPriorities(UUID testId) {
    // 데이터가 없으면 AI 호출 → 저장
    if (summaryRepository.findByTestId(testId).isEmpty()) {
      log.info("[AI][GET_TOP_PRIORITIES][FAIL] 데이터 없음, AI 생성 시작: testId={}", testId);
      generateAndSave(testId);
    }
    return dtoConverter.getTopPriorities(testId);
  }
}
