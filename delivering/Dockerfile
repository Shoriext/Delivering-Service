FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY delivering/pom.xml ./pom.xml
RUN mvn -q -DskipTests dependency:go-offline

COPY delivering/src ./src
RUN mvn -q -DskipTests package \
    && cp target/delivering-0.0.1-SNAPSHOT.jar /workspace/application.jar

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app
COPY --from=build --chown=spring:spring /workspace/application.jar application.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
