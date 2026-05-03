# Use Debian-based JRE to support native libraries (PyTorch/DJL)
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the JAR built locally from the build/libs directory
COPY build/libs/*.jar app.jar

EXPOSE 8080

# Limit JVM heap for EC2 Free Tier
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
