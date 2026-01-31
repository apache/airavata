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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.StatusRepository;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.common.utils.AiravataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ExperimentStatusRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final StatusRepository statusRepository;

    private String gatewayId;
    private String projectId;
    private String experimentId;

    public ExperimentStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            StatusRepository statusRepository) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.statusRepository = statusRepository;
    }

    @BeforeEach
    public void setUp() throws RegistryException, RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("test@example.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("testProject");
        project.setOwner("testUser");
        project.setGatewayId(gatewayId);
        projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment");
        experimentId = experimentService.addExperiment(experimentModel);
        assertNotNull(experimentId, "Experiment ID should not be null");
    }

    @Test
    public void testExperimentStatusRepository_InitialStatusCreation() throws RegistryException, RegistryException {

        assertEquals(
                1,
                experimentService
                        .getExperiment(experimentId)
                        .getExperimentStatus()
                        .size(),
                "Experiment should have initial CREATED status");

        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(experimentId, StatusParentType.EXPERIMENT);
        assertNotNull(statuses, "Statuses list should not be null");
        assertEquals(1, statuses.size(), "Experiment should have initial CREATED status");
        
        StatusEntity initialStatus = statuses.get(0);
        assertNotNull(initialStatus, "Initial status should exist");
        assertEquals(ExperimentState.CREATED.name(), initialStatus.getState(), "Initial status should be CREATED");
        assertEquals(experimentId, initialStatus.getParentId(), "Parent ID should match experiment ID");
        assertEquals(StatusParentType.EXPERIMENT, initialStatus.getParentType(), "Parent type should be EXPERIMENT");
    }

    @Test
    public void testExperimentStatusRepository_StateTransitions() throws RegistryException, RegistryException {

        String statusId1 = "EXP_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity validatedStatus = new StatusEntity(statusId1, experimentId, StatusParentType.EXPERIMENT, ExperimentState.VALIDATED.name());
        statusRepository.save(validatedStatus);
        // Clear JPA cache to ensure fresh load with the newly added status
        flushAndClear();
        
        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(experimentId, StatusParentType.EXPERIMENT);
        assertEquals(2, statuses.size(), "Experiment should have 2 statuses");

        String statusId2 = "EXP_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity executingStatus = new StatusEntity(statusId2, experimentId, StatusParentType.EXPERIMENT, ExperimentState.EXECUTING.name());
        statusRepository.save(executingStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(experimentId, StatusParentType.EXPERIMENT);
        assertNotNull(latestStatusOpt, "Latest status optional should not be null");
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        StatusEntity retrievedStatus = latestStatusOpt.get();
        assertEquals(ExperimentState.EXECUTING.name(), retrievedStatus.getState(), "Status should be updated to EXECUTING");
        assertEquals(experimentId, retrievedStatus.getParentId(), "Parent ID should match experiment ID");
        assertEquals(StatusParentType.EXPERIMENT, retrievedStatus.getParentType(), "Parent type should be EXPERIMENT");
    }

    @Test
    public void testExperimentStatusRepository_StatusUpdateWithReason() throws RegistryException {

        String statusId1 = "EXP_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status = new StatusEntity(statusId1, experimentId, StatusParentType.EXPERIMENT, ExperimentState.EXECUTING.name());
        statusRepository.save(status);
        flushAndClear();

        String statusId2 = "EXP_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity updatedStatus = new StatusEntity(statusId2, experimentId, StatusParentType.EXPERIMENT, ExperimentState.EXECUTING.name());
        updatedStatus.setReason("Updated execution reason");
        statusRepository.save(updatedStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(experimentId, StatusParentType.EXPERIMENT);
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        StatusEntity retrievedStatus = latestStatusOpt.get();
        assertEquals(ExperimentState.EXECUTING.name(), retrievedStatus.getState(), "Status should remain EXECUTING");
        assertEquals("Updated execution reason", retrievedStatus.getReason(), "Reason should be updated");
        assertEquals(experimentId, retrievedStatus.getParentId(), "Parent ID should match experiment ID");
        assertEquals(StatusParentType.EXPERIMENT, retrievedStatus.getParentType(), "Parent type should be EXPERIMENT");
    }
}
