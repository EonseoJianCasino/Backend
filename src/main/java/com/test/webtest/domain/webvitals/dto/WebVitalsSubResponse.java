package com.test.webtest.domain.webvitals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebVitalsSubResponse {
    private FcpResponse fcp;
    private TtfbResponse ttfb;
    private LcpResponse lcp;
    private InpResponse inp;
    private ClsResponse cls;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FcpResponse {
        private String entryType;
        private Integer startTime;
        private Instant createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtfbResponse {
        private String entryType;
        private Integer startTime;
        private Double responseStart;
        private Double requestStart;
        private Double domainLookupStart;
        private Double connectStart;
        private Double connectEnd;
        private Instant createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LcpResponse {
        private Integer startTime;
        private Integer renderTime;
        private Integer renderedSize;
        private String element;
        private Instant createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InpResponse {
        private String entryType;
        private String name;
        private Double startTime;
        private Integer duration;
        private Double processingStart;
        private Double processingEnd;
        private Integer interactionId;
        private String target;
        private Instant createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClsResponse {
        private String entryType;
        private Double startTime;
        private Double clsValue;
        private Boolean hadRecentInput;
        private String sources;
        private String previousRect;
        private Instant createdAt;
    }
}

