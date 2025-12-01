package com.test.webtest.domain.webvitals.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class LcpDto {
    private String name;
    private Double value;
    private String rating;
    private List<LcpEntry> entries;

    @Getter
    @NoArgsConstructor
    public static class LcpEntry {
        private String name;

        @JsonProperty("entryType")
        private String entryType;

        @JsonProperty("startTime")
        private Integer startTime;

        private Integer duration;

        private Integer size;

        @JsonProperty("renderTime")
        private Integer renderTime;

        @JsonProperty("loadTime")
        private Integer loadTime;

        private String id;

        private String url;
    }
}

