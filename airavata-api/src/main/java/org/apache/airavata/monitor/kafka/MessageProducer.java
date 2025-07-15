/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.monitor.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);
    final Producer<String, JobStatusResult> producer;
    final String topic;

    public MessageProducer() throws ApplicationSettingsException {
        producer = createProducer();
        topic = ServerSettings.getSetting("job.monitor.broker.topic");
    }

    private Producer<String, JobStatusResult> createProducer() throws ApplicationSettingsException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, ServerSettings.getSetting("job.monitor.broker.publisher.id"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JobStatusResultSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    public void submitMessageToQueue(JobStatusResult jobStatusResult) throws ExecutionException, InterruptedException {
        var jobId = jobStatusResult.getJobId();
        final var record = new ProducerRecord<>(topic, jobId, jobStatusResult);
        producer.send(record).get();
        log.info("MessageProducer posted to {}: {}->{}", topic, jobId, jobStatusResult);
        producer.flush();
    }
}
