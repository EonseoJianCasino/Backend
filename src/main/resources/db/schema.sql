-- === Base Schema (최신 정답 상태) ===

-- 안전하게 기존 잔여물을 제거(신규 DB면 영향 없음)

-- tests
CREATE TABLE IF NOT EXISTS tests (
  id          uuid PRIMARY KEY,                       -- 앱에서 UUID 생성
  domain_name varchar(255) NOT NULL,
  url         text NOT NULL,
  status      varchar(20) NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- web_vitals
CREATE TABLE IF NOT EXISTS web_vitals (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  -- 컬럼 최종 명/타입 정합
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
  test_id                  uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  has_csp                  boolean DEFAULT false,
  has_hsts                 boolean DEFAULT false,
  x_frame_options          varchar(50),
  x_content_type_options   varchar(50),
  created_at               timestamptz NOT NULL DEFAULT now()
);

-- scores
CREATE TABLE IF NOT EXISTS scores (
  id                uuid PRIMARY KEY,
  test_id           uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  total_score       int  NOT NULL,
  performance_score int,
  security_score    int,
  created_at        timestamptz NOT NULL DEFAULT now()
);

-- priorities
CREATE TABLE IF NOT EXISTS priorities (
  id         uuid PRIMARY KEY,
  test_id    uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  type       varchar(20) NOT NULL, -- PERFORMANCE / SECURITY
  metric     varchar(50) NOT NULL,
  reason     text,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- ai_recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
  id               uuid PRIMARY KEY,
  test_id          uuid NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  title            varchar(200) NOT NULL,
  detail           text,
  expected_impact  text,
  created_at       timestamptz NOT NULL DEFAULT now()
);

-- 제약 대신 인덱스로 유일성 보장
CREATE UNIQUE INDEX IF NOT EXISTS uq_web_vitals_test_idx ON web_vitals(test_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_security_vitals_test_idx ON security_vitals(test_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_scores_test_idx ON scores(test_id);

