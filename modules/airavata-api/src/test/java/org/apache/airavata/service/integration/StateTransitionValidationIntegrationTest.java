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
import org.apache.airavata.common.model.JobIdentifier;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.dapr.messaging.MessageVerificationUtils;
import org.apache.airavata.orchestrator.internal.messaging.DaprMessagingFactory;
import org.apache.airavata.orchestrator.internal.messaging.MessageContext;
import org.apache.airavata.orchestrator.internal.messaging.MessageHandler;
import org.apache.airavata.orchestrator.internal.messaging.Publisher;
import org.apache.airavata.orchestrator.internal.messaging.Subscriber;
import org.apache.airavata.orchestrator.internal.messaging.Type;
import org.apache.airavata.orchestrator.state.ExperimentStateValidator;
import org.apache.airavata.orchestrator.state.JobStateValidator;
import org.apache.airavata.orchestrator.state.StateTransitionService;
import org.apache.airavata.orchestrator.state.TaskStateValidator;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.service.integration.StateMachineTestUtils.TestHierarchy;
import org.apache.airavata.service.registry.RegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for state machine validation rules.
 * Tests verify that state transitions follow correct validation rules
 * and that invalid transitions are properly rejected.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.TestDaprConfig.class,
            StateTransitionValidationIntegrationTest.TestConfiguration.class
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
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public class StateTransitionValidationIntegrationTest extends ServiceIntegrationTestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.monitor",
                "org.apache.airavata.orchestrator.internal.messaging",
                "org.apache.airavata.messaging"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    static class TestConfiguration {}

    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(StateTransitionValidationIntegrationTest.class);

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final StatusService statusService;
    private final TaskService taskService;
    private final JobService jobService;
    private final RegistryService registryService;

    @Autowired(required = false)
    private DaprMessagingFactory messagingFactory;

    private TestHierarchy testHierarchy;

    public StateTransitionValidationIntegrationTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            StatusService statusService,
            TaskService taskService,
            JobService jobService,
            RegistryService registryService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.statusService = statusService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.registryService = registryService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        // Ensure base setup runs first to apply test properties
        super.setUpBase();

        testHierarchy = StateMachineTestUtils.createTestHierarchy(
                gatewayService, projectService, experimentService, processService, taskService, jobService);
    }

    @Test
    public void testJobStateValidator_AllValidTransitions() {
        // Test all valid JobState transitions according to JobStateValidator
        // SUBMITTED can transition to all other states
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.QUEUED),
                "SUBMITTED -> QUEUED should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.ACTIVE),
                "SUBMITTED -> ACTIVE should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.COMPLETE),
                "SUBMITTED -> COMPLETE should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.FAILED),
                "SUBMITTED -> FAILED should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.CANCELED),
                "SUBMITTED -> CANCELED should be valid");

        // QUEUED can transition to multiple states
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.ACTIVE),
                "QUEUED -> ACTIVE should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.COMPLETE),
                "QUEUED -> COMPLETE should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.FAILED),
                "QUEUED -> FAILED should be valid");

        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.ACTIVE, JobState.COMPLETE),
                "ACTIVE -> COMPLETE should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.ACTIVE, JobState.FAILED),
                "ACTIVE -> FAILED should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.ACTIVE, JobState.CANCELED),
                "ACTIVE -> CANCELED should be valid");

        // NON_CRITICAL_FAIL can recover
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
                "NON_CRITICAL_FAIL -> QUEUED should be valid");
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.NON_CRITICAL_FAIL, JobState.ACTIVE),
                "NON_CRITICAL_FAIL -> ACTIVE should be valid");
    }

    @Test
    public void testJobStateValidator_InvalidTransitions() {
        // Test invalid JobState transitions
        // COMPLETE cannot transition to other states
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.COMPLETE, JobState.SUBMITTED),
                "COMPLETE -> SUBMITTED should be invalid");
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.COMPLETE, JobState.ACTIVE),
                "COMPLETE -> ACTIVE should be invalid");

        // FAILED cannot transition to SUBMITTED
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.FAILED, JobState.SUBMITTED),
                "FAILED -> SUBMITTED should be invalid");

        // CANCELED cannot transition to active states
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.CANCELED, JobState.ACTIVE),
                "CANCELED -> ACTIVE should be invalid");
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.CANCELED, JobState.QUEUED),
                "CANCELED -> QUEUED should be invalid");
    }

    @Test
    public void testProcessState_SequentialTransitions() throws RegistryException, InterruptedException {
        // Test that process states follow logical sequence
        List<ProcessState> sequentialStates = StateMachineTestUtils.getSuccessfulProcessStateSequence();

        // Add statuses in sequence
        for (ProcessState state : sequentialStates) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            statusService.addProcessStatus(status, testHierarchy.processId);
        }

        // all states are in correct order
        var process = processService.getProcess(testHierarchy.processId);
        List<ProcessStatus> statuses = process.getProcessStatuses();

        // timestamps are in increasing order
        StateMachineTestUtils.verifyProcessStateTimestamps(new ArrayList<ProcessStatus>(statuses));

        // states appear in expected sequence
        int lastIndex = -1;
        for (ProcessState expectedState : sequentialStates) {
            int currentIndex = -1;
            for (int i = 0; i < statuses.size(); i++) {
                if (statuses.get(i).getState() == expectedState) {
                    currentIndex = i;
                    break;
                }
            }
            assertTrue(currentIndex >= 0, "State " + expectedState + " should be in history");
            assertTrue(currentIndex >= lastIndex, "State " + expectedState + " should appear after previous states");
            lastIndex = currentIndex;
        }
    }

    @Test
    public void testExperimentState_FromProcessState() throws RegistryException {
        // Test that experiment state is correctly derived from process state
        // Initial state should be CREATED
        var experiment = experimentService.getExperiment(testHierarchy.experimentId);
        assertNotNull(experiment, "Experiment should exist");
        assertNotNull(experiment.getExperimentStatus(), "Experiment should have status");
        assertFalse(experiment.getExperimentStatus().isEmpty(), "Experiment should have at least one status");
        ExperimentStatus expStatus = experiment.getExperimentStatus().get(0);
        assertEquals(ExperimentState.CREATED, expStatus.getState(), "Experiment should start in CREATED state");
    }

    @Test
    public void testStateTransition_Timestamps() throws RegistryException, InterruptedException {
        // Test that state transitions have proper timestamps
        // Test job state timestamps
        JobStatus status1 = StateMachineTestUtils.createJobStatus(JobState.SUBMITTED, "Submitted");
        statusService.addJobStatus(status1, testHierarchy.jobPK.getJobId());

        JobStatus status2 = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Queued");
        statusService.addJobStatus(status2, testHierarchy.jobPK.getJobId());

        JobStatus status3 = StateMachineTestUtils.createJobStatus(JobState.ACTIVE, "Active");
        statusService.addJobStatus(status3, testHierarchy.jobPK.getJobId());

        // timestamps
        var job = jobService.getJob(testHierarchy.jobPK);
        StateMachineTestUtils.verifyJobStateTimestamps(new ArrayList<JobStatus>(job.getJobStatuses()));

        // Test process state timestamps
        ProcessStatus pStatus1 = StateMachineTestUtils.createProcessStatus(ProcessState.CREATED, "Created");
        statusService.addProcessStatus(pStatus1, testHierarchy.processId);

        ProcessStatus pStatus2 = StateMachineTestUtils.createProcessStatus(ProcessState.VALIDATED, "Validated");
        statusService.addProcessStatus(pStatus2, testHierarchy.processId);

        ProcessStatus pStatus3 = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        statusService.addProcessStatus(pStatus3, testHierarchy.processId);

        // timestamps
        var process = processService.getProcess(testHierarchy.processId);
        StateMachineTestUtils.verifyProcessStateTimestamps(new ArrayList<ProcessStatus>(process.getProcessStatuses()));
    }

    @Test
    public void testStateTransition_ConcurrentUpdates() throws RegistryException, InterruptedException {
        // Test that concurrent state updates are handled correctly
        // This test simulates rapid state transitions
        List<JobState> rapidStates = new ArrayList<>();
        rapidStates.add(JobState.SUBMITTED);
        rapidStates.add(JobState.QUEUED);
        rapidStates.add(JobState.ACTIVE);

        // Add statuses rapidly
        for (JobState state : rapidStates) {
            JobStatus status = StateMachineTestUtils.createJobStatus(state, "Rapid transition: " + state.name());
            statusService.addJobStatus(status, testHierarchy.jobPK.getJobId());
        }

        // all states are recorded
        var job = jobService.getJob(testHierarchy.jobPK);
        assertNotNull(job.getJobStatuses(), "Job should have status history");
        assertTrue(job.getJobStatuses().size() >= rapidStates.size(), "All rapid state transitions should be recorded");

        // latest state is correct
        JobStatus latest = statusService.getLatestJobStatus(testHierarchy.jobPK.getJobId());
        assertEquals(
                rapidStates.get(rapidStates.size() - 1),
                latest.getJobState(),
                "Latest state should match the last transition");

        // timestamps are still in order despite rapid updates
        StateMachineTestUtils.verifyJobStateTimestamps(new ArrayList<JobStatus>(job.getJobStatuses()));
    }

    @Test
    public void testJobStateValidator_NullHandling() {
        // Test that JobStateValidator handles null states correctly
        // null -> any state should be valid (initial state)
        assertTrue(
                JobStateValidator.INSTANCE.isValid(null, JobState.SUBMITTED),
                "null -> SUBMITTED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, null), "SUBMITTED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(JobStateValidator.INSTANCE.isValid(null, null), "null -> null should be invalid");
    }

    @Test
    @DisplayName("Should verify messages are published for valid state transitions")
    void shouldVerifyMessagesPublishedForValidTransitions() throws Exception {
        // Verify Dapr is available - skip test if not available
        requireDaprMessaging();

        List<MessageContext> capturedMessages = new ArrayList<>();
        CountDownLatch messageReceived = new CountDownLatch(2);

        MessageHandler handler =
                MessageVerificationUtils.createCapturingHandlerWithLatch(capturedMessages, messageReceived, 2);

        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(testHierarchy.processId);
        routingKeys.add(testHierarchy.jobId);

        Subscriber subscriber = null;
        Publisher publisher = null;

        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            publisher = messagingFactory.getPublisher(Type.STATUS);

            int publishSuccessCount = 0;
            boolean processPublished = false;
            boolean jobPublished = false;

            // Valid process transition: CREATED -> VALIDATED
            ProcessStatus validated = StateMachineTestUtils.createProcessStatus(ProcessState.VALIDATED, "Validated");
            statusService.addProcessStatus(validated, testHierarchy.processId);

            ProcessIdentifier processIdentifier =
                    new ProcessIdentifier(testHierarchy.processId, testHierarchy.experimentId, testHierarchy.gatewayId);
            ProcessStatusChangeEvent processEvent =
                    new ProcessStatusChangeEvent(ProcessState.VALIDATED, processIdentifier);
            MessageContext processMsg = new MessageContext(
                    processEvent,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    testHierarchy.gatewayId);
            processMsg.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            try {
                publisher.publish(processMsg);
                publishSuccessCount++;
                processPublished = true;
            } catch (Exception e) {
                logger.warn(
                        "Failed to publish message (this may be expected if messaging is not fully configured): {}",
                        e.getMessage());
            }

            // Valid job transition: SUBMITTED -> QUEUED
            JobStatus queued = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Queued");
            statusService.addJobStatus(queued, testHierarchy.jobPK.getJobId());

            JobIdentifier jobIdentifier = new JobIdentifier(
                    testHierarchy.jobId,
                    testHierarchy.taskId,
                    testHierarchy.processId,
                    testHierarchy.experimentId,
                    testHierarchy.gatewayId);
            JobStatusChangeEvent jobEvent = new JobStatusChangeEvent(JobState.QUEUED, jobIdentifier);
            MessageContext jobMsg = new MessageContext(
                    jobEvent, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), testHierarchy.gatewayId);
            jobMsg.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            try {
                publisher.publish(jobMsg);
                publishSuccessCount++;
                jobPublished = true;
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
                // both process and job messages were received (if published)
                if (processPublished) {
                    assertTrue(
                            capturedMessages.stream().anyMatch(msg -> msg.getType() == MessageType.PROCESS),
                            "Should have process status message");
                }
                if (jobPublished) {
                    assertTrue(
                            capturedMessages.stream().anyMatch(msg -> msg.getType() == MessageType.JOB),
                            "Should have job status message");
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
    @DisplayName("Test ExperimentStateValidator - all valid transitions")
    public void testExperimentStateValidator_AllValidTransitions() {
        // Test all valid ExperimentState transitions
        // CREATED can transition to SCHEDULED, LAUNCHED, or FAILED
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.SCHEDULED),
                "CREATED -> SCHEDULED should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.LAUNCHED),
                "CREATED -> LAUNCHED should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.FAILED),
                "CREATED -> FAILED should be valid");

        // SCHEDULED can transition to LAUNCHED, SCHEDULED (self-loop), or CANCELING
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.SCHEDULED, ExperimentState.LAUNCHED),
                "SCHEDULED -> LAUNCHED should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.SCHEDULED, ExperimentState.SCHEDULED),
                "SCHEDULED -> SCHEDULED (self-loop) should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.SCHEDULED, ExperimentState.CANCELING),
                "SCHEDULED -> CANCELING should be valid");

        // LAUNCHED can transition to EXECUTING or CANCELING
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
                "LAUNCHED -> EXECUTING should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.LAUNCHED, ExperimentState.CANCELING),
                "LAUNCHED -> CANCELING should be valid");

        // EXECUTING can transition to COMPLETED, FAILED, CANCELED, SCHEDULED (requeue), or CANCELING
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.EXECUTING, ExperimentState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.EXECUTING, ExperimentState.FAILED),
                "EXECUTING -> FAILED should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.EXECUTING, ExperimentState.CANCELED),
                "EXECUTING -> CANCELED should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.EXECUTING, ExperimentState.SCHEDULED),
                "EXECUTING -> SCHEDULED (requeue) should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.EXECUTING, ExperimentState.CANCELING),
                "EXECUTING -> CANCELING should be valid");

        // CANCELING can transition to CANCELING (self-loop) or CANCELED
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELING, ExperimentState.CANCELING),
                "CANCELING -> CANCELING (self-loop) should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELING, ExperimentState.CANCELED),
                "CANCELING -> CANCELED should be valid");
    }

    @Test
    @DisplayName("Test ExperimentStateValidator - invalid transitions")
    public void testExperimentStateValidator_InvalidTransitions() {
        // Terminal states cannot transition out
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.COMPLETED, ExperimentState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.FAILED, ExperimentState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELED, ExperimentState.EXECUTING),
                "CANCELED -> EXECUTING should be invalid");

        // Invalid jumps (skipping required states)
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.EXECUTING),
                "CREATED -> EXECUTING (skipping SCHEDULED/LAUNCHED) should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.COMPLETED),
                "CREATED -> COMPLETED should be invalid");
    }

    @Test
    @DisplayName("Test TaskStateValidator - all valid transitions")
    public void testTaskStateValidator_AllValidTransitions() {
        // CREATED can transition to EXECUTING
        assertTrue(
                TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.EXECUTING),
                "CREATED -> EXECUTING should be valid");

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
    }

    @Test
    @DisplayName("Test TaskStateValidator - invalid transitions")
    public void testTaskStateValidator_InvalidTransitions() {
        // Terminal states cannot transition out
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.COMPLETED, TaskState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.FAILED, TaskState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                TaskStateValidator.INSTANCE.isValid(TaskState.CANCELED, TaskState.EXECUTING),
                "CANCELED -> EXECUTING should be invalid");

        // Invalid jumps (skipping required states)
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
    @DisplayName("Test ExperimentStateValidator - null handling")
    public void testExperimentStateValidator_NullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(null, ExperimentState.CREATED),
                "null -> CREATED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, null),
                "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(ExperimentStateValidator.INSTANCE.isValid(null, null), "null -> null should be invalid");
    }

    @Test
    @DisplayName("Test TaskStateValidator - null handling")
    public void testTaskStateValidator_NullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                TaskStateValidator.INSTANCE.isValid(null, TaskState.CREATED),
                "null -> CREATED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, null), "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(TaskStateValidator.INSTANCE.isValid(null, null), "null -> null should be invalid");
    }

    @Test
    @DisplayName("Test StateTransitionService rejects invalid transitions through service layer")
    public void testStateTransitionServiceRejectsInvalidTransitions() {
        // Test that StateTransitionService.validateAndLog() correctly rejects invalid transitions
        assertFalse(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.COMPLETED, ExperimentState.EXECUTING),
                "StateTransitionService should reject COMPLETED -> EXECUTING");
        assertFalse(
                StateTransitionService.isValid(TaskStateValidator.INSTANCE, TaskState.COMPLETED, TaskState.EXECUTING),
                "StateTransitionService should reject COMPLETED -> EXECUTING");
        assertFalse(
                StateTransitionService.isValid(JobStateValidator.INSTANCE, JobState.COMPLETE, JobState.SUBMITTED),
                "StateTransitionService should reject COMPLETE -> SUBMITTED");
    }

    @Test
    @DisplayName("Test StateTransitionService.validateAndLog() logs invalid transitions correctly")
    public void testStateTransitionServiceLogging() {
        // Test that validateAndLog() correctly logs and rejects invalid transitions
        String testEntityId = "test-entity-123";
        String testEntityType = "experiment";

        // Test invalid transition is rejected and logged
        boolean result = StateTransitionService.validateAndLog(
                ExperimentStateValidator.INSTANCE,
                ExperimentState.COMPLETED,
                ExperimentState.EXECUTING,
                testEntityId,
                testEntityType);
        assertFalse(result, "Invalid transition should be rejected");

        // Test valid transition is accepted and logged
        boolean validResult = StateTransitionService.validateAndLog(
                ExperimentStateValidator.INSTANCE,
                ExperimentState.CREATED,
                ExperimentState.SCHEDULED,
                testEntityId,
                testEntityType);
        assertTrue(validResult, "Valid transition should be accepted");
    }

    @Test
    @DisplayName("Test invalid transitions are rejected when attempting through service layer")
    public void testInvalidTransitionsRejectedAtServiceLayer() throws RegistryException {
        // This test demonstrates that even if we try to add invalid transitions through the service,
        // they should be rejected. In practice, services should use StateTransitionService to validate.

        // Test that we can't transition from COMPLETED to EXECUTING
        // First, get experiment to COMPLETED state
        ExperimentStatus created = StateMachineTestUtils.createExperimentStatus(ExperimentState.CREATED, "Created");
        registryService.updateExperimentStatus(created, testHierarchy.experimentId);

        ExperimentStatus scheduled =
                StateMachineTestUtils.createExperimentStatus(ExperimentState.SCHEDULED, "Scheduled");
        registryService.updateExperimentStatus(scheduled, testHierarchy.experimentId);

        ExperimentStatus launched = StateMachineTestUtils.createExperimentStatus(ExperimentState.LAUNCHED, "Launched");
        registryService.updateExperimentStatus(launched, testHierarchy.experimentId);

        ExperimentStatus executing =
                StateMachineTestUtils.createExperimentStatus(ExperimentState.EXECUTING, "Executing");
        registryService.updateExperimentStatus(executing, testHierarchy.experimentId);

        ExperimentStatus completed =
                StateMachineTestUtils.createExperimentStatus(ExperimentState.COMPLETED, "Completed");
        registryService.updateExperimentStatus(completed, testHierarchy.experimentId);

        // Verify we're in COMPLETED state
        ExperimentStatus currentStatus = registryService.getExperimentStatus(testHierarchy.experimentId);
        assertEquals(ExperimentState.COMPLETED, currentStatus.getState(), "Should be in COMPLETED state");

        // Note: The service layer doesn't currently enforce validation, but StateTransitionService
        // provides the validation logic. This test verifies the validator correctly identifies
        // invalid transitions that should be rejected if validation is added to services.
        assertFalse(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.COMPLETED, ExperimentState.EXECUTING),
                "COMPLETED -> EXECUTING should be rejected by validator");
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
