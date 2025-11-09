package com.test.webtest.domain.priorities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityDto {
    private String type;
    private String metric;
    private String reason;
    private Integer rank;
    private Double value; // web vital용 수치 (선택적)
	private String status; // 양호, 주의, 긴급 저장
}
