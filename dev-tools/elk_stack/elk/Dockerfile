################################################################
# Airavata ELK Stack
# Builds an image for Apache Kafka from binary distribution.
################################################################
FROM sebp/elk

WORKDIR ${LOGSTASH_HOME}

RUN gosu logstash bin/logstash-plugin install logstash-input-kafka logstash-filter-ruby logstash-filter-json

RUN mkdir airavata

ADD logstash-airavata.conf ${LOGSTASH_HOME}/airavata/logstash-airavata.conf