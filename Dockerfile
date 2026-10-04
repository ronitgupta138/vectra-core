# Multi-stage Dockerfile for Vectra Core
# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r vectra && useradd -r -g vectra vectra

COPY --from=builder --chown=vectra:vectra /workspace/target/vectra-core-*.jar app.jar

USER vectra:vectra
EXPOSE 8081

ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:+UseStringDeduplication -Dspring.threads.virtual.enabled=true"

HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
