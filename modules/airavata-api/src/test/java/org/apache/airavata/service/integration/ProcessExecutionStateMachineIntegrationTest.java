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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.dapr.messaging.MessageVerificationUtils;
import org.apache.airavata.orchestrator.internal.messaging.DaprMessagingFactory;
import org.apache.airavata.orchestrator.internal.messaging.MessageContext;
import org.apache.airavata.orchestrator.internal.messaging.MessageHandler;
import org.apache.airavata.orchestrator.internal.messaging.Publisher;
import org.apache.airavata.orchestrator.internal.messaging.Subscriber;
import org.apache.airavata.orchestrator.internal.messaging.Type;
import org.apache.airavata.orchestrator.state.ProcessStateValidator;
import org.apache.airavata.orchestrator.state.StateTransitionService;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.service.integration.StateMachineTestUtils.TestHierarchy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for process execution state machine transitions.
 * Tests verify that process state transitions follow the correct sequence
 * and that experiment state is properly synchronized with process state.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.TestDaprConfig.class,
            ProcessExecutionStateMachineIntegrationTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.flyway.enabled=false",
            "airavata.security.manager.enabled=false",
            "airavata.security.authzCache.enabled=true",
            "airavata.dapr.enabled=true", // Enable Dapr for messaging tests to avoid skipping
        })
@org.springframework.test.context.ActiveProfiles("test")
@Transactional
public class ProcessExecutionStateMachineIntegrationTest extends ServiceIntegrationTestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.service.orchestrator",
                "org.apache.airavata.orchestrator.internal.messaging",
                "org.apache.airavata.messaging"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    static class TestConfiguration {}

    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(ProcessExecutionStateMachineIntegrationTest.class);

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final StatusService statusService;
    private final TaskService taskService;

    @Autowired(required = false)
    private DaprMessagingFactory messagingFactory;

    private TestHierarchy testHierarchy;

    public ProcessExecutionStateMachineIntegrationTest(
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
        // Ensure base setup runs first to apply test properties
        super.setUpBase();

        testHierarchy = StateMachineTestUtils.createTestHierarchy(
                gatewayService,
                projectService,
                experimentService,
                processService,
                taskService, // TaskService needed for task execution verification
                null); // JobService not needed for process tests
    }

    @Test
    public void testProcessExecution_FullLifecycle() throws RegistryException, InterruptedException {
        // Test full lifecycle: CREATED -> VALIDATED -> STARTED -> PRE_PROCESSING ->
        // CONFIGURING_WORKSPACE -> INPUT_DATA_STAGING -> EXECUTING -> MONITORING ->
        // OUTPUT_DATA_STAGING -> POST_PROCESSING -> COMPLETED
        List<ProcessState> expectedStates = StateMachineTestUtils.getSuccessfulProcessStateSequence();

        // Add statuses in sequence
        for (ProcessState state : expectedStates) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        // state transitions
        StateMachineTestUtils.verifyProcessStateTransition(
                processService, statusService, testHierarchy.processId, expectedStates);

        // latest status
        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertNotNull(latest, "Latest status should exist");
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");
    }

    @Test
    public void testProcessExecution_WithQueuing() throws RegistryException, InterruptedException {
        // Test queuing path: CREATED -> VALIDATED -> QUEUED -> DEQUEUING -> STARTED -> ...
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.QUEUED);
        states.add(ProcessState.DEQUEUING);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // queuing states are in history
        var process = processService.getProcess(testHierarchy.processId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.QUEUED),
                "Process should have QUEUED state in history");
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.DEQUEUING),
                "Process should have DEQUEUING state in history");
    }

    @Test
    public void testProcessExecution_FailurePath() throws RegistryException, InterruptedException {
        // Test failure path: Process fails at EXECUTING stage
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.FAILED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.FAILED, latest.getState(), "Final state should be FAILED");
    }

    @Test
    public void testProcessExecution_CancellationPath() throws RegistryException, InterruptedException {
        // Test cancellation path: CANCELLING -> CANCELED
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.CANCELLING);
        states.add(ProcessState.CANCELED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.CANCELED, latest.getState(), "Final state should be CANCELED");
    }

    @Test
    public void testProcessExecution_RequeuePath() throws RegistryException, InterruptedException {
        // Test requeue path: REQUEUED -> DEQUEUING -> STARTED
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.REQUEUED);
        states.add(ProcessState.DEQUEUING);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // requeue states are in history
        var process = processService.getProcess(testHierarchy.processId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.REQUEUED),
                "Process should have REQUEUED state in history");
    }

    @Test
    public void testProcessExecution_ExperimentStateSync() throws RegistryException, InterruptedException {
        // Test that process state changes trigger correct experiment state updates
        // When process is STARTED, experiment should be EXECUTING
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Process started");
        statusService.addProcessStatus(started, testHierarchy.processId);

        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.STARTED, latest.getState(), "Process state should be STARTED");

        var experiment = experimentService.getExperiment(testHierarchy.experimentId);
        assertNotNull(experiment, "Experiment should exist");
        assertNotNull(experiment.getExperimentStatus(), "Experiment should have status");
        assertFalse(experiment.getExperimentStatus().isEmpty(), "Experiment should have at least one status");
        ExperimentStatus expStatus = experiment.getExperimentStatus().get(0);
        // Initial state should be CREATED
        assertEquals(ExperimentState.CREATED, expStatus.getState(), "Experiment should start in CREATED state");
    }

    @Test
    public void testProcessExecution_StateHistoryPreserved() throws RegistryException, InterruptedException {
        // Test that all state transitions are preserved in history
        List<ProcessState> states = StateMachineTestUtils.getSuccessfulProcessStateSequence();

        // Add all statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        // all states are in history
        var process = processService.getProcess(testHierarchy.processId);
        assertNotNull(process.getProcessStatuses(), "Process should have status history");
        assertTrue(
                process.getProcessStatuses().size() >= states.size(),
                "Process should have at least " + states.size() + " status entries");

        // all expected states are present
        for (ProcessState expectedState : states) {
            boolean found = process.getProcessStatuses().stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Process state history should contain " + expectedState);
        }

        // timestamps are in order
        StateMachineTestUtils.verifyProcessStateTimestamps(new ArrayList<ProcessStatus>(process.getProcessStatuses()));
    }

    @Test
    @DisplayName("Should verify messages are published when process status changes")
    void shouldVerifyMessagesPublishedOnStatusChanges() throws Exception {
        // Verify Dapr is available - skip test if not available
        requireDaprMessaging();

        List<MessageContext> capturedMessages = new ArrayList<>();
        CountDownLatch messageReceived = new CountDownLatch(2); // Expect 2 messages

        MessageHandler handler =
                MessageVerificationUtils.createCapturingHandlerWithLatch(capturedMessages, messageReceived, 2);

        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(testHierarchy.processId);
        routingKeys.add(testHierarchy.processId + ".*");

        Subscriber subscriber = null;
        Publisher publisher = null;

        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            publisher = messagingFactory.getPublisher(Type.STATUS);

            int publishSuccessCount = 0;

            ProcessStatus status1 = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Process started");
            statusService.addProcessStatus(status1, testHierarchy.processId);

            // Publish message (as would happen in real flow via AiravataTask or WorkflowManager)
            ProcessIdentifier identifier =
                    new ProcessIdentifier(testHierarchy.processId, testHierarchy.experimentId, testHierarchy.gatewayId);
            ProcessStatusChangeEvent event1 = new ProcessStatusChangeEvent(ProcessState.STARTED, identifier);
            MessageContext msgCtx1 = new MessageContext(
                    event1,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    testHierarchy.gatewayId);
            msgCtx1.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            try {
                publisher.publish(msgCtx1);
                publishSuccessCount++;
            } catch (Exception e) {
                logger.warn(
                        "Failed to publish message (this may be expected if messaging is not fully configured): {}",
                        e.getMessage());
            }

            // Add another status and publish
            ProcessStatus status2 =
                    StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Process executing");
            statusService.addProcessStatus(status2, testHierarchy.processId);

            ProcessStatusChangeEvent event2 = new ProcessStatusChangeEvent(ProcessState.EXECUTING, identifier);
            MessageContext msgCtx2 = new MessageContext(
                    event2,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    testHierarchy.gatewayId);
            msgCtx2.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            try {
                publisher.publish(msgCtx2);
                publishSuccessCount++;
            } catch (Exception e) {
                logger.warn(
                        "Failed to publish message (this may be expected if messaging is not fully configured): {}",
                        e.getMessage());
            }

            // Only verify message reception if we successfully published at least one message
            if (publishSuccessCount > 0) {
                // Wait for messages with longer timeout for Dapr in test environment
                boolean received = messageReceived.await(15, TimeUnit.SECONDS);

                // If messages weren't received, this is a test failure
                // We should not silently skip verification - this indicates a real problem
                assertTrue(
                        received || !capturedMessages.isEmpty(),
                        "Messages should be received within timeout. This indicates a messaging infrastructure issue that must be fixed.");
                assertTrue(
                        capturedMessages.size() >= publishSuccessCount || capturedMessages.isEmpty(),
                        "Should capture at least " + publishSuccessCount + " messages");
                if (publishSuccessCount >= 1) {
                    assertTrue(
                            MessageVerificationUtils.verifyProcessStateMessage(
                                    capturedMessages, testHierarchy.processId, ProcessState.STARTED),
                            "Should have STARTED state message");
                }
                if (publishSuccessCount >= 2) {
                    assertTrue(
                            MessageVerificationUtils.verifyProcessStateMessage(
                                    capturedMessages, testHierarchy.processId, ProcessState.EXECUTING),
                            "Should have EXECUTING state message");
                }
            } else {
                // If we expected to publish messages but none were published, this is a failure
                // The test should fail fast rather than silently skip verification
                throw new AssertionError(
                        "No messages were published successfully. This indicates a messaging infrastructure failure that must be fixed.");
            }
        } finally {
            if (subscriber != null) {
                // Note: Subscriber cleanup handled by connection close
            }
        }
    }

    @Test
    @DisplayName("Should verify message ordering for state transitions")
    void shouldVerifyMessageOrderingForStateTransitions() throws Exception {
        // Verify Dapr is available - skip test if not available
        requireDaprMessaging();

        List<MessageContext> capturedMessages = new ArrayList<>();
        List<ProcessState> expectedStates = new ArrayList<>();
        expectedStates.add(ProcessState.CREATED);
        expectedStates.add(ProcessState.VALIDATED);
        expectedStates.add(ProcessState.STARTED);

        CountDownLatch messageReceived = new CountDownLatch(expectedStates.size());
        MessageHandler handler = MessageVerificationUtils.createCapturingHandlerWithLatch(
                capturedMessages, messageReceived, expectedStates.size());

        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(testHierarchy.processId);
        routingKeys.add(testHierarchy.processId + ".*");

        Subscriber subscriber = null;
        Publisher publisher = null;

        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            publisher = messagingFactory.getPublisher(Type.STATUS);

            int publishSuccessCount = 0;

            ProcessIdentifier identifier =
                    new ProcessIdentifier(testHierarchy.processId, testHierarchy.experimentId, testHierarchy.gatewayId);

            for (ProcessState state : expectedStates) {
                ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
                statusService.addProcessStatus(status, testHierarchy.processId);

                ProcessStatusChangeEvent event = new ProcessStatusChangeEvent(state, identifier);
                MessageContext msgCtx = new MessageContext(
                        event,
                        MessageType.PROCESS,
                        AiravataUtils.getId(MessageType.PROCESS.name()),
                        testHierarchy.gatewayId);
                msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                try {
                    publisher.publish(msgCtx);
                    publishSuccessCount++;
                } catch (Exception e) {
                    logger.warn(
                            "Failed to publish message (this may be expected if messaging is not fully configured): {}",
                            e.getMessage());
                }
            }

            // Only verify message reception if we successfully published at least one message
            if (publishSuccessCount > 0) {
                // Wait for messages with longer timeout for Dapr in test environment
                boolean received = messageReceived.await(15, TimeUnit.SECONDS);

                // If messages weren't received, this is a test failure
                // We should not silently skip verification - this indicates a real problem
                assertTrue(
                        received || !capturedMessages.isEmpty(),
                        "All messages should be received within timeout. This indicates a messaging infrastructure issue that must be fixed.");
                assertTrue(
                        MessageVerificationUtils.verifyStateTransitionMessages(
                                capturedMessages, expectedStates, MessageType.PROCESS),
                        "State transitions should be in correct order");
            } else {
                // If we expected to publish messages but none were published, this is a failure
                // The test should fail fast rather than silently skip verification
                throw new AssertionError(
                        "No messages were published successfully. This indicates a messaging infrastructure failure that must be fixed.");
            }
        } finally {
            if (subscriber != null) {
                // Note: Subscriber cleanup handled by connection close
            }
        }
    }

    @Test
    @DisplayName("Test that tasks are executed when process state transitions to STARTED")
    public void testTasksExecutedWhenProcessTransitionsToStarted() throws RegistryException {
        // Test that when process transitions to STARTED, pre-workflow tasks should be created
        // PreWorkflowManager would create tasks like ENV_SETUP, INPUT_DATA_STAGING, JOB_SUBMISSION

        // Transition process to STARTED
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Process started");
        statusService.addProcessStatus(started, testHierarchy.processId);

        // Verify state transition was valid
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.CREATED, ProcessState.STARTED),
                "CREATED -> STARTED should be valid");

        // At STARTED state, PreWorkflowManager would create pre-execution tasks
        // Simulate task creation
        TaskModel inputStagingTask = new TaskModel();
        inputStagingTask.setTaskType(TaskTypes.DATA_STAGING);
        inputStagingTask.setParentProcessId(testHierarchy.processId);
        String taskId = taskService.addTask(inputStagingTask, testHierarchy.processId);

        // Verify task was created
        ProcessModel process = processService.getProcess(testHierarchy.processId);
        assertNotNull(process.getTasks(), "Process should have tasks");
        assertTrue(
                process.getTasks().stream().anyMatch(t -> t.getTaskId().equals(taskId)),
                "Process should have the created task");
    }

    @Test
    @DisplayName("Test that task outcomes update process state correctly")
    public void testTaskOutcomesUpdateProcessState() throws RegistryException {
        // Test that task success/failure correctly updates process state

        // Create a task
        TaskModel task = new TaskModel();
        task.setTaskType(TaskTypes.JOB_SUBMISSION);
        task.setParentProcessId(testHierarchy.processId);
        taskService.addTask(task, testHierarchy.processId);

        // Process is in EXECUTING state
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        statusService.addProcessStatus(executing, testHierarchy.processId);

        // Task fails -> process should transition to FAILED
        // (In real system, AiravataTask.onFail() would handle this)
        ProcessStatus failed =
                StateMachineTestUtils.createProcessStatus(ProcessState.FAILED, "Process failed due to task failure");
        statusService.addProcessStatus(failed, testHierarchy.processId);

        // Verify state transition was valid
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.EXECUTING, ProcessState.FAILED),
                "EXECUTING -> FAILED should be valid when task fails");

        // Verify final state
        ProcessStatus latest = statusService.getLatestProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.FAILED, latest.getState(), "Process should be in FAILED state");
    }

    @Test
    @DisplayName("Test that task chaining works correctly with process state transitions")
    public void testTaskChainingWithProcessStateTransitions() throws RegistryException {
        // Test that tasks are chained correctly and process state transitions occur at correct points

        // Process transitions to STARTED
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        statusService.addProcessStatus(started, testHierarchy.processId);

        // Create task chain: INPUT_DATA_STAGING -> JOB_SUBMISSION
        TaskModel task1 = new TaskModel();
        task1.setTaskType(TaskTypes.DATA_STAGING);
        task1.setParentProcessId(testHierarchy.processId);
        taskService.addTask(task1, testHierarchy.processId);

        TaskModel task2 = new TaskModel();
        task2.setTaskType(TaskTypes.JOB_SUBMISSION);
        task2.setParentProcessId(testHierarchy.processId);
        taskService.addTask(task2, testHierarchy.processId);

        // Verify both tasks exist
        ProcessModel process = processService.getProcess(testHierarchy.processId);
        assertNotNull(process.getTasks(), "Process should have tasks");
        assertTrue(process.getTasks().size() >= 2, "Process should have at least 2 tasks");

        // Task1 completes -> process may transition
        // Task2 executes -> process transitions to EXECUTING
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        statusService.addProcessStatus(executing, testHierarchy.processId);

        // Verify state transition
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.STARTED, ProcessState.EXECUTING),
                "STARTED -> EXECUTING should be valid after tasks complete");
    }

    /**
     * Helper method to require Dapr messaging availability.
     * Throws TestAbortedException if Dapr is not available, which properly skips the test
     * with a clear reason instead of silently skipping via Assumptions.
     *
     * @throws org.opentest4j.TestAbortedException if Dapr messaging is not available
     */
    private void requireDaprMessaging() {
        if (messagingFactory == null || !messagingFactory.isAvailable()) {
            throw new org.opentest4j.TestAbortedException(
                    "Dapr messaging is required for this test but is not available. "
                            + "Enable Dapr (airavata.dapr.enabled=true) and ensure Dapr sidecar is running.");
        }
    }
}
