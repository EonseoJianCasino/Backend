# Docker 실행 

## 사전 작업
Docker Desktop 실행

---

## Windows

```bash
# DB 스키마 연결 (Docker Desktop 첫 실행 시 1회만 수행)
docker compose up db
docker cp ./src/main/resources/db/schema.sql webtest-db:/tmp/schema.sql
docker exec -it webtest-db psql -U dev -d webtest -v ON_ERROR_STOP=1 -f /tmp/schema.sql
docker compose down

# 도커 실행 (로그에 tomcat 8080 출력되면 정상 실행)
docker compose up --build
```

---

## Mac

```bash
# DB 스키마 연결 (Docker Desktop 첫 실행 시 1회만 수행)
docker compose up db
psql -h 127.0.0.1 -p 55432 -U dev -d webtest
docker compose exec db psql -U dev -d webtest
docker compose down

# 도커 실행 (로그에 tomcat 8080 출력되면 정상 실행)
docker compose up --build
