# ======================================
# Stage 1: Build
# ======================================
FROM maven:3.9.11-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy dependency files first for caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ======================================
# Stage 2: Runtime
# ======================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# (1) Create necessary directories and user before copying files
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads /app/logs \
    && chown -R spring:spring /app

# Switch to non-root
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check for container monitoring
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# (2) Allow runtime env vars for config (matches your application-prod.yml)
ENV SPRING_PROFILES_ACTIVE=prod

# (3) Run the app with container-aware memory settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
