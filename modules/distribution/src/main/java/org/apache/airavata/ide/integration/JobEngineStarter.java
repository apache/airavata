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
package org.apache.airavata.ide.integration;

import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Spring-based starter for Job Engine components.
 *
 * <p>This class starts Helix Controller, Global Participant, and Workflow Managers
 * using Spring Dependency Injection. All components are obtained from the Spring
 * application context, eliminating the need for reflection or manual instantiation.
 *
 * <p>This starter should be run within a Spring Boot application context.
 * Use {@link org.apache.airavata.server.UnifiedApplication} or create a Spring Boot
 * application that includes this component.
 *
 * <p>Components are started in the following order:
 * <ol>
 *   <li>Helix Controller - Manages Helix cluster</li>
 *   <li>Global Participant - Executes workflow tasks</li>
 *   <li>Pre Workflow Manager - Handles process launch events</li>
 *   <li>Post Workflow Manager - Handles job completion events</li>
 * </ol>
 */
@Component
@ConditionalOnProperty(name = "services.helix.enabled", havingValue = "true", matchIfMissing = true)
public class JobEngineStarter implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(JobEngineStarter.class);

    private final ApplicationContext applicationContext;

    public JobEngineStarter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Job Engine components via Spring DI...");

        // Check for components in Spring context
        // These are conditionally created based on @ConditionalOnProperty annotations
        // Components are actually started by BackgroundServicesLauncher - we just verify they're available
        if (applicationContext.getBeanNamesForType(HelixController.class).length > 0) {
            logger.info("Helix Controller is available and will be started by BackgroundServicesLauncher");
        } else {
            logger.warn("HelixController bean not found - may be disabled via configuration");
        }

        if (applicationContext.getBeanNamesForType(GlobalParticipant.class).length > 0) {
            logger.info("Global Participant is available and will be started by BackgroundServicesLauncher");
            // GlobalParticipant automatically discovers task classes from Spring context
            // No reflection needed - task classes are obtained via
            // applicationContext.getBeansOfType(AbstractTask.class)
        } else {
            logger.warn("GlobalParticipant bean not found - may be disabled via configuration");
        }

        if (applicationContext.getBeanNamesForType(PreWorkflowManager.class).length > 0) {
            logger.info("Pre Workflow Manager is available and will be started by BackgroundServicesLauncher");
        } else {
            logger.warn("PreWorkflowManager bean not found - may be disabled via configuration");
        }

        if (applicationContext.getBeanNamesForType(PostWorkflowManager.class).length > 0) {
            logger.info("Post Workflow Manager is available and will be started by BackgroundServicesLauncher");
        } else {
            logger.warn("PostWorkflowManager bean not found - may be disabled via configuration");
        }

        logger.info("Job Engine starter completed - components will be started by BackgroundServicesLauncher");
    }

    /**
     * Legacy main method - throws UnsupportedOperationException.
     *
     * <p>This class must be run within a Spring Boot application context.
     * Use {@link org.apache.airavata.server.UnifiedApplication} or create a Spring Boot
     * application that includes this component.
     *
     * @param args command line arguments (not used)
     * @throws UnsupportedOperationException always - this class must be used within Spring context
     */
    public static void main(String[] args) throws Exception {
        throw new UnsupportedOperationException(
                "JobEngineStarter must be used within a Spring Boot application context. "
                        + "Use org.apache.airavata.server.UnifiedApplication or create a Spring Boot application.");
    }
}
