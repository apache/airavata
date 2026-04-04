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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.DataType;
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

public class ExperimentOutputRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentOutputRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ExperimentOutputRepository experimentOutputRepository;

    public ExperimentOutputRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentOutputRepository = new ExperimentOutputRepository();
    }

    @Test
    public void ExperimentInputRepositoryTest() throws RegistryException {
        Gateway gateway = Gateway.newBuilder().setGatewayId("gateway").build();
        gateway = gateway.toBuilder().setDomain("SEAGRID").build();
        gateway = gateway.toBuilder().setEmailAddress("abc@d.com").build();
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = Project.newBuilder().setName("projectName").build();
        project = project.toBuilder().setOwner("user").build();
        project = project.toBuilder().setGatewayId(gatewayId).build();

        String projectId = projectRepository.addProject(project, gatewayId);

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

        String experimentId = experimentRepository.addExperiment(experimentModel);
        assertTrue(experimentId != null);

        OutputDataObjectType outputDataObjectTypeExp =
                OutputDataObjectType.newBuilder().setName("outputE").build();
        outputDataObjectTypeExp =
                outputDataObjectTypeExp.toBuilder().setType(DataType.STRING).build();

        List<OutputDataObjectType> outputDataObjectTypeExpList = new ArrayList<>();
        outputDataObjectTypeExpList.add(outputDataObjectTypeExp);

        assertEquals(
                experimentId,
                experimentOutputRepository.addExperimentOutputs(outputDataObjectTypeExpList, experimentId));
        assertTrue(experimentRepository
                        .getExperiment(experimentId)
                        .getExperimentOutputsList()
                        .size()
                == 1);

        outputDataObjectTypeExp =
                outputDataObjectTypeExp.toBuilder().setValue("oValueE").build();
        experimentOutputRepository.updateExperimentOutputs(outputDataObjectTypeExpList, experimentId);

        List<OutputDataObjectType> retrievedExpOutputList =
                experimentOutputRepository.getExperimentOutputs(experimentId);
        assertTrue(retrievedExpOutputList.size() == 1);
        assertEquals("oValueE", retrievedExpOutputList.get(0).getValue());
        assertEquals(DataType.STRING, retrievedExpOutputList.get(0).getType());

        experimentRepository.removeExperiment(experimentId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
