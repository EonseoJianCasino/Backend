package com.test.webtest.domain.webvitals.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ClsDto {
    private String name;
    private Double value;
    private String rating;
    private List<ClsEntry> entries;

    @Getter
    @NoArgsConstructor
    public static class ClsEntry {
        private String name;

        @JsonProperty("entryType")
        private String entryType;

        @JsonProperty("startTime")
        private Double startTime;

        private Integer duration;

        private Double value;

        @JsonProperty("hadRecentInput")
        private Boolean hadRecentInput;

        @JsonProperty("lastInputTime")
        private Double lastInputTime;

        private List<ClsSource> sources;
    }

    @Getter
    @NoArgsConstructor
    public static class ClsSource {
        @JsonProperty("previousRect")
        private ClsRect previousRect;

        @JsonProperty("currentRect")
        private ClsRect currentRect;
    }

    @Getter
    @NoArgsConstructor
    public static class ClsRect {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
        private Double top;
        private Double right;
        private Double bottom;
        private Double left;
    }
}

