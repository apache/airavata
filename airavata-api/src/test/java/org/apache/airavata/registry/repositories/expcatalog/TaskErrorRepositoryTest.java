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

import java.util.List;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskErrorService;
import org.apache.airavata.registry.services.TaskService;
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
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            TaskErrorRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false"
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class TaskErrorRepositoryTest extends TestBase {

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
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final TaskErrorService taskErrorService;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;
    private String taskId;

    public TaskErrorRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            TaskErrorService taskErrorService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.taskErrorService = taskErrorService;
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
    public void testTaskErrorRepository_CreateAndUpdate() throws RegistryException {
        // Test creating and updating task errors
        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error-1");
        errorModel.setActualErrorMessage("Initial error message");
        errorModel.setUserFriendlyMessage("User-friendly initial message");

        String taskErrorId = taskErrorService.addTaskError(errorModel, taskId);
        assertNotNull(taskErrorId, "Task error ID should not be null");
        assertEquals("error-1", taskErrorId, "Error ID should match");

        // Verify error is associated with task
        assertTrue(taskService.getTask(taskId).getTaskErrors().size() == 1, "Task should have one error");

        // Update error message
        errorModel.setActualErrorMessage("Updated error message");
        errorModel.setUserFriendlyMessage("Updated user-friendly message");
        taskErrorService.updateTaskError(errorModel, taskId);

        // Verify update
        List<ErrorModel> retrievedErrorList = taskErrorService.getTaskError(taskId);
        assertEquals(1, retrievedErrorList.size(), "Should have one error");
        assertEquals(
                "Updated error message",
                retrievedErrorList.get(0).getActualErrorMessage(),
                "Error message should be updated");
        assertEquals(
                "Updated user-friendly message",
                retrievedErrorList.get(0).getUserFriendlyMessage(),
                "User-friendly message should be updated");
    }

    @Test
    public void testTaskErrorRepository_MultipleErrorsPerTask() throws RegistryException {
        // Test that a task can have multiple errors (important for error history)
        ErrorModel error1 = new ErrorModel();
        error1.setErrorId("error-1");
        error1.setActualErrorMessage("First error");
        String errorId1 = taskErrorService.addTaskError(error1, taskId);

        ErrorModel error2 = new ErrorModel();
        error2.setErrorId("error-2");
        error2.setActualErrorMessage("Second error");
        String errorId2 = taskErrorService.addTaskError(error2, taskId);

        ErrorModel error3 = new ErrorModel();
        error3.setErrorId("error-3");
        error3.setActualErrorMessage("Third error");
        String errorId3 = taskErrorService.addTaskError(error3, taskId);

        // Verify all errors are associated with the task
        List<ErrorModel> errors = taskErrorService.getTaskError(taskId);
        assertEquals(3, errors.size(), "Task should have 3 errors");

        // Verify all error IDs are present
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId1)), "Error 1 should be present");
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId2)), "Error 2 should be present");
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId3)), "Error 3 should be present");
    }
}
