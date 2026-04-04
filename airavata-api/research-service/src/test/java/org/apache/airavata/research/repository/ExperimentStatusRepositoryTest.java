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

import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentStatusRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ExperimentStatusRepository experimentStatusRepository;

    public ExperimentStatusRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        experimentStatusRepository = new ExperimentStatusRepository();
    }

    @Test
    public void ExperimentStatusRepositoryTest() throws RegistryException {
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
        // addExperiment adds the CREATED experiment status
        assertEquals(
                1,
                experimentRepository
                        .getExperiment(experimentId)
                        .getExperimentStatusList()
                        .size());

        ExperimentStatus experimentStatus = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_VALIDATED)
                .build();
        String experimentStatusId = experimentStatusRepository.addExperimentStatus(experimentStatus, experimentId);
        assertTrue(experimentStatusId != null);
        assertEquals(
                2,
                experimentRepository
                        .getExperiment(experimentId)
                        .getExperimentStatusList()
                        .size());

        experimentStatus = experimentStatus.toBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_EXECUTING)
                .build();
        experimentStatusRepository.updateExperimentStatus(experimentStatus, experimentId);

        ExperimentStatus updatedExecutingStatus = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_EXECUTING)
                .build();
        updatedExecutingStatus =
                updatedExecutingStatus.toBuilder().setReason("updated reason").build();
        updatedExecutingStatus = updatedExecutingStatus.toBuilder()
                .setTimeOfStateChange(experimentStatus.getTimeOfStateChange())
                .build();
        String updatedExperimentStatusId =
                experimentStatusRepository.updateExperimentStatus(updatedExecutingStatus, experimentId);

        ExperimentStatus retrievedExpStatus = experimentStatusRepository.getExperimentStatus(experimentId);
        assertEquals(ExperimentState.EXPERIMENT_STATE_EXECUTING, retrievedExpStatus.getState());
        assertEquals("updated reason", updatedExecutingStatus.getReason());

        experimentRepository.removeExperiment(experimentId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
