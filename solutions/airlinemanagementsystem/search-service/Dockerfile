# Use the same base image as your other services
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/*.jar app.jar

# Expose the Search Service port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]