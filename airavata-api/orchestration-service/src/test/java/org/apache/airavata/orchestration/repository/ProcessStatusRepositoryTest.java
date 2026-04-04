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
package org.apache.airavata.orchestration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.research.util.ExperimentTestHelper;
import org.apache.airavata.research.util.ProjectTestHelper;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessStatusRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectTestHelper projectRepository;
    ExperimentTestHelper experimentRepository;
    ProcessRepository processRepository;
    ProcessStatusRepository processStatusRepository;

    public ProcessStatusRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectTestHelper();
        experimentRepository = new ExperimentTestHelper();
        processRepository = new ProcessRepository();
        processStatusRepository = new ProcessStatusRepository();
    }

    @Test
    public void ProcessStatusRepositoryTest() throws RegistryException {
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

        ProcessModel processModel =
                ProcessModel.newBuilder().setExperimentId(experimentId).build();
        String processId = processRepository.addProcess(processModel, experimentId);
        assertTrue(processId != null);

        // addProcess automatically adds the CREATED ProcessStatus
        assertTrue(
                processRepository.getProcess(processId).getProcessStatusesList().size() == 1);
        ProcessStatus processStatus =
                processRepository.getProcess(processId).getProcessStatusesList().get(0);
        assertEquals(ProcessState.PROCESS_STATE_CREATED, processStatus.getState());

        processStatus = processStatus.toBuilder()
                .setState(ProcessState.PROCESS_STATE_EXECUTING)
                .build();
        processStatusRepository.updateProcessStatus(processStatus, processId);

        ProcessStatus retrievedStatus = processStatusRepository.getProcessStatus(processId);
        assertEquals(ProcessState.PROCESS_STATE_EXECUTING, retrievedStatus.getState());

        ProcessStatus updatedStatus = ProcessStatus.newBuilder()
                .setState(ProcessState.PROCESS_STATE_MONITORING)
                .build();
        // Verify that ProcessStatus without id can be added with updateProcessStatus
        String updatedStatusId = processStatusRepository.updateProcessStatus(updatedStatus, processId);
        retrievedStatus = processStatusRepository.getProcessStatus(processId);
        assertEquals(ProcessState.PROCESS_STATE_MONITORING, retrievedStatus.getState());
        assertEquals(updatedStatusId, retrievedStatus.getStatusId());
        assertNull(retrievedStatus.getReason());

        // Verify that updating status with same ProcessState as most recent ProcessStatus will update the most recent
        // ProcessStatus
        ProcessStatus updatedStatusWithReason = ProcessStatus.newBuilder()
                .setState(ProcessState.PROCESS_STATE_MONITORING)
                .build();
        updatedStatusWithReason =
                updatedStatusWithReason.toBuilder().setReason("test-reason").build();
        String updateStatusWithReasonId =
                processStatusRepository.updateProcessStatus(updatedStatusWithReason, processId);
        retrievedStatus = processStatusRepository.getProcessStatus(processId);
        assertEquals(ProcessState.PROCESS_STATE_MONITORING, retrievedStatus.getState());
        assertEquals(updateStatusWithReasonId, retrievedStatus.getStatusId());
        assertEquals(updatedStatusId, updateStatusWithReasonId);
        assertEquals("test-reason", retrievedStatus.getReason());

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }
}
