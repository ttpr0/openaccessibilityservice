FROM openjdk:17

WORKDIR /oas

COPY src /oas/src
COPY files /oas/files
COPY pom.xml /oas/pom.xml
COPY .mvn /oas/.mvn
COPY mvnw /oas/mvnw

RUN chmod +x ./mvnw  

RUN ./mvnw package

EXPOSE 5001

ENTRYPOINT ["java", "-jar", "./target/oas.jar"]

