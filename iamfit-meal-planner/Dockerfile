FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S iamfit && adduser -S iamfit -G iamfit

COPY --from=builder /app/target/*.jar app.jar
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

USER iamfit
EXPOSE 8080

ENTRYPOINT ["sh", "entrypoint.sh"]