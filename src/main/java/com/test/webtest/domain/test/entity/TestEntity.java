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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
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

    // 엔티티 생성 팩토리 메서드
    public static TestEntity create(String url){
        return TestEntity.builder()
            .id(UUID.randomUUID())
            .url(url)
            .domainName(UrlNormalizer.extractDomain(url))
            .status(StatusType.PENDING)
            .build();
    }
}
