# Built from the microservicios folder (context): it needs the parent pom and shared-kernel
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-kernel shared-kernel
COPY auth-service/pom.xml auth-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY evidence-service/pom.xml evidence-service/pom.xml
COPY evidence-service/src evidence-service/src
RUN mvn -q -pl evidence-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
# Runs without root: a compromised process cannot change the image or install anything.
# /app/data is the only writable folder (evidence on disk when EVIDENCE_STORAGE=disk).
RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin alerta \
    && mkdir -p /app/data && chown alerta /app/data
COPY --from=build /app/evidence-service/target/app.jar app.jar
EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-jar", "app.jar"]
