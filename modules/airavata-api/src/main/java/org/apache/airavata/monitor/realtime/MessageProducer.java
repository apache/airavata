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
package org.apache.airavata.monitor.realtime;

import java.util.concurrent.ExecutionException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.monitor.JobStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

/**
 * Kafka message producer using Spring Kafka.
 * Publishes job status results to Kafka topics.
 */
@Component
@ConditionalOnProperty(prefix = "airavata.kafka", name = "enabled", havingValue = "true")
@ConditionalOnBean(KafkaTemplate.class)
public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public MessageProducer(KafkaTemplate<String, Object> kafkaTemplate, AiravataServerProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.services().monitor().compute().brokerTopic();
    }

    /**
     * Legacy constructor for backward compatibility when Spring Kafka is not available.
     */
    public MessageProducer(AiravataServerProperties properties) {
        this.kafkaTemplate = null;
        this.topic = properties.services().monitor().compute().brokerTopic();
    }

    /**
     * Submit a job status result to the Kafka topic.
     */
    public void submitMessageToQueue(JobStatusResult jobStatusResult) throws ExecutionException, InterruptedException {
        var jobId = jobStatusResult.getJobId();

        if (kafkaTemplate != null) {
            // Use Spring Kafka
            SendResult<String, Object> result =
                    kafkaTemplate.send(topic, jobId, jobStatusResult).get();
            log.info(
                    "MessageProducer posted to {} partition {} offset {}: {}->{}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    jobId,
                    jobStatusResult);
        } else {
            log.warn("KafkaTemplate not available - message not sent");
            throw new IllegalStateException("KafkaTemplate not configured. Use Spring Kafka configuration.");
        }
    }
}
