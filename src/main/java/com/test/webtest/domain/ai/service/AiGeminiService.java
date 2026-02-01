package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.global.error.exception.AiCallFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.netty.http.client.PrematureCloseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class AiGeminiService {

    private final WebClient geminiWebClient;

    @Value("${app.gemini.model}")
    private String defaultModel;

    public AiGeminiService(WebClient geminiWebClient) {
        this.geminiWebClient = geminiWebClient;
    }

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
        } catch (WebClientResponseException e) {
            // 즉시 실패 (4xx, 5xx 등) - 재시도 안 함
            log.error("[GEMINI][FAIL] 즉시 실패 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiCallFailedException(e, false);
        } catch (Exception e) {
            // 타임아웃 여부 확인
            boolean isTimeout = isTimeoutException(e);
            if (isTimeout) {
                log.warn("[GEMINI][TIMEOUT] 60초 타임아웃 발생: {}", e.getMessage());
                throw new AiCallFailedException("AI 서비스 응답 타임아웃 (60초)", e, true);
            } else {
                // 기타 즉시 실패 (네트워크 연결 실패 등) - 재시도 안 함
                log.error("[GEMINI][FAIL] 즉시 실패: {}", e.getMessage(), e);
                throw new AiCallFailedException(e, false);
            }
        }
    }

    private boolean isTimeoutException(Exception e) {
        // 타임아웃 예외 체크
        if (e instanceof TimeoutException) {
            return true;
        }
        
        // 예외 메시지로 타임아웃 확인
        String message = e.getMessage();
        if (message != null && (message.contains("timeout") || message.contains("Timeout") || 
            message.contains("ReadTimeout") || message.contains("ResponseTimeout"))) {
            return true;
        }
        
        // Reactor의 타임아웃 예외 체크
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof TimeoutException) {
                return true;
            }
            String causeClassName = cause.getClass().getName();
            if (causeClassName.contains("TimeoutException") || 
                causeClassName.contains("ReadTimeout") ||
                causeClassName.contains("ResponseTimeout")) {
                return true;
            }
            if (cause instanceof PrematureCloseException) {
                return false; // 연결 종료는 타임아웃이 아님
            }
            String causeMessage = cause.getMessage();
            if (causeMessage != null && (causeMessage.contains("timeout") || 
                causeMessage.contains("Timeout") || causeMessage.contains("ReadTimeout"))) {
                return true;
            }
            cause = cause.getCause();
        }
        
        // Reactor Exceptions의 unwrap으로 확인
        Throwable unwrapped = Exceptions.unwrap(e);
        if (unwrapped instanceof TimeoutException) {
            return true;
        }
        if (unwrapped != null && unwrapped.getClass().getName().contains("TimeoutException")) {
            return true;
        }
        
        return false;
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

