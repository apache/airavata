################################################################
# Airavata ELK Stack
# Builds an image for Apache Kafka from binary distribution.
################################################################
FROM sebp/elk

WORKDIR ${LOGSTASH_HOME}

RUN gosu logstash bin/logstash-plugin install logstash-input-kafka logstash-filter-ruby logstash-filter-json

#ADD logstash-airavata.conf logstash-airavata.conf

RUN mkdir airavata