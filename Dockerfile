FROM openjdk:latest
LABEL authors="artem"
WORKDIR /app
EXPOSE 8080
COPY build/libs/TestTaskBot-0.0.1-SNAPSHOT-plain.jar /app/TestTaskBot-0.0.1-SNAPSHOT-plain.jar
ENTRYPOINT ["java", "-jar", "/app/TestTaskBot-0.0.1-SNAPSHOT-plain.jar"]