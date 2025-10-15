# ------------------------------
# Stage 1: Build
# ------------------------------
FROM maven:3.9.11-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy Maven wrapper & pom files to leverage cache
COPY pom.xml .mvn mvnw ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build Spring Boot app (skip tests)
RUN ./mvnw clean package -DskipTests

# ------------------------------
# Stage 2: Runtime
# ------------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from build stage
# This will copy the first JAR it finds from target folder
COPY --from=build /app/target/*.jar /app/app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run Spring Boot app
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "/app/app.jar"]
