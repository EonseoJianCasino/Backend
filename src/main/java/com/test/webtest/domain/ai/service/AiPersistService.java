package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.AiSavePayload;
import com.test.webtest.domain.ai.dto.AiResponse;
import com.test.webtest.domain.ai.service.*;
import com.test.webtest.domain.ai.entity.airecommendationentity.*;
import com.test.webtest.domain.logicstatus.entity.LogicStatusEntity;
import com.test.webtest.domain.logicstatus.entity.LogicStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.test.webtest.domain.ai.schema.AiSchemas.buildPerfAdviceSchema;

import java.util.UUID;


@Service
public class AiPersistService {

    private final AiRecommendationService geminiService;
    private final AiRecommendationRepository recRepo;
    private final AiExpectationRepository expRepo;
    private final LogicStatusRepository logicStatusRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiPersistService(
            AiRecommendationService geminiService,
            AiRecommendationRepository recRepo,
            AiExpectationRepository expRepo,
            LogicStatusRepository logicStatusRepo
    ) {
        this.geminiService = geminiService;
        this.recRepo = recRepo;
        this.expRepo = expRepo;
        this.logicStatusRepo = logicStatusRepo;
    }

    @Transactional
    public void generateAndSave(UUID testId, String promptText) {
//        AiResponse resp = geminiService.generate(
//                wrapAsJsonInstruction(promptText),
//                null, null, true
//        );

        AiResponse resp = geminiService.generateWithSchema(promptText, buildPerfAdviceSchema());


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

    private String wrapAsJsonInstruction(String userPrompt) {
        return """
        You are a web performance & security expert.
        Return STRICT JSON with two arrays: "recommendations" and "expectations".
        Respond with ONLY JSON. No preface.

        INPUT:
        """ + userPrompt;
    }

}
