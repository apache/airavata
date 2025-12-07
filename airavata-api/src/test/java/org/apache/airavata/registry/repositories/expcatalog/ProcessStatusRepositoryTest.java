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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class ProcessStatusRepositoryTest extends TestBase {

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ProjectService projectService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessStatusService processStatusService;

    public ProcessStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void testProcessStatusRepository() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");

        String experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processService.addProcess(processModel, experimentId);
        assertTrue(processId != null);

        // addProcess automatically adds the CREATED ProcessStatus
        assertTrue(processService.getProcess(processId).getProcessStatuses().size() == 1);
        ProcessStatus processStatus =
                processService.getProcess(processId).getProcessStatuses().get(0);
        assertEquals(ProcessState.CREATED, processStatus.getState());

        processStatus.setState(ProcessState.EXECUTING);
        processStatusService.updateProcessStatus(processStatus, processId);

        ProcessStatus retrievedStatus = processStatusService.getProcessStatus(processId);
        assertEquals(ProcessState.EXECUTING, retrievedStatus.getState());

        ProcessStatus updatedStatus = new ProcessStatus(ProcessState.MONITORING);
        // Verify that ProcessStatus without id can be added with updateProcessStatus
        processStatusService.updateProcessStatus(updatedStatus, processId);
        retrievedStatus = processStatusService.getProcessStatus(processId);
        assertEquals(ProcessState.MONITORING, retrievedStatus.getState());
        assertTrue(retrievedStatus.getStatusId() != null);
        assertNull(retrievedStatus.getReason());

        // Verify that updating status with same ProcessState as most recent ProcessStatus will update the most recent
        // ProcessStatus
        ProcessStatus updatedStatusWithReason = new ProcessStatus(ProcessState.MONITORING);
        updatedStatusWithReason.setReason("test-reason");
        processStatusService.updateProcessStatus(updatedStatusWithReason, processId);
        retrievedStatus = processStatusService.getProcessStatus(processId);
        assertEquals(ProcessState.MONITORING, retrievedStatus.getState());
        assertEquals("test-reason", retrievedStatus.getReason());

        experimentService.removeExperiment(experimentId);
        processService.removeProcess(processId);
        gatewayService.removeGateway(gatewayId);
        projectService.removeProject(projectId);
    }
}
