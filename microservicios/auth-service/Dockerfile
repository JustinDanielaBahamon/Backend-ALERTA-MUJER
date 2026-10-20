# Se construye desde la carpeta microservicios (contexto): necesita el pom padre y shared-kernel
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-kernel shared-kernel
COPY auth-service/pom.xml auth-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY evidence-service/pom.xml evidence-service/pom.xml
COPY auth-service/src auth-service/src
RUN mvn -q -pl auth-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/auth-service/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
