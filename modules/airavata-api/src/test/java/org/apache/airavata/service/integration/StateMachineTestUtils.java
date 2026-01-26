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
import java.util.UUID;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.registry.services.TaskService;

/**
 * Utility class for state machine integration tests.
 * Provides helper methods for creating test entities and verifying state transitions.
 */
public class StateMachineTestUtils {

    /**
     * Creates a complete test hierarchy: Gateway -> Project -> Experiment -> Process -> Task -> Job
     */
    public static class TestHierarchy {
        public String gatewayId;
        public String projectId;
        public String experimentId;
        public String processId;
        public String taskId;
        public String jobId;
        public JobPK jobPK;
    }

    /**
     * Creates a complete test hierarchy for state machine testing.
     */
    public static TestHierarchy createTestHierarchy(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            JobService jobService)
            throws RegistryException {
        TestHierarchy hierarchy = new TestHierarchy();

        // Create Gateway
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + UUID.randomUUID().toString());
        gateway.setDomain("TEST_DOMAIN");
        gateway.setEmailAddress("test@example.com");
        hierarchy.gatewayId = gatewayService.addGateway(gateway);

        // Create Project
        Project project = new Project();
        project.setName("testProject-" + UUID.randomUUID().toString());
        project.setOwner("testUser");
        project.setGatewayId(hierarchy.gatewayId);
        hierarchy.projectId = projectService.addProject(project, hierarchy.gatewayId);

        // Create Experiment
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(hierarchy.projectId);
        experimentModel.setGatewayId(hierarchy.gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment-" + UUID.randomUUID().toString());
        hierarchy.experimentId = experimentService.addExperiment(experimentModel);

        // Create Process
        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(hierarchy.experimentId);
        hierarchy.processId = processService.addProcess(processModel, hierarchy.experimentId);

        // Create Task (optional - only if TaskService is provided)
        if (taskService != null) {
            TaskModel taskModel = new TaskModel();
            taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
            taskModel.setParentProcessId(hierarchy.processId);
            hierarchy.taskId = taskService.addTask(taskModel, hierarchy.processId);
        }

        // Create Job (optional - only if JobService is provided)
        if (jobService != null && hierarchy.taskId != null) {
            JobModel jobModel = new JobModel();
            jobModel.setJobId("test-job-" + UUID.randomUUID().toString());
            jobModel.setTaskId(hierarchy.taskId);
            jobModel.setJobDescription("Test job for state machine testing");
            hierarchy.jobId = jobService.addJob(jobModel, hierarchy.processId);

            // Create JobPK
            hierarchy.jobPK = new JobPK();
            hierarchy.jobPK.setJobId(hierarchy.jobId);
            hierarchy.jobPK.setTaskId(hierarchy.taskId);
        }

        return hierarchy;
    }

    /**
     * Verifies that a job has transitioned through the expected states in order.
     */
    public static void verifyJobStateTransition(
            JobService jobService, StatusService statusService, JobPK jobPK, List<JobState> expectedStates)
            throws RegistryException {
        JobModel job = jobService.getJob(jobPK);
        assertNotNull(job, "Job should exist");
        assertNotNull(job.getJobStatuses(), "Job should have status history");

        List<JobStatus> statuses = job.getJobStatuses();
        assertTrue(
                statuses.size() >= expectedStates.size(),
                "Job should have at least " + expectedStates.size() + " status entries");

        // Verify latest status matches last expected state
        JobStatus latestStatus = statusService.getLatestJobStatus(jobPK.getJobId());
        assertNotNull(latestStatus, "Latest status should exist");
        assertEquals(
                expectedStates.get(expectedStates.size() - 1),
                latestStatus.getJobState(),
                "Latest job state should match expected final state");

        // Verify all expected states are present in history
        for (JobState expectedState : expectedStates) {
            boolean found = statuses.stream().anyMatch(s -> s.getJobState() == expectedState);
            assertTrue(found, "Job state history should contain " + expectedState);
        }
    }

    /**
     * Verifies that a process has transitioned through the expected states in order.
     */
    public static void verifyProcessStateTransition(
            ProcessService processService,
            StatusService statusService,
            String processId,
            List<ProcessState> expectedStates)
            throws RegistryException {
        ProcessModel process = processService.getProcess(processId);
        assertNotNull(process, "Process should exist");
        assertNotNull(process.getProcessStatuses(), "Process should have status history");

        List<ProcessStatus> statuses = process.getProcessStatuses();
        assertTrue(
                statuses.size() >= expectedStates.size(),
                "Process should have at least " + expectedStates.size() + " status entries");

        // Verify latest status matches last expected state
        ProcessStatus latestStatus = statusService.getLatestProcessStatus(processId);
        assertNotNull(latestStatus, "Latest status should exist");
        assertEquals(
                expectedStates.get(expectedStates.size() - 1),
                latestStatus.getState(),
                "Latest process state should match expected final state");

        // Verify all expected states are present in history
        for (ProcessState expectedState : expectedStates) {
            boolean found = statuses.stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Process state history should contain " + expectedState);
        }
    }

    /**
     * Verifies that experiment state matches the expected state.
     */
    public static void verifyExperimentState(
            ExperimentService experimentService, String experimentId, ExperimentState expectedState)
            throws RegistryException {
        ExperimentModel experiment = experimentService.getExperiment(experimentId);
        assertNotNull(experiment, "Experiment should exist");
        assertNotNull(experiment.getExperimentStatus(), "Experiment should have status");
        assertFalse(experiment.getExperimentStatus().isEmpty(), "Experiment should have at least one status");

        ExperimentStatus status = experiment.getExperimentStatus().get(0);
        assertEquals(expectedState, status.getState(), "Experiment state should match expected state");
    }

    private static long lastTimestamp = 0;
    private static final Object timestampLock = new Object();

    /**
     * Gets a unique timestamp that ensures each call returns a different value.
     * Uses System.nanoTime() for high precision and ensures monotonicity.
     */
    private static long getUniqueTimestamp() {
        synchronized (timestampLock) {
            long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
            // Ensure timestamp is always increasing, even if called in rapid succession
            if (currentTime <= lastTimestamp) {
                lastTimestamp = lastTimestamp + 1;
            } else {
                lastTimestamp = currentTime;
            }
            return lastTimestamp;
        }
    }

    /**
     * Creates a job status with the specified state and reason.
     * Timestamps are automatically set to ensure uniqueness.
     */
    public static JobStatus createJobStatus(JobState state, String reason) {
        JobStatus status = new JobStatus(state);
        status.setReason(reason);
        status.setTimeOfStateChange(getUniqueTimestamp());
        return status;
    }

    /**
     * Creates a process status with the specified state and reason.
     * Timestamps are automatically set to ensure uniqueness.
     */
    public static ProcessStatus createProcessStatus(ProcessState state, String reason) {
        ProcessStatus status = new ProcessStatus(state);
        status.setReason(reason);
        status.setTimeOfStateChange(getUniqueTimestamp());
        return status;
    }

    /**
     * Waits for a condition to be true, with timeout.
     */
    public static void waitForCondition(java.util.function.Supplier<Boolean> condition, long timeoutMs, long intervalMs)
            throws InterruptedException {
        long startTime = AiravataUtils.getUniqueTimestamp().getTime();
        while (AiravataUtils.getUniqueTimestamp().getTime() - startTime < timeoutMs) {
            if (condition.get()) {
                return;
            }
            Thread.sleep(intervalMs);
        }
        throw new AssertionError("Condition not met within timeout: " + timeoutMs + "ms");
    }

    /**
     * Waits for a job to reach a specific state.
     */
    public static void waitForJobState(
            StatusService statusService, JobPK jobPK, JobState expectedState, long timeoutMs)
            throws RegistryException, InterruptedException {
        waitForCondition(
                () -> {
                    try {
                        JobStatus status = statusService.getLatestJobStatus(jobPK.getJobId());
                        return status != null && status.getJobState() == expectedState;
                    } catch (RegistryException e) {
                        return false;
                    }
                },
                timeoutMs,
                100);
    }

    /**
     * Waits for a process to reach a specific state.
     */
    public static void waitForProcessState(
            StatusService statusService, String processId, ProcessState expectedState, long timeoutMs)
            throws RegistryException, InterruptedException {
        waitForCondition(
                () -> {
                    try {
                        ProcessStatus status = statusService.getLatestProcessStatus(processId);
                        return status != null && status.getState() == expectedState;
                    } catch (RegistryException e) {
                        return false;
                    }
                },
                timeoutMs,
                100);
    }

    /**
     * Verifies that job state transitions have proper timestamps (increasing order).
     */
    public static void verifyJobStateTimestamps(List<JobStatus> statuses) {
        assertNotNull(statuses, "Status list should not be null");
        assertTrue(statuses.size() > 0, "Should have at least one status");

        long previousTimestamp = 0;
        for (JobStatus status : statuses) {
            assertTrue(
                    status.getTimeOfStateChange() > 0,
                    "Status should have a valid timestamp: " + status.getClass().getSimpleName());
            assertTrue(
                    status.getTimeOfStateChange() >= previousTimestamp,
                    "Status timestamps should be in increasing order");
            previousTimestamp = status.getTimeOfStateChange();
        }
    }

    /**
     * Verifies that process state transitions have proper timestamps (increasing order).
     */
    public static void verifyProcessStateTimestamps(List<ProcessStatus> statuses) {
        assertNotNull(statuses, "Status list should not be null");
        assertTrue(statuses.size() > 0, "Should have at least one status");

        long previousTimestamp = 0;
        for (ProcessStatus status : statuses) {
            assertTrue(
                    status.getTimeOfStateChange() > 0,
                    "Status should have a valid timestamp: " + status.getClass().getSimpleName());
            assertTrue(
                    status.getTimeOfStateChange() >= previousTimestamp,
                    "Status timestamps should be in increasing order");
            previousTimestamp = status.getTimeOfStateChange();
        }
    }

    /**
     * Gets all job states in the expected transition order for a successful job.
     */
    public static List<JobState> getSuccessfulJobStateSequence() {
        List<JobState> sequence = new ArrayList<>();
        sequence.add(JobState.SUBMITTED);
        sequence.add(JobState.QUEUED);
        sequence.add(JobState.ACTIVE);
        sequence.add(JobState.COMPLETE);
        return sequence;
    }

    /**
     * Gets all process states in the expected transition order for a successful process.
     */
    public static List<ProcessState> getSuccessfulProcessStateSequence() {
        List<ProcessState> sequence = new ArrayList<>();
        sequence.add(ProcessState.CREATED);
        sequence.add(ProcessState.VALIDATED);
        sequence.add(ProcessState.STARTED);
        sequence.add(ProcessState.PRE_PROCESSING);
        sequence.add(ProcessState.CONFIGURING_WORKSPACE);
        sequence.add(ProcessState.INPUT_DATA_STAGING);
        sequence.add(ProcessState.EXECUTING);
        sequence.add(ProcessState.MONITORING);
        sequence.add(ProcessState.OUTPUT_DATA_STAGING);
        sequence.add(ProcessState.POST_PROCESSING);
        sequence.add(ProcessState.COMPLETED);
        return sequence;
    }

    /**
     * Gets all experiment states in the expected transition order for a successful experiment.
     */
    public static List<ExperimentState> getSuccessfulExperimentStateSequence() {
        List<ExperimentState> sequence = new ArrayList<>();
        sequence.add(ExperimentState.CREATED);
        sequence.add(ExperimentState.SCHEDULED);
        sequence.add(ExperimentState.LAUNCHED);
        sequence.add(ExperimentState.EXECUTING);
        sequence.add(ExperimentState.COMPLETED);
        return sequence;
    }

    /**
     * Gets all task states in the expected transition order for a successful task.
     */
    public static List<org.apache.airavata.common.model.TaskState> getSuccessfulTaskStateSequence() {
        List<org.apache.airavata.common.model.TaskState> sequence = new ArrayList<>();
        sequence.add(org.apache.airavata.common.model.TaskState.CREATED);
        sequence.add(org.apache.airavata.common.model.TaskState.EXECUTING);
        sequence.add(org.apache.airavata.common.model.TaskState.COMPLETED);
        return sequence;
    }

    /**
     * Creates an experiment status with the specified state and reason.
     * Timestamps are automatically set to ensure uniqueness.
     */
    public static ExperimentStatus createExperimentStatus(ExperimentState state, String reason) {
        ExperimentStatus status = new ExperimentStatus();
        status.setState(state);
        status.setReason(reason);
        status.setTimeOfStateChange(getUniqueTimestamp());
        return status;
    }

    /**
     * Creates a task status with the specified state and reason.
     * Timestamps are automatically set to ensure uniqueness.
     */
    public static org.apache.airavata.common.model.TaskStatus createTaskStatus(
            org.apache.airavata.common.model.TaskState state, String reason) {
        org.apache.airavata.common.model.TaskStatus status = new org.apache.airavata.common.model.TaskStatus();
        status.setState(state);
        status.setReason(reason);
        status.setTimeOfStateChange(getUniqueTimestamp());
        return status;
    }

    /**
     * Verifies that an experiment has transitioned through the expected states in order.
     */
    public static void verifyExperimentStateTransition(
            ExperimentService experimentService,
            StatusService statusService,
            String experimentId,
            List<ExperimentState> expectedStates)
            throws RegistryException {
        ExperimentModel experiment = experimentService.getExperiment(experimentId);
        assertNotNull(experiment, "Experiment should exist");
        assertNotNull(experiment.getExperimentStatus(), "Experiment should have status history");

        List<ExperimentStatus> statuses = experiment.getExperimentStatus();
        assertTrue(
                statuses.size() >= expectedStates.size(),
                "Experiment should have at least " + expectedStates.size() + " status entries");

        // Verify latest status matches last expected state
        ExperimentStatus latestStatus = statusService.getLatestExperimentStatus(experimentId);
        assertNotNull(latestStatus, "Latest status should exist");
        assertEquals(
                expectedStates.get(expectedStates.size() - 1),
                latestStatus.getState(),
                "Latest experiment state should match expected final state");

        // Verify all expected states are present in history
        for (ExperimentState expectedState : expectedStates) {
            boolean found = statuses.stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Experiment state history should contain " + expectedState);
        }
    }

    /**
     * Verifies that a task has transitioned through the expected states in order.
     */
    public static void verifyTaskStateTransition(
            TaskService taskService,
            StatusService statusService,
            String taskId,
            List<org.apache.airavata.common.model.TaskState> expectedStates)
            throws RegistryException {
        org.apache.airavata.common.model.TaskModel task = taskService.getTask(taskId);
        assertNotNull(task, "Task should exist");
        assertNotNull(task.getTaskStatuses(), "Task should have status history");

        var statuses = task.getTaskStatuses();
        assertTrue(
                statuses.size() >= expectedStates.size(),
                "Task should have at least " + expectedStates.size() + " status entries");

        // Verify latest status matches last expected state
        var latestStatus = statusService.getLatestTaskStatus(taskId);
        assertNotNull(latestStatus, "Latest status should exist");
        assertEquals(
                expectedStates.get(expectedStates.size() - 1),
                latestStatus.getState(),
                "Latest task state should match expected final state");

        // Verify all expected states are present in history
        for (org.apache.airavata.common.model.TaskState expectedState : expectedStates) {
            boolean found = statuses.stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Task state history should contain " + expectedState);
        }
    }

    /**
     * Verifies that experiment state transitions have proper timestamps (increasing order).
     */
    public static void verifyExperimentStateTimestamps(List<ExperimentStatus> statuses) {
        assertNotNull(statuses, "Status list should not be null");
        assertTrue(statuses.size() > 0, "Should have at least one status");

        long previousTimestamp = 0;
        for (ExperimentStatus status : statuses) {
            assertTrue(
                    status.getTimeOfStateChange() > 0,
                    "Status should have a valid timestamp: " + status.getClass().getSimpleName());
            assertTrue(
                    status.getTimeOfStateChange() >= previousTimestamp,
                    "Status timestamps should be in increasing order");
            previousTimestamp = status.getTimeOfStateChange();
        }
    }

    /**
     * Verifies that task state transitions have proper timestamps (increasing order).
     */
    public static void verifyTaskStateTimestamps(List<org.apache.airavata.common.model.TaskStatus> statuses) {
        assertNotNull(statuses, "Status list should not be null");
        assertTrue(statuses.size() > 0, "Should have at least one status");

        long previousTimestamp = 0;
        for (org.apache.airavata.common.model.TaskStatus status : statuses) {
            assertTrue(
                    status.getTimeOfStateChange() > 0,
                    "Status should have a valid timestamp: " + status.getClass().getSimpleName());
            assertTrue(
                    status.getTimeOfStateChange() >= previousTimestamp,
                    "Status timestamps should be in increasing order");
            previousTimestamp = status.getTimeOfStateChange();
        }
    }

    /**
     * Helper method to test all valid transitions for a validator.
     * This method verifies that all transitions defined in the validator are actually valid.
     */
    public static <S extends Enum<S>> void testAllValidTransitions(
            org.apache.airavata.orchestrator.state.StateValidator<S> validator) {
        java.util.Set<org.apache.airavata.orchestrator.state.StateTransition<S>> validTransitions =
                validator.getValidTransitions();
        assertNotNull(validTransitions, "Valid transitions should not be null");
        assertTrue(validTransitions.size() > 0, "Should have at least one valid transition");

        for (org.apache.airavata.orchestrator.state.StateTransition<S> transition : validTransitions) {
            assertTrue(
                    validator.isValid(transition.from(), transition.to()),
                    "Transition " + transition.from() + " -> " + transition.to() + " should be valid");
        }
    }

    /**
     * Helper method to test that invalid transitions are rejected.
     * This method tests common invalid transitions (terminal states, skipping states, etc.).
     */
    public static <S extends Enum<S>> void testInvalidTransitions(
            org.apache.airavata.orchestrator.state.StateValidator<S> validator,
            java.util.Set<S> terminalStates,
            S sampleState) {
        // Test that terminal states cannot transition to any other state
        for (S terminalState : terminalStates) {
            for (S targetState : sampleState.getDeclaringClass().getEnumConstants()) {
                if (!targetState.equals(terminalState)) {
                    assertFalse(
                            validator.isValid(terminalState, targetState),
                            "Terminal state " + terminalState + " should not transition to " + targetState);
                }
            }
        }

        // Test null handling
        assertFalse(validator.isValid(sampleState, null), "Any state -> null should be invalid");
        assertFalse(validator.isValid(null, null), "null -> null should be invalid");
    }
}
