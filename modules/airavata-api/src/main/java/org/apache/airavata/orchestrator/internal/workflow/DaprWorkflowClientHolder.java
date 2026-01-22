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
package org.apache.airavata.orchestrator.internal.workflow;

import io.dapr.workflows.client.DaprWorkflowClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Configuration for Dapr Workflow Client.
 *
 * <p>Provides a DaprWorkflowClient bean that can be used to schedule
 * new workflow instances from message handlers and other components.
 *
 * <p>Requires both conditions to be met:
 * <ul>
 *   <li>{@code airavata.services.controller.enabled=true} - Controller service must be enabled</li>
 *   <li>{@code airavata.dapr.enabled=true} - Dapr must be enabled (sidecar available)</li>
 * </ul>
 */
@Configuration
@Conditional(DaprWorkflowClientHolder.DaprWorkflowClientCondition.class)
public class DaprWorkflowClientHolder {

    /**
     * Condition that requires both controller service and Dapr to be enabled.
     */
    static class DaprWorkflowClientCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var env = context.getEnvironment();
            boolean controllerEnabled =
                    "true".equalsIgnoreCase(env.getProperty("airavata.services.controller.enabled", "false"));
            boolean daprEnabled = "true".equalsIgnoreCase(env.getProperty("airavata.dapr.enabled", "false"));
            return controllerEnabled && daprEnabled;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DaprWorkflowClientHolder.class);

    @Bean
    public DaprWorkflowClient daprWorkflowClient() {
        try {
            DaprWorkflowClient client = new DaprWorkflowClient();
            logger.info("DaprWorkflowClient created successfully");
            return client;
        } catch (Exception e) {
            logger.error("Failed to create DaprWorkflowClient", e);
            return null;
        }
    }
}
