package com.test.webtest.domain.test.dto;


import com.test.webtest.domain.test.entity.TestEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TestResponse {
    private UUID id;
    private String url;
    private String domainName;
    private String ip;
    private String status;
    private Instant createdAt;

    public static TestResponse fromEntity(TestEntity entity) {
        return TestResponse.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .domainName(entity.getDomainName())
                .ip(entity.getIp())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
    }

