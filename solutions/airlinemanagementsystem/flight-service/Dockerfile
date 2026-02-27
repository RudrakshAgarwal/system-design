# Use the standard JRE (Ubuntu-based) which works perfectly on M1/M2/M3
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the locally built JAR
COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]