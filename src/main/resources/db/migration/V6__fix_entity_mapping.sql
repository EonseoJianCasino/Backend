-- V6__fix_entity_mapping.sql
-- 최소한의 Entity 매핑 문제 해결

-- 누락된 컬럼 추가
ALTER TABLE web_vitals ADD COLUMN IF NOT EXISTS inp double precision;
ALTER TABLE web_vitals ADD COLUMN IF NOT EXISTS tbt double precision;

-- web_vitals 컬럼 타입을 Entity와 맞춤 (int -> double precision)
ALTER TABLE web_vitals ALTER COLUMN lcp TYPE double precision USING lcp::double precision;
ALTER TABLE web_vitals ALTER COLUMN fid TYPE double precision USING fid::double precision;
ALTER TABLE web_vitals ALTER COLUMN fcp TYPE double precision USING fcp::double precision;
ALTER TABLE web_vitals ALTER COLUMN ttfb TYPE double precision USING ttfb::double precision;
