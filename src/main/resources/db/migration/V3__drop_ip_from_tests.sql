-- tests 테이블에서 ip 컬럼 제거
ALTER TABLE tests
    DROP COLUMN IF EXISTS ip
