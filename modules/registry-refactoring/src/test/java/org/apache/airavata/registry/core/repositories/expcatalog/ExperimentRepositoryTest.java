/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

public class ExperimentRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;

    public ExperimentRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
    }

    @Test
    public void ExperimentRepositoryTest() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectRepository.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");
        experimentModel.setGatewayInstanceId("gateway-instance-id");

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        experimentModel.setDescription("description");
        experimentRepository.updateExperiment(experimentModel, experimentId);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals("description", retrievedExperimentModel.getDescription());
        assertEquals(ExperimentType.SINGLE_APPLICATION, retrievedExperimentModel.getExperimentType());
        assertEquals("gateway-instance-id", retrievedExperimentModel.getGatewayInstanceId());
        assertEquals(1, retrievedExperimentModel.getExperimentStatusSize());
        assertEquals(ExperimentState.CREATED, retrievedExperimentModel.getExperimentStatus().get(0).getState());

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setAiravataAutoSchedule(true);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        computationalResourceSchedulingModel.setResourceHostId("resource-host-id");
        computationalResourceSchedulingModel.setTotalCPUCount(12);
        computationalResourceSchedulingModel.setNodeCount(13);
        computationalResourceSchedulingModel.setNumberOfThreads(14);
        computationalResourceSchedulingModel.setOverrideAllocationProjectNumber("override-project-num");
        computationalResourceSchedulingModel.setOverrideLoginUserName("override-login-username");
        computationalResourceSchedulingModel.setOverrideScratchLocation("override-scratch-location");
        computationalResourceSchedulingModel.setQueueName("queue-name");
        computationalResourceSchedulingModel.setStaticWorkingDir("static-working-dir");
        computationalResourceSchedulingModel.setTotalPhysicalMemory(1333);
        computationalResourceSchedulingModel.setWallTimeLimit(77);
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        assertEquals(experimentId, experimentRepository.addUserConfigurationData(userConfigurationDataModel, experimentId));

        userConfigurationDataModel.setStorageId("storage2");
        experimentRepository.updateUserConfigurationData(userConfigurationDataModel, experimentId);

        final UserConfigurationDataModel retrievedUserConfigurationDataModel = experimentRepository.getUserConfigurationData(experimentId);
        assertEquals("storage2", retrievedUserConfigurationDataModel.getStorageId());
        final ComputationalResourceSchedulingModel retrievedComputationalResourceScheduling = retrievedUserConfigurationDataModel.getComputationalResourceScheduling();
        assertNotNull(retrievedComputationalResourceScheduling);
        assertEquals("resource-host-id", retrievedComputationalResourceScheduling.getResourceHostId());
        assertEquals( 12, retrievedComputationalResourceScheduling.getTotalCPUCount());
        assertEquals(13, retrievedComputationalResourceScheduling.getNodeCount());
        assertEquals(14, retrievedComputationalResourceScheduling.getNumberOfThreads());
        assertEquals("override-project-num", retrievedComputationalResourceScheduling.getOverrideAllocationProjectNumber());
        assertEquals("override-login-username", retrievedComputationalResourceScheduling.getOverrideLoginUserName());
        assertEquals("override-scratch-location", retrievedComputationalResourceScheduling.getOverrideScratchLocation());
        assertEquals("queue-name", retrievedComputationalResourceScheduling.getQueueName());
        assertEquals("static-working-dir", retrievedComputationalResourceScheduling.getStaticWorkingDir());
        assertEquals(1333, retrievedComputationalResourceScheduling.getTotalPhysicalMemory());
        assertEquals(77, retrievedComputationalResourceScheduling.getWallTimeLimit());

        List<String> experimentIdList = experimentRepository.getExperimentIDs(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        assertTrue(experimentIdList.size() == 1);
        assertTrue(experimentIdList.get(0).equals(experimentId));

        experimentRepository.removeExperiment(experimentId);
        assertFalse(experimentRepository.isExperimentExist(experimentId));

        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
