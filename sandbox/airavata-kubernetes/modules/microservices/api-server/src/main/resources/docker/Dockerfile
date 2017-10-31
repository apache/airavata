FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ${project.build.finalName}.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar