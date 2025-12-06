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
                "required", List.of("metric", "current_score", "achievable_score", "delta")//,
//                "additionalProperties", false
        );

        Map<String, Object> webElement = Map.of(
                "type", "object",
                "properties", Map.of(
                        "element_name", Map.of("type", "string"),
                        "expected_gain", Map.of("type", "integer"),
                        "related_metrics", Map.of("type", "array", "items", Map.of("type", "string")),
                        "metric_deltas", Map.of("type", "array", "items", metricDelta),
                        "detailed_plan", Map.of("type", "string"),
                        "benefit_summary", Map.of("type", "string")
                ),
                "required", List.of("element_name", "expected_gain", "related_metrics", "metric_deltas", "detailed_plan", "benefit_summary")//,
//                "additionalProperties", false
        );

        Map<String, Object> securityMetric = Map.of(
                "type", "object",
                "properties", Map.of(
                        "metric", Map.of("type", "string"),
                        "current_score", Map.of("type", "integer"),
                        "achievable_score", Map.of("type", "integer"),
                        "delta", Map.of("type", "integer"),
                        "expected_gain", Map.of("type", "integer"),
                        "improvement_plan", Map.of("type", "string"),
                        "expected_benefit", Map.of("type", "string"),
                        "impact_title", Map.of("type", "string"),
                        "impact_description", Map.of("type", "string")
                ),
                "required", List.of("metric", "current_score", "achievable_score", "delta", "expected_gain", "improvement_plan", "expected_benefit", "impact_title", "impact_description")//,
//                "additionalProperties", false
        );

        Map<String, Object> majorImprovement = Map.of(
                "type", "object",
                "properties", Map.of(
                        "metric", Map.of("type", "string"),
                        "title", Map.of("type", "string"),
                        "description", Map.of("type", "string")
                ),
                "required", List.of("metric", "title", "description")//,
//                "additionalProperties", false
        );

        Map<String, Object> prioritySchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "rank", Map.of("type", "integer"),
                        "target_type", Map.of("type", "string"),
                        "target_name", Map.of("type", "string"),
                        "expected_gain", Map.of("type", "integer"),
                        "reason", Map.of("type", "string")
                ),
                "required", List.of("rank", "target_type", "target_name", "expected_gain", "reason")//,
//                "additionalProperties", false
        );

        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "overall_expected_improvement", Map.of("type", "integer"),
                        "overall_total_after", Map.of("type", "integer"),
                        "web_elements", Map.of("type", "array", "items", webElement),
                        "security_metrics", Map.of("type", "array", "items", securityMetric),
                        "major_improvements", Map.of("type", "array", "items", majorImprovement),
                        "top_priorities", Map.of("type", "array", "items", prioritySchema)
                ),
                "required", List.of(
                        "overall_expected_improvement",
                        "overall_total_after",
                        "web_elements",
                        "security_metrics",
                        "major_improvements",
                        "top_priorities"
                )//,
//                "additionalProperties", false
        );

    }

}
