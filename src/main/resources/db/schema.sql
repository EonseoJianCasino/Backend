-- =========================================
-- schema.sql (idempotent & safe)
-- =========================================

-- tests
CREATE TABLE IF NOT EXISTS tests (
  id          uuid PRIMARY KEY,
  domain_name varchar(255) NOT NULL,
  url         text NOT NULL,
  status      varchar(20) NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- web_vitals (공유 PK가 아니라면 UNIQUE 제약)
CREATE TABLE IF NOT EXISTS web_vitals (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL UNIQUE REFERENCES tests(id) ON DELETE CASCADE,
  lcp        double precision,
  fid        double precision,
  cls        double precision,
  fcp        double precision,
  ttfb       double precision,
  inp        double precision,
  tbt        double precision,
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
  created_at               timestamptz NOT NULL DEFAULT now()
);

-- scores: 구조가 바뀌었으므로 안전하게 교체
DROP TABLE IF EXISTS scores CASCADE;
CREATE TABLE scores (
  id           uuid PRIMARY KEY,
  test_id      uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  total        int  NOT NULL, -- 0~100
  lcp_score    int,
  cls_score    int,
  inp_score    int,
  fcp_score    int,
  tbt_score    int,
  ttfb_score   int,
  created_at   timestamptz NOT NULL DEFAULT now()
);

-- priorities (스크립트 산출 결과)
CREATE TABLE IF NOT EXISTS priorities (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  type       varchar(20) NOT NULL, -- PERFORMANCE / SECURITY
  metric     varchar(50) NOT NULL,
  reason     text,
  rank       int NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- ai_recommendations (분리 설계 + priority 연결)
DROP TABLE IF EXISTS ai_recommendations CASCADE;
CREATE TABLE ai_recommendations (
  id            uuid PRIMARY KEY,
  test_id          uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  type             varchar(20)  NOT NULL,       -- PERF / SEC
  metric           varchar(50),
  title            varchar(200) NOT NULL,
  content          text         NOT NULL,
  created_at       timestamptz NOT NULL DEFAULT now()
);

-- ai_expectations (기대효과)
DROP TABLE IF EXISTS ai_expectations CASCADE;
CREATE TABLE ai_expectations (
  id      uuid PRIMARY KEY,
  test_id     uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  metric      varchar(50) NOT NULL, -- LCP, CSP, INP...
  content     text        NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);
