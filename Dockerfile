FROM openjdk:17
ARG JAR_FILE=./target/sp_queue-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]