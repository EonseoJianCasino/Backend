package com.test.webtest.domain.webvitals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TtfbDto {
    private String name;
    private Double value;
    private String rating;
    private List<TtfbEntry> entries;

    @Getter
    @NoArgsConstructor
    public static class TtfbEntry {
        private String name;

        @JsonProperty("entryType")
        private String entryType;

        @JsonProperty("startTime")
        private Integer startTime;

        @JsonProperty("responseStart")
        private Double responseStart;

        @JsonProperty("requestStart")
        private Double requestStart;

        @JsonProperty("domainLookupStart")
        private Double domainLookupStart;

        @JsonProperty("connectStart")
        private Double connectStart;

        @JsonProperty("connectEnd")
        private Double connectEnd;
    }
}

