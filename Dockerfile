FROM openjdk:14-jdk-alpine
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 7890
ENTRYPOINT ["java", "-jar", "/app.jar"]