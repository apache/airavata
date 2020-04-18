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

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class ExperimentRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;

    private String gatewayId;

    private String projectId;

    public ExperimentRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        projectId = projectRepository.addProject(project, gatewayId);
    }

    @Test
    public void ExperimentRepositoryTest() throws RegistryException {

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");
        experimentModel.setGatewayInstanceId("gateway-instance-id");

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);
        assertEquals(0, experimentRepository.getExperiment(experimentId).getEmailAddressesSize());

        experimentModel.setDescription("description");
        experimentModel.addToEmailAddresses("notify@example.com");
        experimentModel.addToEmailAddresses("notify2@example.com");
        experimentRepository.updateExperiment(experimentModel, experimentId);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals("description", retrievedExperimentModel.getDescription());
        assertEquals(ExperimentType.SINGLE_APPLICATION, retrievedExperimentModel.getExperimentType());
        assertEquals("gateway-instance-id", retrievedExperimentModel.getGatewayInstanceId());
        assertEquals(1, retrievedExperimentModel.getExperimentStatusSize());
        assertEquals(ExperimentState.CREATED, retrievedExperimentModel.getExperimentStatus().get(0).getState());
        assertEquals(2, retrievedExperimentModel.getEmailAddressesSize());
        assertEquals("notify@example.com", retrievedExperimentModel.getEmailAddresses().get(0));
        assertEquals("notify2@example.com", retrievedExperimentModel.getEmailAddresses().get(1));

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

        experimentRepository.removeExperiment(experimentId);
        assertFalse(experimentRepository.isExperimentExist(experimentId));
    }

    @Test
    public void testExperimentInputs() throws RegistryException {

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");
        experimentModel.setGatewayInstanceId("gateway-instance-id");

        InputDataObjectType input1 = new InputDataObjectType();
        input1.setName("name1");
        input1.setIsRequired(true);
        input1.setType(DataType.STRING);
        input1.setInputOrder(0);
        input1.setApplicationArgument("-arg1");
        input1.setDataStaged(true);
        input1.setIsReadOnly(true);
        input1.setMetaData("{\"foo\": 123}");
        input1.setRequiredToAddedToCommandLine(true);
        input1.setStandardInput(true);
        input1.setStorageResourceId("storageResourceId");
        input1.setUserFriendlyDescription("First argument");
        input1.setValue("value1");
        input1.setOverrideFilename("gaussian.com");
        experimentModel.addToExperimentInputs(input1);

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals(1, retrievedExperimentModel.getExperimentInputsSize());
        InputDataObjectType retrievedInput1 = retrievedExperimentModel.getExperimentInputs().get(0);
        assertEquals("name1", retrievedInput1.getName());
        assertTrue(retrievedInput1.isIsRequired());
        assertEquals(DataType.STRING, retrievedInput1.getType());
        assertEquals(0, retrievedInput1.getInputOrder());
        assertEquals("-arg1", retrievedInput1.getApplicationArgument());
        assertTrue(retrievedInput1.isDataStaged());
        assertTrue(retrievedInput1.isIsReadOnly());
        assertEquals("{\"foo\": 123}", retrievedInput1.getMetaData());
        assertTrue(retrievedInput1.isRequiredToAddedToCommandLine());
        assertTrue(retrievedInput1.isStandardInput());
        assertEquals("storageResourceId", retrievedInput1.getStorageResourceId());
        assertEquals("First argument", retrievedInput1.getUserFriendlyDescription());
        assertEquals("value1", retrievedInput1.getValue());
        assertEquals("gaussian.com", retrievedInput1.getOverrideFilename());

        // Update values of the input
        retrievedInput1.setIsRequired(false);
        retrievedInput1.setType(DataType.URI);
        retrievedInput1.setInputOrder(1);
        retrievedInput1.setApplicationArgument("-arg1a");
        retrievedInput1.setDataStaged(false);
        retrievedInput1.setIsReadOnly(false);
        retrievedInput1.setMetaData("{\"bar\": 456}");
        retrievedInput1.setRequiredToAddedToCommandLine(false);
        retrievedInput1.setStandardInput(false);
        retrievedInput1.setStorageResourceId("storageResourceId2");
        retrievedInput1.setUserFriendlyDescription("First argument~");
        retrievedInput1.setValue("value1a");
        retrievedInput1.setOverrideFilename("gaussian.com-updated");

        experimentRepository.updateExperiment(retrievedExperimentModel, experimentId);

        retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals(1, retrievedExperimentModel.getExperimentInputsSize());
        retrievedInput1 = retrievedExperimentModel.getExperimentInputs().get(0);
        assertFalse(retrievedInput1.isIsRequired());
        assertEquals(DataType.URI, retrievedInput1.getType());
        assertEquals(1, retrievedInput1.getInputOrder());
        assertEquals("-arg1a", retrievedInput1.getApplicationArgument());
        assertFalse(retrievedInput1.isDataStaged());
        assertFalse(retrievedInput1.isIsReadOnly());
        assertEquals("{\"bar\": 456}", retrievedInput1.getMetaData());
        assertFalse(retrievedInput1.isRequiredToAddedToCommandLine());
        assertFalse(retrievedInput1.isStandardInput());
        assertEquals("storageResourceId2", retrievedInput1.getStorageResourceId());
        assertEquals("First argument~", retrievedInput1.getUserFriendlyDescription());
        assertEquals("value1a", retrievedInput1.getValue());
        assertEquals("gaussian.com-updated", retrievedInput1.getOverrideFilename());

        experimentRepository.removeExperiment(experimentId);
        assertFalse(experimentRepository.isExperimentExist(experimentId));
    }

    /**
     * Verify that slashes (forward and backward) are replaced with underscores.
     */
    @Test
    public void testSlashesInExperimentName() throws RegistryException {

        // Forward slashes
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name/forward-slash//a");

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId.startsWith("name_forward-slash__a"));

        // Backward slashes
        experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name\\backward-slash\\\\a");

        experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId.startsWith("name_backward-slash__a"));
    }
}
