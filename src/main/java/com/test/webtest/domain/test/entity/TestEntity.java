package com.test.webtest.domain.test.entity;

import com.test.webtest.global.common.constants.StatusType;
import com.test.webtest.global.common.util.UrlNormalizer;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String domainName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @CreationTimestamp
    private Instant createdAt;

    @Builder(builderMethodName = "internalBuilder")
    private TestEntity(UUID id, String url, String domainName, StatusType status) {
        this.id = id;
        this.url = url;
        this.domainName = domainName;
        this.status = status;
    }

    // 엔티티 생성 팩토리 메서드
    public static TestEntity create(String url){
        return internalBuilder()
            .id(UUID.randomUUID())
            .url(url)
            .domainName(UrlNormalizer.extractDomain(url))
            .status(StatusType.PENDING)
            .build();
    }
}
