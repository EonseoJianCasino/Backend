package com.test.webtest.domain.ai;

import java.util.List;

public class AiSavePayload {

    public List<Rec> recommendations;
    public List<Exp> expectations;

    public static class Rec {
        public String type;   // "PERF" | "SEC"
        public String metric; // "LCP" | "CLS" | "INP" | ...
        public String title;
        public String content;
    }
    public static class Exp {
        public String metric;
        public String content;
    }

}
