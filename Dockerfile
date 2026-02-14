# Step 1 : build jar with Gradle Wrapper
FROM gradle:8.4.0-jdk17 AS build
WORKDIR /app

COPY . .

RUN gradle bootJar --no-daemon

# Step 2 : copy jar from Step 1 and run it
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
