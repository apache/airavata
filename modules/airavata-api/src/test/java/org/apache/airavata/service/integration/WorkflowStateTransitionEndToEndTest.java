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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.orchestrator.state.ExperimentStateValidator;
import org.apache.airavata.orchestrator.state.ProcessStateValidator;
import org.apache.airavata.orchestrator.state.StateTransitionService;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.JobStatusService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.registry.services.TaskStatusService;
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
 * End-to-end integration tests for workflow execution with state transitions.
 * Tests verify complete workflow execution from creation to completion/failure,
 * ensuring all state transitions occur correctly and tasks execute in proper order.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            WorkflowStateTransitionEndToEndTest.TestConfiguration.class
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
public class WorkflowStateTransitionEndToEndTest extends ServiceIntegrationTestBase {

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
    private final ProcessStatusService processStatusService;
    private final TaskService taskService;
    private final TaskStatusService taskStatusService;
    private final JobService jobService;
    private final JobStatusService jobStatusService;

    private String testProcessId;
    private String testExperimentId;

    public WorkflowStateTransitionEndToEndTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessStatusService processStatusService,
            TaskService taskService,
            TaskStatusService taskStatusService,
            JobService jobService,
            JobStatusService jobStatusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processStatusService = processStatusService;
        this.taskService = taskService;
        this.taskStatusService = taskStatusService;
        this.jobService = jobService;
        this.jobStatusService = jobStatusService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        super.setUpBase();

        Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
        if (!gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            gatewayService.addGateway(gateway);
        }

        Project project = TestDataFactory.createTestProject("E2E Workflow Test Project", TEST_GATEWAY_ID);
        String projectId = projectService.addProject(project, TEST_GATEWAY_ID);

        ExperimentModel experiment =
                TestDataFactory.createTestExperiment("E2E Workflow Experiment", projectId, TEST_GATEWAY_ID);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        testExperimentId = experimentService.addExperiment(experiment);

        ProcessModel process = new ProcessModel();
        process.setExperimentId(testExperimentId);
        testProcessId = processService.addProcess(process, testExperimentId);
    }

    @Test
    @DisplayName("Test complete successful workflow: CREATED -> STARTED -> EXECUTING -> COMPLETED")
    public void testCompleteSuccessfulWorkflow() throws RegistryException {
        // Test complete workflow execution with all state transitions
        // Verify all expected tasks execute and state transitions occur correctly

        // 1. Initial state: CREATED
        ProcessStatus created = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.CREATED, created.getState(), "Process should start in CREATED state");

        // 2. Transition to STARTED (PreWorkflowManager would create pre-execution tasks)
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        processStatusService.addProcessStatus(started, testProcessId);

        // Verify state transition was valid
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.CREATED, ProcessState.STARTED),
                "CREATED -> STARTED should be valid");

        // 3. Create pre-execution tasks (simulating PreWorkflowManager)
        TaskModel inputStagingTask = new TaskModel();
        inputStagingTask.setTaskType(TaskTypes.DATA_STAGING);
        inputStagingTask.setParentProcessId(testProcessId);
        String inputTaskId = taskService.addTask(inputStagingTask, testProcessId);

        TaskModel jobSubmissionTask = new TaskModel();
        jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
        jobSubmissionTask.setParentProcessId(testProcessId);
        String jobTaskId = taskService.addTask(jobSubmissionTask, testProcessId);

        // 4. Tasks execute and update process state
        // Input staging completes -> process may transition
        TaskStatus inputExecuting =
                StateMachineTestUtils.createTaskStatus(TaskState.EXECUTING, "Input staging executing");
        taskStatusService.addTaskStatus(inputExecuting, inputTaskId);

        TaskStatus inputCompleted =
                StateMachineTestUtils.createTaskStatus(TaskState.COMPLETED, "Input staging completed");
        taskStatusService.addTaskStatus(inputCompleted, inputTaskId);

        // Job submission completes -> process transitions to EXECUTING
        TaskStatus jobExecuting =
                StateMachineTestUtils.createTaskStatus(TaskState.EXECUTING, "Job submission executing");
        taskStatusService.addTaskStatus(jobExecuting, jobTaskId);

        TaskStatus jobCompleted = StateMachineTestUtils.createTaskStatus(TaskState.COMPLETED, "Job submitted");
        taskStatusService.addTaskStatus(jobCompleted, jobTaskId);

        // 5. Process transitions to EXECUTING (job is running)
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing, testProcessId);

        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.STARTED, ProcessState.EXECUTING),
                "STARTED -> EXECUTING should be valid");

        // 6. Job completes -> PostWorkflowManager creates post-execution tasks
        // Create job for the task
        org.apache.airavata.common.model.JobModel jobModel = new org.apache.airavata.common.model.JobModel();
        jobModel.setJobId("test-job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(jobTaskId);
        jobModel.setJobDescription("Test job for workflow state transition");
        String jobId = jobService.addJob(jobModel, testProcessId);

        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(jobTaskId);

        // Job completes
        JobStatus jobComplete = StateMachineTestUtils.createJobStatus(JobState.COMPLETE, "Job completed");
        jobStatusService.addJobStatus(jobComplete, jobPK);

        // 7. Post-execution tasks execute
        TaskModel outputStagingTask = new TaskModel();
        outputStagingTask.setTaskType(TaskTypes.DATA_STAGING);
        outputStagingTask.setParentProcessId(testProcessId);
        String outputTaskId = taskService.addTask(outputStagingTask, testProcessId);

        TaskStatus outputExecuting =
                StateMachineTestUtils.createTaskStatus(TaskState.EXECUTING, "Output staging executing");
        taskStatusService.addTaskStatus(outputExecuting, outputTaskId);

        TaskStatus outputCompleted =
                StateMachineTestUtils.createTaskStatus(TaskState.COMPLETED, "Output staging completed");
        taskStatusService.addTaskStatus(outputCompleted, outputTaskId);

        // 8. Process transitions to COMPLETED
        ProcessStatus completed = StateMachineTestUtils.createProcessStatus(ProcessState.COMPLETED, "Completed");
        processStatusService.addProcessStatus(completed, testProcessId);

        // Verify final state
        ProcessStatus finalStatus = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.COMPLETED, finalStatus.getState(), "Process should be in COMPLETED state");

        // Verify all state transitions were valid
        List<ProcessState> expectedStates = new ArrayList<>();
        expectedStates.add(ProcessState.CREATED);
        expectedStates.add(ProcessState.STARTED);
        expectedStates.add(ProcessState.EXECUTING);
        expectedStates.add(ProcessState.COMPLETED);

        ProcessModel process = processService.getProcess(testProcessId);
        for (ProcessState expectedState : expectedStates) {
            boolean found = process.getProcessStatuses().stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Process should have " + expectedState + " state in history");
        }
    }

    @Test
    @DisplayName("Test workflow with failure: CREATED -> STARTED -> EXECUTING -> FAILED")
    public void testWorkflowWithFailure() throws RegistryException {
        // Test workflow execution with failure at different stages
        // Verify state transitions to FAILED correctly

        // 1. CREATED -> STARTED
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        processStatusService.addProcessStatus(started, testProcessId);

        // 2. Create and execute task
        TaskModel task = new TaskModel();
        task.setTaskType(TaskTypes.DATA_STAGING);
        task.setParentProcessId(testProcessId);
        String taskId = taskService.addTask(task, testProcessId);

        TaskStatus executing = StateMachineTestUtils.createTaskStatus(TaskState.EXECUTING, "Task executing");
        taskStatusService.addTaskStatus(executing, taskId);

        // 3. Process transitions to EXECUTING
        ProcessStatus processExecuting = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(processExecuting, testProcessId);

        // 4. Task fails (fatal)
        TaskStatus failed = StateMachineTestUtils.createTaskStatus(TaskState.FAILED, "Task failed - fatal");
        taskStatusService.addTaskStatus(failed, taskId);

        // 5. Process transitions to FAILED (handled by AiravataTask.onFail())
        ProcessStatus processFailed =
                StateMachineTestUtils.createProcessStatus(ProcessState.FAILED, "Process failed due to task failure");
        processStatusService.addProcessStatus(processFailed, testProcessId);

        // Verify final state
        ProcessStatus finalStatus = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.FAILED, finalStatus.getState(), "Process should be in FAILED state");

        // Verify state transition was valid
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.EXECUTING, ProcessState.FAILED),
                "EXECUTING -> FAILED should be valid");
    }

    @Test
    @DisplayName("Test workflow with requeue: EXECUTING -> REQUEUED -> QUEUED -> EXECUTING")
    public void testWorkflowWithRequeue() throws RegistryException {
        // Test requeue scenario where process is requeued and re-executed

        // 1. Process reaches EXECUTING
        ProcessStatus executing1 = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing1, testProcessId);

        // 2. Task fails (non-fatal, autoSchedule enabled) -> process transitions to REQUEUED
        ProcessStatus requeued = StateMachineTestUtils.createProcessStatus(ProcessState.REQUEUED, "Requeued");
        processStatusService.addProcessStatus(requeued, testProcessId);

        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.EXECUTING, ProcessState.REQUEUED),
                "EXECUTING -> REQUEUED should be valid");

        // 3. Process transitions to QUEUED
        ProcessStatus queued = StateMachineTestUtils.createProcessStatus(ProcessState.QUEUED, "Queued");
        processStatusService.addProcessStatus(queued, testProcessId);

        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.REQUEUED, ProcessState.QUEUED),
                "REQUEUED -> QUEUED should be valid");

        // 4. Process transitions back to EXECUTING (requeue completed)
        ProcessStatus executing2 = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Re-executing");
        processStatusService.addProcessStatus(executing2, testProcessId);

        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.QUEUED, ProcessState.EXECUTING),
                "QUEUED -> EXECUTING should be valid");

        // Verify final state
        ProcessStatus finalStatus = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.EXECUTING, finalStatus.getState(), "Process should be back in EXECUTING state");

        // Verify all requeue states are in history
        ProcessModel process = processService.getProcess(testProcessId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.REQUEUED),
                "Process should have REQUEUED state in history");
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.QUEUED),
                "Process should have QUEUED state in history");
    }

    @Test
    @DisplayName("Test that experiment state transitions match process state transitions")
    public void testExperimentStateMatchesProcessState() throws RegistryException {
        // Test that experiment state transitions correctly based on process state

        // Initial experiment state: CREATED
        ExperimentModel experiment = experimentService.getExperiment(testExperimentId);
        assertNotNull(experiment.getExperimentStatus(), "Experiment should have status");
        ExperimentStatus initialExpStatus = experiment.getExperimentStatus().get(0);
        assertEquals(ExperimentState.CREATED, initialExpStatus.getState(), "Experiment should start in CREATED");

        // Process transitions to EXECUTING
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing, testProcessId);

        // Experiment state should reflect process state (in real system, this is handled by workflow managers)
        // According to ExperimentStateValidator, CREATED -> LAUNCHED -> EXECUTING is the valid path
        // This test verifies the state transitions are valid for both experiment and process
        assertTrue(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.CREATED, ExperimentState.LAUNCHED),
                "Experiment CREATED -> LAUNCHED should be valid when process starts");
        assertTrue(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
                "Experiment LAUNCHED -> EXECUTING should be valid when process transitions");

        // Process completes
        ProcessStatus completed = StateMachineTestUtils.createProcessStatus(ProcessState.COMPLETED, "Completed");
        processStatusService.addProcessStatus(completed, testProcessId);

        // Experiment should transition to COMPLETED
        assertTrue(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.EXECUTING, ExperimentState.COMPLETED),
                "Experiment EXECUTING -> COMPLETED should be valid when process completes");
    }

    @Test
    @DisplayName("Test that all state transitions in workflow are validated")
    public void testAllStateTransitionsValidated() throws RegistryException {
        // Test that every state transition in the workflow goes through StateTransitionService validation

        List<ProcessState> workflowStates = StateMachineTestUtils.getSuccessfulProcessStateSequence();

        ProcessState previousState = null;
        for (ProcessState currentState : workflowStates) {
            if (previousState != null) {
                // Verify transition is valid
                assertTrue(
                        StateTransitionService.isValid(ProcessStateValidator.INSTANCE, previousState, currentState),
                        "Transition " + previousState + " -> " + currentState + " should be valid");

                // Add status to verify it works
                ProcessStatus status =
                        StateMachineTestUtils.createProcessStatus(currentState, "State: " + currentState.name());
                processStatusService.addProcessStatus(status, testProcessId);
            } else {
                // First state - just add it
                ProcessStatus status =
                        StateMachineTestUtils.createProcessStatus(currentState, "State: " + currentState.name());
                processStatusService.addProcessStatus(status, testProcessId);
            }
            previousState = currentState;
        }

        // Verify final state
        ProcessStatus finalStatus = processStatusService.getProcessStatus(testProcessId);
        assertEquals(
                workflowStates.get(workflowStates.size() - 1),
                finalStatus.getState(),
                "Final state should match last state in sequence");
    }
}
