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
package org.apache.airavata.dapr.monitor;

import io.dapr.client.DaprClient;
import java.util.Collections;
import org.apache.airavata.dapr.config.DaprConfigConstants;
import org.apache.airavata.monitor.JobStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Dapr Pub/Sub message producer for monitoring. Publishes {@link JobStatusResult}
 * to the monitoring-data-topic. Replaces the Kafka-based producer.
 */
@Component
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true")
@ConditionalOnBean(DaprClient.class)
public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);
    /** Dapr topic for external broker format (jobName, status, task) consumed by RealtimeMonitor. */
    public static final String MONITORING_TOPIC = org.apache.airavata.dapr.messaging.DaprTopics.MONITORING;
    /** Dapr topic for JobStatusResult (MessageProducer publish, PostWorkflowManager consume). */
    public static final String MONITORING_JOB_STATUS_TOPIC =
            org.apache.airavata.dapr.messaging.DaprTopics.MONITORING_JOB_STATUS;

    private final DaprClient daprClient;
    private final String pubsubName;
    private final String topic;

    public MessageProducer(
            DaprClient daprClient,
            @Value("${" + DaprConfigConstants.DAPR_PUBSUB_NAME + ":" + DaprConfigConstants.DEFAULT_PUBSUB_NAME + "}")
                    String pubsubName,
            @Value("${airavata.dapr.topic.monitoring-job-status:" + MONITORING_JOB_STATUS_TOPIC + "}") String topic) {
        this.daprClient = daprClient;
        this.pubsubName = pubsubName;
        this.topic = (topic != null && !topic.isBlank()) ? topic : MONITORING_JOB_STATUS_TOPIC;
    }

    /**
     * Publish a job status result to the Dapr monitoring topic.
     */
    public void submitMessageToQueue(JobStatusResult jobStatusResult) {
        var jobId = jobStatusResult.getJobId();
        try {
            daprClient
                    .publishEvent(pubsubName, topic, jobStatusResult, Collections.singletonMap("routingKey", jobId))
                    .block();
            log.debug("MessageProducer published to {}: {} -> {}", topic, jobId, jobStatusResult);
        } catch (Exception e) {
            log.error("Failed to publish monitoring message for job {} to topic {}", jobId, topic, e);
            throw new RuntimeException("Failed to publish to Dapr topic: " + topic, e);
        }
    }
}
