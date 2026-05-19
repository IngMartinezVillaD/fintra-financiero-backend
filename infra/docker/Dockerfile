# ── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S pluto && adduser -S pluto -G pluto
USER pluto

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75 -Dspring.threads.virtual.enabled=true"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
