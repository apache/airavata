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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentSearchFields;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.service.registry.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName(
        "AiravataService Integration Tests - Main service operations with database persistence and full functionality")
public class AiravataServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired(required = false)
    private AiravataService airavataService;

    private final RegistryService registryService;

    public AiravataServiceIntegrationTest(RegistryService registryService) {
        this.registryService = registryService;
    }

    @BeforeEach
    void setUp() throws RegistryException {
        if (!registryService.isGatewayExist(TEST_GATEWAY_ID)) {
            org.apache.airavata.common.model.Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
            registryService.addGateway(gateway);
        }
    }

    @Nested
    @DisplayName("Experiment Creation and Management")
    class ExperimentCreationTests {

        @Test
        @DisplayName("Should create experiment with project association and retrieve it with all fields")
        void shouldCreateExperiment() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Test Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Test Experiment", projectId, TEST_GATEWAY_ID);

            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);
            assertThat(experimentId).isNotNull();
            ExperimentModel retrieved = airavataService.getExperiment(experimentId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getExperimentId()).isEqualTo(experimentId);
            assertThat(retrieved.getExperimentName()).isEqualTo("Test Experiment");
            assertThat(retrieved.getProjectId()).isEqualTo(projectId);
            assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        }

        @Test
        @DisplayName("Should get experiment status and verify initial state is CREATED")
        void shouldGetExperimentStatus() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Status Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Status Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            ExperimentStatus status = airavataService.getExperimentStatus(experimentId);
            assertThat(status).isNotNull();
            assertThat(status.getState()).isNotNull();
            assertThat(status.getState()).isEqualTo(ExperimentState.CREATED);
        }

        @Test
        @DisplayName("Should delete experiment and throw exception when retrieving deleted experiment")
        void shouldDeleteExperiment() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Delete Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment = TestDataFactory.createTestExperiment("To Delete", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            ExperimentModel beforeDelete = airavataService.getExperiment(experimentId);
            assertThat(beforeDelete).isNotNull();

            boolean deleted = airavataService.deleteExperiment(experimentId);
            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> airavataService.getExperiment(experimentId))
                    .isInstanceOf(AiravataSystemException.class);
        }
    }

    @Nested
    @DisplayName("Experiment Search and Filtering")
    class ExperimentSearchTests {

        @Test
        @DisplayName("Should search experiments by name filter without throwing exceptions")
        void shouldSearchExperiments() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Search Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Search Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            Map<ExperimentSearchFields, String> filters = new HashMap<>();
            filters.put(ExperimentSearchFields.EXPERIMENT_NAME, "Search");
            List<ExperimentSummaryModel> experiments =
                    airavataService.searchExperiments(testAuthzToken, TEST_GATEWAY_ID, TEST_USERNAME, filters, 10, 0);

            assertThat(experiments).isNotNull();
        }

        @Test
        @DisplayName("Should search experiments by project ID filter without throwing exceptions")
        void shouldSearchExperimentsByProject() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Search Project 2", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Project Search Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            Map<ExperimentSearchFields, String> filters = new HashMap<>();
            filters.put(ExperimentSearchFields.PROJECT_ID, projectId);
            List<ExperimentSummaryModel> experiments =
                    airavataService.searchExperiments(testAuthzToken, TEST_GATEWAY_ID, TEST_USERNAME, filters, 10, 0);

            assertThat(experiments).isNotNull();
        }

        @Test
        @DisplayName("Should get experiment outputs and return non-null list")
        void shouldGetExperimentOutputs() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Output Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Output Experiment", projectId, TEST_GATEWAY_ID);
            if (experiment.getExperimentOutputs() == null) {
                experiment.setExperimentOutputs(new ArrayList<>());
            }
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("test_output");
            output.setValue("test_value");
            experiment.getExperimentOutputs().add(output);

            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            List<OutputDataObjectType> outputs = airavataService.getExperimentOutputs(experimentId);

            assertThat(outputs).isNotNull();
        }

        @Test
        @DisplayName("Should clone experiment with new name and verify cloned experiment exists with correct fields")
        void shouldCloneExperiment() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Clone Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Original Experiment", projectId, TEST_GATEWAY_ID);
            String originalExperimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            ExperimentModel originalExperiment = airavataService.getExperiment(originalExperimentId);
            assertThat(originalExperiment).isNotNull();

            String clonedExperimentId = airavataService.cloneExperiment(
                    testAuthzToken, originalExperimentId, "Cloned Experiment", projectId, originalExperiment);

            assertThat(clonedExperimentId).isNotNull();
            assertThat(clonedExperimentId).isNotEqualTo(originalExperimentId);

            ExperimentModel clonedExperiment = airavataService.getExperiment(clonedExperimentId);
            assertThat(clonedExperiment).isNotNull();
            assertThat(clonedExperiment.getExperimentName()).isEqualTo("Cloned Experiment");
            assertThat(clonedExperiment.getProjectId()).isEqualTo(projectId);
        }
    }

    @Nested
    @DisplayName("Experiment Validation")
    class ExperimentValidationTests {

        @Test
        @DisplayName("Should update experiment description and persist changes successfully")
        void shouldUpdateExperiment() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Update Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Original Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            ExperimentModel retrieved = airavataService.getExperiment(experimentId);
            retrieved.setDescription("Updated description");
            airavataService.updateExperiment(testAuthzToken, experimentId, retrieved);

            ExperimentModel updated = airavataService.getExperiment(experimentId);
            assertThat(updated.getDescription()).isEqualTo("Updated description");
        }
    }

    @Nested
    @DisplayName("Experiment Statistics")
    class ExperimentStatisticsTests {

        @Test
        @DisplayName("Should get experiment statistics for time range and return valid statistics object")
        void shouldGetExperimentStatistics() throws Exception {
            if (airavataService == null) {
                return;
            }

            Project project = TestDataFactory.createTestProject("Stats Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Stats Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = airavataService.createExperiment(TEST_GATEWAY_ID, experiment);

            long fromTime = AiravataUtils.getUniqueTimestamp().getTime() - 86400000;
            long toTime = AiravataUtils.getUniqueTimestamp().getTime();
            org.apache.airavata.common.model.ExperimentStatistics stats = airavataService.getExperimentStatistics(
                    TEST_GATEWAY_ID, fromTime, toTime, TEST_USERNAME, null, null, null, 10, 0);

            assertThat(stats).isNotNull();
        }
    }
}
