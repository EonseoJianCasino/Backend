-- web_vitals 컬럼명 변경
BEGIN;
ALTER TABLE web_vitals
  RENAME COLUMN lcp_ms  TO lcp;

ALTER TABLE web_vitals
  RENAME COLUMN fid_ms  TO fid;

ALTER TABLE web_vitals
  RENAME COLUMN fcp_ms  TO fcp;

ALTER TABLE web_vitals
  RENAME COLUMN ttfb_ms TO ttfb;

-- 1:1 보장을 위한 UNIQUE(test_id) 추가
ALTER TABLE web_vitals
  ADD CONSTRAINT uq_web_vitals_test UNIQUE (test_id);

ALTER TABLE security_vitals
  ADD CONSTRAINT uq_security_vitals_test UNIQUE (test_id);

ALTER TABLE scores
  ADD CONSTRAINT uq_scores_test UNIQUE (test_id);