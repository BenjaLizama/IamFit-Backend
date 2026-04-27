FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

RUN apk add --no-cache libc6-compat gcompat protoc

COPY pom.xml .
COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
EXPOSE 50051
ENTRYPOINT ["java", "-jar", "app.jar"]