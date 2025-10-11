-- src/main/resources/db/migration/V1__init.sql

create table if not exists tests (
  id uuid primary key,                       -- default 제거 (앱이 UUID 생성)
  domain_name varchar(255) not null,
  url text not null,
  status varchar(20) not null,
  created_at timestamptz not null default now()
);

create table if not exists web_vitals (
  id uuid primary key,
  test_id uuid not null references tests(id) on delete cascade,
  lcp_ms int,
  fid_ms int,
  cls double precision,
  fcp_ms int,
  ttfb_ms int,
  created_at timestamptz not null default now()
);

create table if not exists security_vitals (
  id uuid primary key,
  test_id uuid not null references tests(id) on delete cascade,
  has_csp boolean default false,
  has_hsts boolean default false,
  x_frame_options varchar(50),
  x_content_type_options varchar(50),
  created_at timestamptz not null default now()
);

create table if not exists scores (
  id uuid primary key,
  test_id uuid not null references tests(id) on delete cascade,
  total_score int not null,
  performance_score int,
  security_score int,
  created_at timestamptz not null default now()
);

create table if not exists priorities (
  id uuid primary key,
  test_id uuid not null references tests(id) on delete cascade,
  type varchar(20) not null, -- PERFORMANCE / SECURITY
  metric varchar(50) not null,
  reason text,
  created_at timestamptz not null default now()
);

create table if not exists ai_recommendations (
  id uuid primary key,
  test_id uuid not null references tests(id) on delete cascade,
  title varchar(200) not null,
  detail text,
  expected_impact text,
  created_at timestamptz not null default now()
);

create table if not exists ips (
  test_id uuid primary key references tests(id) on delete cascade,
  ipv4 inet,
  ipv6 inet
);
