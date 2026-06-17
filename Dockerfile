# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Cache dependencies layer separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn package -DskipTests -B -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime

# Non-root user for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

WORKDIR /app

# Copy only the fat jar from build stage
COPY --from=builder /workspace/target/*.jar app.jar

# Pre-warm Spring class loader
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Layered image structure for faster rebuilds and smaller diffs
FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app
COPY --from=runtime /app/extracted/dependencies/ ./
COPY --from=runtime /app/extracted/spring-boot-loader/ ./
COPY --from=runtime /app/extracted/snapshot-dependencies/ ./
COPY --from=runtime /app/extracted/application/ ./

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
