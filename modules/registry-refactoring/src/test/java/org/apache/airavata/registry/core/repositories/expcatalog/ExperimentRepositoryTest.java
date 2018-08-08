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
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class ExperimentRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;

    public ExperimentRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
    }

    private Gateway createSampleGateway(String tag) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway" + tag);
        gateway.setDomain("SEAGRID" + tag);
        gateway.setEmailAddress("abc@d + " + tag + "+.com");
        return gateway;
    }

    private Project createSampleProject(String tag) {
        Project project = new Project();
        project.setName("projectName" + tag);
        project.setOwner("user" + tag);
        return project;
    }

    private ExperimentModel createSampleExperiment(String projectId, String gatewayId, String tag) {
        InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
        inputDataObjectTypeExp.setName("inputE");
        inputDataObjectTypeExp.setType(DataType.STRING);

        OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
        outputDataObjectTypeExp.setName("outputE");
        outputDataObjectTypeExp.setType(DataType.STRING);

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        ProcessModel processModel = new ProcessModel();
        processModel.setProcessId("Id");

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.addToExperimentInputs(inputDataObjectTypeExp);
        experimentModel.addToExperimentOutputs(outputDataObjectTypeExp);
        experimentModel.addToErrors(errorModel);
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.addToProcesses(processModel);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user" + tag);
        experimentModel.setExperimentName("name" + tag);
        experimentModel.setGatewayInstanceId("gateway-instance-id" + tag);
        return experimentModel;
    }

    @Test
    public void addExperimentRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ExperimentModel retreivedExperimentModel = experimentRepository.getExperiment(experimentId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel, retreivedExperimentModel,"__isset_bitfield", "experimentInputs", "experimentOutputs", "experimentStatus", "errors", "processes"));

        //Individual Object Comparison for the excluded fields, UserconfigurationData is tested in the updateExperimentRepositoryTest()
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getExperimentInputs(), retreivedExperimentModel.getExperimentInputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getExperimentOutputs(), retreivedExperimentModel.getExperimentOutputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getErrors(), retreivedExperimentModel.getErrors(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getProcesses(), retreivedExperimentModel.getProcesses(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getExperimentStatus(), retreivedExperimentModel.getExperimentStatus(), "__isset_bitfield"));
    }

    @Test
    public void updateExperimentRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

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

        experimentRepository.addUserConfigurationData(userConfigurationDataModel, experimentId);
        experimentModel.setUserConfigurationData(userConfigurationDataModel);
        userConfigurationDataModel.setStorageId("storage2");
        experimentRepository.updateUserConfigurationData(userConfigurationDataModel, experimentId);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel, retrievedExperimentModel,"__isset_bitfield", "userConfigurationData", "experimentInputs", "experimentOutputs", "experimentStatus", "errors", "processes"));
        //computationalResourceScheduling
        //Individual Object Comparison for the excluded fields, UserconfigurationData is tested in the updateExperimentRepositoryTest()
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getExperimentInputs(), retrievedExperimentModel.getExperimentInputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getExperimentOutputs(), retrievedExperimentModel.getExperimentOutputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getErrors(), retrievedExperimentModel.getErrors(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getProcesses(), retrievedExperimentModel.getProcesses(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getExperimentStatus(), retrievedExperimentModel.getExperimentStatus(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getUserConfigurationData(), retrievedExperimentModel.getUserConfigurationData(), "__isset_bitfield", "computationalResourceScheduling"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(experimentModel.getUserConfigurationData().getComputationalResourceScheduling(), retrievedExperimentModel.getUserConfigurationData().getComputationalResourceScheduling(), "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleExperimentRepositoryTest() throws RegistryException {
            List<ExperimentModel> actualexperimentModelList = new ArrayList<>();
            List<String> experimentIdList = new ArrayList<>();

            for (int i = 0 ; i < 5; i++) {
                Gateway gateway = createSampleGateway("" + i);
                String gatewayId = gatewayRepository.addGateway(gateway);
                Assert.assertNotNull(gatewayId);

                Project project = createSampleProject("" + i);
                String projectId = projectRepository.addProject(project, gatewayId);
                Assert.assertNotNull(projectId);

                ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
                String experimentId = experimentRepository.addExperiment(experimentModel);
                Assert.assertNotNull(experimentId);
                experimentIdList.add(experimentId);

                List<String> experimentIdTempList = experimentRepository.getExperimentIDs(DBConstants.Experiment.GATEWAY_ID, gatewayId);
                assertEquals(1, experimentIdTempList.size());

                actualexperimentModelList.add(experimentModel);

            }

            for (int j = 0 ; j < 5; j++) {
                ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentIdList.get(j));
                ExperimentModel actualExperimentModel = actualexperimentModelList.get(j);
                Assert.assertTrue(EqualsBuilder.reflectionEquals(actualExperimentModel, retrievedExperimentModel,"__isset_bitfield", "experimentInputs", "experimentOutputs", "experimentStatus", "errors", "processes"));
            }
    }

    @Test
    public void retrieveMultipleExperimentRepositoryTest() throws RegistryException {
        List<String> experimentIdList = new ArrayList<>();
        HashMap<String, ExperimentModel> actualExperimentModelMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);
            experimentIdList.add(experimentId);

            List<String> experimentIdTempList = experimentRepository.getExperimentIDs(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            assertEquals(1, experimentIdTempList.size());

            actualExperimentModelMap.put(experimentId,experimentModel);
        }

        for (int j = 0 ; j < 5; j++) {
            ExperimentModel actualExperimentModel = experimentRepository.getExperiment(experimentIdList.get(j));
            ExperimentModel savedExperimentModel = actualExperimentModelMap.get(experimentIdList.get(j));

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualExperimentModel, savedExperimentModel,"__isset_bitfield", "experimentInputs", "experimentOutputs", "experimentStatus", "errors", "processes"));
        }
    }

}
