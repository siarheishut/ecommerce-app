FROM eclipse-temurin:24-jdk AS builder

WORKDIR /usr/src/app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline --batch-mode --no-transfer-progress

COPY src ./src

RUN ./mvnw package -DskipTests --batch-mode --no-transfer-progress

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

COPY --from=builder /usr/src/app/target/ecommerce-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
