# Built from the microservicios folder (context): it needs the parent pom and shared-kernel
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-kernel shared-kernel
COPY auth-service/pom.xml auth-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY evidence-service/pom.xml evidence-service/pom.xml
COPY alert-service/src alert-service/src
RUN mvn -q -pl alert-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/alert-service/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
