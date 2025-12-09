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

import java.util.List;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.error.ProjectNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.service.RegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for RegistryService (Registry and catalog operations).
 */
@DisplayName("RegistryService Integration Tests")
public class RegistryServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final RegistryService registryService;
    private final ComputeResourceService computeResourceService;

    public RegistryServiceIntegrationTest(
            RegistryService registryService, ComputeResourceService computeResourceService) {
        this.registryService = registryService;
        this.computeResourceService = computeResourceService;
    }

    @Nested
    @DisplayName("Gateway Operations")
    class GatewayOperationsTests {

        @Test
        @DisplayName("Should check if gateway exists")
        void shouldCheckGatewayExists() throws RegistryServiceException {
            // Act
            boolean exists = registryService.isGatewayExist(TEST_GATEWAY_ID);

            // Assert
            assertThat(exists).isNotNull();
        }

        @Test
        @DisplayName("Should get all gateways")
        void shouldGetAllGateways() throws RegistryServiceException {
            // Act
            List<Gateway> gateways = registryService.getAllGateways();

            // Assert
            assertThat(gateways).isNotNull();
        }
    }

    @Nested
    @DisplayName("Project Operations")
    class ProjectOperationsTests {

        @Test
        @DisplayName("Should create and retrieve project")
        void shouldCreateAndRetrieveProject() throws RegistryServiceException, ProjectNotFoundException {
            // Arrange
            Project project = TestDataFactory.createTestProject("Test Project", TEST_GATEWAY_ID);

            // Act
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            Project retrieved = registryService.getProject(projectId);

            // Assert
            assertThat(projectId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getProjectID()).isEqualTo(projectId);
            assertThat(retrieved.getName()).isEqualTo("Test Project");
        }

        @Test
        @DisplayName("Should get user projects")
        void shouldGetUserProjects() throws RegistryServiceException {
            // Arrange
            Project project = TestDataFactory.createTestProject("User Project", TEST_GATEWAY_ID);
            project.setOwner("test-user");
            registryService.createProject(TEST_GATEWAY_ID, project);

            // Act
            List<Project> projects = registryService.getUserProjects(TEST_GATEWAY_ID, "test-user", 10, 0);

            // Assert
            assertThat(projects).isNotNull();
        }
    }

    @Nested
    @DisplayName("Experiment Operations")
    class ExperimentOperationsTests {

        @Test
        @DisplayName("Should create and retrieve experiment")
        void shouldCreateAndRetrieveExperiment() throws RegistryServiceException {
            // Arrange
            Project project = TestDataFactory.createTestProject("Experiment Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment =
                    TestDataFactory.createTestExperiment("Test Experiment", projectId, TEST_GATEWAY_ID);

            // Act
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);
            ExperimentModel retrieved = registryService.getExperiment(experimentId);

            // Assert
            assertThat(experimentId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getExperimentId()).isEqualTo(experimentId);
            assertThat(retrieved.getExperimentName()).isEqualTo("Test Experiment");
        }

        @Test
        @DisplayName("Should get experiments in project")
        void shouldGetExperimentsInProject() throws RegistryServiceException {
            // Arrange
            Project project = TestDataFactory.createTestProject("Project for Experiments", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment = TestDataFactory.createTestExperiment("Exp 1", projectId, TEST_GATEWAY_ID);
            registryService.createExperiment(TEST_GATEWAY_ID, experiment);

            // Act
            List<ExperimentModel> experiments =
                    registryService.getExperimentsInProject(TEST_GATEWAY_ID, projectId, 10, 0);

            // Assert
            assertThat(experiments).isNotNull();
            assertThat(experiments.size()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should delete experiment")
        void shouldDeleteExperiment() throws RegistryServiceException {
            // Arrange
            Project project = TestDataFactory.createTestProject("Delete Project", TEST_GATEWAY_ID);
            String projectId = registryService.createProject(TEST_GATEWAY_ID, project);
            ExperimentModel experiment = TestDataFactory.createTestExperiment("To Delete", projectId, TEST_GATEWAY_ID);
            String experimentId = registryService.createExperiment(TEST_GATEWAY_ID, experiment);

            // Act
            boolean deleted = registryService.deleteExperiment(experimentId);

            // Assert
            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> registryService.getExperiment(experimentId))
                    .isInstanceOf(RegistryServiceException.class);
        }
    }

    @Nested
    @DisplayName("Compute Resource Operations")
    class ComputeResourceOperationsTests {

        @Test
        @DisplayName("Should register SLURM compute resource")
        void shouldRegisterSlurmComputeResource() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription computeResource =
                    TestDataFactory.createSlurmComputeResource("slurm-host.example.com");

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getComputeResourceId()).isEqualTo(resourceId);
            assertThat(retrieved.getHostName()).isEqualTo("slurm-host.example.com");
        }

        @Test
        @DisplayName("Should register AWS compute resource")
        void shouldRegisterAwsComputeResource() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getComputeResourceId()).isEqualTo(resourceId);
        }

        @Test
        @DisplayName("Should get available compute resources")
        void shouldGetAvailableComputeResources() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription slurmResource = TestDataFactory.createSlurmComputeResource("slurm1.example.com");
            slurmResource.setEnabled(true);
            computeResourceService.addComputeResource(slurmResource);

            // Act
            var availableResources = computeResourceService.getAvailableComputeResourceIdList();

            // Assert
            assertThat(availableResources).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Operations")
    class UserOperationsTests {

        @Test
        @DisplayName("Should check if user exists")
        void shouldCheckUserExists() throws RegistryServiceException {
            // Act
            boolean exists = registryService.isUserExists(TEST_GATEWAY_ID, TEST_USERNAME);

            // Assert
            assertThat(exists).isNotNull();
        }

        @Test
        @DisplayName("Should get all users in gateway")
        void shouldGetAllUsersInGateway() throws RegistryServiceException {
            // Act
            List<String> users = registryService.getAllUsersInGateway(TEST_GATEWAY_ID);

            // Assert
            assertThat(users).isNotNull();
        }
    }

    @Nested
    @DisplayName("API Version")
    class APIVersionTests {

        @Test
        @DisplayName("Should return API version")
        void shouldReturnAPIVersion() {
            // Act
            String version = registryService.getAPIVersion();

            // Assert
            assertThat(version).isNotNull().isNotEmpty();
        }
    }
}
