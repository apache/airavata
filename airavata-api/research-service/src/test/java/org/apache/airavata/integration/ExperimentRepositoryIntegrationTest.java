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

import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.repository.ExperimentRepository;
import org.apache.airavata.research.repository.ProjectRepository;
import org.apache.airavata.util.TestBase;
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
        Gateway gateway =
                Gateway.newBuilder().setGatewayId("integration-test-gateway").build();
        gateway = gateway.toBuilder().setDomain("INTEGRATION").build();
        gateway = gateway.toBuilder().setEmailAddress("integration@test.com").build();
        gatewayId = gatewayRepository.addGateway(gateway);

        Project project =
                Project.newBuilder().setName("Integration Test Project").build();
        project = project.toBuilder().setOwner("integration-user").build();
        project = project.toBuilder().setGatewayId(gatewayId).build();
        projectId = projectRepository.addProject(project, gatewayId);

        logger.info("Test DB initialized. gatewayId={}, projectId={}", gatewayId, projectId);
    }

    // --- Helper ---

    private ExperimentModel buildExperiment(String name) {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setProjectId(projectId).build();
        experiment = experiment.toBuilder().setGatewayId(gatewayId).build();
        experiment = experiment.toBuilder()
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        experiment = experiment.toBuilder().setUserName("integration-user").build();
        experiment = experiment.toBuilder().setExperimentName(name).build();
        experiment =
                experiment.toBuilder().setGatewayInstanceId("gw-instance-1").build();
        experiment = experiment.toBuilder()
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();
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
        assertEquals(1, retrieved.getExperimentStatusCount(), "initial status should be set");
        assertEquals(
                ExperimentState.EXPERIMENT_STATE_CREATED,
                retrieved.getExperimentStatusList().get(0).getState(),
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
                experimentRepository.getExperiment(experimentId).getEmailAddressesCount(),
                "freshly created experiment should have no email addresses");

        experiment =
                experiment.toBuilder().setDescription("updated description").build();
        experiment = experiment.toBuilder().addEmailAddresses("a@example.com").build();
        experiment = experiment.toBuilder().addEmailAddresses("b@example.com").build();
        experimentRepository.updateExperiment(experiment, experimentId);

        ExperimentModel updated = experimentRepository.getExperiment(experimentId);
        assertEquals("updated description", updated.getDescription());
        assertEquals(2, updated.getEmailAddressesCount());
        assertEquals("a@example.com", updated.getEmailAddressesList().get(0));
        assertEquals("b@example.com", updated.getEmailAddressesList().get(1));

        experimentRepository.removeExperiment(experimentId);
    }

    @Test
    @Order(3)
    @DisplayName("Add and update user configuration data")
    void addAndUpdateUserConfigurationData() throws Exception {
        ExperimentModel experiment = buildExperiment("config-test-experiment");
        String experimentId = experimentRepository.addExperiment(experiment);

        ComputationalResourceSchedulingModel scheduling = ComputationalResourceSchedulingModel.newBuilder()
                .setResourceHostId("host-1")
                .build();
        scheduling = scheduling.toBuilder().setTotalCpuCount(8).build();
        scheduling = scheduling.toBuilder().setNodeCount(2).build();
        scheduling = scheduling.toBuilder().setQueueName("batch").build();
        scheduling = scheduling.toBuilder().setWallTimeLimit(60).build();

        UserConfigurationDataModel config = UserConfigurationDataModel.newBuilder()
                .setAiravataAutoSchedule(true)
                .build();
        config = config.toBuilder().setOverrideManualScheduledParams(false).build();
        config = config.toBuilder()
                .setComputationalResourceScheduling(scheduling)
                .build();

        assertEquals(
                experimentId,
                experimentRepository.addUserConfigurationData(config, experimentId),
                "addUserConfigurationData should return the experimentId");

        config = config.toBuilder().setInputStorageResourceId("storage-in").build();
        config = config.toBuilder().setOutputStorageResourceId("storage-out").build();
        experimentRepository.updateUserConfigurationData(config, experimentId);

        UserConfigurationDataModel retrieved = experimentRepository.getUserConfigurationData(experimentId);
        assertEquals("storage-in", retrieved.getInputStorageResourceId());
        assertEquals("storage-out", retrieved.getOutputStorageResourceId());

        ComputationalResourceSchedulingModel retrievedScheduling = retrieved.getComputationalResourceScheduling();
        assertNotNull(retrievedScheduling);
        assertEquals("host-1", retrievedScheduling.getResourceHostId());
        assertEquals(8, retrievedScheduling.getTotalCpuCount());
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
