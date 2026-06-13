# Multi-stage build: builda o JAR num container, copia pra imagem final menor
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

# Copia o necessário pra build (mvnw + parent + commons + serviço)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY commons commons
COPY services/notification-service services/notification-service

# Builda apenas o notification-service e seus deps
RUN ./mvnw -pl services/notification-service -am package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /workspace/services/notification-service/target/notification-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
