# Stage 1: Build the application using Maven and JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS builder
 # Set working directory inside container
WORKDIR /app
# Copy Maven configuration to resolve dependencies
COPY pom.xml .
# Pre-fetch all dependencies (offline build)
RUN mvn dependency:go-offline -B
# Copy project source files
COPY src ./src
# Build the application and generate the jar file
RUN mvn clean package -DskipTests

# Stage 2: Use a lightweight JDK image to run the app
FROM openjdk:21-jdk-slim  AS runner
# Set working directory in the runtime container
WORKDIR /app
# Copy built jar from builder stage
COPY --from=builder ./app/target/course-service-0.0.1-SNAPSHOT.jar ./app.jar
# Document the app's port (optional)
 # Run the jar file using Java
ENTRYPOINT ["java", "-jar", "app.jar"]