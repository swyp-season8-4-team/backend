# 빌드 스테이지
FROM gradle:8.6-jdk17-alpine AS build
WORKDIR /app

# Gradle 캐시 최적화
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew build --no-daemon -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안: 비root 사용자 생성 및 시스템 패키지 업데이트
RUN apk update && apk upgrade && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# 애플리케이션 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 권한 설정
RUN chown -R appuser:appgroup /app && \
    chmod 755 /app && \
    chmod 644 /app/app.jar

# 비root 사용자로 전환
USER appuser

# JVM 최적화 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
