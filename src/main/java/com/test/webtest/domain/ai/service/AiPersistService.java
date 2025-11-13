package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.AiSavePayload;
import com.test.webtest.domain.ai.dto.AiMetricAdviceBundleResponse;
import com.test.webtest.domain.ai.dto.AiResponse;
//import com.test.webtest.domain.ai.entity.AiExpectation;
import com.test.webtest.domain.ai.dto.MetricAdviceResponse;
import com.test.webtest.domain.ai.entity.AiMetricAdvice;
//import com.test.webtest.domain.ai.entity.AiRecommendation;
import com.test.webtest.domain.ai.entity.Metric;
//import com.test.webtest.domain.ai.repository.AiExpectationRepository;
//import com.test.webtest.domain.ai.repository.AiRecommendationRepository;
//import com.test.webtest.domain.logicstatus.entity.LogicStatusRepository;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.test.webtest.domain.ai.entity.Metric.*;
import static com.test.webtest.domain.ai.schema.AiSchemas.buildPerfAdviceSchema;

import java.util.List;
import java.util.UUID;

import com.test.webtest.domain.webvitals.repository.ClsRepository;
import com.test.webtest.domain.webvitals.repository.FcpRepository;
import com.test.webtest.domain.webvitals.repository.InpRepository;
import com.test.webtest.domain.webvitals.repository.LcpRepository;
import com.test.webtest.domain.webvitals.repository.TtfbRepository;

import com.test.webtest.domain.webvitals.entity.*;

import java.util.Optional;

import com.test.webtest.domain.ai.repository.AiMetricAdviceRepository;


@Service
public class AiPersistService {

    private final AiRecommendationService geminiService;
//    private final AiRecommendationRepository recRepo;
//    private final AiExpectationRepository expRepo;
    private final LogicStatusRepository logicStatusRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final ClsRepository clsRepo;
    private final FcpRepository fcpRepo;
    private final InpRepository inpRepo;
    private final TtfbRepository ttfbRepo;
    private final LcpRepository lcpRepo;

    private final AiMetricAdviceRepository adviceRepo;

    public AiPersistService(
            AiRecommendationService geminiService,
//            AiRecommendationRepository recRepo,
//            AiExpectationRepository expRepo,
            LogicStatusRepository logicStatusRepo, ClsRepository clsRepo, FcpRepository fcpRepo, InpRepository inpRepo, TtfbRepository ttfbRepo, LcpRepository lcpRepo, AiMetricAdviceRepository adviceRepo
    ) {
        this.geminiService = geminiService;
//        this.recRepo = recRepo;
//        this.expRepo = expRepo;
        this.logicStatusRepo = logicStatusRepo;

        // 보조지표 레포지토리들 사용하기 위해 아래 parameter들 추가함
        this.clsRepo = clsRepo;
        this.fcpRepo = fcpRepo;
        this.inpRepo = inpRepo;
        this.ttfbRepo = ttfbRepo;
        this.lcpRepo = lcpRepo;
        this.adviceRepo = adviceRepo;
    }

    @Transactional
    public void generateAndSave(UUID testId) {

        Optional<ClsEntity> clsResults = clsRepo.findByTest_Id(testId);
        Optional<FcpEntity> fcpResults = fcpRepo.findByTest_Id(testId);
        Optional<InpEntity> inpResults = inpRepo.findByTest_Id(testId);
        Optional<LcpEntity> lcpResults = lcpRepo.findByTest_Id(testId);
        Optional<TtfbEntity> ttfbResults = ttfbRepo.findByTest_Id(testId);



//        AiResponse resp = geminiService.generate(
//                wrapAsJsonInstruction(promptText),
//                null, null, true
//        );

        String domainPrompt = buildPromptFromDb(testId, clsResults, fcpResults, inpResults, lcpResults, ttfbResults);

//        String finalPrompt = wrapAsJsonInstruction(domainPrompt);

//        AiResponse resp = geminiService.generateWithSchema(promptText, buildPerfAdviceSchema());
//        AiResponse resp = geminiService.generateWithSchema(finalPrompt, buildPerfAdviceSchema());
        AiResponse resp = geminiService.generateWithSchema(domainPrompt, buildPerfAdviceSchema());




//        AiSavePayload payload;
//        try {
//            payload = objectMapper.readValue(resp.getText(), AiSavePayload.class);
//        } catch (Exception e) {
//            throw new IllegalStateException("AI JSON parse failed. text=" + resp.getText(), e);
//        }

//        if (payload.recommendations != null) {
//            for (var r : payload.recommendations) {
//                String type = (r.type == null || r.type.isBlank()) ? "PERF" : r.type;
//                recRepo.save(AiRecommendation.of(testId, type, r.metric, r.title, r.content));
//            }
//        }
//        if (payload.expectations != null) {
//            for (var e : payload.expectations) {
//                expRepo.save(AiExpectation.of(testId, e.metric, e.content));
//            }
//        }

        AiSavePayload payload;

        try{
            payload = objectMapper.readValue(resp.getText(), AiSavePayload.class);
        }
        catch (Exception e){
            throw new IllegalStateException("AI JSON parse failed: " + resp.getText(), e);
        }

//        if (payload.LCP != null) {
//            saveMetricAdvice(testId, Metric.LCP, payload.LCP);
//        }
//        if (payload.CLS != null) {
//            saveMetricAdvice(testId, Metric.CLS, payload.CLS);
//        }
//        if (payload.FCP != null) {
//            saveMetricAdvice(testId, Metric.LCP, payload.FCP);
//        }
//        if (payload.TTFB != null) {
//            saveMetricAdvice(testId, Metric.CLS, payload.TTFB);
//        }if (payload.LCP != null) {
//            saveMetricAdvice(testId, Metric.LCP, payload.LCP);
//        }

        saveOneMetric(testId, LCP, payload.LCP);
        saveOneMetric(testId, Metric.CLS, payload.CLS);
        saveOneMetric(testId, Metric.INP,  payload.INP);
        saveOneMetric(testId, FCP,  payload.FCP);
        saveOneMetric(testId, TTFB, payload.TTFB);


        logicStatusRepo.markAiTriggered(testId);

        // ✅ logicstatus 폴더에 있는 엔티티/레포 사용
//        var ls = logicStatusRepo.findById(testId)
//                .orElseGet(() -> {
//                    var newStatus = new LogicStatusEntity();
//                    newStatus.setTestId(testId);
//                    return newStatus;
//                });
//        ls.markAiTriggered();
//        logicStatusRepo.save(ls);

        // 4️⃣ ✅ AI가 완료되었음을 logic_status에 반영
//        logicStatusRepo.markAiTriggered(testId);

    }

//    private String wrapAsJsonInstruction(String userPrompt) {
//        return """
//        You are a web performance & security expert.
//        Return STRICT JSON with two arrays: "recommendations" and "expectations".
//        Respond with ONLY JSON. No preface.
//
//        INPUT:
//        """ + userPrompt;
//    }

    private String buildPromptFromDb(UUID testId, Optional<ClsEntity> clsResults, Optional<FcpEntity> fcpResults, Optional<InpEntity> inpResults, Optional<LcpEntity> lcpResults, Optional<TtfbEntity> ttfbResults) {

        String lcpStartTime = lcpResults.map(LcpEntity::getStartTime).map(Object::toString).orElse("N/A");
        String lcpRenderTime = lcpResults.map(LcpEntity::getRenderTime).map(Object::toString).orElse("N/A");
        String lcpSize = lcpResults.map(LcpEntity::getRenderedSize).map(Object::toString).orElse("N/A");
//                getLcpSize).map(Object::toString).orElse("N/A");
        String lcpElement = lcpResults.map(LcpEntity::getElement).orElse("N/A");

        String clsEntryType = clsResults.map(ClsEntity::getEntryType).orElse("N/A");
        String clsStartTime = clsResults.map(ClsEntity::getStartTime).map(Object::toString).orElse("N/A");
        String clsValue = clsResults.map(ClsEntity::getClsValue).map(Object::toString).orElse("N/A");
        String clsHadRecentInp = clsResults.map(ClsEntity::getHadRecentInput).map(Object::toString).orElse("N/A");
        String clsPreviousRect = clsResults.map(ClsEntity::getPreviousRect).orElse("N/A"); // 이거 없애야 함....
        String sources = clsResults.map(ClsEntity::getSources).orElse("N/A");

        String inpEntryType = inpResults.map(InpEntity::getEntryType).orElse("N/A");
        String inpName = inpResults.map(InpEntity::getName).orElse("N/A");
        String inpStartTime = inpResults.map(InpEntity::getStartTime).map(Object::toString).orElse("N/A");
        String inpDuration = inpResults.map(InpEntity::getDuration).map(Object::toString).orElse("N/A");
        String inpProcStart = inpResults.map(InpEntity::getStartTime).map(Object::toString).orElse("N/A");
        String inpProcEnd = inpResults.map(InpEntity::getProcessingEnd).map(Object::toString).orElse("N/A");
        String inpInteractionId = inpResults.map(InpEntity::getInteractionId).map(Object::toString).orElse("N/A");
        String inpTarget = inpResults.map(InpEntity::getTarget).orElse("N/A");

        String fcpEntryType = fcpResults.map(FcpEntity::getEntryType).orElse("N/A");
        String fcpStartTime = fcpResults.map(FcpEntity::getStartTime).map(Object::toString).orElse("N/A");

        String ttfbEntryType = ttfbResults.map(TtfbEntity::getEntryType).orElse("N/A");
        String ttfbStartTime = ttfbResults.map(TtfbEntity::getStartTime).map(Object::toString).orElse("N/A");
        String ttfbResponseStart = ttfbResults.map(TtfbEntity::getResponseStart).map(Object::toString).orElse("N/A");
        String ttfbRequestStart = ttfbResults.map(TtfbEntity::getReqeustStart).map(Object::toString).orElse("N/A");
        String ttfbDnsStart = ttfbResults.map(TtfbEntity::getDomainLookupStart).map(Object::toString).orElse("N/A");
        String ttfbConnectStart = ttfbResults.map(TtfbEntity::getConnectStart).map(Object::toString).orElse("N/A");
        String ttfbConnectEnd = ttfbResults.map(TtfbEntity::getConnectEnd).map(Object::toString).orElse("N/A");



        return String.format("You are an IT security expert. I want advice on web performance analysis." +
                "The LCP metric has a startTime of %s, a renderTime of %s, a size of %s, and the element is %s." +
                "The CLS metric has an entryType of %s, startTime of %s, a value of %s, hadRecentInput of %s, and sources of %s." +
                "The INP metric has an entryType of %s, name of %s, startTime of %s, duration of %s, processingStart of %s, processingEnd of %s, interactionId of %s, and target of %s." +
                "The FCP metric has an entryType of %s, and startTime of %s." +
                "The TTFB metric has an entryType of %s, startTime of %s, responseStart of %s, requestStart of %s, domainLookupStart of %s, connectStart of %s, and connectEnd of %s." +
                "Regarding these LCP, CLS, INP, FCP, and TTFB metrics, please provide the following in JSON format for each metric: " +
                "A one-line summary of the areas needing improvement, A list of potential improvements, The estimated score improvement, The expected benefits, related metrics." +
                "Give it to me strictly in dictionary format, without any introductory phrases.", lcpStartTime, lcpRenderTime, lcpSize, lcpElement,
                clsEntryType, clsStartTime, clsValue, clsHadRecentInp, sources,
                inpEntryType, inpName, inpStartTime, inpDuration, inpProcStart, inpProcEnd, inpInteractionId, inpTarget,
                fcpEntryType, fcpStartTime,
                ttfbEntryType, ttfbStartTime, ttfbResponseStart, ttfbRequestStart, ttfbDnsStart, ttfbConnectStart, ttfbConnectEnd
                );
    }


    private void saveOneMetric(UUID testId, Metric metric, AiSavePayload.MetricAdvice dto) {

        if (dto == null) return; // AI가 어떤 metric은 안 줄 수도 있음

        // 메인 엔티티 저장
        AiMetricAdvice advice = AiMetricAdvice.of(
                testId,
                metric,
                dto.summary_of_improvement_areas,
                dto.estimated_score_improvement
        );

        // 리스트: potential_improvements
        int i = 0;
        if (dto.potential_improvements != null) {
            for (String s : dto.potential_improvements) {
                advice.addImprovement(i++, s);
            }
        }

        // 리스트: expected_benefits
        i = 0;
        if (dto.expected_benefits != null) {
            for (String s : dto.expected_benefits) {
                advice.addBenefit(i++, s);
            }
        }

        // 리스트: related_metrics
        i = 0;
        if (dto.related_metrics != null) {
            for (String s : dto.related_metrics) {
                advice.addRelatedMetric(i++, s);
            }
        }

        // 저장 (cascade=ALL 이므로 리스트들도 함께 저장됨)
        adviceRepo.save(advice);
    }
    

    @Transactional(readOnly = true)
    public AiMetricAdviceBundleResponse getMetricAdviceBundle(UUID testId) {

        var list = adviceRepo.findByTestId(testId);
        AiMetricAdviceBundleResponse bundle = new AiMetricAdviceBundleResponse();

        for (AiMetricAdvice advice : list) {

            MetricAdviceResponse dto = toResponseDto(advice);

            switch (advice.getMetric()) {
                case LCP -> bundle.LCP = dto;
                case CLS -> bundle.CLS = dto;
                case INP -> bundle.INP = dto;
                case FCP -> bundle.FCP = dto;
                case TTFB -> bundle.TTFB = dto;
            }
        }

        return bundle;
    }


    private MetricAdviceResponse toResponseDto(AiMetricAdvice advice) {

        // ord 순서대로 소팅해서 리스트 변환
        List<String> improvements = advice.getImprovements().stream()
                .sorted((a, b) -> Integer.compare(a.getOrd(), b.getOrd()))
                .map(i -> i.getText())
                .toList();

        List<String> benefits = advice.getBenefits().stream()
                .sorted((a, b) -> Integer.compare(a.getOrd(), b.getOrd()))
                .map(b -> b.getText())
                .toList();

        List<String> related = advice.getRelatedMetrics().stream()
                .sorted((a, b) -> Integer.compare(a.getOrd(), b.getOrd()))
                .map(r -> r.getMetricText())
                .toList();

        return new MetricAdviceResponse(
                advice.getMetric().name(),   // "LCP"
                advice.getSummary(),
                advice.getEstimatedLabel(),
                improvements,
                benefits,
                related
        );
    }



}
