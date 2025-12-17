-- =========================================
-- schema.sql (최신 스키마 - 테이블 정의만)
-- =========================================

DROP TABLE IF EXISTS ai_web_element_metric_delta CASCADE;
DROP TABLE IF EXISTS ai_web_element_related_metric CASCADE;
DROP TABLE IF EXISTS ai_web_element CASCADE;
DROP TABLE IF EXISTS ai_security_metric_related CASCADE;
DROP TABLE IF EXISTS ai_security_metric CASCADE;
DROP TABLE IF EXISTS ai_analysis_summary CASCADE;
DROP TABLE IF EXISTS ai_major_improvement CASCADE;
DROP TABLE IF EXISTS ai_top_priority CASCADE;

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
  web_sub_received boolean NOT NULL DEFAULT false,
  sec_received   boolean NOT NULL DEFAULT false,
  scores_ready   boolean NOT NULL DEFAULT false,
  ai_triggered   boolean NOT NULL DEFAULT false,
  ai_ready       boolean NOT NULL DEFAULT false,
  updated_at     timestamptz NOT NULL DEFAULT now(),
  created_at     timestamptz NOT NULL DEFAULT now()
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

-- ai_top_priority: 우선순위 TOP N (양호|주의|긴급)
CREATE TABLE IF NOT EXISTS ai_top_priority (
    id            BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    test_id       uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    rank          INT          NOT NULL,
    status        VARCHAR(20)  NULL,
    target_type   VARCHAR(50)  NULL,
    target_name   VARCHAR(100) NULL,
    reason        TEXT         NULL
);

-- ai_major_improvement: 핵심 개선 항목
CREATE TABLE IF NOT EXISTS ai_major_improvement (
    id          BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    test_id     uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    rank        INT          NOT NULL,
    metric      VARCHAR(50)  NULL,
    title       VARCHAR(50)  NULL,
    description VARCHAR(100) NULL
);

-- ai_web_element: 웹 요소별 개선 조언
CREATE TABLE IF NOT EXISTS ai_web_element (
    id                  BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    test_id             uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    element_name        VARCHAR(50)  NULL,
    status              VARCHAR(20)  NULL,
    benefit_summary     TEXT         NULL,
    expected_score_gain INT          NULL,
    benefit_detail      TEXT         NULL
);

-- ai_web_element_metric_delta: 웹 요소별 지표 변화
CREATE TABLE IF NOT EXISTS ai_web_element_metric_delta (
    id               BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    web_element_id   BIGINT       NOT NULL REFERENCES ai_web_element(id) ON DELETE CASCADE,
    metric           VARCHAR(20)  NULL,
    current_score    INT          NULL,
    achievable_score INT          NULL,
    delta            INT          NULL
);

-- ai_web_element_related_metric: 웹 요소 관련 지표
CREATE TABLE IF NOT EXISTS ai_web_element_related_metric (
    id             BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    web_element_id BIGINT       NOT NULL REFERENCES ai_web_element(id) ON DELETE CASCADE,
    ord            INT          NOT NULL,
    metric_text    VARCHAR(50)  NULL
);

-- ai_security_metric: 보안 지표별 개선 조언
CREATE TABLE IF NOT EXISTS ai_security_metric (
    id                  BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    test_id             uuid         NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    metric_name         VARCHAR(50)  NULL,
    status              VARCHAR(20)  NULL,
    benefit_summary     TEXT         NULL,
    delta               INT          NULL,
    expected_score_gain INT          NULL,
    benefit_detail      TEXT         NULL
);

-- ai_security_metric_related: 보안 지표 관련 지표
CREATE TABLE IF NOT EXISTS ai_security_metric_related (
    id                  BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    security_metric_id  BIGINT       NOT NULL REFERENCES ai_security_metric(id) ON DELETE CASCADE,
    ord                 INT          NOT NULL,
    metric_text         VARCHAR(50)  NULL
);
