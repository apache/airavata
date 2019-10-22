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
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProcessStatusRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessStatusRepositoryTest.class);

    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    ProcessStatusRepository processStatusRepository;

    public ProcessStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
        processStatusRepository = new ProcessStatusRepository();
    }

    @Test
    public void ProcessStatusRepositoryTest() throws RegistryException {
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

        String experimentId = experimentRepository.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);
        assertTrue(processId != null);

        // addProcess automatically adds the CREATED ProcessStatus
        assertTrue(processRepository.getProcess(processId).getProcessStatuses().size() == 1);
        ProcessStatus processStatus = processRepository.getProcess(processId).getProcessStatuses().get(0);
        assertEquals(ProcessState.CREATED, processStatus.getState());

        processStatus.setState(ProcessState.EXECUTING);
        processStatusRepository.updateProcessStatus(processStatus, processId);

        ProcessStatus retrievedStatus = processStatusRepository.getProcessStatus(processId);
        assertEquals(ProcessState.EXECUTING, retrievedStatus.getState());

        ProcessStatus updatedStatus = new ProcessStatus(ProcessState.MONITORING);
        // Verify that ProcessStatus without id can be added with updateProcessStatus
        String updatedStatusId = processStatusRepository.updateProcessStatus(updatedStatus, processId);
        retrievedStatus = processStatusRepository.getProcessStatus(processId);
        assertEquals(ProcessState.MONITORING, retrievedStatus.getState());
        assertEquals(updatedStatusId, retrievedStatus.getStatusId());
        assertNull(retrievedStatus.getReason());

        // Verify that updating status with same ProcessState as most recent ProcessStatus will update the most recent ProcessStatus
        ProcessStatus updatedStatusWithReason = new ProcessStatus(ProcessState.MONITORING);
        updatedStatusWithReason.setReason("test-reason");
        String updateStatusWithReasonId = processStatusRepository.updateProcessStatus(updatedStatusWithReason, processId);
        retrievedStatus = processStatusRepository.getProcessStatus(processId);
        assertEquals(ProcessState.MONITORING, retrievedStatus.getState());
        assertEquals(updateStatusWithReasonId, retrievedStatus.getStatusId());
        assertEquals(updatedStatusId, updateStatusWithReasonId);
        assertEquals("test-reason", retrievedStatus.getReason());


        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        gatewayRepository.removeGateway(gatewayId);
        projectRepository.removeProject(projectId);
    }

}
