package com.test.webtest.domain.ai.controller;

import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.dto.PromptRequest;
import com.test.webtest.domain.ai.service.AiRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/gemini") // 여기 api 명에 맞게 수정해야 할 듯
public class AiRecommendationController {

    private final AiRecommendationService geminiService;

    public AiRecommendationController(AiRecommendationService geminiService){

        this.geminiService = geminiService;
    }


    // 단발 호출(문자열 반환)
    @PostMapping("/complete")
    public ResponseEntity<AiResponse> complete(@Valid @RequestBody PromptRequest req) {
        var out = geminiService.generate(
                req.getPrompt(),
                req.getSystem(),
                req.getModel(),
                Boolean.TRUE.equals(req.getJsonMode())
        );
        return ResponseEntity.ok(out);
    }

    // SSE 스트리밍
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam String prompt,
            @RequestParam(required = false) String model
    ) {
        return geminiService.stream(prompt, model)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    // JSON Mode 강제 엔드포인트(정형 데이터 파싱용)
    @PostMapping("/json")
    public ResponseEntity<String> json(@Valid @RequestBody PromptRequest req) {
        var out = geminiService.generate(
                req.getPrompt(),
                req.getSystem(),
                req.getModel(),
                true
        );
        return ResponseEntity.ok(out.getText());
    }

}
