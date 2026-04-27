FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

RUN apk add --no-cache libc6-compat

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 50051

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]