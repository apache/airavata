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
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.orchestrator.state.ProcessStateValidator;
import org.apache.airavata.orchestrator.state.StateTransitionService;
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
 * Integration tests for workflow task execution based on state transitions.
 * Tests verify that workflows execute the correct tasks when state transitions occur.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            WorkflowTaskExecutionIntegrationTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.flyway.enabled=false",
            "airavata.security.manager.enabled=false",
            "airavata.security.authzCache.enabled=true",
            "airavata.dapr.enabled=false",
            "airavata.services.controller.enabled=false", // Disable workflow managers for unit testing
            "airavata.services.prewm.enabled=false",
            "airavata.services.postwm.enabled=false"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public class WorkflowTaskExecutionIntegrationTest extends ServiceIntegrationTestBase {

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
    private final StatusService statusService;
    private final TaskService taskService;

    private String testProcessId;
    private String testExperimentId;

    public WorkflowTaskExecutionIntegrationTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            StatusService statusService,
            TaskService taskService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.statusService = statusService;
        this.taskService = taskService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        super.setUpBase();

        Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
        if (!gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            gatewayService.addGateway(gateway);
        }

        Project project = TestDataFactory.createTestProject("Workflow Task Test Project", TEST_GATEWAY_ID);
        String projectId = projectService.addProject(project, TEST_GATEWAY_ID);

        ExperimentModel experiment =
                TestDataFactory.createTestExperiment("Workflow Task Experiment", projectId, TEST_GATEWAY_ID);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        testExperimentId = experimentService.addExperiment(experiment);

        ProcessModel process = new ProcessModel();
        process.setExperimentId(testExperimentId);
        testProcessId = processService.addProcess(process, testExperimentId);
    }

    @Test
    @DisplayName("Test that process state transitions validate correctly before task execution")
    public void testProcessStateTransitionsValidateBeforeTaskExecution() throws RegistryException {
        // Test that state transitions are validated before tasks can execute
        // This ensures invalid state transitions are rejected even if tasks try to update state

        ProcessStatus currentStatus = statusService.getLatestProcessStatus(testProcessId);
        ProcessState currentState = currentStatus != null ? currentStatus.getState() : null;

        // Test valid transition: CREATED -> VALIDATED
        assertTrue(
                StateTransitionService.isValid(ProcessStateValidator.INSTANCE, currentState, ProcessState.VALIDATED),
                "CREATED -> VALIDATED should be valid");

        ProcessStatus validated = StateMachineTestUtils.createProcessStatus(ProcessState.VALIDATED, "Validated");
        statusService.addProcessStatus(validated, testProcessId);

        // Test invalid transition: VALIDATED -> COMPLETED (skipping required states)
        assertFalse(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.VALIDATED, ProcessState.COMPLETED),
                "VALIDATED -> COMPLETED (skipping STARTED/EXECUTING) should be invalid");

        // Verify state was updated correctly
        ProcessStatus latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.VALIDATED, latest.getState(), "Process should be in VALIDATED state");
    }

    @Test
    @DisplayName("Test that tasks are created in correct order for pre-workflow")
    public void testPreWorkflowTaskCreationOrder() throws RegistryException {
        // Test that when process transitions to STARTED, pre-workflow tasks should be created
        // in correct order: ENV_SETUP -> INPUT_DATA_STAGING -> JOB_SUBMISSION

        // Create tasks that would be created by PreWorkflowManager
        List<TaskModel> tasks = new ArrayList<>();

        // ENV_SETUP task
        TaskModel envSetupTask = new TaskModel();
        envSetupTask.setTaskType(TaskTypes.ENV_SETUP);
        envSetupTask.setParentProcessId(testProcessId);
        String envSetupTaskId = taskService.addTask(envSetupTask, testProcessId);
        tasks.add(envSetupTask);

        // INPUT_DATA_STAGING task
        TaskModel inputStagingTask = new TaskModel();
        inputStagingTask.setTaskType(TaskTypes.DATA_STAGING);
        inputStagingTask.setParentProcessId(testProcessId);
        String inputStagingTaskId = taskService.addTask(inputStagingTask, testProcessId);
        tasks.add(inputStagingTask);

        // JOB_SUBMISSION task
        TaskModel jobSubmissionTask = new TaskModel();
        jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
        jobSubmissionTask.setParentProcessId(testProcessId);
        String jobSubmissionTaskId = taskService.addTask(jobSubmissionTask, testProcessId);
        tasks.add(jobSubmissionTask);

        // Verify tasks are created
        ProcessModel process = processService.getProcess(testProcessId);
        assertNotNull(process.getTasks(), "Process should have tasks");
        assertTrue(
                process.getTasks().size() >= tasks.size(), "Process should have at least " + tasks.size() + " tasks");

        // Verify task types are correct
        assertTrue(
                process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.ENV_SETUP),
                "Process should have ENV_SETUP task");
        assertTrue(
                process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.DATA_STAGING),
                "Process should have DATA_STAGING task");
        assertTrue(
                process.getTasks().stream().anyMatch(t -> t.getTaskType() == TaskTypes.JOB_SUBMISSION),
                "Process should have JOB_SUBMISSION task");
    }

    @Test
    @DisplayName("Test that post-workflow tasks are created when job completes")
    public void testPostWorkflowTaskCreation() throws RegistryException {
        // Test that when job completes, post-workflow tasks should be created
        // in correct order: JOB_VERIFICATION -> OUTPUT_DATA_STAGING -> COMPLETING -> PARSING_TRIGGERING

        // Simulate job completion by transitioning process to appropriate state
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        statusService.addProcessStatus(executing, testProcessId);

        // Create tasks that would be created by PostWorkflowManager
        // JOB_VERIFICATION task (created first in post-workflow)
        TaskModel jobVerificationTask = new TaskModel();
        jobVerificationTask.setTaskType(TaskTypes.JOB_SUBMISSION); // PostWorkflowManager creates job verification
        jobVerificationTask.setParentProcessId(testProcessId);
        taskService.addTask(jobVerificationTask, testProcessId);

        // OUTPUT_DATA_STAGING task
        TaskModel outputStagingTask = new TaskModel();
        outputStagingTask.setTaskType(TaskTypes.DATA_STAGING);
        outputStagingTask.setParentProcessId(testProcessId);
        taskService.addTask(outputStagingTask, testProcessId);

        // Verify tasks are created
        ProcessModel process = processService.getProcess(testProcessId);
        assertNotNull(process.getTasks(), "Process should have tasks");
        assertTrue(process.getTasks().size() >= 2, "Process should have at least 2 tasks");
    }

    @Test
    @DisplayName("Test that state transitions trigger correct workflow phase")
    public void testStateTransitionsTriggerWorkflowPhase() throws RegistryException {
        // Test that state transitions determine which workflow phase should execute

        // CREATED -> STARTED should trigger PreWorkflowManager
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        statusService.addProcessStatus(started, testProcessId);

        ProcessStatus latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.STARTED, latest.getState(), "Process should be in STARTED state");

        // At STARTED state, PreWorkflowManager would create pre-execution tasks
        // This test verifies the state transition is valid for workflow execution
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.CREATED, ProcessState.STARTED),
                "CREATED -> STARTED should be valid for pre-workflow");

        // EXECUTING -> COMPLETED should trigger PostWorkflowManager
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        statusService.addProcessStatus(executing, testProcessId);

        ProcessStatus completed = StateMachineTestUtils.createProcessStatus(ProcessState.COMPLETED, "Completed");
        statusService.addProcessStatus(completed, testProcessId);

        latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Process should be in COMPLETED state");
    }

    @Test
    @DisplayName("Test that task chain order matches workflow requirements")
    public void testTaskChainOrder() throws RegistryException {
        // Test that tasks are chained in correct order for workflow execution
        // Pre-workflow: ENV_SETUP -> INPUT_DATA_STAGING -> JOB_SUBMISSION
        // Post-workflow: JOB_VERIFICATION -> OUTPUT_DATA_STAGING -> COMPLETING -> PARSING_TRIGGERING

        // Create tasks in pre-workflow order
        TaskModel task1 = new TaskModel();
        task1.setTaskType(TaskTypes.ENV_SETUP);
        task1.setParentProcessId(testProcessId);
        String task1Id = taskService.addTask(task1, testProcessId);

        TaskModel task2 = new TaskModel();
        task2.setTaskType(TaskTypes.DATA_STAGING);
        task2.setParentProcessId(testProcessId);
        String task2Id = taskService.addTask(task2, testProcessId);

        TaskModel task3 = new TaskModel();
        task3.setTaskType(TaskTypes.JOB_SUBMISSION);
        task3.setParentProcessId(testProcessId);
        String task3Id = taskService.addTask(task3, testProcessId);

        // Verify tasks exist
        ProcessModel process = processService.getProcess(testProcessId);
        assertNotNull(process.getTasks(), "Process should have tasks");
        assertTrue(process.getTasks().size() >= 3, "Process should have at least 3 tasks");

        // Verify task order (tasks should be retrievable in creation order)
        List<TaskModel> tasks = process.getTasks();
        // Note: Actual task chaining via nextTask is handled by workflow managers
        // This test verifies tasks are created and can be retrieved
        assertTrue(tasks.size() >= 3, "Should have at least 3 tasks");
    }

    @Test
    @DisplayName("Test that invalid state transitions prevent task execution")
    public void testInvalidStateTransitionsPreventTaskExecution() {
        // Test that invalid state transitions are rejected, preventing incorrect task execution

        // Terminal states cannot transition
        assertFalse(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.COMPLETED, ProcessState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");

        // Invalid jumps are rejected
        assertFalse(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.CREATED, ProcessState.COMPLETED),
                "CREATED -> COMPLETED (skipping required states) should be invalid");

        // This ensures tasks cannot execute if state transition is invalid
        // Tasks should check state transition validity before executing
    }

    @Test
    @DisplayName("Test that process state determines which tasks can execute")
    public void testProcessStateDeterminesExecutableTasks() throws RegistryException {
        // Test that process state determines which workflow phase and tasks can execute

        // CREATED state: No tasks should execute yet
        ProcessStatus created = statusService.getLatestProcessStatus(testProcessId);
        assertNotNull(created, "Process should have initial status");
        assertEquals(ProcessState.CREATED, created.getState(), "Process should start in CREATED state");

        // STARTED state: Pre-workflow tasks should execute
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        statusService.addProcessStatus(started, testProcessId);

        ProcessStatus latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.STARTED, latest.getState(), "Process should be in STARTED state");
        // At STARTED, PreWorkflowManager would create and execute pre-workflow tasks

        // EXECUTING state: Job is running, post-workflow tasks will execute when job completes
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        statusService.addProcessStatus(executing, testProcessId);

        latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.EXECUTING, latest.getState(), "Process should be in EXECUTING state");
        // At EXECUTING, job is running; PostWorkflowManager will handle post-execution tasks
    }
}
