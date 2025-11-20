package com.test.webtest.domain.ai.dto;

import java.util.List;

public record TopPrioritiesResponse(
        List<TopPriorityDto> topPriorities) {
    public record TopPriorityDto(
            Integer rank,
            String targetType,
            String targetName,
            Integer expectedGain,
            String reason) {
    }
}
