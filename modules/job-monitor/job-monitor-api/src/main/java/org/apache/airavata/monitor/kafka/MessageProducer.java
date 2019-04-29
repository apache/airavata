/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.monitor.kafka;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MessageProducer {

    final Producer<String, JobStatusResult> producer;

    public MessageProducer() throws ApplicationSettingsException {
        producer = createProducer();
    }

    private Producer<String, JobStatusResult> createProducer() throws ApplicationSettingsException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                ServerSettings.getSetting("job.monitor.broker.url"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, ServerSettings.getSetting("job.monitor.broker.publisher.id"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JobStatusResultSerializer.class.getName());
        return new KafkaProducer<String, JobStatusResult>(props);
    }

    public void submitMessageToQueue(JobStatusResult jobStatusResult) throws ExecutionException, InterruptedException, ApplicationSettingsException {
        final ProducerRecord<String, JobStatusResult> record = new ProducerRecord<>(
                ServerSettings.getSetting("job.monitor.broker.topic"),
                jobStatusResult.getJobId(),
                jobStatusResult);
        RecordMetadata recordMetadata = producer.send(record).get();
        producer.flush();
    }
}
