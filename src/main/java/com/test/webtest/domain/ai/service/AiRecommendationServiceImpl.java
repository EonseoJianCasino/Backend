package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService {


    private final WebClient geminiWebClient;
    private final AiPersistService aiPersistService; // <- 여기서 호출 3

    @Value("${app.gemini.model}")
    private String defaultModel;

    @Autowired
    public AiRecommendationServiceImpl(WebClient geminiWebClient, AiPersistService aiPersistService) {
        this.geminiWebClient = geminiWebClient;
        this.aiPersistService = aiPersistService;
    }

    @Override
    @Async("logicExecutor")
    public void invokeAsync(UUID testId) {
        log.info("[AI] invoke recommendations for testId={}", testId);
        aiPersistService.generateAndSave(testId);
    }

    @Override
    public AiResponse generate(String userPrompt, String system, String model, boolean jsonMode) {

        String useModel = (model == null || model.isBlank()) ? defaultModel : model;

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userPrompt)))));

        if (system != null && !system.isBlank()) {
            body.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", system))));
        }

        if (jsonMode) {
            body.put("generationConfig", Map.of(
                    "respose_mime_type", "application/json"));
        }

        String path = String.format("/models/%s:generateContent", useModel);

        Map<?, ?> resp = geminiWebClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return new AiResponse(extractText(resp));
    }

    @Override
    public AiResponse generateWithSchema(String prompt, Map<String, Object> jsonSchema) {

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt)))));

        Map<String, Object> genCfg = new HashMap<>();
        genCfg.put("responseMimeType", "application/json");
        genCfg.put("responseSchema", jsonSchema);
        body.put("generationConfig", genCfg);

        String path = String.format("/models/%s:generateContent", defaultModel);

        try {

            Map<?, ?> resp = geminiWebClient.post()
                    .uri(path)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return new AiResponse(extractText(resp));

        }catch(WebClientResponseException e){
            log.error("[GEMINI] status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> resp) {
        if (resp == null)
            return "(no response)";
        try {
            var candidates = (List<Map<String, Object>>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty())
                return "(no candidates)";
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");
            StringBuilder sb = new StringBuilder();
            for (var p : parts) {
                Object text = p.get("text");
                if (text instanceof String s)
                    sb.append(s);
            }
            return sb.length() > 0 ? sb.toString() : "(empty)";
        } catch (Exception e) {
            return "(parse error)";
        }
    }
}
