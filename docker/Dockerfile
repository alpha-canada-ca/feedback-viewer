# Multi-stage build for Spring Boot application
# Stage 1: Build
FROM maven:3.8.6-eclipse-temurin-8 AS builder

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/PageSuccess-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Set default profile to postgres (can be overridden)
ENV SPRING_PROFILES_ACTIVE=postgres

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
