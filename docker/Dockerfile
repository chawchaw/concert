# Use a base image with JDK 17 on Mac OS M1/M2 ARM64
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the jar file into the container
COPY build/libs/concert-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
