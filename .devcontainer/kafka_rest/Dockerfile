FROM eclipse-temurin:17-jre

WORKDIR /opt/rest-proxy

ARG TARBALL_URL=https://packages.confluent.io/archive/7.5/confluent-7.5.0.tar.gz

RUN apt-get install -y wget tar

RUN wget -qO- $TARBALL_URL | tar xz --strip-components=2 confluent-7.5.0/share/java/kafka-rest

ENV KAFKA_REST_BOOTSTRAP_SERVERS=PLAINTEXT://kafka:9092
ENV KAFKA_REST_LISTENERS=http://0.0.0.0:8082

CMD ["java","-jar","/opt/rest-proxy/kafka-rest-server.jar"]
