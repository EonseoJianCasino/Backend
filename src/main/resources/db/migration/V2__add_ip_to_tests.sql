-- src/main/resources/db/migration/V4__add_ip_to_tests.sql

ALTER TABLE tests
ADD COLUMN IF NOT EXISTS ip varchar(255) NOT NULL DEFAULT '0.0.0.0';

COMMENT ON COLUMN tests.ip IS '요청 클라이언트의 IPv4 혹은 IPv6 주소';
