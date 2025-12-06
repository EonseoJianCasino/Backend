-- =========================================
-- schema.sql (최신 스키마 - 테이블 정의만)
-- =========================================

DROP TABLE IF EXISTS ai_metric_benefit CASCADE;
DROP TABLE IF EXISTS ai_metric_improvement CASCADE;
DROP TABLE IF EXISTS ai_analysis_summary CASCADE;
DROP TABLE IF EXISTS ai_metric_related_metric CASCADE;
DROP TABLE IF EXISTS ai_major_improvement CASCADE;
DROP TABLE IF EXISTS ai_top_priority CASCADE;
DROP TABLE IF EXISTS ai_metric_advice CASCADE;

-- tests
CREATE TABLE IF NOT EXISTS tests (
  id          uuid PRIMARY KEY,
  domain_name varchar(255) NOT NULL,
  url         text NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- web_vitals
CREATE TABLE IF NOT EXISTS web_vitals (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  lcp        double precision,
  cls        double precision,
  fcp        double precision,
  ttfb       double precision,
  inp        double precision,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS lcp (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  start_time        int,
  render_time        int,
  rendered_size        int,
  element       text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cls (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  entry_type        varchar(30),
  start_time        double precision,
  cls_value        double precision,
  had_recent_input       boolean DEFAULT false,
  sources        text,
  previous_rect        text,
  created_at timestamptz NOT NULL DEFAULT now()
);


CREATE TABLE IF NOT EXISTS inp (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  entry_type        varchar(30),
  name        varchar(30),
  start_time        double precision,
  duration       int,
  processing_start        double precision,
  processing_end        double precision,
  interaction_id        int,
  target        varchar(30),
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS fcp (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  entry_type        varchar(30),
  start_time        int,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ttfb (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  entry_type        varchar(30),
  start_time        int,
  response_start        double precision,
  request_start       double precision,
  domain_lookup_start        double precision,
  connect_start        double precision,
  connect_end        double precision,
  created_at timestamptz NOT NULL DEFAULT now()
);


-- security_vitals
CREATE TABLE IF NOT EXISTS security_vitals (
  id                       uuid PRIMARY KEY,
  test_id                  uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  has_csp                  boolean DEFAULT false,
  has_hsts                 boolean DEFAULT false,
  x_frame_options          varchar(50),
  x_content_type_options   varchar(50),
  referrer_policy          varchar(50),
  hsts_max_age             bigint,
  hsts_include_subdomains  boolean,
  hsts_preload             boolean,
  csp_has_unsafe_inline    boolean,
  csp_has_unsafe_eval      boolean,
  csp_frame_ancestors      text,
  cookie_secure_all        boolean,
  cookie_httponly_all      boolean,
  cookie_samesite_policy   varchar(32),
  has_cookies              boolean,
  ssl_valid                boolean,
  ssl_chain_valid          boolean,
  ssl_days_remaining       int,
  ssl_issuer               text,
  ssl_subject              text,
  csp_raw                  text,
  hsts_raw                 text,
  created_at               timestamptz NOT NULL DEFAULT now()
);

-- scores
CREATE TABLE IF NOT EXISTS scores (
  id             uuid PRIMARY KEY,
  test_id        uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  total          int  NOT NULL, -- 0~100
  security_total int NOT NULL,
  web_total      int NOT NULL,
  lcp_score      int,
  cls_score      int,
  inp_score      int,
  fcp_score      int,
  ttfb_score     int,
  hsts_score               int,
  frame_ancestors_score    int,
  ssl_score                int,
  xcto_score               int,
  referrer_policy_score    int,
  cookies_score            int,
  csp_score                int,
  created_at   timestamptz NOT NULL DEFAULT now()
);


-- urgent_level -- GOOD, POOR, WARNING
CREATE TABLE IF NOT EXISTS urgent_level (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  lcp_status varchar(10),
  cls_status varchar(10),
  inp_status varchar(10),
  fcp_status varchar(10),
  ttfb_status varchar(10),
   hsts_status              varchar(10),
  frame_ancestors_status   varchar(20),
  ssl_status               varchar(10),
  xcto_status              varchar(10),
  referrer_policy_status   varchar(20),
  cookies_status           varchar(10),
  csp_status               varchar(10),
  created_at timestamptz NOT NULL DEFAULT now()
);

-- logic_status
CREATE TABLE IF NOT EXISTS logic_status (
  test_id        uuid PRIMARY KEY REFERENCES tests(id) ON DELETE CASCADE,
  web_received   boolean NOT NULL DEFAULT false,
  sec_received   boolean NOT NULL DEFAULT false,
  scores_ready   boolean NOT NULL DEFAULT false,
  ai_triggered   boolean NOT NULL DEFAULT false,
  ai_ready       boolean NOT NULL DEFAULT false,
  updated_at     timestamptz NOT NULL DEFAULT now(),
  created_at     timestamptz NOT NULL DEFAULT now()
);

-- ai_metric_advice: 각 메트릭(LCP, CLS...)에 대한 AI 조언 헤더
CREATE TABLE IF NOT EXISTS ai_metric_advice (
    id             uuid         NOT NULL PRIMARY KEY,              -- UUID
    test_id        uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    metric         varchar(20)  NOT NULL,                          -- 'LCP', 'CLS', 'INP', 'FCP', 'TTFB' ...
    summary        text         NULL,                              -- summary_of_improvement_areas
    estimated_label varchar(255) NULL,                             -- "점수 20점 향상 예상" 같은 문구
    created_at     timestamptz  NOT NULL DEFAULT now()
);

-- ai_metric_improvement: "구체적인 개선 방안" 리스트
CREATE TABLE IF NOT EXISTS ai_metric_improvement (
    id        BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    advice_id uuid         NOT NULL,          -- ai_metric_advice.id
    ord       INT          NOT NULL,          -- 리스트 내 순서 (0,1,2,...)
    text      TEXT         NOT NULL,
    CONSTRAINT fk_improvement_advice
        FOREIGN KEY (advice_id)
        REFERENCES ai_metric_advice(id)
        ON DELETE CASCADE
);

-- ai_metric_benefit: "이 개선을 하면 어떤 이득이 있는지" 리스트
CREATE TABLE IF NOT EXISTS ai_metric_benefit (
    id        BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    advice_id uuid         NOT NULL,          -- ai_metric_advice.id
    ord       INT          NOT NULL,
    text      TEXT         NOT NULL,
    CONSTRAINT fk_benefit_advice
        FOREIGN KEY (advice_id)
        REFERENCES ai_metric_advice(id)
        ON DELETE CASCADE
);

-- ai_metric_related_metric: 관련 메트릭(FCP, TTFB 등)
CREATE TABLE IF NOT EXISTS ai_metric_related_metric (
    id          BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    advice_id   uuid         NOT NULL,         -- ai_metric_advice.id
    ord         INT          NOT NULL,
    metric_text VARCHAR(50)  NOT NULL,         -- "FCP", "TTFB", "FID", ...
    CONSTRAINT fk_related_metric_advice
        FOREIGN KEY (advice_id)
        REFERENCES ai_metric_advice(id)
        ON DELETE CASCADE
);

-- ai_analysis_summary: 전체 분석 요약
CREATE TABLE IF NOT EXISTS ai_analysis_summary (
    id                           uuid         NOT NULL PRIMARY KEY,
    test_id                      uuid         NOT NULL UNIQUE
                                              REFERENCES tests(id) ON DELETE CASCADE,
    overall_expected_improvement INT          NOT NULL,
    overall_total_after          INT          NULL,
    created_at                   timestamptz  NOT NULL DEFAULT now()
);

-- ai_major_improvement: 가장 큰 개선 포인트 (핵심 개선 항목)
CREATE TABLE IF NOT EXISTS ai_major_improvement (
    id          BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    test_id     uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    ord         INT          NOT NULL,
    metric      VARCHAR(50)  NULL,
    title       VARCHAR(50)  NULL,
    description VARCHAR(100) NULL
);

-- ai_top_priority: 우선순위 TOP N
CREATE TABLE IF NOT EXISTS ai_top_priority (
    id            BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    test_id       uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    rank          INT          NOT NULL,
    target_type   VARCHAR(50)  NULL,
    target_name   VARCHAR(100) NULL,
    expected_gain INT          NOT NULL,
    reason        TEXT         NULL
);

