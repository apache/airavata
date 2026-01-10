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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProcessStatusRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessStatusService processStatusService;

    public ProcessStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessStatusService processStatusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processStatusService = processStatusService;
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

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        String processId = processService.addProcess(processModel, experimentId);
        assertTrue(processId != null);

        assertTrue(processService.getProcess(processId).getProcessStatuses().size() == 1);
        ProcessStatus processStatus =
                processService.getProcess(processId).getProcessStatuses().get(0);
        assertEquals(ProcessState.CREATED, processStatus.getState());

        processStatus.setState(ProcessState.EXECUTING);
        processStatusService.updateProcessStatus(processStatus, processId);

        ProcessStatus retrievedStatus = processStatusService.getProcessStatus(processId);
        assertEquals(ProcessState.EXECUTING, retrievedStatus.getState());

        ProcessStatus updatedStatus = new ProcessStatus(ProcessState.MONITORING);

        processStatusService.updateProcessStatus(updatedStatus, processId);
        retrievedStatus = processStatusService.getProcessStatus(processId);
        assertEquals(ProcessState.MONITORING, retrievedStatus.getState());
        assertTrue(retrievedStatus.getStatusId() != null);
        assertNull(retrievedStatus.getReason());

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

    @Test
    public void testProcessStatusRepository_MultipleStatusHistory() throws RegistryException, InterruptedException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-multi-" + java.util.UUID.randomUUID().toString());
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

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        String processId = processService.addProcess(processModel, experimentId);

        ProcessStatus status1 = new ProcessStatus(ProcessState.CREATED);
        processStatusService.addProcessStatus(status1, processId);

        ProcessStatus status2 = new ProcessStatus(ProcessState.EXECUTING);
        processStatusService.addProcessStatus(status2, processId);

        ProcessStatus status3 = new ProcessStatus(ProcessState.COMPLETED);
        processStatusService.addProcessStatus(status3, processId);

        assertTrue(
                processService.getProcess(processId).getProcessStatuses().size() >= 3,
                "Process should have at least 3 statuses in history");

        ProcessStatus latest = processStatusService.getProcessStatus(processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Latest status should be COMPLETED");

        // Verify strict timestamp ordering
        java.util.List<ProcessStatus> statuses = processService.getProcess(processId).getProcessStatuses();
        ProcessStatus s1 = statuses.stream().filter(s -> s.getState() == ProcessState.CREATED).findFirst().orElse(null);
        ProcessStatus s2 = statuses.stream().filter(s -> s.getState() == ProcessState.EXECUTING).findFirst().orElse(null);
        ProcessStatus s3 = statuses.stream().filter(s -> s.getState() == ProcessState.COMPLETED).findFirst().orElse(null);

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotNull(s3);

        assertTrue(s2.getTimeOfStateChange() > s1.getTimeOfStateChange(),
                "Status 2 timestamp (" + s2.getTimeOfStateChange() + ") should be greater than Status 1 (" + s1.getTimeOfStateChange() + ")");
        assertTrue(s3.getTimeOfStateChange() > s2.getTimeOfStateChange(),
                "Status 3 timestamp (" + s3.getTimeOfStateChange() + ") should be greater than Status 2 (" + s2.getTimeOfStateChange() + ")");
    }
}
