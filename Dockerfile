# 빌드 스테이지
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN gradle build -x test --no-daemon

# 실행 스테이지 (Alpine은 ARM64 미지원 → 표준 이미지 사용)
FROM eclipse-temurin:17-jre
WORKDIR /app

# 보안: non-root 사용자 (Debian/Ubuntu 계열)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]