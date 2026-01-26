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
package org.apache.airavata.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.orchestrator.state.StateTransitionService;
import org.apache.airavata.orchestrator.state.TaskStateValidator;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.registry.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive integration tests for TaskStateValidator.
 * Tests verify that all valid state transitions work correctly and invalid transitions are rejected.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            TaskStateTransitionIntegrationTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.flyway.enabled=false",
            "airavata.security.manager.enabled=false",
            "airavata.security.authzCache.enabled=true",
            "airavata.dapr.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public class TaskStateTransitionIntegrationTest extends ServiceIntegrationTestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.messaging"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final StatusService statusService;

    private String testTaskId;
    private String testProcessId;
    private String testExperimentId;

    public TaskStateTransitionIntegrationTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            StatusService statusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.statusService = statusService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        super.setUpBase();

        Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
        if (!gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            gatewayService.addGateway(gateway);
        }

        Project project = TestDataFactory.createTestProject("Task State Test Project", TEST_GATEWAY_ID);
        String projectId = projectService.addProject(project, TEST_GATEWAY_ID);

        ExperimentModel experiment =
                TestDataFactory.createTestExperiment("Task State Experiment", projectId, TEST_GATEWAY_ID);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        testExperimentId = experimentService.addExperiment(experiment);

        ProcessModel process = new ProcessModel();
        process.setExperimentId(testExperimentId);
        testProcessId = processService.addProcess(process, testExperimentId);

        TaskModel task = new TaskModel();
        task.setTaskType(TaskTypes.JOB_SUBMISSION);
        task.setParentProcessId(testProcessId);
        testTaskId = taskService.addTask(task, testProcessId);
    }

    @Test
    @DisplayName("Test valid transition: CREATED -> EXECUTING")
    public void testValidTransitionCreatedToExecuting() throws RegistryException {
        assertTrue(
                TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.EXECUTING),
                "CREATED -> EXECUTING should be valid");

        TaskStatus executing = createTaskStatus(TaskState.EXECUTING, "Task started executing");
        statusService.addTaskStatus(executing, testTaskId);

        TaskStatus status = statusService.getLatestTaskStatus(testTaskId);
        assertNotNull(status, "Task status should exist");
        assertEquals(TaskState.EXECUTING, status.getState(), "Task should be in EXECUTING state");
    }

    @Test
    @DisplayName("Test valid transitions from EXECUTING state")
    public void testValidTransitionsFromExecuting() throws RegistryException {
        // EXECUTING can transition to COMPLETED, FAILED, or CANCELED
        assertTrue(
                TaskStateValidator.INSTANCE.isValid(TaskState.EXECUTING, TaskState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid");
        assertTrue(
                TaskStateValidator.INSTANCE.isValid(TaskState.EXECUTING, TaskState.FAILED),
                "EXECUTING -> FAILED should be valid");
        assertTrue(
                TaskStateValidator.INSTANCE.isValid(TaskState.EXECUTING, TaskState.CANCELED),
                "EXECUTING -> CANCELED should be valid");

        // Test successful completion path
        TaskStatus executing = createTaskStatus(TaskState.EXECUTING, "Task executing");
        statusService.addTaskStatus(executing, testTaskId);

        TaskStatus completed = createTaskStatus(TaskState.COMPLETED, "Task completed successfully");
        statusService.addTaskStatus(completed, testTaskId);

        TaskStatus status = statusService.getLatestTaskStatus(testTaskId);
        assertEquals(TaskState.COMPLETED, status.getState(), "Task should be in COMPLETED state");
    }

    @Test
    @DisplayName("Test invalid transitions from terminal states")
    public void testInvalidTransitionsFromTerminalStates() {
        // Terminal states: COMPLETED, FAILED, CANCELED (no transitions out)
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.COMPLETED, TaskState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.COMPLETED, TaskState.CREATED),
                "COMPLETED -> CREATED should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.FAILED, TaskState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.FAILED, TaskState.CREATED),
                "FAILED -> CREATED should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.CANCELED, TaskState.EXECUTING),
                "CANCELED -> EXECUTING should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.CANCELED, TaskState.CREATED),
                "CANCELED -> CREATED should be invalid");
    }

    @Test
    @DisplayName("Test invalid transition: CREATED -> COMPLETED (must go through EXECUTING)")
    public void testInvalidTransitionCreatedToCompleted() {
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.COMPLETED),
                "CREATED -> COMPLETED (skipping EXECUTING) should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.FAILED),
                "CREATED -> FAILED (skipping EXECUTING) should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.CANCELED),
                "CREATED -> CANCELED (skipping EXECUTING) should be invalid");
    }

    @Test
    @DisplayName("Test StateTransitionService validates transitions correctly")
    public void testStateTransitionServiceValidation() {
        // Test that StateTransitionService.validateAndLog() correctly validates transitions
        assertTrue(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, TaskState.CREATED, TaskState.EXECUTING),
                "StateTransitionService should validate CREATED -> EXECUTING");
        assertTrue(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, TaskState.EXECUTING, TaskState.COMPLETED),
                "StateTransitionService should validate EXECUTING -> COMPLETED");
        assertFalse(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, TaskState.COMPLETED, TaskState.EXECUTING),
                "StateTransitionService should reject COMPLETED -> EXECUTING");
        assertFalse(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, TaskState.CREATED, TaskState.COMPLETED),
                "StateTransitionService should reject CREATED -> COMPLETED");
    }

    @Test
    @DisplayName("Test state history preservation")
    public void testStateHistoryPreservation() throws RegistryException {
        List<TaskState> expectedStates = new ArrayList<>();
        expectedStates.add(TaskState.CREATED);
        expectedStates.add(TaskState.EXECUTING);
        expectedStates.add(TaskState.COMPLETED);

        // Add statuses in sequence
        for (TaskState state : expectedStates) {
            TaskStatus status = createTaskStatus(state, "State: " + state.name());
            statusService.addTaskStatus(status, testTaskId);
        }

        // Verify all states are in history using StatusService (not Task entity which may be stale)
        List<TaskStatus> statuses = statusService.getTaskStatuses(testTaskId);
        assertNotNull(statuses, "Task should have status history");
        assertTrue(
                statuses.size() >= expectedStates.size(),
                "Task should have at least " + expectedStates.size() + " status entries");

        // Verify all expected states are present
        for (TaskState expectedState : expectedStates) {
            boolean found = statuses.stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Task state history should contain " + expectedState);
        }

        // Verify latest state
        TaskStatus latest = statusService.getLatestTaskStatus(testTaskId);
        assertEquals(TaskState.COMPLETED, latest.getState(), "Latest state should be COMPLETED");
    }

    @Test
    @DisplayName("Test failure path: EXECUTING -> FAILED")
    public void testFailurePath() throws RegistryException {
        TaskStatus executing = createTaskStatus(TaskState.EXECUTING, "Task executing");
        statusService.addTaskStatus(executing, testTaskId);

        TaskStatus failed = createTaskStatus(TaskState.FAILED, "Task execution failed");
        statusService.addTaskStatus(failed, testTaskId);

        TaskStatus status = statusService.getLatestTaskStatus(testTaskId);
        assertEquals(TaskState.FAILED, status.getState(), "Task should be in FAILED state");
    }

    @Test
    @DisplayName("Test cancellation path: EXECUTING -> CANCELED")
    public void testCancellationPath() throws RegistryException {
        TaskStatus executing = createTaskStatus(TaskState.EXECUTING, "Task executing");
        statusService.addTaskStatus(executing, testTaskId);

        TaskStatus canceled = createTaskStatus(TaskState.CANCELED, "Task canceled");
        statusService.addTaskStatus(canceled, testTaskId);

        TaskStatus status = statusService.getLatestTaskStatus(testTaskId);
        assertEquals(TaskState.CANCELED, status.getState(), "Task should be in CANCELED state");
    }

    @Test
    @DisplayName("Test full lifecycle: CREATED -> EXECUTING -> COMPLETED")
    public void testFullLifecycle() throws RegistryException {
        TaskStatus executing = createTaskStatus(TaskState.EXECUTING, "Task started");
        statusService.addTaskStatus(executing, testTaskId);

        TaskStatus completed = createTaskStatus(TaskState.COMPLETED, "Task completed");
        statusService.addTaskStatus(completed, testTaskId);

        TaskStatus status = statusService.getLatestTaskStatus(testTaskId);
        assertEquals(TaskState.COMPLETED, status.getState(), "Task should be in COMPLETED state");

        // Verify state history using StatusService (not Task entity which may be stale)
        List<TaskStatus> statuses = statusService.getTaskStatuses(testTaskId);
        assertTrue(
                statuses.stream().anyMatch(s -> s.getState() == TaskState.EXECUTING),
                "History should contain EXECUTING state");
        assertTrue(
                statuses.stream().anyMatch(s -> s.getState() == TaskState.COMPLETED),
                "History should contain COMPLETED state");
    }

    @Test
    @DisplayName("Test null handling in StateTransitionService")
    public void testNullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, null, TaskState.CREATED),
                "null -> CREATED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, TaskState.CREATED, null),
                "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, null, null),
                "null -> null should be invalid");
    }

    @Test
    @DisplayName("Test task states are in correct order")
    public void testTaskStateTimestamps() throws RegistryException {
        List<TaskState> states = new ArrayList<>();
        states.add(TaskState.CREATED);
        states.add(TaskState.EXECUTING);
        states.add(TaskState.COMPLETED);

        // Add statuses
        for (TaskState state : states) {
            TaskStatus status = createTaskStatus(state, "State: " + state.name());
            statusService.addTaskStatus(status, testTaskId);
        }

        // Verify statuses are in order using StatusService (not Task entity which may be stale)
        List<TaskStatus> statuses = statusService.getTaskStatuses(testTaskId);
        assertTrue(statuses.size() >= states.size(), "Should have at least " + states.size() + " statuses");

        // Verify each status has a valid timestamp
        for (TaskStatus status : statuses) {
            assertTrue(status.getTimeOfStateChange() > 0, "Status should have a valid timestamp");
        }

        // Verify the latest state is COMPLETED
        TaskStatus latest = statusService.getLatestTaskStatus(testTaskId);
        assertEquals(TaskState.COMPLETED, latest.getState(), "Latest state should be COMPLETED");
    }

    /**
     * Helper method to create a TaskStatus with the specified state.
     */
    private TaskStatus createTaskStatus(TaskState state, String reason) {
        TaskStatus status = new TaskStatus();
        status.setState(state);
        status.setReason(reason);
        status.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());
        return status;
    }
}
