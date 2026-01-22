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
package org.apache.airavata.task.parsing;

import io.dapr.client.DaprClient;
import java.util.Collections;
import org.apache.airavata.config.conditional.ConditionalOnParticipant;
import org.apache.airavata.orchestrator.config.OrchestratorConfig;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.orchestrator.messaging.Topics;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.base.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link ProcessCompletionMessage} to Dapr parsing-data-topic when a process
 * completes, triggering the ParserWorkflowManager. Replaces Kafka producer.
 *
 * <p>Note: This task uses DaprClient directly (rather than DaprPublisher) because
 * ProcessCompletionMessage is published in a custom format expected by ParsingHandler,
 * not wrapped in MessageContext like standard messaging events.
 */
@TaskDef(name = "Parsing Triggering Task")
@Component
@ConditionalOnParticipant
public class ParsingTriggeringTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(ParsingTriggeringTask.class);

    private final DaprClient daprClient;
    private final String pubsubName;

    public ParsingTriggeringTask(
            TaskUtil taskUtil,
            ApplicationContext applicationContext,
            RegistryService registryService,
            UserProfileService userProfileService,
            CredentialStoreService credentialStoreService,
            MessagingFactory messagingFactory,
            @Autowired(required = false) DaprClient daprClient,
            @Value("${" + OrchestratorConfig.PUBSUB_NAME + ":" + OrchestratorConfig.DEFAULT_PUBSUB_NAME + "}")
                    String pubsubName) {
        super(
                taskUtil,
                applicationContext,
                registryService,
                userProfileService,
                credentialStoreService,
                messagingFactory);
        this.daprClient = daprClient;
        this.pubsubName = pubsubName;
    }

    public void submitMessageToParserEngine(ProcessCompletionMessage completionMessage) {
        if (daprClient == null) {
            throw new IllegalStateException("DaprClient not available; enable airavata.dapr.enabled for parsing.");
        }
        var experimentId = completionMessage.getExperimentId();
        daprClient
                .publishEvent(
                        pubsubName,
                        Topics.PARSING,
                        completionMessage,
                        Collections.singletonMap("routingKey", experimentId))
                .block();
        logger.info("ParsingTriggeringTask posted to {}: {}->{}", Topics.PARSING, experimentId, completionMessage);
    }

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        logger.info("Starting parsing triggering task {}, experiment id {}", getTaskId(), getExperimentId());

        ProcessCompletionMessage completionMessage = new ProcessCompletionMessage();
        completionMessage.setExperimentId(getExperimentId());
        completionMessage.setProcessId(getProcessId());
        completionMessage.setGatewayId(getGatewayId());

        try {
            submitMessageToParserEngine(completionMessage);
        } catch (Exception e) {
            logger.error("Failed to submit completion message to parsing engine", e);
        }
        return onSuccess("Successfully completed parsing triggering task");
    }

    @Override
    public void onCancel(TaskContext taskContext) {}
}
