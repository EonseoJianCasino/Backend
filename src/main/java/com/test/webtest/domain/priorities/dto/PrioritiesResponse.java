package com.test.webtest.domain.priorities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/*
 * 우선순위 응답 DTO
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrioritiesResponse {

    private UUID testId;
    private Integer totalCount; // webvitalcount  -> 받은 priority 중에 webvital 이 몇개인지
    private Integer webVitalCount;
}

