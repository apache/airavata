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
package org.apache.airavata.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.airavata.execution.repository.ExperimentRepository;
import org.apache.airavata.execution.repository.GatewayRepository;
import org.apache.airavata.execution.repository.ProjectRepository;
import org.apache.airavata.execution.util.common.TestBase;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test template for ExperimentRepository. Demonstrates the recommended pattern for
 * writing new repository integration tests against a real MariaDB instance.
 *
 * <p>Key patterns shown here:
 * <ul>
 *   <li>Extend {@link AbstractIntegrationTest} — gets the shared Testcontainers MariaDB container</li>
 *   <li>Use {@code @BeforeAll} to initialize the DB schema and seed prerequisite data</li>
 *   <li>Use {@code @AfterAll} to clean up system property overrides</li>
 *   <li>Each {@code @Test} method tests a single behavior with a {@code @DisplayName}</li>
 * </ul>
 *
 * <p>Run with: {@code mvn test -pl airavata-api -Dgroups=integration -DexcludedGroups=""}
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExperimentRepositoryIntegrationTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepositoryIntegrationTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;

    private String gatewayId;
    private String projectId;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();

        // Seed: gateway and project are prerequisites for experiments
        Gateway gateway = new Gateway();
        gateway.setGatewayId("integration-test-gateway");
        gateway.setDomain("INTEGRATION");
        gateway.setEmailAddress("integration@test.com");
        gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("Integration Test Project");
        project.setOwner("integration-user");
        project.setGatewayId(gatewayId);
        projectId = projectRepository.addProject(project, gatewayId);

        logger.info("Test DB initialized. gatewayId={}, projectId={}", gatewayId, projectId);
    }

    // --- Helper ---

    private ExperimentModel buildExperiment(String name) {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setProjectId(projectId);
        experiment.setGatewayId(gatewayId);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experiment.setUserName("integration-user");
        experiment.setExperimentName(name);
        experiment.setGatewayInstanceId("gw-instance-1");
        experiment.setUserConfigurationData(new UserConfigurationDataModel());
        return experiment;
    }

    // --- Tests ---

    @Test
    @Order(1)
    @DisplayName("Create experiment and retrieve it by id")
    void createAndRetrieveExperiment() throws Exception {
        ExperimentModel experiment = buildExperiment("create-test-experiment");

        String experimentId = experimentRepository.addExperiment(experiment);
        assertNotNull(experimentId, "addExperiment should return a non-null id");

        ExperimentModel retrieved = experimentRepository.getExperiment(experimentId);
        assertNotNull(retrieved, "getExperiment should return the created experiment");
        assertEquals(gatewayId, retrieved.getGatewayId());
        assertEquals("integration-user", retrieved.getUserName());
        assertEquals("gw-instance-1", retrieved.getGatewayInstanceId());
        assertEquals(ExperimentType.SINGLE_APPLICATION, retrieved.getExperimentType());
        assertEquals(1, retrieved.getExperimentStatusSize(), "initial status should be set");
        assertEquals(
                ExperimentState.CREATED,
                retrieved.getExperimentStatus().get(0).getState(),
                "initial state should be CREATED");

        // Cleanup
        experimentRepository.removeExperiment(experimentId);
        assertFalse(experimentRepository.isExperimentExist(experimentId), "experiment should be deleted");
    }

    @Test
    @Order(2)
    @DisplayName("Update experiment description and email addresses")
    void updateExperimentFields() throws Exception {
        ExperimentModel experiment = buildExperiment("update-test-experiment");
        String experimentId = experimentRepository.addExperiment(experiment);

        assertEquals(
                0,
                experimentRepository.getExperiment(experimentId).getEmailAddressesSize(),
                "freshly created experiment should have no email addresses");

        experiment.setDescription("updated description");
        experiment.addToEmailAddresses("a@example.com");
        experiment.addToEmailAddresses("b@example.com");
        experimentRepository.updateExperiment(experiment, experimentId);

        ExperimentModel updated = experimentRepository.getExperiment(experimentId);
        assertEquals("updated description", updated.getDescription());
        assertEquals(2, updated.getEmailAddressesSize());
        assertEquals("a@example.com", updated.getEmailAddresses().get(0));
        assertEquals("b@example.com", updated.getEmailAddresses().get(1));

        experimentRepository.removeExperiment(experimentId);
    }

    @Test
    @Order(3)
    @DisplayName("Add and update user configuration data")
    void addAndUpdateUserConfigurationData() throws Exception {
        ExperimentModel experiment = buildExperiment("config-test-experiment");
        String experimentId = experimentRepository.addExperiment(experiment);

        ComputationalResourceSchedulingModel scheduling = new ComputationalResourceSchedulingModel();
        scheduling.setResourceHostId("host-1");
        scheduling.setTotalCPUCount(8);
        scheduling.setNodeCount(2);
        scheduling.setQueueName("batch");
        scheduling.setWallTimeLimit(60);

        UserConfigurationDataModel config = new UserConfigurationDataModel();
        config.setAiravataAutoSchedule(true);
        config.setOverrideManualScheduledParams(false);
        config.setComputationalResourceScheduling(scheduling);

        assertEquals(
                experimentId,
                experimentRepository.addUserConfigurationData(config, experimentId),
                "addUserConfigurationData should return the experimentId");

        config.setInputStorageResourceId("storage-in");
        config.setOutputStorageResourceId("storage-out");
        experimentRepository.updateUserConfigurationData(config, experimentId);

        UserConfigurationDataModel retrieved = experimentRepository.getUserConfigurationData(experimentId);
        assertEquals("storage-in", retrieved.getInputStorageResourceId());
        assertEquals("storage-out", retrieved.getOutputStorageResourceId());

        ComputationalResourceSchedulingModel retrievedScheduling = retrieved.getComputationalResourceScheduling();
        assertNotNull(retrievedScheduling);
        assertEquals("host-1", retrievedScheduling.getResourceHostId());
        assertEquals(8, retrievedScheduling.getTotalCPUCount());
        assertEquals(2, retrievedScheduling.getNodeCount());
        assertEquals("batch", retrievedScheduling.getQueueName());
        assertEquals(60, retrievedScheduling.getWallTimeLimit());

        experimentRepository.removeExperiment(experimentId);
    }

    @Test
    @Order(4)
    @DisplayName("Slashes in experiment name are replaced with underscores in generated id")
    void slashesInNameAreNormalized() throws Exception {
        ExperimentModel experiment = buildExperiment("name/forward-slash//a");
        String experimentId = experimentRepository.addExperiment(experiment);
        assertTrue(
                experimentId.startsWith("name_forward-slash__a"),
                "forward slashes should be replaced with underscores");
        experimentRepository.removeExperiment(experimentId);

        experiment = buildExperiment("name\\backward-slash\\\\a");
        experimentId = experimentRepository.addExperiment(experiment);
        assertTrue(
                experimentId.startsWith("name_backward-slash__a"),
                "backward slashes should be replaced with underscores");
        experimentRepository.removeExperiment(experimentId);
    }
}
