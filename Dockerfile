FROM openjdk:17

WORKDIR /oas

COPY src /oas/src
COPY pom.xml /oas/pom.xml
COPY .mvn /oas/.mvn
COPY mvnw /oas/mvnw

RUN chmod +x ./mvnw

RUN ./mvnw package

COPY oas-config.yml /oas/oas-config.yml

EXPOSE 5001

ENTRYPOINT ["java", "-jar", "./target/oas.jar"]

