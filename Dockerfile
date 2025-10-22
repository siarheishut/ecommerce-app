FROM openjdk:24-jdk-slim

WORKDIR /usr/src/app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline --no-transfer-progress

COPY src ./src

RUN ./mvnw package -DskipTests --no-transfer-progress

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/ecommerce-0.0.1-SNAPSHOT.jar"]
