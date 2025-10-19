package com.test.webtest.domain.test.entity;

public class TestEntity {
    @Id
    private UUID id;

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
                .id(UUID.randomUUID())
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
