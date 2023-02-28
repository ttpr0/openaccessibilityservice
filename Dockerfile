FROM openjdk:11

WORKDIR /oas

COPY src /oas/src
COPY files /oas/files
COPY pom.xml /oas/pom.xml
COPY .mvn /oas/.mvn
COPY mvnw /oas/mvnw

RUN mvnw package

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "./target/oas.jar"]

