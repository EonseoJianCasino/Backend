package com.test.webtest.domain.ai.schema;

import java.util.List;
import java.util.Map;

public class AiSchemas {

    public static Map<String, Object> buildPerfAdviceSchema(){

        Map<String, Object> metricDelta = Map.of(
                "type", "object",
                "properties", Map.of(
                        "metric", Map.of("type", "string"),
                        "current_score", Map.of("type", "integer"),
                        "achievable_score", Map.of("type", "integer"),
                        "delta", Map.of("type", "integer")
                ),
                "required", List.of("metric", "current_score", "achievable_score", "delta")
        );

        Map<String, Object> webElement = Map.of(
                "type", "object",
                "properties", Map.of(
                        "element_name", Map.of("type", "string"),
                        "status", Map.of("type", "string"),
                        "benefit_summary", Map.of("type", "string"),
                        "expected_score_gain", Map.of("type", "integer"),
                        "metric_deltas", Map.of("type", "array", "items", metricDelta),
                        "related_metrics", Map.of("type", "array", "items", Map.of("type", "string")),
                        "benefit_detail", Map.of("type", "string")
                ),
                "required", List.of("element_name", "status", "benefit_summary", "expected_score_gain", "metric_deltas", "related_metrics", "benefit_detail")
        );

        Map<String, Object> securityMetric = Map.of(
                "type", "object",
                "properties", Map.of(
                        "metric_name", Map.of("type", "string"),
                        "status", Map.of("type", "string"),
                        "benefit_summary", Map.of("type", "string"),
                        "delta", Map.of("type", "integer"),
                        "expected_score_gain", Map.of("type", "integer"),
                        "related_metrics", Map.of("type", "array", "items", Map.of("type", "string")),
                        "benefit_detail", Map.of("type", "string")
                ),
                "required", List.of("metric_name", "status", "benefit_summary", "delta", "expected_score_gain", "related_metrics", "benefit_detail")
        );

        Map<String, Object> majorImprovement = Map.of(
                "type", "object",
                "properties", Map.of(
                        "rank", Map.of("type", "integer"),
                        "metric", Map.of("type", "string"),
                        "title", Map.of("type", "string"),
                        "description", Map.of("type", "string")
                ),
                "required", List.of("rank", "metric", "title", "description")
        );

        Map<String, Object> prioritySchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "rank", Map.of("type", "integer"),
                        "status", Map.of("type", "string"),
                        "target_type", Map.of("type", "string"),
                        "target_name", Map.of("type", "string"),
                        "reason", Map.of("type", "string")
                ),
                "required", List.of("rank", "status", "target_type", "target_name", "reason")
        );

        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "overall_expected_improvement", Map.of("type", "integer"),
                        "overall_total_after", Map.of("type", "integer"),
                        "top_priorities", Map.of("type", "array", "items", prioritySchema),
                        "web_elements", Map.of("type", "array", "items", webElement),
                        "security_metrics", Map.of("type", "array", "items", securityMetric),
                        "major_improvements", Map.of("type", "array", "items", majorImprovement)
                ),
                "required", List.of(
                        "overall_expected_improvement",
                        "overall_total_after",
                        "top_priorities",
                        "web_elements",
                        "security_metrics",
                        "major_improvements"
                )
        );

    }

}
