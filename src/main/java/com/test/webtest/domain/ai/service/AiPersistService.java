package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.AiSavePayload;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.service.*;
import com.test.webtest.domain.ai.entity.airecommendationentity.*;
import com.test.webtest.domain.logicstatus.entity.LogicStatusEntity;
//import com.test.webtest.domain.logicstatus.entity.LogicStatusRepository;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


@Service
public class AiPersistService {

    private final AiRecommendationService geminiService;
    private final AiRecommendationRepository recRepo;
    private final AiExpectationRepository expRepo;
    private final LogicStatusRepository logicStatusRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final ClsRepository clsRepo;
    private final FcpRepository fcpRepo;
    private final InpRepository inpRepo;
    private final TtfbRepository ttfbRepo;
    private final LcpRepository lcpRepo;

    public AiPersistService(
            AiRecommendationService geminiService,
            AiRecommendationRepository recRepo,
            AiExpectationRepository expRepo,
            LogicStatusRepository logicStatusRepo, ClsRepository clsRepo, FcpRepository fcpRepo, InpRepository inpRepo, TtfbRepository ttfbRepo, LcpRepository lcpRepo
    ) {
        this.geminiService = geminiService;
        this.recRepo = recRepo;
        this.expRepo = expRepo;
        this.logicStatusRepo = logicStatusRepo;

        // 보조지표 레포지토리들 사용하기 위해 아래 parameter들 추가함
        this.clsRepo = clsRepo;
        this.fcpRepo = fcpRepo;
        this.inpRepo = inpRepo;
        this.ttfbRepo = ttfbRepo;
        this.lcpRepo = lcpRepo;
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


        AiSavePayload payload;
        try {
            payload = objectMapper.readValue(resp.getText(), AiSavePayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("AI JSON parse failed. text=" + resp.getText(), e);
        }

        if (payload.recommendations != null) {
            for (var r : payload.recommendations) {
                String type = (r.type == null || r.type.isBlank()) ? "PERF" : r.type;
                recRepo.save(AiRecommendation.of(testId, type, r.metric, r.title, r.content));
            }
        }
        if (payload.expectations != null) {
            for (var e : payload.expectations) {
                expRepo.save(AiExpectation.of(testId, e.metric, e.content));
            }
        }

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
        logicStatusRepo.markAiTriggered(testId);

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







        return """
                You are an IT security expert. I want advice on web performance analysis.\s
                The LCP metric has a startTime of {1236}, a renderTime of {1236}, a size of {6496}, and the element is {h1.text-[30px].font-semibold}.\s
                The CLS metric has an entryType of {'layout-shift'}, startTime of {1062.8999999985099}, a value of {0.0055210489993099869}, hadRecentInput of {false}, and sources of {node: body, currentRect: { 'x': 0, 'y': 0, 'width': 2898, 'height': 1426, 'top': 0, 'right': 2898, 'bottom': 1426, 'left': 0 }, previousRect: { 'x': 16, 'y': 16, 'width': 2866, 'height': 1394, 'top': 16, 'right': 2882, 'bottom': 1410, 'left': 16 } }.\s
                The INP metric has an entryType of {'first-input'}, name of {'keydown'}, startTime of {7760.0999999996275}, duration of {48}, processingStart of {7794.199999999255}, processingEnd of {7800.799999998882}, interactionId of {9482}, and target of {body}.\s
                The FCP metric has an entryType of {paint}, and startTime of {1236}.\s
                The TTFB metric has an entryType of {navigation}, startTime of {0}, responseStart of {390.8999999985099}, requestStart of {77.69999999962747}, domainLookupStart of {77.09999999962747}, connectStart of {77.09999999962747}, and connectEnd of {77.39999999850988}.\s
                Regarding these LCP, CLS, INP, FCP, and TTFB metrics, please provide the following in JSON format for each metric: A one-line summary of the areas needing improvement, A list of potential improvements, The estimated score improvement, The expected benefits, related metrics. Give it to me strictly in dictionary format, without any introductory phrases.
                """.formatted(testId);
    }

}
