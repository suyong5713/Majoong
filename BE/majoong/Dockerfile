FROM openjdk:11.0.16-jdk

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app.jar"]
