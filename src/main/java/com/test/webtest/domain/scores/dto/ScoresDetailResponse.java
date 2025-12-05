package com.test.webtest.domain.scores.dto;

import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;

import java.util.ArrayList;
import java.util.List;


public record ScoresDetailResponse(
        int total,
        List<Metric> charData

) {
    public record Metric(
            String name,
            int score,
            String urgentStatus
    ) {}
    public static ScoresDetailResponse from(ScoresEntity se, UrgentLevelEntity ue) {
        List<Metric> charData = List.of(
                metric("lcp", se.getLcpScore(), ue.getLcpStatus()),
                metric("cls", se.getClsScore(), ue.getClsStatus()),
                metric("inp", se.getInpScore(), ue.getInpStatus()),
                metric("fcp", se.getFcpScore(), ue.getFcpStatus()),
                metric("ttfb", se.getTtfbScore(), ue.getTtfbStatus())
        );

        return new ScoresDetailResponse(
                n(se.getTotal()),
                charData
        );
    }
    private static int n(Integer v) { return v == null ? 0 : v; }

    private static Metric metric(String name, Integer score, String urgentStatus) {
        return new Metric(name, n(score), urgentStatus);
    }
}