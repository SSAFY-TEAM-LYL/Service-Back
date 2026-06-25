FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system lyl && useradd --system --gid lyl lyl

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

USER lyl

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
