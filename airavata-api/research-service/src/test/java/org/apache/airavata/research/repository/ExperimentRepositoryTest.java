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
package org.apache.airavata.research.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;

    private String gatewayId;

    private String projectId;

    public ExperimentRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Gateway gateway = Gateway.newBuilder().setGatewayId("gateway").build();
        gateway = gateway.toBuilder().setDomain("SEAGRID").build();
        gateway = gateway.toBuilder().setEmailAddress("abc@d.com").build();
        gatewayId = gatewayRepository.addGateway(gateway);

        Project project = Project.newBuilder().setName("projectName").build();
        project = project.toBuilder().setOwner("user").build();
        project = project.toBuilder().setGatewayId(gatewayId).build();

        projectId = projectRepository.addProject(project, gatewayId);
    }

    @Test
    public void ExperimentRepositoryTest() throws RegistryException {

        ExperimentModel experimentModel =
                ExperimentModel.newBuilder().setProjectId(projectId).build();
        experimentModel = experimentModel.toBuilder().setGatewayId(gatewayId).build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        experimentModel = experimentModel.toBuilder().setUserName("user").build();
        experimentModel = experimentModel.toBuilder().setExperimentName("name").build();
        experimentModel = experimentModel.toBuilder()
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();
        experimentModel = experimentModel.toBuilder()
                .setGatewayInstanceId("gateway-instance-id")
                .build();

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);
        assertEquals(0, experimentRepository.getExperiment(experimentId).getEmailAddressesCount());

        experimentModel =
                experimentModel.toBuilder().setDescription("description").build();
        experimentModel = experimentModel.toBuilder()
                .addEmailAddresses("notify@example.com")
                .build();
        experimentModel = experimentModel.toBuilder()
                .addEmailAddresses("notify2@example.com")
                .build();
        experimentRepository.updateExperiment(experimentModel, experimentId);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals("description", retrievedExperimentModel.getDescription());
        assertEquals(ExperimentType.SINGLE_APPLICATION, retrievedExperimentModel.getExperimentType());
        assertEquals("gateway-instance-id", retrievedExperimentModel.getGatewayInstanceId());
        assertEquals(1, retrievedExperimentModel.getExperimentStatusCount());
        assertEquals(
                ExperimentState.EXPERIMENT_STATE_CREATED,
                retrievedExperimentModel.getExperimentStatusList().get(0).getState());
        assertEquals(2, retrievedExperimentModel.getEmailAddressesCount());
        assertEquals(
                "notify@example.com",
                retrievedExperimentModel.getEmailAddressesList().get(0));
        assertEquals(
                "notify2@example.com",
                retrievedExperimentModel.getEmailAddressesList().get(1));

        UserConfigurationDataModel userConfigurationDataModel = UserConfigurationDataModel.newBuilder()
                .setAiravataAutoSchedule(true)
                .build();
        userConfigurationDataModel = userConfigurationDataModel.toBuilder()
                .setOverrideManualScheduledParams(false)
                .build();
        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                ComputationalResourceSchedulingModel.newBuilder()
                        .setResourceHostId("resource-host-id")
                        .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setTotalCpuCount(12)
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setNodeCount(13)
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setNumberOfThreads(14)
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setOverrideAllocationProjectNumber("override-project-num")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setOverrideLoginUserName("override-login-username")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setOverrideScratchLocation("override-scratch-location")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setQueueName("queue-name")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setStaticWorkingDir("static-working-dir")
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setTotalPhysicalMemory(1333)
                .build();
        computationalResourceSchedulingModel = computationalResourceSchedulingModel.toBuilder()
                .setWallTimeLimit(77)
                .build();
        userConfigurationDataModel = userConfigurationDataModel.toBuilder()
                .setComputationalResourceScheduling(computationalResourceSchedulingModel)
                .build();
        assertEquals(
                experimentId, experimentRepository.addUserConfigurationData(userConfigurationDataModel, experimentId));

        userConfigurationDataModel = userConfigurationDataModel.toBuilder()
                .setInputStorageResourceId("storage2")
                .build();
        userConfigurationDataModel = userConfigurationDataModel.toBuilder()
                .setOutputStorageResourceId("storage2")
                .build();
        experimentRepository.updateUserConfigurationData(userConfigurationDataModel, experimentId);

        final UserConfigurationDataModel retrievedUserConfigurationDataModel =
                experimentRepository.getUserConfigurationData(experimentId);
        assertEquals("storage2", retrievedUserConfigurationDataModel.getInputStorageResourceId());
        assertEquals("storage2", retrievedUserConfigurationDataModel.getOutputStorageResourceId());
        final ComputationalResourceSchedulingModel retrievedComputationalResourceScheduling =
                retrievedUserConfigurationDataModel.getComputationalResourceScheduling();
        assertNotNull(retrievedComputationalResourceScheduling);
        assertEquals("resource-host-id", retrievedComputationalResourceScheduling.getResourceHostId());
        assertEquals(12, retrievedComputationalResourceScheduling.getTotalCpuCount());
        assertEquals(13, retrievedComputationalResourceScheduling.getNodeCount());
        assertEquals(14, retrievedComputationalResourceScheduling.getNumberOfThreads());
        assertEquals(
                "override-project-num", retrievedComputationalResourceScheduling.getOverrideAllocationProjectNumber());
        assertEquals("override-login-username", retrievedComputationalResourceScheduling.getOverrideLoginUserName());
        assertEquals(
                "override-scratch-location", retrievedComputationalResourceScheduling.getOverrideScratchLocation());
        assertEquals("queue-name", retrievedComputationalResourceScheduling.getQueueName());
        assertEquals("static-working-dir", retrievedComputationalResourceScheduling.getStaticWorkingDir());
        assertEquals(1333, retrievedComputationalResourceScheduling.getTotalPhysicalMemory());
        assertEquals(77, retrievedComputationalResourceScheduling.getWallTimeLimit());

        experimentRepository.removeExperiment(experimentId);
        assertFalse(experimentRepository.isExperimentExist(experimentId));
    }

    @Test
    public void testExperimentInputs() throws RegistryException {

        ExperimentModel experimentModel =
                ExperimentModel.newBuilder().setProjectId(projectId).build();
        experimentModel = experimentModel.toBuilder().setGatewayId(gatewayId).build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        experimentModel = experimentModel.toBuilder().setUserName("user").build();
        experimentModel = experimentModel.toBuilder().setExperimentName("name").build();
        experimentModel = experimentModel.toBuilder()
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();
        experimentModel = experimentModel.toBuilder()
                .setGatewayInstanceId("gateway-instance-id")
                .build();

        InputDataObjectType input1 =
                InputDataObjectType.newBuilder().setName("name1").build();
        input1 = input1.toBuilder().setIsRequired(true).build();
        input1 = input1.toBuilder().setType(DataType.STRING).build();
        input1 = input1.toBuilder().setInputOrder(0).build();
        input1 = input1.toBuilder().setApplicationArgument("-arg1").build();
        input1 = input1.toBuilder().setDataStaged(true).build();
        input1 = input1.toBuilder().setIsReadOnly(true).build();
        input1 = input1.toBuilder().setMetaData("{\"foo\": 123}").build();
        input1 = input1.toBuilder().setRequiredToAddedToCommandLine(true).build();
        input1 = input1.toBuilder().setStandardInput(true).build();
        input1 = input1.toBuilder().setStorageResourceId("storageResourceId").build();
        input1 = input1.toBuilder().setUserFriendlyDescription("First argument").build();
        input1 = input1.toBuilder().setValue("value1").build();
        input1 = input1.toBuilder().setOverrideFilename("gaussian.com").build();
        experimentModel =
                experimentModel.toBuilder().addExperimentInputs(input1).build();

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals(1, retrievedExperimentModel.getExperimentInputsCount());
        InputDataObjectType retrievedInput1 =
                retrievedExperimentModel.getExperimentInputsList().get(0);
        assertEquals("name1", retrievedInput1.getName());
        assertTrue(retrievedInput1.getIsRequired());
        assertEquals(DataType.STRING, retrievedInput1.getType());
        assertEquals(0, retrievedInput1.getInputOrder());
        assertEquals("-arg1", retrievedInput1.getApplicationArgument());
        assertTrue(retrievedInput1.getDataStaged());
        assertTrue(retrievedInput1.getIsReadOnly());
        assertEquals("{\"foo\": 123}", retrievedInput1.getMetaData());
        assertTrue(retrievedInput1.getRequiredToAddedToCommandLine());
        assertTrue(retrievedInput1.getStandardInput());
        assertEquals("storageResourceId", retrievedInput1.getStorageResourceId());
        assertEquals("First argument", retrievedInput1.getUserFriendlyDescription());
        assertEquals("value1", retrievedInput1.getValue());
        assertEquals("gaussian.com", retrievedInput1.getOverrideFilename());

        // Update values of the input
        retrievedInput1 = retrievedInput1.toBuilder().setIsRequired(false).build();
        retrievedInput1 = retrievedInput1.toBuilder().setType(DataType.URI).build();
        retrievedInput1 = retrievedInput1.toBuilder().setInputOrder(1).build();
        retrievedInput1 =
                retrievedInput1.toBuilder().setApplicationArgument("-arg1a").build();
        retrievedInput1 = retrievedInput1.toBuilder().setDataStaged(false).build();
        retrievedInput1 = retrievedInput1.toBuilder().setIsReadOnly(false).build();
        retrievedInput1 =
                retrievedInput1.toBuilder().setMetaData("{\"bar\": 456}").build();
        retrievedInput1 = retrievedInput1.toBuilder()
                .setRequiredToAddedToCommandLine(false)
                .build();
        retrievedInput1 = retrievedInput1.toBuilder().setStandardInput(false).build();
        retrievedInput1 = retrievedInput1.toBuilder()
                .setStorageResourceId("storageResourceId2")
                .build();
        retrievedInput1 = retrievedInput1.toBuilder()
                .setUserFriendlyDescription("First argument~")
                .build();
        retrievedInput1 = retrievedInput1.toBuilder().setValue("value1a").build();
        retrievedInput1 = retrievedInput1.toBuilder()
                .setOverrideFilename("gaussian.com-updated")
                .build();

        experimentRepository.updateExperiment(retrievedExperimentModel, experimentId);

        retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals(1, retrievedExperimentModel.getExperimentInputsCount());
        retrievedInput1 = retrievedExperimentModel.getExperimentInputsList().get(0);
        assertFalse(retrievedInput1.getIsRequired());
        assertEquals(DataType.URI, retrievedInput1.getType());
        assertEquals(1, retrievedInput1.getInputOrder());
        assertEquals("-arg1a", retrievedInput1.getApplicationArgument());
        assertFalse(retrievedInput1.getDataStaged());
        assertFalse(retrievedInput1.getIsReadOnly());
        assertEquals("{\"bar\": 456}", retrievedInput1.getMetaData());
        assertFalse(retrievedInput1.getRequiredToAddedToCommandLine());
        assertFalse(retrievedInput1.getStandardInput());
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
        ExperimentModel experimentModel =
                ExperimentModel.newBuilder().setProjectId(projectId).build();
        experimentModel = experimentModel.toBuilder().setGatewayId(gatewayId).build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        experimentModel = experimentModel.toBuilder().setUserName("user").build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentName("name/forward-slash//a")
                .build();
        experimentModel = experimentModel.toBuilder()
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId.startsWith("name_forward-slash__a"));

        // Backward slashes
        experimentModel = ExperimentModel.newBuilder().setProjectId(projectId).build();
        experimentModel = experimentModel.toBuilder().setGatewayId(gatewayId).build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        experimentModel = experimentModel.toBuilder().setUserName("user").build();
        experimentModel = experimentModel.toBuilder()
                .setExperimentName("name\\backward-slash\\\\a")
                .build();
        experimentModel = experimentModel.toBuilder()
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();

        experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId.startsWith("name_backward-slash__a"));
    }
}
