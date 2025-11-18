-- =========================================
-- schema.sql (최신 스키마 - 테이블 정의만)
-- =========================================

-- tests
CREATE TABLE IF NOT EXISTS tests (
  id          uuid PRIMARY KEY,
  domain_name varchar(255) NOT NULL,
  url         text NOT NULL,
  status      varchar(20) NOT NULL,
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
  id           uuid PRIMARY KEY,
  test_id      uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  total        int  NOT NULL, -- 0~100
  lcp_score    int,
  cls_score    int,
  inp_score    int,
  fcp_score    int,
  ttfb_score   int,
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
-- priorities
CREATE TABLE IF NOT EXISTS priorities (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  type       varchar(20) NOT NULL, -- PERFORMANCE / SECURITY
  metric     varchar(50) NOT NULL,
  reason     text,
  rank       int NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

---- ai_recommendations
--CREATE TABLE IF NOT EXISTS ai_recommendations (
--  id            uuid PRIMARY KEY,
--  test_id       uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
--  type          varchar(20) NOT NULL, -- PERF / SEC
--  metric        varchar(50),
--  title         varchar(200) NOT NULL,
--  content       text NOT NULL,
--  created_at    timestamptz NOT NULL DEFAULT now()
--);
--
---- ai_expectations
--CREATE TABLE IF NOT EXISTS ai_expectations (
--  id           uuid PRIMARY KEY,
--  test_id      uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
--  metric       varchar(50) NOT NULL, -- LCP, CSP, INP...
--  content      text NOT NULL,
--  created_at   timestamptz NOT NULL DEFAULT now()
--);


CREATE TABLE ai_metric_advice (
    id           CHAR(36)    NOT NULL PRIMARY KEY,    -- UUID
    test_id      CHAR(36)    NOT NULL,                -- 테스트 ID (FK)
    metric       VARCHAR(20) NOT NULL,                -- 'LCP', 'CLS', 'INP', 'FCP', 'TTFB'
    summary      TEXT        NULL,                    -- summary_of_improvement_areas
    estimated_label VARCHAR(255) NULL,                -- estimated_score_improvement 원문
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_metric_improvement (
    id        BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    advice_id CHAR(36)    NOT NULL,           -- ai_metric_advice.id (UUID)
    ord       INT         NOT NULL,           -- 리스트 내 순서 (0,1,2,...)
    text      TEXT        NOT NULL,
    CONSTRAINT fk_improvement_advice
        FOREIGN KEY (advice_id) REFERENCES ai_metric_advice(id)
);


CREATE TABLE ai_metric_benefit (
    id        BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    advice_id CHAR(36)    NOT NULL,
    ord       INT         NOT NULL,
    text      TEXT        NOT NULL,
    CONSTRAINT fk_benefit_advice
        FOREIGN KEY (advice_id) REFERENCES ai_metric_advice(id)
);

CREATE TABLE ai_metric_related_metric (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    advice_id   CHAR(36)    NOT NULL,
    ord         INT         NOT NULL,
    metric_text VARCHAR(50) NOT NULL, -- "FCP", "TTFB", "FID", ...
    CONSTRAINT fk_related_metric_advice
        FOREIGN KEY (advice_id) REFERENCES ai_metric_advice(id)
);

-- ai_analysis_summary
CREATE TABLE IF NOT EXISTS ai_analysis_summary (
    id                          CHAR(36)    NOT NULL PRIMARY KEY,
    test_id                     CHAR(36)    NOT NULL UNIQUE,
    overall_expected_improvement INT        NOT NULL,
    web_total_after             INT         NULL,
    security_total_after        INT         NULL,
    overall_total_after         INT         NULL,
    created_at                  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_summary_test
        FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

-- ai_major_improvement
CREATE TABLE IF NOT EXISTS ai_major_improvement (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    summary_id  CHAR(36)    NOT NULL,
    ord         INT         NOT NULL,
    metric      VARCHAR(50) NULL,
    title       VARCHAR(50) NULL,
    description VARCHAR(100) NULL,
    CONSTRAINT fk_major_improvement_summary
        FOREIGN KEY (summary_id) REFERENCES ai_analysis_summary(id) ON DELETE CASCADE
);

-- ai_top_priority
CREATE TABLE IF NOT EXISTS ai_top_priority (
    id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    summary_id   CHAR(36)    NOT NULL,
    rank         INT         NOT NULL,
    target_type  VARCHAR(50) NULL,
    target_name  VARCHAR(100) NULL,
    expected_gain INT        NOT NULL,
    reason       TEXT        NULL,
    CONSTRAINT fk_top_priority_summary
        FOREIGN KEY (summary_id) REFERENCES ai_analysis_summary(id) ON DELETE CASCADE
);

-- logic_status
CREATE TABLE IF NOT EXISTS logic_status (
  test_id        uuid PRIMARY KEY REFERENCES tests(id) ON DELETE CASCADE,
  web_received   boolean NOT NULL DEFAULT false,
  sec_received   boolean NOT NULL DEFAULT false,
  scores_ready   boolean NOT NULL DEFAULT false,
  ai_triggered   boolean NOT NULL DEFAULT false,
  updated_at     timestamptz NOT NULL DEFAULT now(),
  created_at     timestamptz NOT NULL DEFAULT now()
);
