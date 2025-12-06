package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.ai.repository.AiAnalysisSummaryRepository;
import com.test.webtest.global.error.exception.AiCallFailedException;
import com.test.webtest.global.error.model.ErrorCode;
import com.test.webtest.global.longpoll.LongPollingManager;
import com.test.webtest.global.longpoll.LongPollingTopic;
import com.test.webtest.global.longpoll.WaitKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiPersistServiceTest {

    @Mock
    AiGeminiService geminiService;

    @Mock
    AiPromptBuilder promptBuilder;

    @Mock
    AiAnalysisSummaryRepository summaryRepository;

    @Mock
    LongPollingManager longPollingManager;

    @InjectMocks
    AiPersistService aiPersistService;

    @Test
    void invokeAsync_에서_AiCallFailedException_발생시_AI_READY_LongPoll에_AI_CALL_FAILED_응답을_보낸다() {
        // given
        UUID testId = UUID.randomUUID();

        // 아직 요약 데이터 없음 → 실제로 AI 호출 시도
        when(summaryRepository.findByTestId(testId)).thenReturn(Optional.empty());
        when(promptBuilder.buildPrompt(testId)).thenReturn("dummy-prompt");

        // Gemini 호출이 실패하도록 설정
        when(geminiService.generateWithSchema(anyString(), anyMap()))
                .thenThrow(new AiCallFailedException("테스트용 실패"));

        // when
        aiPersistService.invokeAsync(testId);

        // then
        // completeError 가 제대로 호출되었는지 캡쳐
        ArgumentCaptor<WaitKey> keyCaptor = ArgumentCaptor.forClass(WaitKey.class);
        ArgumentCaptor<ErrorCode> codeCaptor = ArgumentCaptor.forClass(ErrorCode.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        verify(longPollingManager).completeError(
                keyCaptor.capture(),
                codeCaptor.capture(),
                msgCaptor.capture()
        );

        WaitKey key = keyCaptor.getValue();
        ErrorCode code = codeCaptor.getValue();
        String msg = msgCaptor.getValue();

        assertThat(key.getTestId()).isEqualTo(testId);
        assertThat(key.getTopic()).isEqualTo(LongPollingTopic.AI_READY);
        assertThat(code).isEqualTo(ErrorCode.AI_CALL_FAILED);
        assertThat(msg).contains("테스트용 실패");
    }
}
