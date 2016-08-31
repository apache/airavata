FROM logstash:2.1.1

COPY *.gem /tmp/
RUN /opt/logstash/bin/plugin install logstash-output-amazon_es 
RUN /opt/logstash/bin/plugin install logstash-codec-avro logstash-codec-cloudtrail logstash-input-journald
ENTRYPOINT []
