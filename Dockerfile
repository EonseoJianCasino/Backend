# Build stage
FROM gradle:8.10-jdk17 AS builder
WORKDIR /app

# 1) Gradle wrapper & 기본 설정만 먼저 복사해서 의존성 캐시 확보
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
COPY gradle.properties ./gradle.properties
RUN chmod +x ./gradlew

# BuildKit 캐시를 gradle 캐시에 연결 (처음 1번만 길고 이후 빠름)
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon dependencies || true

# 2) 소스는 마지막에 복사 (캐시 최대 활용)
COPY src ./src
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
