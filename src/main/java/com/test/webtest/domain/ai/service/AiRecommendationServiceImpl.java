package com.test.webtest.domain.ai.service;

//import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.UUID;
import com.test.webtest.domain.ai.dto.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.*; //HashMap, List, Map




@Service
//@RequiredArgsConstructor
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService{

    private final WebClient geminiWebClient;

    @Value("${app.gemini.model}")
    private String defaultModel;

    public AiRecommendationServiceImpl(WebClient geminiWebClient){
        this.geminiWebClient = geminiWebClient;
    }

    @Override
    @Async("logicExecutor")
    public void invokeAsync(UUID testId) {
        log.info("[AI] invoke recommendations for testId={}", testId); // log를 쓰려면 lombok에서 slf4j를 import해야 함,,

        // 프롬프트 생성 및 외부 AI 호출
    }

    @Override
    public AiResponse generate(String userPrompt, String system, String model, boolean jsonMode){

        String useModel = (model == null || model.isBlank()) ? defaultModel : model;

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userPrompt))
        )));

        if(system != null && !system.isBlank()){
            body.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", system))
            ));
        }

        if (jsonMode){
            body.put("generationConfig", Map.of(
                    "respose_mime_type", "application/json"
            ));
        }

        String path = String.format("/models/%s:generateContent", useModel);

        Map<?, ?>resp = geminiWebClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return new AiResponse(extractText(resp));
    }

    @Override
    public Flux<String> stream(String userPrompt, String model){
        String useModel = (model == null || model.isBlank()) ? defaultModel : model;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt))
                ))
        );

        String path = String.format("/models/%s:streamGenerateContent?alt=sse", useModel);

        return geminiWebClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class);

    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> resp){
        if(resp == null) return "(no response)";
        try {
            var candidates = (List<Map<String, Object>>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "(no candidates)";
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");
            StringBuilder sb = new StringBuilder();
            for (var p : parts) {
                Object text = p.get("text");
                if (text instanceof String s) sb.append(s);
            }
            return sb.length() > 0 ? sb.toString() : "(empty)";
        }catch(Exception e){
            return "(parse error)";
        }
    }



}
