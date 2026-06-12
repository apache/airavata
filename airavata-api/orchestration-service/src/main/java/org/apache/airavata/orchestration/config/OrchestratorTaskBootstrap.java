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
package org.apache.airavata.orchestration.config;

import org.apache.airavata.config.ConditionalOnServer;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.task.AiravataTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Wires the task framework's static {@link AiravataTask#setUserProfileProvider} once at startup so the
 * DB executor's real tasks can resolve user profiles (otherwise {@code getUserProfileProvider()} throws).
 * The provider is the iam-service {@code UserProfileRepository}, autowired by its
 * {@link UserProfileProvider} interface.
 */
@Component
@ConditionalOnServer("orchestrator")
public class OrchestratorTaskBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorTaskBootstrap.class);

    @Autowired
    private UserProfileProvider userProfileProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        AiravataTask.setUserProfileProvider(userProfileProvider);
        logger.info(
                "UserProfileProvider wired into AiravataTask: {}",
                userProfileProvider.getClass().getName());
    }
}
