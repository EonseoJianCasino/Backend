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
  lcp_status   varchar(10), -- GOOD, WARNING, URGENT
  cls_status   varchar(10),
  inp_status   varchar(10),
  fcp_status   varchar(10),
  ttfb_status  varchar(10),
  created_at   timestamptz NOT NULL DEFAULT now()
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

-- ai_recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
  id            uuid PRIMARY KEY,
  test_id       uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  type          varchar(20) NOT NULL, -- PERF / SEC
  metric        varchar(50),
  title         varchar(200) NOT NULL,
  content       text NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now()
);

-- ai_expectations
CREATE TABLE IF NOT EXISTS ai_expectations (
  id           uuid PRIMARY KEY,
  test_id      uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  metric       varchar(50) NOT NULL, -- LCP, CSP, INP...
  content      text NOT NULL,
  created_at   timestamptz NOT NULL DEFAULT now()
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
