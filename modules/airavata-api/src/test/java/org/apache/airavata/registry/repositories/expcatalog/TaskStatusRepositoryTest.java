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
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.registry.services.TaskStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            TaskStatusRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class TaskStatusRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({})
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final TaskStatusService taskStatusService;

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
            TaskStatusService taskStatusService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.taskStatusService = taskStatusService;
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

        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setState(TaskState.EXECUTING);
        taskStatusService.addTaskStatus(taskStatus, taskId);
        assertEquals(1, taskService.getTask(taskId).getTaskStatuses().size(), "Task should have one status");

        taskStatus.setState(TaskState.CREATED);
        taskStatusService.updateTaskStatus(taskStatus, taskId);

        TaskStatus retrievedTaskStatus = taskStatusService.getTaskStatus(taskId);
        assertEquals(TaskState.CREATED, retrievedTaskStatus.getState(), "Task status should be updated to CREATED");
        assertNotNull(retrievedTaskStatus.getStatusId(), "Status ID should be set");
    }

    @Test
    public void testTaskStatusRepository_MultipleStatusHistory() throws RegistryException, InterruptedException {

        TaskStatus status1 = new TaskStatus();
        status1.setState(TaskState.CREATED);
        taskStatusService.addTaskStatus(status1, taskId);

        TaskStatus status2 = new TaskStatus();
        status2.setState(TaskState.EXECUTING);
        taskStatusService.addTaskStatus(status2, taskId);

        TaskStatus status3 = new TaskStatus();
        status3.setState(TaskState.COMPLETED);
        taskStatusService.addTaskStatus(status3, taskId);

        assertTrue(
                taskService.getTask(taskId).getTaskStatuses().size() >= 3,
                "Task should have at least 3 statuses in history");

        TaskStatus latest = taskStatusService.getTaskStatus(taskId);
        assertEquals(TaskState.COMPLETED, latest.getState(), "Latest status should be COMPLETED");

        // Verify strict timestamp ordering
        java.util.List<TaskStatus> statuses = taskService.getTask(taskId).getTaskStatuses();
        TaskStatus s1 = statuses.stream().filter(s -> s.getState() == TaskState.CREATED).findFirst().orElse(null);
        TaskStatus s2 = statuses.stream().filter(s -> s.getState() == TaskState.EXECUTING).findFirst().orElse(null);
        TaskStatus s3 = statuses.stream().filter(s -> s.getState() == TaskState.COMPLETED).findFirst().orElse(null);

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotNull(s3);

        assertTrue(s2.getTimeOfStateChange() > s1.getTimeOfStateChange(),
                "Status 2 timestamp (" + s2.getTimeOfStateChange() + ") should be greater than Status 1 (" + s1.getTimeOfStateChange() + ")");
        assertTrue(s3.getTimeOfStateChange() > s2.getTimeOfStateChange(),
                "Status 3 timestamp (" + s3.getTimeOfStateChange() + ") should be greater than Status 2 (" + s2.getTimeOfStateChange() + ")");
    }
}
