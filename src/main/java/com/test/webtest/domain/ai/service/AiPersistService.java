package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiAnalysisResponse;
import com.test.webtest.domain.ai.dto.AiAnalysisSummaryResponse;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.dto.TopPrioritiesResponse;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.global.longpoll.LongPollingManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.test.webtest.domain.ai.schema.AiSchemas.buildPerfAdviceSchema;

import java.util.UUID;

@Service
public class AiPersistService {

  private final AiRecommendationService geminiService; // <- 여기서 호출
  private final AiPromptBuilder promptBuilder;
  private final AiResponseParser responseParser;
  private final AiEntitySaver entitySaver;
  private final AiDtoConverter dtoConverter;

  public AiPersistService(
          @Lazy AiRecommendationService geminiService,
          AiPromptBuilder promptBuilder,
          AiResponseParser responseParser,
          AiEntitySaver entitySaver,
          AiDtoConverter dtoConverter) {
    this.geminiService = geminiService;
    this.promptBuilder = promptBuilder;
    this.responseParser = responseParser;
    this.entitySaver = entitySaver;
    this.dtoConverter = dtoConverter;
  }
  @Transactional
  public void generateAndSave(UUID testId) {
    String prompt = promptBuilder.buildPrompt(testId);
    AiResponse response = geminiService.generateWithSchema(prompt, buildPerfAdviceSchema());
    var payload = responseParser.parseResponse(response);
    // 1. AI 결과 저장
    entitySaver.saveAll(testId, payload);
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
