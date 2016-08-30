FROM scigap/java:8
ENV ZOOKEEPER localhost:2181/kafka
ENV LOG_DIRS /var/lib/kafka
ENV JMX_PORT 9999
ADD start-kafka.sh /start-kafka.sh
RUN mkdir -p /opt/kafka && curl http://mirrors.sonic.net/apache/kafka/0.8.2.2/kafka_2.11-0.8.2.2.tgz > /tmp/kafka.tgz && \
    tar -zxf /tmp/kafka.tgz  -C /opt/kafka --strip-components=1 && rm -f /tmp/kafka.tgz
ENTRYPOINT /start-kafka.sh