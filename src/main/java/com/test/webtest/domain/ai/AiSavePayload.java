package com.test.webtest.domain.ai;

import java.util.List;

public class AiSavePayload {

//    public List<Rec> recommendations;
//    public List<Exp> expectations;
//
//    public static class Rec {
//        public String type;   // "PERF" | "SEC"
//        public String metric; // "LCP" | "CLS" | "INP" | ...
//        public String title;
//        public String content;
//    }
//    public static class Exp {
//        public String metric;
//        public String content;
//    }


    public MetricAdvice LCP;
    public MetricAdvice CLS;
    public MetricAdvice INP;
    public MetricAdvice FCP;
    public MetricAdvice TTFB;

    public static class MetricAdvice {
        public String summary_of_improvement_areas;
        public List<String> potential_improvements;
        public String estimated_score_improvement;
        public List<String> expected_benefits;
        public List<String> related_metrics;
    }


}
