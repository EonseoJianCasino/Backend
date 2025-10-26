package com.test.webtest.domain.logicstatus.service;

import com.test.webtest.domain.ai.service.AiRecommendationService;
import com.test.webtest.domain.logicstatus.repository.LogicStatusRepository;
import com.test.webtest.domain.scores.service.ScoresService;
import com.test.webtest.global.common.constants.Channel;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogicStatusServiceImpl {
    private final LogicStatusRepository repo;
    private final ScoresService scoresService;
    private final AiRecommendationService aiService;

    @Transactional
    public void onPartialUpdate(UUID testId, Channel channel) {
        switch(channel) {
            case WEB -> repo.markWebReceived(testId);
            case SECURITY -> repo.markSecReceived(testId);
        }

        // 점수 계산 (동기)
        boolean scoresMarked = markScoresReadyIfEligible(testId);
        if (scoresMarked) scoresService.calcAndSave(testId);

        // Ai 호출 (비동기)
        boolean aiMarked = markAiTriggeredIfEligible(testId);
        if (aiMarked) aiService.invokeAsync(testId);
    }

    private boolean markScoresReadyIfEligible(UUID testId) {
        List<Object[]> rows = repo.markScoresReady(testId);
        return !rows.isEmpty();
    }

    private boolean markAiTriggeredIfEligible(UUID testId) {
        List<Object[]> rows = repo.markAiTriggered(testId);
        return !rows.isEmpty();
    }
}
