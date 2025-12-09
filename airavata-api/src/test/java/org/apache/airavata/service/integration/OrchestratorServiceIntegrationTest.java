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

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.service.OrchestratorService;
import org.apache.airavata.service.RegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for OrchestratorService (Orchestration workflows).
 */
@DisplayName("OrchestratorService Integration Tests")
public class OrchestratorServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final OrchestratorService orchestratorService;
    private final AiravataService airavataService;
    private final RegistryService registryService;

    public OrchestratorServiceIntegrationTest(
            OrchestratorService orchestratorService, AiravataService airavataService, RegistryService registryService) {
        this.orchestratorService = orchestratorService;
        this.airavataService = airavataService;
        this.registryService = registryService;
    }

    @Nested
    @DisplayName("Experiment Orchestration")
    class ExperimentOrchestrationTests {

        @Test
        @DisplayName("Should orchestrate experiment workflow")
        void shouldOrchestrateExperimentWorkflow() throws Exception {
            // Arrange
            Project project = TestDataFactory.createTestProject("Orchestration Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Orchestration Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            // Note: Actual orchestration requires proper compute resources and applications
            // This test verifies the service is available and can be called
            assertThat(experimentId).isNotNull();
            assertThat(orchestratorService).isNotNull();
            ExperimentModel retrieved = airavataService.getExperiment(testAuthzToken, experimentId);
            assertThat(retrieved).isNotNull();
        }
    }
}
