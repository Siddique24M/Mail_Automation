# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Using Ubuntu (jammy) instead of Alpine to fix native memory OOM crashes that happen with Java 21 on Alpine
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render dynamically assigns PORT — do not hardcode
EXPOSE 8080

# Use ultra-low memory settings (SerialGC, MaxMetaspaceSize) for Render's 512MB free tier
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx256m", "-XX:MaxMetaspaceSize=128m", "-Xss512k", "-XX:CICompilerCount=2", "-jar", "app.jar"]
