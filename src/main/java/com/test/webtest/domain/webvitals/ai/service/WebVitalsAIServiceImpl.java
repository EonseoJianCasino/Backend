package com.test.webtest.domain.webvitals.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.webtest.domain.test.entity.TestEntity;
import com.test.webtest.domain.test.repository.TestRepository;
import com.test.webtest.domain.webvitals.ai.dto.*;
import com.test.webtest.domain.webvitals.entity.*;
import com.test.webtest.domain.webvitals.repository.*;
import com.test.webtest.global.error.exception.BusinessException;
import com.test.webtest.global.error.model.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebVitalsAIServiceImpl implements WebVitalsAIService {

    private final TestRepository testRepository;
    private final FcpRepository fcpRepository;
    private final TtfbRepository ttfbRepository;
    private final LcpRepository lcpRepository;
    private final InpRepository inpRepository;
    private final ClsRepository clsRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void saveWebVitalsAI(UUID testId, WebVitalsAIRequest request) {
        // 1. TestEntity 조회
        TestEntity testEntity = testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TEST_NOT_FOUND,
                        "요청한 테스트가 존재하지 않습니다. id=" + testId
                ));

        // 2. FCP 저장
        if (request.getFcp() != null && request.getFcp().getEntries() != null 
                && !request.getFcp().getEntries().isEmpty()) {
            FcpDto.FcpEntry entry = request.getFcp().getEntries().get(0);
            
            // 기존 데이터 확인 후 업데이트 또는 신규 생성
            fcpRepository.findByTest_Id(testId)
                    .ifPresentOrElse(
                            existing -> existing.updateFrom(entry.getEntryType(), entry.getStartTime()),
                            () -> {
                                FcpEntity fcpEntity = FcpEntity.create(
                                        testEntity,
                                        entry.getEntryType(),
                                        entry.getStartTime()
                                );
                                fcpRepository.save(fcpEntity);
                            }
                    );
        }

        // 3. TTFB 저장
        if (request.getTtfb() != null && request.getTtfb().getEntries() != null 
                && !request.getTtfb().getEntries().isEmpty()) {
            TtfbDto.TtfbEntry entry = request.getTtfb().getEntries().get(0);
            
            ttfbRepository.findByTest_Id(testId)
                    .ifPresentOrElse(
                            existing -> existing.updateFrom(
                                    entry.getEntryType(),
                                    entry.getStartTime(),
                                    entry.getResponseStart(),
                                    entry.getRequestStart(),
                                    entry.getDomainLookupStart(),
                                    entry.getConnectStart(),
                                    entry.getConnectEnd()
                            ),
                            () -> {
                                TtfbEntity ttfbEntity = TtfbEntity.create(
                                        testEntity,
                                        entry.getEntryType(),
                                        entry.getStartTime(),
                                        entry.getResponseStart(),
                                        entry.getRequestStart(),
                                        entry.getDomainLookupStart(),
                                        entry.getConnectStart(),
                                        entry.getConnectEnd()
                                );
                                ttfbRepository.save(ttfbEntity);
                            }
                    );
        }

        // 4. LCP 저장
        if (request.getLcp() != null && request.getLcp().getEntries() != null 
                && !request.getLcp().getEntries().isEmpty()) {
            LcpDto.LcpEntry entry = request.getLcp().getEntries().get(0);
            
            lcpRepository.findByTest_Id(testId)
                    .ifPresentOrElse(
                            existing -> existing.updateFrom(
                                    entry.getStartTime(),
                                    entry.getRenderTime(),
                                    entry.getSize(),
                                    entry.getUrl()
                            ),
                            () -> {
                                LcpEntity lcpEntity = LcpEntity.create(
                                        testEntity,
                                        entry.getStartTime(),
                                        entry.getRenderTime(),
                                        entry.getSize(), // size -> renderedSize
                                        entry.getUrl()   // url -> element
                                );
                                lcpRepository.save(lcpEntity);
                            }
                    );
        }

        // 5. INP 저장
        if (request.getInp() != null && request.getInp().getEntries() != null 
                && !request.getInp().getEntries().isEmpty()) {
            InpDto.InpEntry entry = request.getInp().getEntries().get(0);
            
            inpRepository.findByTest_Id(testId)
                    .ifPresentOrElse(
                            existing -> existing.updateFrom(
                                    entry.getEntryType(),
                                    entry.getName(),
                                    entry.getStartTime(),
                                    entry.getDuration(),
                                    entry.getProcessingStart(),
                                    entry.getProcessingEnd(),
                                    entry.getInteractionId(),
                                    null // target 정보가 JSON에 없음
                            ),
                            () -> {
                                InpEntity inpEntity = InpEntity.create(
                                        testEntity,
                                        entry.getEntryType(),
                                        entry.getName(),
                                        entry.getStartTime(),
                                        entry.getDuration(),
                                        entry.getProcessingStart(),
                                        entry.getProcessingEnd(),
                                        entry.getInteractionId(),
                                        null // target 정보가 JSON에 없음
                                );
                                inpRepository.save(inpEntity);
                            }
                    );
        }

        // 6. CLS 저장
        if (request.getCls() != null && request.getCls().getEntries() != null 
                && !request.getCls().getEntries().isEmpty()) {
            ClsDto.ClsEntry entry = request.getCls().getEntries().get(0);
            
            // sources와 previousRect를 JSON 문자열로 변환
            String sourcesJson = null;
            String previousRectJson = null;
            
            try {
                if (entry.getSources() != null && !entry.getSources().isEmpty()) {
                    sourcesJson = objectMapper.writeValueAsString(entry.getSources());
                    
                    // previousRect는 sources의 첫 번째 요소에서 추출
                    if (entry.getSources().get(0).getPreviousRect() != null) {
                        previousRectJson = objectMapper.writeValueAsString(
                                entry.getSources().get(0).getPreviousRect()
                        );
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("CLS sources/previousRect JSON 변환 실패", e);
            }
            
            String finalSourcesJson = sourcesJson;
            String finalPreviousRectJson = previousRectJson;
            
            clsRepository.findByTest_Id(testId)
                    .ifPresentOrElse(
                            existing -> existing.updateFrom(
                                    entry.getEntryType(),
                                    entry.getStartTime(),
                                    entry.getValue(),
                                    entry.getHadRecentInput(),
                                    finalSourcesJson,
                                    finalPreviousRectJson
                            ),
                            () -> {
                                ClsEntity clsEntity = ClsEntity.create(
                                        testEntity,
                                        entry.getEntryType(),
                                        entry.getStartTime(),
                                        entry.getValue(),
                                        entry.getHadRecentInput(),
                                        finalSourcesJson,
                                        finalPreviousRectJson
                                );
                                clsRepository.save(clsEntity);
                            }
                    );
        }
    }

    @Override
    @Transactional
    public WebVitalsAIResponse getWebVitalsAI(UUID testId) {
        // 1. TestEntity 존재 여부 확인
        testRepository.findById(testId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TEST_NOT_FOUND,
                        "요청한 테스트가 존재하지 않습니다. id=" + testId
                ));

        // 2. 각 지표별 데이터 조회 및 Response 생성
        WebVitalsAIResponse.FcpResponse fcpResponse = fcpRepository.findByTest_Id(testId)
                .map(entity -> WebVitalsAIResponse.FcpResponse.builder()
                        .entryType(entity.getEntryType())
                        .startTime(entity.getStartTime())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .orElse(null);

        WebVitalsAIResponse.TtfbResponse ttfbResponse = ttfbRepository.findByTest_Id(testId)
                .map(entity -> WebVitalsAIResponse.TtfbResponse.builder()
                        .entryType(entity.getEntryType())
                        .startTime(entity.getStartTime())
                        .responseStart(entity.getResponseStart())
                        .requestStart(entity.getRequestStart())
                        .domainLookupStart(entity.getDomainLookupStart())
                        .connectStart(entity.getConnectStart())
                        .connectEnd(entity.getConnectEnd())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .orElse(null);

        WebVitalsAIResponse.LcpResponse lcpResponse = lcpRepository.findByTest_Id(testId)
                .map(entity -> WebVitalsAIResponse.LcpResponse.builder()
                        .startTime(entity.getStartTime())
                        .renderTime(entity.getRenderTime())
                        .renderedSize(entity.getRenderedSize())
                        .element(entity.getElement())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .orElse(null);

        WebVitalsAIResponse.InpResponse inpResponse = inpRepository.findByTest_Id(testId)
                .map(entity -> WebVitalsAIResponse.InpResponse.builder()
                        .entryType(entity.getEntryType())
                        .name(entity.getName())
                        .startTime(entity.getStartTime())
                        .duration(entity.getDuration())
                        .processingStart(entity.getProcessingStart())
                        .processingEnd(entity.getProcessingEnd())
                        .interactionId(entity.getInteractionId())
                        .target(entity.getTarget())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .orElse(null);

        WebVitalsAIResponse.ClsResponse clsResponse = clsRepository.findByTest_Id(testId)
                .map(entity -> WebVitalsAIResponse.ClsResponse.builder()
                        .entryType(entity.getEntryType())
                        .startTime(entity.getStartTime())
                        .clsValue(entity.getClsValue())
                        .hadRecentInput(entity.getHadRecentInput())
                        .sources(entity.getSources())
                        .previousRect(entity.getPreviousRect())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .orElse(null);

        return WebVitalsAIResponse.builder()
                .fcp(fcpResponse)
                .ttfb(ttfbResponse)
                .lcp(lcpResponse)
                .inp(inpResponse)
                .cls(clsResponse)
                .build();
    }
}

