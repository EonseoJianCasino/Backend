package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiAnalysisResponse;
import com.test.webtest.domain.ai.dto.AiAnalysisSummaryResponse;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.dto.TopPrioritiesResponse;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.global.longpoll.LongPollingManager;
import com.test.webtest.global.longpoll.LongPollingTopic;
import com.test.webtest.global.longpoll.TxAfterCommit;
import com.test.webtest.global.longpoll.WaitKey;
import com.test.webtest.global.longpoll.payload.PhaseReadyPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.test.webtest.domain.ai.schema.AiSchemas.buildPerfAdviceSchema;

import java.util.UUID;

@Service
@Slf4j
public class AiPersistService {

  private final AiRecommendationService geminiService; // <- 여기서 호출
  private final AiPromptBuilder promptBuilder;
  private final AiResponseParser responseParser;
  private final AiEntitySaver entitySaver;
  private final AiDtoConverter dtoConverter;
  private final LogicStatusRepository logicStatusRepository;
  private final LongPollingManager longPollingManager;

  public AiPersistService(
          @Lazy AiRecommendationService geminiService,
          AiPromptBuilder promptBuilder,
          AiResponseParser responseParser,
          AiEntitySaver entitySaver,
          AiDtoConverter dtoConverter,
          LogicStatusRepository logicStatusRepository,
          LongPollingManager longPollingManager
  ) {
    this.geminiService = geminiService;
    this.promptBuilder = promptBuilder;
    this.responseParser = responseParser;
    this.entitySaver = entitySaver;
    this.dtoConverter = dtoConverter;
    this.logicStatusRepository = logicStatusRepository;
    this.longPollingManager = longPollingManager;
  }

  @Transactional
  public void generateAndSave(UUID testId) {
    String prompt = promptBuilder.buildPrompt(testId);
    AiResponse response = geminiService.generateWithSchema(prompt, buildPerfAdviceSchema());
    var payload = responseParser.parseResponse(response);
    // 1. AI 결과 저장
    entitySaver.saveAll(testId, payload);

    // 2. logic_status.ready TRUE
    var rows = logicStatusRepository.markAiReady(testId);
    if (!rows.isEmpty()) {
        log.info("[AI] ai_ready marked TRUE for testId={}", testId);

        // 3. 커밋 후 AI_READY 롱폴 알림
        TxAfterCommit.run(()-> {
            log.info("[LONGPOLL][AI_READY] triggered for testId={}", testId);
            longPollingManager.complete(
                    new WaitKey(testId, LongPollingTopic.AI_READY),
                    new PhaseReadyPayload(LongPollingTopic.AI_READY, testId, java.time.Instant.now())
            );
        });
    } else {
        log.info("[AI] markAiReady returned empty rows (already ready or not triggered), testId={}", testId);
    }
  }

  @Transactional(readOnly = true)
  public AiAnalysisResponse getAnalysis(UUID testId) {
    return dtoConverter.getAnalysis(testId);
  }

  @Transactional(readOnly = true)
  public AiAnalysisSummaryResponse getAnalysisSummary(UUID testId) {
    return dtoConverter.getAnalysisSummary(testId);
  }

  @Transactional(readOnly = true)
  public TopPrioritiesResponse getTopPriorities(UUID testId) {
    return dtoConverter.getTopPriorities(testId);
  }
}
