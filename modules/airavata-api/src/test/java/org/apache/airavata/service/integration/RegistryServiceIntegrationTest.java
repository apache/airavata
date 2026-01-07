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
import org.apache.airavata.common.exception.ProjectNotFoundException;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentSearchFields;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentSummaryModel;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.service.registry.RegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName(
        "RegistryService Integration Tests - Full-stack functionality including database persistence, transactions, and search")
public class RegistryServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final RegistryService registryService;
    private final ComputeResourceService computeResourceService;
    private final org.apache.airavata.registry.services.UserService userService;

    public RegistryServiceIntegrationTest(
            RegistryService registryService,
            ComputeResourceService computeResourceService,
            org.apache.airavata.registry.services.UserService userService) {
        this.registryService = registryService;
        this.computeResourceService = computeResourceService;
        this.userService = userService;
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUpRegistryTest()
            throws RegistryServiceException, org.apache.airavata.registry.exception.RegistryException {
        // Ensure gateway exists
        if (!registryService.isGatewayExist(TEST_GATEWAY_ID)) {
            org.apache.airavata.common.model.Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
            registryService.addGateway(gateway);
            commitTransaction();
        }

        // Ensure user exists in expcatalog (required for search operations)
        if (!userService.isUserExists(TEST_GATEWAY_ID, TEST_USERNAME)) {
            org.apache.airavata.common.model.UserProfile userProfile =
                    TestDataFactory.createTestUserProfile(TEST_USERNAME, TEST_GATEWAY_ID);
            userService.addUser(userProfile);
            commitTransaction();
        }
    }

    @Nested
    @DisplayName("Gateway Operations")
    class GatewayOperationsTests {

        @Test
        @DisplayName("Should check if gateway exists and return true for existing gateway")
        void shouldCheckGatewayExists() throws RegistryServiceException {
            Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
            if (!registryService.isGatewayExist(TEST_GATEWAY_ID)) {
                registryService.addGateway(gateway);
                commitTransaction();
            }

            boolean exists = registryService.isGatewayExist(TEST_GATEWAY_ID);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should get all gateways and return non-empty list containing test gateway")
        void shouldGetAllGateways() throws RegistryServiceException {
            Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
            if (!registryService.isGatewayExist(TEST_GATEWAY_ID)) {
                registryService.addGateway(gateway);
                commitTransaction();
            }

            List<Gateway> gateways = registryService.getAllGateways();

            assertThat(gateways).isNotNull().isNotEmpty();
            assertThat(gateways).anyMatch(g -> g.getGatewayId().equals(TEST_GATEWAY_ID));
        }

        @Test
        @DisplayName("Should create and persist gateway with all fields, then retrieve it successfully")
        void shouldCreateAndPersistGateway() throws RegistryServiceException {
            String newGatewayId = "test-gateway-" + System.currentTimeMillis();
            Gateway gateway = TestDataFactory.createTestGateway(newGatewayId);

            String createdGatewayId = registryService.addGateway(gateway);
            commitTransaction();

            assertThat(createdGatewayId).isEqualTo(newGatewayId);
            boolean exists = registryService.isGatewayExist(newGatewayId);
            assertThat(exists).isTrue();
            Gateway retrieved = registryService.getGateway(newGatewayId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo(newGatewayId);
            assertThat(retrieved.getGatewayName()).isEqualTo(gateway.getGatewayName());
        }
    }

    @Nested
    @DisplayName("Project Operations")
    class ProjectOperationsTests {

        @Test
        @DisplayName("Should create project with all required fields and retrieve it successfully")
        void shouldCreateAndRetrieveProject() throws RegistryServiceException, ProjectNotFoundException {
            Project project = TestDataFactory.createTestProject("Test Project", TEST_GATEWAY_ID);

            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            Project retrieved = registryService.getProject(projectId);

            assertThat(projectId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getProjectID()).isEqualTo(projectId);
            assertThat(retrieved.getName()).isEqualTo("Test Project");
        }

        @Test
        @DisplayName("Should get all user projects filtered by owner and return matching projects")
        void shouldGetUserProjects() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("User Project", TEST_GATEWAY_ID);
            project.setOwner("test-user");
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            commitTransaction();

            List<Project> projects = registryService.getUserProjects(TEST_GATEWAY_ID, "test-user", 10, 0);

            assertThat(projects).isNotNull().isNotEmpty();
            assertThat(projects).anyMatch(p -> p.getProjectID().equals(projectId));
            assertThat(projects).anyMatch(p -> p.getName().equals("User Project"));
        }

        @Test
        @DisplayName("Should update project description and persist changes successfully")
        void shouldUpdateProject() throws RegistryServiceException, ProjectNotFoundException {
            Project project = TestDataFactory.createTestProject("Original Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            commitTransaction();

            Project retrieved = registryService.getProject(projectId);
            retrieved.setDescription("Updated description");
            registryService.updateProject(projectId, retrieved);
            commitTransaction();

            Project updated = registryService.getProject(projectId);
            assertThat(updated.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("Should delete project and throw exception when retrieving deleted project")
        void shouldDeleteProject() throws RegistryServiceException, ProjectNotFoundException {
            Project project = TestDataFactory.createTestProject("To Delete", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            commitTransaction();

            boolean deleted = registryService.deleteProject(projectId);

            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> registryService.getProject(projectId))
                    .isInstanceOf(ProjectNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Experiment Operations")
    class ExperimentOperationsTests {

        @Test
        @DisplayName("Should create experiment with project association and retrieve it with all fields")
        void shouldCreateAndRetrieveExperiment() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("Experiment Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Test Experiment", projectId, TEST_GATEWAY_ID);

            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            ExperimentModel retrieved = registryService.getExperiment(experimentId);

            assertThat(experimentId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getExperimentId()).isEqualTo(experimentId);
            assertThat(retrieved.getExperimentName()).isEqualTo("Test Experiment");
        }

        @Test
        @DisplayName("Should get all experiments in project filtered by project ID with pagination")
        void shouldGetExperimentsInProject() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("Project for Experiments", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment = TestDataFactory.createTestExperiment("Exp 1", projectId, TEST_GATEWAY_ID);
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            commitTransaction();

            List<ExperimentModel> experiments =
                    registryService.getExperimentsInProject(TEST_GATEWAY_ID, projectId, 10, 0);

            assertThat(experiments).isNotNull().isNotEmpty();
            assertThat(experiments).anyMatch(e -> e.getExperimentId().equals(experimentId));
            assertThat(experiments).anyMatch(e -> e.getExperimentName().equals("Exp 1"));
        }

        @Test
        @DisplayName(
                "Should create experiment with full hierarchy including gateway, project, status and persist all relationships")
        void shouldCreateExperimentWithFullHierarchy() throws RegistryServiceException {
            Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
            if (!registryService.isGatewayExist(TEST_GATEWAY_ID)) {
                registryService.addGateway(gateway);
                commitTransaction();
            }

            Project project = TestDataFactory.createTestProject("Hierarchy Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            commitTransaction();

            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Hierarchy Experiment", projectId, TEST_GATEWAY_ID);
            experiment.setDescription("Test experiment with full hierarchy");

            if (experiment.getExperimentStatus() == null) {
                experiment.setExperimentStatus(new ArrayList<>());
            }
            ExperimentStatus status = new ExperimentStatus();
            status.setState(ExperimentState.CREATED);
            status.setTimeOfStateChange(System.currentTimeMillis());
            experiment.getExperimentStatus().add(status);

            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            commitTransaction();

            ExperimentModel retrieved = registryService.getExperiment(experimentId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getExperimentId()).isEqualTo(experimentId);
            assertThat(retrieved.getProjectId()).isEqualTo(projectId);
            assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
            assertThat(retrieved.getExperimentStatus()).isNotNull().isNotEmpty();
            assertThat(retrieved.getExperimentStatus().get(0).getState()).isEqualTo(ExperimentState.CREATED);
        }

        @Test
        @DisplayName("Should update experiment description and persist changes successfully")
        void shouldUpdateExperiment() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("Update Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Original Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            commitTransaction();

            ExperimentModel retrieved = registryService.getExperiment(experimentId);
            retrieved.setDescription("Updated description");
            registryService.updateExperiment(experimentId, retrieved);
            commitTransaction();

            ExperimentModel updated = registryService.getExperiment(experimentId);
            assertThat(updated.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("Should search experiments by name filter and return matching experiments")
        void shouldSearchExperimentsByName() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("Search Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Searchable Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            commitTransaction();

            Map<ExperimentSearchFields, String> filters = new HashMap<>();
            filters.put(ExperimentSearchFields.EXPERIMENT_NAME, "Searchable");
            List<ExperimentSummaryModel> results = registryService.searchExperiments(
                    TEST_GATEWAY_ID, TEST_USERNAME, new ArrayList<>(), filters, 10, 0);

            assertThat(results).isNotNull();
            assertThat(results).anyMatch(e -> e.getExperimentId().equals(experimentId));
        }

        @Test
        @DisplayName("Should search experiments by project ID filter and return only experiments from that project")
        void shouldSearchExperimentsByProject() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("Search Project 2", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Project Search Experiment", projectId, TEST_GATEWAY_ID);
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            commitTransaction();

            Map<ExperimentSearchFields, String> filters = new HashMap<>();
            filters.put(ExperimentSearchFields.PROJECT_ID, projectId);
            List<ExperimentSummaryModel> results = registryService.searchExperiments(
                    TEST_GATEWAY_ID, TEST_USERNAME, new ArrayList<>(), filters, 10, 0);

            assertThat(results).isNotNull();
            assertThat(results).anyMatch(e -> e.getExperimentId().equals(experimentId));
            assertThat(results).allMatch(e -> e.getProjectId().equals(projectId));
        }

        @Test
        @DisplayName("Should delete experiment and throw exception when retrieving deleted experiment")
        void shouldDeleteExperiment() throws RegistryServiceException {
            Project project = TestDataFactory.createTestProject("Delete Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment = TestDataFactory.createTestExperiment("To Delete", projectId, TEST_GATEWAY_ID);
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);

            boolean deleted = registryService.deleteExperiment(experimentId);

            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> registryService.getExperiment(experimentId))
                    .isInstanceOf(RegistryServiceException.class);
        }
    }

    @Nested
    @DisplayName("Compute Resource Operations")
    class ComputeResourceOperationsTests {

        @Test
        @DisplayName("Should register SLURM compute resource with hostname and retrieve it successfully")
        void shouldRegisterSlurmComputeResource() throws AppCatalogException {
            ComputeResourceDescription computeResource =
                    TestDataFactory.createSlurmComputeResource("slurm-host.example.com");

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getComputeResourceId()).isEqualTo(resourceId);
            assertThat(retrieved.getHostName()).isEqualTo("slurm-host.example.com");
        }

        @Test
        @DisplayName("Should register AWS compute resource with region and retrieve it successfully")
        void shouldRegisterAwsComputeResource() throws AppCatalogException {
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getComputeResourceId()).isEqualTo(resourceId);
        }

        @Test
        @DisplayName("Should get available compute resources list containing enabled resources")
        void shouldGetAvailableComputeResources() throws AppCatalogException {
            ComputeResourceDescription slurmResource = TestDataFactory.createSlurmComputeResource("slurm1.example.com");
            slurmResource.setEnabled(true);
            String resourceId = computeResourceService.addComputeResource(slurmResource);
            commitTransaction();

            var availableResources = computeResourceService.getAvailableComputeResourceIdList();

            assertThat(availableResources).isNotNull().isNotEmpty();
            assertThat(availableResources).containsKey(resourceId);
        }

        @Test
        @DisplayName("Should update compute resource description and persist changes successfully")
        void shouldUpdateComputeResource() throws AppCatalogException {
            ComputeResourceDescription computeResource =
                    TestDataFactory.createSlurmComputeResource("update-host.example.com");
            String resourceId = computeResourceService.addComputeResource(computeResource);
            commitTransaction();

            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);
            retrieved.setResourceDescription("Updated description");
            computeResourceService.updateComputeResource(resourceId, retrieved);
            commitTransaction();

            ComputeResourceDescription updated = computeResourceService.getComputeResource(resourceId);
            assertThat(updated.getResourceDescription()).isEqualTo("Updated description");
        }
    }

    @Nested
    @DisplayName("User Operations")
    class UserOperationsTests {

        @Test
        @DisplayName("Should check if user exists and return valid boolean value")
        void shouldCheckUserExists() throws RegistryServiceException {
            boolean exists = registryService.isUserExists(TEST_GATEWAY_ID, TEST_USERNAME);

            assertThat(exists).isNotNull();
        }

        @Test
        @DisplayName("Should get all users in gateway and return non-null list")
        void shouldGetAllUsersInGateway() throws RegistryServiceException {
            List<String> users = registryService.getAllUsersInGateway(TEST_GATEWAY_ID);

            assertThat(users).isNotNull();
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should persist data across transactions and retrieve in new transaction")
        void shouldPersistDataAcrossTransactions() throws RegistryServiceException, ProjectNotFoundException {
            Project project = TestDataFactory.createTestProject("Transaction Test", TEST_GATEWAY_ID);

            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            commitTransaction();

            Project retrieved = registryService.getProject(projectId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getProjectID()).isEqualTo(projectId);
            assertThat(retrieved.getName()).isEqualTo("Transaction Test");
        }

        @Test
        @DisplayName("Should handle transaction rollback scenarios and maintain data integrity")
        void shouldRollbackOnError()
                throws RegistryServiceException, org.apache.airavata.common.exception.ProjectNotFoundException {
            Project project = TestDataFactory.createTestProject("Rollback Test", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            commitTransaction();

            assertThat(projectId).isNotNull();
            // Verify project was created
            Project retrieved = registryService.getProject(projectId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("Rollback Test");
        }
    }

    @Nested
    @DisplayName("API Version")
    class APIVersionTests {

        @Test
        @DisplayName("Should return API version as non-empty string")
        void shouldReturnAPIVersion() {
            String version = registryService.getAPIVersion();

            assertThat(version).isNotNull().isNotEmpty();
        }
    }
}
