package com.test.webtest.domain.priorities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/*
 * 우선순위 저장 요청 DTO
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrioritiesSaveRequest {

    private UUID testId;
    private List<PriorityDto> priorities;
}

