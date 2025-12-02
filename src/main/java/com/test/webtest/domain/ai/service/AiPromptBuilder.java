package com.test.webtest.domain.ai.service;

import com.test.webtest.domain.scores.entity.ScoresEntity;
import com.test.webtest.domain.scores.repository.ScoresRepository;
import com.test.webtest.domain.securityvitals.entity.SecurityVitalsEntity;
import com.test.webtest.domain.securityvitals.repository.SecurityVitalsRepository;
import com.test.webtest.domain.webvitals.entity.*;
import com.test.webtest.domain.webvitals.repository.*;
import com.test.webtest.global.common.util.ScoreCalculator;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AiPromptBuilder {

    private final ClsRepository clsRepo;
    private final FcpRepository fcpRepo;
    private final InpRepository inpRepo;
    private final TtfbRepository ttfbRepo;
    private final LcpRepository lcpRepo;
    private final WebVitalsRepository webVitalsRepo;
    private final SecurityVitalsRepository securityVitalsRepo;
    private final ScoresRepository scoresRepo;
    private final ScoreCalculator scoreCalculator;

    public AiPromptBuilder(
            ClsRepository clsRepo,
            FcpRepository fcpRepo,
            InpRepository inpRepo,
            TtfbRepository ttfbRepo,
            LcpRepository lcpRepo,
            WebVitalsRepository webVitalsRepo,
            SecurityVitalsRepository securityVitalsRepo,
            ScoresRepository scoresRepo,
            ScoreCalculator scoreCalculator) {
        this.clsRepo = clsRepo;
        this.fcpRepo = fcpRepo;
        this.inpRepo = inpRepo;
        this.ttfbRepo = ttfbRepo;
        this.lcpRepo = lcpRepo;
        this.webVitalsRepo = webVitalsRepo;
        this.securityVitalsRepo = securityVitalsRepo;
        this.scoresRepo = scoresRepo;
        this.scoreCalculator = scoreCalculator;
    }

    public String buildPrompt(UUID testId) {
        Optional<ClsEntity> clsResults = clsRepo.findByTest_Id(testId);
        Optional<FcpEntity> fcpResults = fcpRepo.findByTest_Id(testId);
        Optional<InpEntity> inpResults = inpRepo.findByTest_Id(testId);
        Optional<LcpEntity> lcpResults = lcpRepo.findByTest_Id(testId);
        Optional<TtfbEntity> ttfbResults = ttfbRepo.findByTest_Id(testId);
        Optional<WebVitalsEntity> webVitals = webVitalsRepo.findByTest_Id(testId);
        Optional<SecurityVitalsEntity> securityVitals = securityVitalsRepo.findByTest_Id(testId);
        Optional<ScoresEntity> scores = scoresRepo.findByTestId(testId);

        return buildPromptFromDb(
                testId,
                clsResults, fcpResults, inpResults, lcpResults, ttfbResults,
                webVitals, securityVitals, scores);
    }

    private String buildPromptFromDb(
            UUID testId,
            Optional<ClsEntity> clsResults,
            Optional<FcpEntity> fcpResults,
            Optional<InpEntity> inpResults,
            Optional<LcpEntity> lcpResults,
            Optional<TtfbEntity> ttfbResults,
            Optional<WebVitalsEntity> webVitals,
            Optional<SecurityVitalsEntity> securityVitals,
            Optional<ScoresEntity> scores) {
        String lcpStartTime = lcpResults.map(LcpEntity::getStartTime).map(Object::toString).orElse("N/A");
        String lcpRenderTime = lcpResults.map(LcpEntity::getRenderTime).map(Object::toString).orElse("N/A");
        String lcpSize = lcpResults.map(LcpEntity::getRenderedSize).map(Object::toString).orElse("N/A");
        String lcpElement = lcpResults.map(LcpEntity::getElement).orElse("N/A");

        String clsEntryType = clsResults.map(ClsEntity::getEntryType).orElse("N/A");
        String clsStartTime = clsResults.map(ClsEntity::getStartTime).map(Object::toString).orElse("N/A");
        String clsValue = clsResults.map(ClsEntity::getClsValue).map(Object::toString).orElse("N/A");
        String clsHadRecentInp = clsResults.map(ClsEntity::getHadRecentInput).map(Object::toString).orElse("N/A");
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
        String ttfbRequestStart = ttfbResults.map(TtfbEntity::getRequestStart).map(Object::toString).orElse("N/A");
        String ttfbDnsStart = ttfbResults.map(TtfbEntity::getDomainLookupStart).map(Object::toString).orElse("N/A");
        String ttfbConnectStart = ttfbResults.map(TtfbEntity::getConnectStart).map(Object::toString).orElse("N/A");
        String ttfbConnectEnd = ttfbResults.map(TtfbEntity::getConnectEnd).map(Object::toString).orElse("N/A");

        ScoreCalculator.WebScores webScores = webVitals.map(scoreCalculator::toWebScores)
                .orElse(new ScoreCalculator.WebScores(0, 0, 0, 0, 0));
        ScoreCalculator.SecurityScores secScores = securityVitals.map(scoreCalculator::toSecurityScores)
                .orElse(new ScoreCalculator.SecurityScores(0, 0, 0, 0, 0, 0, 0));

        int lcpCurrent = scores.map(ScoresEntity::getLcpScore).orElse(webScores.lcp());
        int clsCurrent = scores.map(ScoresEntity::getClsScore).orElse(webScores.cls());
        int inpCurrent = scores.map(ScoresEntity::getInpScore).orElse(webScores.inp());
        int fcpCurrent = scores.map(ScoresEntity::getFcpScore).orElse(webScores.fcp());
        int ttfbCurrent = scores.map(ScoresEntity::getTtfbScore).orElse(webScores.ttfb());

        int hstsCurrent = scores.map(ScoresEntity::getHstsScore).orElse(secScores.hsts());
        int frameAncestorsCurrent = scores.map(ScoresEntity::getFrameAncestorsScore)
                .orElse(secScores.frameAncestorsOrXfo());
        int sslCurrent = scores.map(ScoresEntity::getSslScore).orElse(secScores.ssl());
        int xctoCurrent = scores.map(ScoresEntity::getXctoScore).orElse(secScores.xcto());
        int referrerPolicyCurrent = scores.map(ScoresEntity::getReferrerPolicyScore).orElse(secScores.referrerPolicy());
        int cookiesCurrent = scores.map(ScoresEntity::getCookiesScore).orElse(secScores.cookies());
        int cspCurrent = scores.map(ScoresEntity::getCspScore).orElse(secScores.csp());

        int currentTotal = scores.map(ScoresEntity::getTotal).orElse(0);

        String securityStatusInfo = buildSecurityStatusInfo(
                securityVitals,
                hstsCurrent, frameAncestorsCurrent, sslCurrent, xctoCurrent,
                referrerPolicyCurrent, cookiesCurrent, cspCurrent);

        return String.format(
                """
                        You are a performance & security optimization planner.
                        Respond STRICTLY in Korean, return ONLY valid JSON matching the schema below.
                        Every numeric field must be an integer. Do not include explanatory prose.

                        [INPUT DATA]
                        - Test ID: %s
                        - Current total score (0~100): %d

                        - Web metrics (raw readings, thresholds, current scores, remaining room):
                          * LCP: startTime=%s, renderTime=%s, size=%s, element=%s
                            Threshold: good<=2500ms, poor>=4000ms
                            Current score: %d, Remaining room: %d
                          * CLS: entryType=%s, startTime=%s, value=%s, hadRecentInput=%s, sources=%s
                            Threshold: good<=0.10, poor>=0.25
                            Current score: %d, Remaining room: %d
                          * INP: entryType=%s, name=%s, startTime=%s, duration=%s, processingStart=%s, processingEnd=%s, interactionId=%s, target=%s
                            Threshold: good<=200ms, poor>=500ms
                            Current score: %d, Remaining room: %d
                          * FCP: entryType=%s, startTime=%s
                            Threshold: good<=1800ms, poor>=3000ms
                            Current score: %d, Remaining room: %d
                          * TTFB: entryType=%s, startTime=%s, responseStart=%s, requestStart=%s, domainLookupStart=%s, connectStart=%s, connectEnd=%s
                            Threshold: good<=800ms, poor>=1800ms
                            Current score: %d, Remaining room: %d

                        - Security metrics (header states, current scores, remaining room):
                          %s
                          * Scoring rules:
                            · HSTS: 100 if max-age≥15768000 AND includeSubDomains, 50 if <2592000 or missing subdomains, else 0.
                            · FRAME-ANCESTORS / X-Frame-Options: 100 if CSP frame-ancestors contains 'none'/'self'/https origin or XFO DENY/SAMEORIGIN, else 0.
                            · SSL: 100 when sslValid & sslChainValid & days≥90, 70 if ≥30, else 0.
                            · X-Content-Type-Options: 100 only for "nosniff".
                            · Referrer-Policy: 100 for "no-referrer" or "strict-origin-when-cross-origin", 50 for origin/same-origin/origin-when-cross-origin/strict-origin/no-referrer-when-downgrade, else 0.
                            · Cookies: count satisfied {Secure, HttpOnly, SameSite strict/lax}. 3→100, 2→70, 1→40, otherwise 0. SameSite=None without Secure → 0.
                            · CSP: 100 with CSP and no unsafe-inline/eval, 50 if unsafe-inline/eval present, 0 if CSP missing.

                        - Improvement budget: each metric can increase up to 100. Provide `achievable_score` per metric (>= current, <=100).

                        [SCORING MODEL]
                        - Web half-score (0~50) weights: 9(LCP)+9(CLS)+8(INP)+8(FCP)+8(TTFB). Each contribution = weight * (achievable_score/100). Sum (max 42) must be scaled by 50/42 and rounded.
                        - Security half-score (0~50) weights: 7(HSTS)+7(FRAME-ANCESTORS/XFO)+8(SSL)+7(XCTO)+7(REFERRER-POLICY)+7(COOKIES)+7(CSP). Sum weight*(achievable/100) and round.
                        - `overall_total_after = min(100, web_total_after + security_total_after)`.

                        [OUTPUT SCHEMA]
                        {
                          "overall_expected_improvement": int,
                          "web_elements": [
                            {
                              "element_name": "string<=15chars",
                              "expected_gain": int,
                              "related_metrics": ["LCP", ...],
                              "metric_deltas": [
                                {
                                  "metric": "LCP|CLS|INP|FCP|TTFB",
                                  "current_score": int,
                                  "achievable_score": int,
                                  "delta": int
                                }
                              ],
                              "detailed_plan": "세부 실행 방안(한글)",
                              "benefit_summary": "예상 효과(한글)"
                            }
                          ],
                          "security_metrics": [
                            {
                              "metric": "HSTS|FRAME-ANCESTORS|SSL|XCTO|REFERRER-POLICY|COOKIES|CSP",
                              "current_score": int,
                              "achievable_score": int,
                              "delta": int,
                              "expected_gain": int,
                              "improvement_plan": "세부 방안(한글)",
                              "expected_benefit": "효과 설명(한글)",
                              "impact_title": "≤10자",
                              "impact_description": "≤20자"
                            }
                          ],
                          "normalization": {
                            "web_total_after": int,
                            "security_total_after": int,
                            "overall_total_after": int
                          },
                          "major_improvements": [
                            {
                              "metric": "지표명",
                              "title": "≤10자",
                              "description": "≤20자"
                            }
                          ],
                          "top_priorities": [
                            {
                              "rank": 1,
                              "target_type": "WEB_ELEMENT|SECURITY_METRIC",
                              "target_name": "요소/지표명",
                              "expected_gain": int,
                              "reason": "간결한 우선순위 사유(한글)"
                            }
                          ]
                        }

                        [INSTRUCTIONS]
                        1. Web 항목은 요소 단위로, Security 항목은 지표 단위로 작성한다.
                        2. 모든 delta는 metric별 남은 여유(100-current)를 넘지 말고, 동일 지표에 대한 여러 항목의 delta 총합이 remaining을 초과하지 않게 관리한다.
                        3. `overall_expected_improvement`는 웹·보안 모든 지표 delta를 합산한 값이다.
                        4. Normalization 값들은 achievable_score 기반으로 ScoreCalculator 규칙을 적용해 계산한다.
                        5. `impact_title`와 `major_improvements[].title`은 10자 이내, 설명은 20자 이내로 한다.
                        6. `top_priorities`는 웹 요소와 보안 지표를 통틀어 영향도가 가장 큰 3가지를 선택한다. expected_gain은 해당 대상이 기여하는 총 delta를 사용한다.
                        7. JSON 외의 텍스트 금지, 모든 서술은 한국어로 작성하되 평가 기준·필드명 등은 스펙을 유지한다.
                        """,
                testId.toString(),
                currentTotal,
                lcpStartTime, lcpRenderTime, lcpSize, lcpElement, lcpCurrent, Math.max(0, 100 - lcpCurrent),
                clsEntryType, clsStartTime, clsValue, clsHadRecentInp, sources, clsCurrent,
                Math.max(0, 100 - clsCurrent),
                inpEntryType, inpName, inpStartTime, inpDuration, inpProcStart, inpProcEnd, inpInteractionId, inpTarget,
                inpCurrent, Math.max(0, 100 - inpCurrent),
                fcpEntryType, fcpStartTime, fcpCurrent, Math.max(0, 100 - fcpCurrent),
                ttfbEntryType, ttfbStartTime, ttfbResponseStart, ttfbRequestStart, ttfbDnsStart, ttfbConnectStart,
                ttfbConnectEnd,
                ttfbCurrent, Math.max(0, 100 - ttfbCurrent),
                securityStatusInfo);
    }

    private String buildSecurityStatusInfo(
            Optional<SecurityVitalsEntity> securityVitals,
            int hstsCurrent, int frameAncestorsCurrent, int sslCurrent, int xctoCurrent,
            int referrerPolicyCurrent, int cookiesCurrent, int cspCurrent) {
        if (securityVitals.isEmpty()) {
            return String.format("""
                    * HSTS: Current score: %d, Remaining room: %d
                    * FRAME-ANCESTORS: Current score: %d, Remaining room: %d
                    * SSL: Current score: %d, Remaining room: %d
                    * XCTO: Current score: %d, Remaining room: %d
                    * REFERRER-POLICY: Current score: %d, Remaining room: %d
                    * COOKIES: Current score: %d, Remaining room: %d
                    * CSP: Current score: %d, Remaining room: %d""",
                    hstsCurrent, Math.max(0, 100 - hstsCurrent),
                    frameAncestorsCurrent, Math.max(0, 100 - frameAncestorsCurrent),
                    sslCurrent, Math.max(0, 100 - sslCurrent),
                    xctoCurrent, Math.max(0, 100 - xctoCurrent),
                    referrerPolicyCurrent, Math.max(0, 100 - referrerPolicyCurrent),
                    cookiesCurrent, Math.max(0, 100 - cookiesCurrent),
                    cspCurrent, Math.max(0, 100 - cspCurrent));
        }

        SecurityVitalsEntity sec = securityVitals.get();

        return String.format("""
                * HSTS: hasHsts=%s, maxAge=%s, includeSubdomains=%s
                  Current score: %d, Remaining room: %d
                * FRAME-ANCESTORS: cspFrameAncestors=%s, xFrameOptions=%s
                  Current score: %d, Remaining room: %d
                * SSL: sslValid=%s, sslChainValid=%s, daysRemaining=%s
                  Current score: %d, Remaining room: %d
                * XCTO: xContentTypeOptions=%s
                  Current score: %d, Remaining room: %d
                * REFERRER-POLICY: referrerPolicy=%s
                  Current score: %d, Remaining room: %d
                * COOKIES: secureAll=%s, httpOnlyAll=%s, sameSitePolicy=%s
                  Current score: %d, Remaining room: %d
                * CSP: hasCsp=%s, hasUnsafeInline=%s, hasUnsafeEval=%s
                  Current score: %d, Remaining room: %d""",
                sec.getHasHsts(), sec.getHstsMaxAge(), sec.getHstsIncludeSubdomains(),
                hstsCurrent, Math.max(0, 100 - hstsCurrent),
                sec.getCspFrameAncestors(), sec.getXFrameOptions(),
                frameAncestorsCurrent, Math.max(0, 100 - frameAncestorsCurrent),
                sec.getSslValid(), sec.getSslChainValid(), sec.getSslDaysRemaining(),
                sslCurrent, Math.max(0, 100 - sslCurrent),
                sec.getXContentTypeOptions(),
                xctoCurrent, Math.max(0, 100 - xctoCurrent),
                sec.getReferrerPolicy(),
                referrerPolicyCurrent, Math.max(0, 100 - referrerPolicyCurrent),
                sec.getCookieSecureAll(), sec.getCookieHttpOnlyAll(), sec.getCookieSameSitePolicy(),
                cookiesCurrent, Math.max(0, 100 - cookiesCurrent),
                sec.getHasCsp(), sec.getCspHasUnsafeInline(), sec.getCspHasUnsafeEval(),
                cspCurrent, Math.max(0, 100 - cspCurrent));
    }
}
