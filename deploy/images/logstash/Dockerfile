FROM logstash:2

COPY *.gem /tmp/
RUN logstash-plugin install /tmp/*.gem && rm -f /tmp/*.gem
RUN logstash-plugin install logstash-codec-avro logstash-codec-cloudtrail logstash-input-journald
ENTRYPOINT []