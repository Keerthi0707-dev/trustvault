# ==============================================================================
# TrustVault Multi-Stage Production Dockerfile
# Single deployable container serving React SPA, REST APIs, and Storage mediation
# ==============================================================================

# Stage 1: Build the React frontend with Node.js
FROM node:22-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Spring Boot executable JAR with Java 21 & Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app

# Cache Maven dependencies
COPY backend/pom.xml ./backend/
RUN mvn -f backend/pom.xml dependency:go-offline -B || true

# Copy backend source
COPY backend/ ./backend/

# Copy compiled frontend assets from Stage 1 into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static/

# Package executable JAR
RUN mvn -f backend/pom.xml clean package -DskipTests -B

# Stage 3: Minimal, secure JRE runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create non-root user for principle of least privilege
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from Stage 2
COPY --from=backend-builder /app/backend/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV SESSION_COOKIE_SECURE=true

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java -Djava.security.egd=file:/dev/./urandom -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xss256k -Dserver.port=${PORT:-8080} -jar app.jar"]
