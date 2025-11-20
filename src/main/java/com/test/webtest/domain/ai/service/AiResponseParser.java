package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.AiSavePayload;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public AiResponseParser() {
        this.objectMapper = new ObjectMapper();
    }

    public AiSavePayload parseResponse(AiResponse response) {
        try {
            return objectMapper.readValue(response.getText(), AiSavePayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("AI JSON parse failed: " + response.getText(), e);
        }
    }
}
