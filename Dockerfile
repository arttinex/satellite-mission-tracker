# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1: build the jar with Maven + JDK 25.
# Dependencies are resolved in their own layer (before `COPY src`) so
# `docker build` only re-downloads them when pom.xml actually changes.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Stage 2: minimal runtime image - JRE only, no Maven/JDK/build tools.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Run as a non-root user rather than the image default (root).
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /build/target/*.jar app.jar

# Render injects its own PORT value at runtime and routes traffic to it;
# 8080 here is just the local-run default (application.properties reads
# server.port=${PORT:8080}, so this stays in sync automatically).
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q -O- http://localhost:${PORT}/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
