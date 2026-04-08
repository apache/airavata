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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResearchIoParamRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ResearchIoParamRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ResearchIoParamRepository researchIoParamRepository;

    public ResearchIoParamRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        researchIoParamRepository = new ResearchIoParamRepository();
    }

    private String setupExperiment(String gatewayId, String projectId) throws RegistryException {
        ExperimentModel experimentModel = ExperimentModel.newBuilder()
                .setProjectId(projectId)
                .setGatewayId(gatewayId)
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .setUserName("user")
                .setExperimentName("name")
                .setUserConfigurationData(UserConfigurationDataModel.getDefaultInstance())
                .build();
        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertNotNull(experimentId);
        return experimentId;
    }

    @Test
    public void testExperimentInputs() throws RegistryException {
        Gateway gateway = Gateway.newBuilder()
                .setGatewayId("gateway")
                .setDomain("SEAGRID")
                .setEmailAddress("abc@d.com")
                .build();
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = Project.newBuilder()
                .setName("projectName")
                .setOwner("user")
                .setGatewayId(gatewayId)
                .build();
        String projectId = projectRepository.addProject(project, gatewayId);

        String experimentId = setupExperiment(gatewayId, projectId);

        InputDataObjectType input = InputDataObjectType.newBuilder()
                .setName("inputE")
                .setType(DataType.STRING)
                .build();

        List<InputDataObjectType> inputList = new ArrayList<>();
        inputList.add(input);

        assertEquals(experimentId, researchIoParamRepository.addExperimentInputs(inputList, experimentId));
        assertEquals(
                1,
                experimentRepository
                        .getExperiment(experimentId)
                        .getExperimentInputsList()
                        .size());

        input = input.toBuilder().setValue("iValueE").build();
        inputList.set(0, input);
        researchIoParamRepository.updateExperimentInputs(inputList, experimentId);

        List<InputDataObjectType> retrieved = researchIoParamRepository.getExperimentInputs(experimentId);
        assertEquals(1, retrieved.size());
        assertEquals("iValueE", retrieved.get(0).getValue());
        assertEquals(DataType.STRING, retrieved.get(0).getType());

        experimentRepository.removeExperiment(experimentId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

    @Test
    public void testExperimentOutputs() throws RegistryException {
        Gateway gateway = Gateway.newBuilder()
                .setGatewayId("gateway2")
                .setDomain("SEAGRID")
                .setEmailAddress("abc@d.com")
                .build();
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = Project.newBuilder()
                .setName("projectName")
                .setOwner("user")
                .setGatewayId(gatewayId)
                .build();
        String projectId = projectRepository.addProject(project, gatewayId);

        String experimentId = setupExperiment(gatewayId, projectId);

        OutputDataObjectType output = OutputDataObjectType.newBuilder()
                .setName("outputE")
                .setType(DataType.STRING)
                .build();

        List<OutputDataObjectType> outputList = new ArrayList<>();
        outputList.add(output);

        assertEquals(experimentId, researchIoParamRepository.addExperimentOutputs(outputList, experimentId));
        assertEquals(
                1,
                experimentRepository
                        .getExperiment(experimentId)
                        .getExperimentOutputsList()
                        .size());

        output = output.toBuilder().setValue("oValueE").build();
        outputList.set(0, output);
        researchIoParamRepository.updateExperimentOutputs(outputList, experimentId);

        List<OutputDataObjectType> retrieved = researchIoParamRepository.getExperimentOutputs(experimentId);
        assertEquals(1, retrieved.size());
        assertEquals("oValueE", retrieved.get(0).getValue());
        assertEquals(DataType.STRING, retrieved.get(0).getType());

        experimentRepository.removeExperiment(experimentId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
