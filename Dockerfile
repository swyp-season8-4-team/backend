# 빌드 스테이지
FROM gradle:8.6-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src
RUN ./gradlew build --no-daemon -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar desserbeeApp.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "desserbeeApp.jar"]
