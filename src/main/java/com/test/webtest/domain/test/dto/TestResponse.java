package com.test.webtest.domain.test.dto;

import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.global.common.constants.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TestResponse {
    private final String url;
    private final String domainName;
    private final StatusType status;
    private final Instant createdAt;
    private final UUID testId;

    public static TestResponse fromEntity(TestEntity e) {
        return TestResponse.builder()
                .url(e.getUrl())
                .domainName(e.getDomainName())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .testId(e.getId())
                .build();
    }
}
