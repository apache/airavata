package org.apache.airavata.job.monitor.kafka;

import org.apache.airavata.job.monitor.parser.JobStatusResult;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MessageProducer {
    private final static String TOPIC = "parsed-data";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";

    final Producer<String, JobStatusResult> producer;

    public MessageProducer() {
        producer = createProducer();
    }

    private Producer<String, JobStatusResult> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JobStatusResultSerializer.class.getName());
        return new KafkaProducer<String, JobStatusResult>(props);
    }

    public void submitMessageToQueue(JobStatusResult jobStatusResult) throws ExecutionException, InterruptedException {
        final ProducerRecord<String, JobStatusResult> record = new ProducerRecord<>(TOPIC, jobStatusResult);
        RecordMetadata recordMetadata = producer.send(record).get();
        producer.flush();
    }
}
