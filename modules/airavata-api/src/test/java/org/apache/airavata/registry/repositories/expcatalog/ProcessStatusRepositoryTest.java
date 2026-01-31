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
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.StatusRepository;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
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
    private final StatusRepository statusRepository;

    public ProcessStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            StatusRepository statusRepository) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.statusRepository = statusRepository;
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

        java.util.List<StatusEntity> initialStatuses = statusRepository.findByParentIdAndParentType(processId, StatusParentType.PROCESS);
        assertEquals(1, initialStatuses.size(), "Process should have initial CREATED status");
        StatusEntity initialStatus = initialStatuses.get(0);
        assertEquals(ProcessState.CREATED.name(), initialStatus.getState(), "Initial status should be CREATED");
        assertEquals(processId, initialStatus.getParentId(), "Parent ID should match process ID");
        assertEquals(StatusParentType.PROCESS, initialStatus.getParentType(), "Parent type should be PROCESS");

        String statusId1 = "PROC_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity executingStatus = new StatusEntity(statusId1, processId, StatusParentType.PROCESS, ProcessState.EXECUTING.name());
        statusRepository.save(executingStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(processId, StatusParentType.PROCESS);
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        StatusEntity retrievedStatus = latestStatusOpt.get();
        assertEquals(ProcessState.EXECUTING.name(), retrievedStatus.getState(), "Status should be EXECUTING");

        String statusId2 = "PROC_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity monitoringStatus = new StatusEntity(statusId2, processId, StatusParentType.PROCESS, ProcessState.MONITORING.name());
        statusRepository.save(monitoringStatus);
        flushAndClear();

        latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(processId, StatusParentType.PROCESS);
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        retrievedStatus = latestStatusOpt.get();
        assertEquals(ProcessState.MONITORING.name(), retrievedStatus.getState(), "Status should be MONITORING");
        assertNotNull(retrievedStatus.getStatusId(), "Status ID should not be null");
        assertNull(retrievedStatus.getReason(), "Reason should be null");

        String statusId3 = "PROC_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity monitoringStatusWithReason = new StatusEntity(statusId3, processId, StatusParentType.PROCESS, ProcessState.MONITORING.name());
        monitoringStatusWithReason.setReason("test-reason");
        statusRepository.save(monitoringStatusWithReason);
        flushAndClear();

        latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(processId, StatusParentType.PROCESS);
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        retrievedStatus = latestStatusOpt.get();
        assertEquals(ProcessState.MONITORING.name(), retrievedStatus.getState(), "Status should be MONITORING");
        assertEquals("test-reason", retrievedStatus.getReason(), "Reason should be set");

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

        String statusId1 = "PROC_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status1 = new StatusEntity(statusId1, processId, StatusParentType.PROCESS, ProcessState.CREATED.name());
        statusRepository.save(status1);

        Thread.sleep(10); // Ensure timestamp difference

        String statusId2 = "PROC_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status2 = new StatusEntity(statusId2, processId, StatusParentType.PROCESS, ProcessState.EXECUTING.name());
        statusRepository.save(status2);

        Thread.sleep(10); // Ensure timestamp difference

        String statusId3 = "PROC_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status3 = new StatusEntity(statusId3, processId, StatusParentType.PROCESS, ProcessState.COMPLETED.name());
        statusRepository.save(status3);
        flushAndClear();

        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(processId, StatusParentType.PROCESS);
        assertTrue(statuses.size() >= 3, "Process should have at least 3 statuses in history");

        java.util.Optional<StatusEntity> latestOpt = statusRepository.findLatestByParentIdAndParentType(processId, StatusParentType.PROCESS);
        assertTrue(latestOpt.isPresent(), "Latest status should exist");
        StatusEntity latest = latestOpt.get();
        assertEquals(ProcessState.COMPLETED.name(), latest.getState(), "Latest status should be COMPLETED");

        // Verify strict timestamp ordering
        StatusEntity s1 = statuses.stream()
                .filter(s -> ProcessState.CREATED.name().equals(s.getState()))
                .findFirst()
                .orElse(null);
        StatusEntity s2 = statuses.stream()
                .filter(s -> ProcessState.EXECUTING.name().equals(s.getState()))
                .findFirst()
                .orElse(null);
        StatusEntity s3 = statuses.stream()
                .filter(s -> ProcessState.COMPLETED.name().equals(s.getState()))
                .findFirst()
                .orElse(null);

        assertNotNull(s1, "CREATED status should exist");
        assertNotNull(s2, "EXECUTING status should exist");
        assertNotNull(s3, "COMPLETED status should exist");

        // Verify sequence number ordering (sequence numbers guarantee deterministic creation order)
        assertTrue(
                s2.getSequenceNum() > s1.getSequenceNum(),
                "Status 2 sequence (" + s2.getSequenceNum() + ") should be greater than Status 1 ("
                        + s1.getSequenceNum() + ")");
        assertTrue(
                s3.getSequenceNum() > s2.getSequenceNum(),
                "Status 3 sequence (" + s3.getSequenceNum() + ") should be greater than Status 2 ("
                        + s2.getSequenceNum() + ")");
    }
}
