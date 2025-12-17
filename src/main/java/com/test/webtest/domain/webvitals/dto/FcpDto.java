package com.test.webtest.domain.webvitals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FcpDto {
    private String name;
    private Double value;
    private String rating;
    private List<FcpEntry> entries;

    @Getter
    @NoArgsConstructor
    public static class FcpEntry {
        private String name;

        @JsonProperty("entryType")
        private String entryType;

        @JsonProperty("startTime")
        private Integer startTime;

        private Integer duration;
    }
}

