# Stage 1: Build stage
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Pre-download dependencies to cache Docker layer
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-privileged service user for security hardening
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
