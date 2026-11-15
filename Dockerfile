# ==========================================
# Stage 1: Build & Package (Maven + JDK 25)
# ==========================================
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy Maven project descriptor
COPY pom.xml .

# Copy application source code
COPY src ./src

# Compile and package application into an executable JAR (skipping tests for fast build)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime Environment (Java 25 JRE)
# ==========================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy compiled JAR artifact from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
