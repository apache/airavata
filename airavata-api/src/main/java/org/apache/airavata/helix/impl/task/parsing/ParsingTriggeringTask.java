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
package org.apache.airavata.helix.impl.task.parsing;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.parsing.kafka.ProcessCompletionMessageSerializer;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskResult;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TaskDef(name = "Parsing Triggering Task")
public class ParsingTriggeringTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(ParsingTriggeringTask.class);

    private static Producer<String, ProcessCompletionMessage> producer;
    private String topic;

    public ParsingTriggeringTask(
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.RegistryService registryService,
            org.apache.airavata.service.UserProfileService userProfileService,
            org.apache.airavata.service.CredentialStoreService credentialStoreService) {
        super(applicationContext, registryService, userProfileService, credentialStoreService);
        // Topic will be initialized in @PostConstruct or from ApplicationContext
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            var ctx = getApplicationContext();
            if (ctx != null) {
                var props = ctx.getBean(AiravataServerProperties.class);
                topic = props.services.parser.topic;
            }
        } catch (Exception e) {
            logger.warn("Could not get properties from ApplicationContext", e);
        }
    }

    private void createProducer() {
        if (producer == null) {
            try {
                var ctx = getApplicationContext();
                if (ctx == null) {
                    throw new IllegalStateException("ApplicationContext not available");
                }
                var props = ctx.getBean(AiravataServerProperties.class);
                Properties kafkaProps = new Properties();
                kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.kafka.brokerUrl);
                kafkaProps.put(ProducerConfig.CLIENT_ID_CONFIG, props.services.parser.brokerConsumerGroup);
                kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                kafkaProps.put(
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        ProcessCompletionMessageSerializer.class.getName());
                producer = new KafkaProducer<>(kafkaProps);
            } catch (Exception e) {
                logger.error("Failed to create Kafka producer", e);
                throw new RuntimeException("Failed to create Kafka producer", e);
            }
        }
    }

    public void submitMessageToParserEngine(ProcessCompletionMessage completionMessage)
            throws ExecutionException, InterruptedException {
        var experimentId = completionMessage.getExperimentId();
        var record = new ProducerRecord<>(topic, experimentId, completionMessage);
        producer.send(record).get();
        logger.info("ParsingTriggeringTask posted to {}: {}->{}", topic, experimentId, completionMessage);
        producer.flush();
    }

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {

        logger.info("Starting parsing triggering task {}, experiment id {}", getTaskId(), getExperimentId());

        ProcessCompletionMessage completionMessage = new ProcessCompletionMessage();
        completionMessage.setExperimentId(getExperimentId());
        completionMessage.setProcessId(getProcessId());
        completionMessage.setGatewayId(getGatewayId());

        try {
            createProducer();
            submitMessageToParserEngine(completionMessage);
        } catch (Exception e) {
            logger.error("Failed to submit completion message to parsing engine", e);
        }
        return onSuccess("Successfully completed parsing triggering task");
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
