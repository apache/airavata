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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.StatusRepository;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class TaskStatusRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final StatusRepository statusRepository;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;
    private String taskId;

    public TaskStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            StatusRepository statusRepository) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.statusRepository = statusRepository;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("test@example.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("testProject");
        project.setOwner("testUser");
        project.setGatewayId(gatewayId);
        projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment");
        experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        processId = processService.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);
        taskId = taskService.addTask(taskModel, processId);
        assertNotNull(taskId, "Task ID should not be null");
    }

    @Test
    public void testTaskStatusRepository_StateTransitions() throws RegistryException {

        String statusId1 = "TASK_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity taskStatus = new StatusEntity(statusId1, taskId, StatusParentType.TASK, TaskState.EXECUTING.name());
        statusRepository.save(taskStatus);
        // Clear JPA cache to ensure fresh load with the newly added status
        flushAndClear();
        
        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(taskId, StatusParentType.TASK);
        assertEquals(1, statuses.size(), "Task should have one status");
        assertEquals(taskId, statuses.get(0).getParentId(), "Parent ID should match task ID");
        assertEquals(StatusParentType.TASK, statuses.get(0).getParentType(), "Parent type should be TASK");

        String statusId2 = "TASK_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity createdStatus = new StatusEntity(statusId2, taskId, StatusParentType.TASK, TaskState.CREATED.name());
        statusRepository.save(createdStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(taskId, StatusParentType.TASK);
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        StatusEntity retrievedTaskStatus = latestStatusOpt.get();
        assertEquals(TaskState.CREATED.name(), retrievedTaskStatus.getState(), "Task status should be updated to CREATED");
        assertNotNull(retrievedTaskStatus.getStatusId(), "Status ID should be set");
        assertEquals(taskId, retrievedTaskStatus.getParentId(), "Parent ID should match task ID");
        assertEquals(StatusParentType.TASK, retrievedTaskStatus.getParentType(), "Parent type should be TASK");
    }

    @Test
    public void testTaskStatusRepository_MultipleStatusHistory() throws RegistryException, InterruptedException {

        String statusId1 = "TASK_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status1 = new StatusEntity(statusId1, taskId, StatusParentType.TASK, TaskState.CREATED.name());
        statusRepository.save(status1);

        Thread.sleep(10); // Ensure timestamp difference

        String statusId2 = "TASK_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status2 = new StatusEntity(statusId2, taskId, StatusParentType.TASK, TaskState.EXECUTING.name());
        statusRepository.save(status2);

        Thread.sleep(10); // Ensure timestamp difference

        String statusId3 = "TASK_STATUS_" + AiravataUtils.getId("STATUS");
        StatusEntity status3 = new StatusEntity(statusId3, taskId, StatusParentType.TASK, TaskState.COMPLETED.name());
        statusRepository.save(status3);

        // Clear JPA cache to ensure fresh load with the newly added statuses
        flushAndClear();
        
        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(taskId, StatusParentType.TASK);
        assertTrue(statuses.size() >= 3, "Task should have at least 3 statuses in history");

        java.util.Optional<StatusEntity> latestOpt = statusRepository.findLatestByParentIdAndParentType(taskId, StatusParentType.TASK);
        assertTrue(latestOpt.isPresent(), "Latest status should exist");
        StatusEntity latest = latestOpt.get();
        assertEquals(TaskState.COMPLETED.name(), latest.getState(), "Latest status should be COMPLETED");

        // Verify strict timestamp ordering
        StatusEntity s1 = statuses.stream()
                .filter(s -> TaskState.CREATED.name().equals(s.getState()))
                .findFirst()
                .orElse(null);
        StatusEntity s2 = statuses.stream()
                .filter(s -> TaskState.EXECUTING.name().equals(s.getState()))
                .findFirst()
                .orElse(null);
        StatusEntity s3 = statuses.stream()
                .filter(s -> TaskState.COMPLETED.name().equals(s.getState()))
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
