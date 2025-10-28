package com.test.webtest.domain.ai.schema;

import java.util.List;
import java.util.Map;

public class AiSchemas {

    public static Map<String, Object> buildPerfAdviceSchema(){

        Map<String, Object> metricSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "summary", Map.of("type", "string"),
                        "improvements", Map.of("type", "array", "items", Map.of("type", "string")),
                        "estimatedScoreImprovement", Map.of("type", "string"),
                        "expectedBenefits", Map.of("type", "array", "items", Map.of("type", "string")),
                        "relatedMetrics", Map.of("type", "array", "items", Map.of("type", "string"))
                ),
                "required", List.of("summary","improvements","estimatedScoreImprovement","expectedBenefits","relatedMetrics"),
                "additionalProperties", false
        );

        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "LCP", metricSchema,
                        "CLS", metricSchema,
                        "INP", metricSchema,
                        "FCP", metricSchema,
                        "TTFB", metricSchema
                ),
                "required", List.of("LCP","CLS","INP","FCP","TTFB"),
                "additionalProperties", false
        );

    }

}
