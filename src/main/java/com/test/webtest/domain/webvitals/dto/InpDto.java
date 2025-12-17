package com.test.webtest.domain.webvitals.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class InpDto {
    private String name;
    private Double value;
    private String rating;
    private List<InpEntry> entries;

    @Getter
    @NoArgsConstructor
    public static class InpEntry {
        private String name;

        @JsonProperty("entryType")
        private String entryType;

        @JsonProperty("startTime")
        private Double startTime;

        private Integer duration;

        @JsonProperty("interactionId")
        private Integer interactionId;

        @JsonProperty("processingStart")
        private Double processingStart;

        @JsonProperty("processingEnd")
        private Double processingEnd;

        private Boolean cancelable;
    }
}

