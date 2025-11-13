package com.test.webtest.domain.priorities.service;

import com.test.webtest.domain.priorities.dto.PrioritiesResponse;
import com.test.webtest.domain.priorities.dto.PriorityDto;
import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.securityvitals.service.SecurityMessageService;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.urgentlevel.entity.UrgentLevelEntity;
import com.test.webtest.domain.urgentlevel.repository.UrgentLevelRepository;
import com.test.webtest.domain.webvitals.entity.WebVitalsEntity;
import com.test.webtest.domain.webvitals.repository.WebVitalsRepository;
import com.test.webtest.global.common.util.ScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrioritiesServiceImpl 테스트")
class PrioritiesServiceImplTest {

    @Mock
    private ScoreCalculator scoreCalculator;
    @Mock
    private WebVitalsRepository webRepository;
    @Mock
    private SecurityVitalsRepository securityRepository;
    @Mock
    private SecurityMessageService securityMessageService;
    @Mock
    private ScoresRepository scoresRepository;
    @Mock
    private UrgentLevelRepository urgentLevelRepository;

    @InjectMocks
    private PrioritiesServiceImpl prioritiesService;

    private UUID testId;
    private TestEntity testEntity;
    private ScoresEntity scoresEntity;
    private UrgentLevelEntity urgentLevelEntity;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testEntity = TestEntity.create("https://example.com");
        
        // ScoresEntity 생성 (점수만 포함)
        scoresEntity = ScoresEntity.create(
                testEntity,
                75,  // total
                80,  // lcp
                70,  // cls
                60,  // inp
                90,  // fcp
                85   // ttfb
        );

        // UrgentLevelEntity 생성 (status만 포함)
        urgentLevelEntity = UrgentLevelEntity.create(
                testEntity,
                "GOOD",    // lcpStatus
                "POOR",    // clsStatus
                "WARNING", // inpStatus
                "GOOD",    // fcpStatus
                "POOR"     // ttfbStatus
        );
    }

    @Test
    @DisplayName("urgentLevel에서 status를 정상적으로 조회하는지 테스트")
    void testGetBottom3_WithUrgentLevel() {
        // given
        when(scoresRepository.findByTestId(testId)).thenReturn(Optional.of(scoresEntity));
        when(securityRepository.findByTest_Id(testId)).thenReturn(Optional.empty());
        when(webRepository.findByTest_Id(testId)).thenReturn(Optional.empty());
        when(urgentLevelRepository.findByTestId(testId)).thenReturn(Optional.of(urgentLevelEntity));
        
        // bottom3가 LCP, CLS, INP를 반환하도록 설정 (점수가 낮은 순서)
        when(scoreCalculator.bottom3(any(), any()))
                .thenReturn(java.util.List.of("INP", "CLS", "TTFB"));

        // when
        PrioritiesResponse response = prioritiesService.getBottom3(testId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTestId()).isEqualTo(testId);
        assertThat(response.getPriorities()).hasSize(3);
        assertThat(response.getTotalCount()).isEqualTo(3);
        
        // urgentLevel이 정상적으로 포함되는지 확인
        PriorityDto inpPriority = response.getPriorities().get(0);
        assertThat(inpPriority.getMetric()).isEqualTo("INP");
        assertThat(inpPriority.getUrgentLevel()).isEqualTo("WARNING");
        
        PriorityDto clsPriority = response.getPriorities().get(1);
        assertThat(clsPriority.getMetric()).isEqualTo("CLS");
        assertThat(clsPriority.getUrgentLevel()).isEqualTo("POOR");
        
        PriorityDto ttfbPriority = response.getPriorities().get(2);
        assertThat(ttfbPriority.getMetric()).isEqualTo("TTFB");
        assertThat(ttfbPriority.getUrgentLevel()).isEqualTo("POOR");
    }

    @Test
    @DisplayName("urgentLevel이 null일 때도 정상 동작하는지 테스트")
    void testGetBottom3_WithoutUrgentLevel() {
        // given
        when(scoresRepository.findByTestId(testId)).thenReturn(Optional.of(scoresEntity));
        when(securityRepository.findByTest_Id(testId)).thenReturn(Optional.empty());
        when(webRepository.findByTest_Id(testId)).thenReturn(Optional.empty());
        when(urgentLevelRepository.findByTestId(testId)).thenReturn(Optional.empty());
        
        when(scoreCalculator.bottom3(any(), any()))
                .thenReturn(java.util.List.of("LCP", "CLS", "INP"));

        // when
        PrioritiesResponse response = prioritiesService.getBottom3(testId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPriorities()).hasSize(3);
        
        // urgentLevel이 null인지 확인
        PriorityDto lcpPriority = response.getPriorities().get(0);
        assertThat(lcpPriority.getUrgentLevel()).isNull();
    }
}

