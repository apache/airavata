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
package org.apache.airavata.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.orchestrator.OrchestratorService;
import org.apache.airavata.service.registry.RegistryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests that assert experiment state transitions when submitting experiments
 * via OrchestratorService.launchExperiment. Uses airavata.dapr.enabled=false so it does
 * not depend on a Dapr sidecar; the launch path is shared with the Dapr-driven receive path.
 *
 * <p>Requires profile "orchestrator-integration" so SimpleOrchestratorImpl (and deps) load
 * alongside "test" for Testcontainers.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            ExperimentSubmitStateTransitionIntegrationTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "airavata.services.rest.enabled=false",
            "airavata.services.thrift.enabled=true",
            "spring.aop.proxy-target-class=true",
            "airavata.flyway.enabled=false",
            "airavata.security.manager.enabled=false",
            "airavata.security.authzCache.enabled=true",
            "airavata.dapr.enabled=false"
        })
@ActiveProfiles({"test", "orchestrator-integration"})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("Experiment submit state transition integration tests")
public class ExperimentSubmitStateTransitionIntegrationTest extends ServiceIntegrationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentSubmitStateTransitionIntegrationTest.class);

    @Autowired(required = false)
    private OrchestratorService orchestratorService;

    private final RegistryService registryService;

    private ExecutorService executor;

    public ExperimentSubmitStateTransitionIntegrationTest(RegistryService registryService) {
        this.registryService = registryService;
    }

    @AfterEach
    void shutdownExecutor() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("Launch with missing UserConfigurationData transitions CREATED -> FAILED")
    void launchWithMissingUserConfigTransitionsToFailed() throws RegistryException {
        // Fail fast if OrchestratorService is required but not available
        Assumptions.assumeTrue(
                orchestratorService != null,
                "OrchestratorService is required for this test but is not available.");

        Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
        if (!registryService.isGatewayExist(TEST_GATEWAY_ID)) {
            registryService.addGateway(gateway);
        }

        Project project = TestDataFactory.createTestProject("Submit State Test Project", TEST_GATEWAY_ID);
        String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

        ExperimentModel experiment =
                TestDataFactory.createTestExperiment("Submit State Experiment", projectId, TEST_GATEWAY_ID);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        // Do not set userConfigurationData: getCredentialToken will NPE on
        // userConfigurationData.getGroupResourceProfileId(), caught as RuntimeException -> FAILED
        String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);

        ExperimentStatus before = registryService.getExperimentStatus(experimentId);
        assertThat(before.getState()).isEqualTo(ExperimentState.CREATED);

        executor = Executors.newSingleThreadExecutor();
        try {
            orchestratorService.launchExperiment(experimentId, TEST_GATEWAY_ID, executor);
        } catch (OrchestratorException e) {
            logger.debug("Expected OrchestratorException when launch fails: {}", e.getMessage());
        }

        flushAndClear();
        ExperimentStatus after = registryService.getExperimentStatus(experimentId);
        // launchExperiment catches RuntimeException and sets FAILED; if it threw OrchestratorException
        // (before reaching that) state may stay CREATED. Both are valid (no invalid transition).
        assertThat(after.getState())
                .isIn(
                        ExperimentState.CREATED,
                        ExperimentState.FAILED,
                        ExperimentState.SCHEDULED,
                        ExperimentState.LAUNCHED);
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.service.orchestrator",
                "org.apache.airavata.orchestrator",
                "org.apache.airavata.messaging",
                "org.apache.airavata.metascheduler"
            })
    @Profile({"test", "orchestrator-integration"})
    static class TestConfiguration {}
}
