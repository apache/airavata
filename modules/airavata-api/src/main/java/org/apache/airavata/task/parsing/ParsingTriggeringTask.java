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

import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.orchestrator.ParsingHandler;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Triggers the ParserWorkflowManager when a process completes by calling
 * ParsingHandler.onParsingMessage directly (same-JVM call, no pub/sub).
 */
@TaskDef(name = "Parsing Triggering Task")
@Component
@ConditionalOnParticipant
public class ParsingTriggeringTask extends AiravataTask {

    private static final Logger logger = LoggerFactory.getLogger(ParsingTriggeringTask.class);

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ParsingHandler parsingHandler;

    public ParsingTriggeringTask(
            TaskUtil taskUtil,
            ApplicationContext applicationContext,
            RegistryService registryService,
            UserProfileService userProfileService,
            CredentialStoreService credentialStoreService,
            MessagingFactory messagingFactory) {
        super(
                taskUtil,
                applicationContext,
                registryService,
                userProfileService,
                credentialStoreService,
                messagingFactory);
    }

    public void submitMessageToParserEngine(ProcessCompletionMessage completionMessage) {
        if (parsingHandler != null) {
            parsingHandler.onParsingMessage(completionMessage);
            logger.info("ParsingTriggeringTask invoked ParsingHandler for experiment {}", completionMessage.getExperimentId());
        } else {
            logger.debug("ParsingHandler not available; parsing workflow not triggered for experiment {}",
                    completionMessage.getExperimentId());
        }
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
