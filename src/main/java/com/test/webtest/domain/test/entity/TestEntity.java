package com.test.webtest.domain.test.entity;

import com.test.webtest.global.common.constants.StatusType;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TestEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String domainName;

    @Column(nullable = false)
    private String ip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @CreationTimestamp
    private Instant createdAt;

    // 엔티티 생성 팩토리 메서드
    public static TestEntity create(String url, String ip){
        return TestEntity.builder()
                .id(UUID.randomUUID().toString())
                .url(url)
                .domainName(extractDomain(url))
                .ip(ip)
                .status(StatusType.PENDING)
                .build();
    }

    // URL 에서 domainName 자동 추출
    public static String extractDomain(String url) {
        try{
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null ? host.replace("www.","") : url;
        } catch(URISyntaxException e) {
            return url;
        }
    }
}
