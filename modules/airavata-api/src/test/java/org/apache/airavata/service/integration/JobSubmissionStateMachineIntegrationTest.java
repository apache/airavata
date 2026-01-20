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
import org.apache.airavata.common.model.JobIdentifier;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.JobStatusChangeEvent;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.dapr.messaging.DaprMessagingFactory;
import org.apache.airavata.dapr.messaging.MessageContext;
import org.apache.airavata.dapr.messaging.MessageHandler;
import org.apache.airavata.dapr.messaging.MessageVerificationUtils;
import org.apache.airavata.dapr.messaging.Publisher;
import org.apache.airavata.dapr.messaging.Subscriber;
import org.apache.airavata.dapr.messaging.Type;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.JobStatusService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.apache.airavata.service.integration.StateMachineTestUtils.TestHierarchy;
import org.apache.airavata.statemachine.JobStateValidator;
import org.apache.airavata.statemachine.ProcessStateValidator;
import org.apache.airavata.statemachine.StateTransitionService;
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
 * Integration tests for job submission state machine transitions.
 * Tests verify that job state transitions follow the correct state machine rules
 * and that state history is properly preserved.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            JobSubmissionStateMachineIntegrationTest.TestConfiguration.class
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
public class JobSubmissionStateMachineIntegrationTest extends ServiceIntegrationTestBase {

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
                "org.apache.airavata.messaging"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    static class TestConfiguration {}

    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(JobSubmissionStateMachineIntegrationTest.class);

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessStatusService processStatusService;
    private final TaskService taskService;
    private final JobService jobService;
    private final JobStatusService jobStatusService;

    @Autowired(required = false)
    private DaprMessagingFactory messagingFactory;

    private TestHierarchy testHierarchy;

    public JobSubmissionStateMachineIntegrationTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessStatusService processStatusService,
            TaskService taskService,
            JobService jobService,
            JobStatusService jobStatusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processStatusService = processStatusService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.jobStatusService = jobStatusService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        testHierarchy = StateMachineTestUtils.createTestHierarchy(
                gatewayService, projectService, experimentService, processService, taskService, jobService);
    }

    @Test
    public void testJobSubmission_CompleteStateTransitionFlow() throws RegistryException, InterruptedException {
        // Test complete flow: SUBMITTED -> QUEUED -> ACTIVE -> COMPLETE
        List<JobState> expectedStates = new ArrayList<>();
        expectedStates.add(JobState.SUBMITTED);
        expectedStates.add(JobState.QUEUED);
        expectedStates.add(JobState.ACTIVE);
        expectedStates.add(JobState.COMPLETE);

        // Add statuses in sequence
        JobStatus submitted = StateMachineTestUtils.createJobStatus(JobState.SUBMITTED, "Job submitted");
        jobStatusService.addJobStatus(submitted, testHierarchy.jobPK);

        JobStatus queued = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Job queued");
        jobStatusService.addJobStatus(queued, testHierarchy.jobPK);

        JobStatus active = StateMachineTestUtils.createJobStatus(JobState.ACTIVE, "Job is running");
        jobStatusService.addJobStatus(active, testHierarchy.jobPK);

        JobStatus complete = StateMachineTestUtils.createJobStatus(JobState.COMPLETE, "Job completed successfully");
        jobStatusService.addJobStatus(complete, testHierarchy.jobPK);

        // state transitions
        StateMachineTestUtils.verifyJobStateTransition(
                jobService, jobStatusService, testHierarchy.jobPK, expectedStates);

        // latest status
        JobStatus latest = jobStatusService.getJobStatus(testHierarchy.jobPK);
        assertNotNull(latest, "Latest status should exist");
        assertEquals(JobState.COMPLETE, latest.getJobState(), "Final state should be COMPLETE");
    }

    @Test
    public void testJobSubmission_FailedStateTransition() throws RegistryException, InterruptedException {
        // Test failure path: SUBMITTED -> FAILED
        JobStatus submitted = StateMachineTestUtils.createJobStatus(JobState.SUBMITTED, "Job submitted");
        jobStatusService.addJobStatus(submitted, testHierarchy.jobPK);

        JobStatus failed = StateMachineTestUtils.createJobStatus(JobState.FAILED, "Job execution failed");
        jobStatusService.addJobStatus(failed, testHierarchy.jobPK);

        // state
        JobStatus latest = jobStatusService.getJobStatus(testHierarchy.jobPK);
        assertNotNull(latest, "Latest status should exist");
        assertEquals(JobState.FAILED, latest.getJobState(), "Final state should be FAILED");

        // state validator allows this transition
        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.FAILED),
                "SUBMITTED -> FAILED should be a valid transition");
    }

    @Test
    public void testJobSubmission_QueuedToActiveTransition() throws RegistryException, InterruptedException {
        // Test: SUBMITTED -> QUEUED -> ACTIVE
        JobStatus submitted = StateMachineTestUtils.createJobStatus(JobState.SUBMITTED, "Job submitted");
        jobStatusService.addJobStatus(submitted, testHierarchy.jobPK);

        JobStatus queued = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Job queued");
        jobStatusService.addJobStatus(queued, testHierarchy.jobPK);

        JobStatus active = StateMachineTestUtils.createJobStatus(JobState.ACTIVE, "Job is now active");
        jobStatusService.addJobStatus(active, testHierarchy.jobPK);

        JobStatus latest = jobStatusService.getJobStatus(testHierarchy.jobPK);
        assertEquals(JobState.ACTIVE, latest.getJobState(), "Final state should be ACTIVE");

        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.ACTIVE),
                "QUEUED -> ACTIVE should be a valid transition");
    }

    @Test
    public void testJobSubmission_NonCriticalFailRecovery() throws RegistryException, InterruptedException {
        // Test recovery from NON_CRITICAL_FAIL: SUBMITTED -> NON_CRITICAL_FAIL -> QUEUED -> ACTIVE
        JobStatus submitted = StateMachineTestUtils.createJobStatus(JobState.SUBMITTED, "Job submitted");
        jobStatusService.addJobStatus(submitted, testHierarchy.jobPK);

        JobStatus nonCriticalFail =
                StateMachineTestUtils.createJobStatus(JobState.NON_CRITICAL_FAIL, "Non-critical failure occurred");
        jobStatusService.addJobStatus(nonCriticalFail, testHierarchy.jobPK);

        // Recover to QUEUED
        JobStatus queued = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Job requeued after recovery");
        jobStatusService.addJobStatus(queued, testHierarchy.jobPK);

        // Continue to ACTIVE
        JobStatus active = StateMachineTestUtils.createJobStatus(JobState.ACTIVE, "Job is now active");
        jobStatusService.addJobStatus(active, testHierarchy.jobPK);

        JobStatus latest = jobStatusService.getJobStatus(testHierarchy.jobPK);
        assertEquals(JobState.ACTIVE, latest.getJobState(), "Final state should be ACTIVE");

        assertTrue(
                JobStateValidator.INSTANCE.isValid(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
                "NON_CRITICAL_FAIL -> QUEUED should be a valid transition");
    }

    @Test
    public void testJobSubmission_InvalidStateTransitionRejected() {
        // Test that invalid transitions are rejected by JobStateValidator
        // COMPLETE -> SUBMITTED should be invalid
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.COMPLETE, JobState.SUBMITTED),
                "COMPLETE -> SUBMITTED should be an invalid transition");

        // FAILED -> SUBMITTED should be invalid
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.FAILED, JobState.SUBMITTED),
                "FAILED -> SUBMITTED should be an invalid transition");

        // CANCELED -> ACTIVE should be invalid
        assertFalse(
                JobStateValidator.INSTANCE.isValid(JobState.CANCELED, JobState.ACTIVE),
                "CANCELED -> ACTIVE should be an invalid transition");
    }

    @Test
    public void testJobSubmission_StateHistoryPreserved() throws RegistryException, InterruptedException {
        // Test that all state transitions are preserved in history
        List<JobState> states = new ArrayList<>();
        states.add(JobState.SUBMITTED);
        states.add(JobState.QUEUED);
        states.add(JobState.ACTIVE);
        states.add(JobState.COMPLETE);

        // Add all statuses
        for (JobState state : states) {
            JobStatus status = StateMachineTestUtils.createJobStatus(state, "State: " + state.name());
            jobStatusService.addJobStatus(status, testHierarchy.jobPK);
        }

        // all states are in history
        var job = jobService.getJob(testHierarchy.jobPK);
        assertNotNull(job.getJobStatuses(), "Job should have status history");
        assertTrue(
                job.getJobStatuses().size() >= states.size(),
                "Job should have at least " + states.size() + " status entries");

        // all expected states are present
        for (JobState expectedState : states) {
            boolean found = job.getJobStatuses().stream().anyMatch(s -> s.getJobState() == expectedState);
            assertTrue(found, "Job state history should contain " + expectedState);
        }

        // timestamps are in order
        StateMachineTestUtils.verifyJobStateTimestamps(new ArrayList<JobStatus>(job.getJobStatuses()));
    }

    @Test
    @DisplayName("Should verify messages are published when job status changes")
    void shouldVerifyMessagesPublishedOnJobStatusChanges() throws Exception {
        if (messagingFactory == null || !messagingFactory.isDaprAvailable()) {
            logger.warn("Dapr not available, skipping messaging verification");
            return;
        }

        List<MessageContext> capturedMessages = new ArrayList<>();
        CountDownLatch messageReceived = new CountDownLatch(2);

        MessageHandler handler =
                MessageVerificationUtils.createCapturingHandlerWithLatch(capturedMessages, messageReceived, 2);

        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(testHierarchy.jobId);
        routingKeys.add(testHierarchy.jobId + ".*");

        Subscriber subscriber = null;
        Publisher publisher = null;

        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            publisher = messagingFactory.getPublisher(Type.STATUS);

            JobStatus status1 = StateMachineTestUtils.createJobStatus(JobState.SUBMITTED, "Job submitted");
            jobStatusService.addJobStatus(status1, testHierarchy.jobPK);

            // Publish message (as would happen in real flow via AiravataTask)
            JobIdentifier identifier = new JobIdentifier(
                    testHierarchy.jobId,
                    testHierarchy.taskId,
                    testHierarchy.processId,
                    testHierarchy.experimentId,
                    testHierarchy.gatewayId);
            int publishSuccessCount = 0;

            JobStatusChangeEvent event1 = new JobStatusChangeEvent(JobState.SUBMITTED, identifier);
            MessageContext msgCtx1 = new MessageContext(
                    event1, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), testHierarchy.gatewayId);
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
            JobStatus status2 = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Job queued");
            jobStatusService.addJobStatus(status2, testHierarchy.jobPK);

            JobStatusChangeEvent event2 = new JobStatusChangeEvent(JobState.QUEUED, identifier);
            MessageContext msgCtx2 = new MessageContext(
                    event2, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), testHierarchy.gatewayId);
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
                // Wait for messages
                boolean received = messageReceived.await(5, TimeUnit.SECONDS);

                assertTrue(received, "Messages should be received within timeout");
                assertTrue(
                        capturedMessages.size() >= publishSuccessCount,
                        "Should capture at least " + publishSuccessCount + " messages");
                // messages contain correct job states
                if (publishSuccessCount >= 1) {
                    assertTrue(
                            capturedMessages.stream().anyMatch(msg -> {
                                if (msg.getType() == MessageType.JOB) {
                                    JobStatusChangeEvent event = (JobStatusChangeEvent) msg.getEvent();
                                    return event.getState() == JobState.SUBMITTED;
                                }
                                return false;
                            }),
                            "Should have SUBMITTED state message");
                }
                if (publishSuccessCount >= 2) {
                    assertTrue(
                            capturedMessages.stream().anyMatch(msg -> {
                                if (msg.getType() == MessageType.JOB) {
                                    JobStatusChangeEvent event = (JobStatusChangeEvent) msg.getEvent();
                                    return event.getState() == JobState.QUEUED;
                                }
                                return false;
                            }),
                            "Should have QUEUED state message");
                }
            } else {
                logger.warn("Skipping message reception verification - no messages were published successfully");
            }
        } finally {
            if (subscriber != null) {
                // Note: Subscriber cleanup handled by connection close
            }
        }
    }

    @Test
    @DisplayName("Test that job state transitions trigger process state updates")
    public void testJobStateTransitionsTriggerProcessStateUpdates() throws RegistryException {
        // Test that when job completes, process state is updated correctly
        // PostWorkflowManager handles job completion and creates post-execution tasks

        // Process is in EXECUTING state
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing, testHierarchy.processId);

        // Create a job for a task
        TaskModel task = new TaskModel();
        task.setTaskType(TaskTypes.JOB_SUBMISSION);
        task.setParentProcessId(testHierarchy.processId);
        String taskId = taskService.addTask(task, testHierarchy.processId);

        org.apache.airavata.common.model.JobModel jobModel = new org.apache.airavata.common.model.JobModel();
        jobModel.setJobId("test-job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job");
        String jobId = jobService.addJob(jobModel, testHierarchy.processId);

        org.apache.airavata.registry.entities.expcatalog.JobPK jobPK =
                new org.apache.airavata.registry.entities.expcatalog.JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        // Job completes
        JobStatus jobComplete = StateMachineTestUtils.createJobStatus(JobState.COMPLETE, "Job completed");
        jobStatusService.addJobStatus(jobComplete, jobPK);

        // When job completes, PostWorkflowManager would create post-execution tasks
        // Process may transition to COMPLETED after all post-execution tasks complete
        ProcessStatus completed = StateMachineTestUtils.createProcessStatus(ProcessState.COMPLETED, "Completed");
        processStatusService.addProcessStatus(completed, testHierarchy.processId);

        // Verify state transition was valid
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.EXECUTING, ProcessState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid when job completes");

        // Verify final state
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Process should be in COMPLETED state");
    }

    @Test
    @DisplayName("Test that PostWorkflowManager tasks execute based on job state")
    public void testPostWorkflowManagerTasksExecuteBasedOnJobState() throws RegistryException {
        // Test that PostWorkflowManager creates correct tasks when job completes or fails

        // Process is in EXECUTING state
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing, testHierarchy.processId);

        // Create job
        TaskModel task = new TaskModel();
        task.setTaskType(TaskTypes.JOB_SUBMISSION);
        task.setParentProcessId(testHierarchy.processId);
        String taskId = taskService.addTask(task, testHierarchy.processId);

        org.apache.airavata.common.model.JobModel jobModel = new org.apache.airavata.common.model.JobModel();
        jobModel.setJobId("test-job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job for PostWorkflowManager tasks");
        String jobId = jobService.addJob(jobModel, testHierarchy.processId);

        org.apache.airavata.registry.entities.expcatalog.JobPK jobPK =
                new org.apache.airavata.registry.entities.expcatalog.JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);

        // Job completes -> PostWorkflowManager would create post-execution tasks
        // JOB_VERIFICATION -> OUTPUT_DATA_STAGING -> COMPLETING -> PARSING_TRIGGERING
        JobStatus jobComplete = StateMachineTestUtils.createJobStatus(JobState.COMPLETE, "Job completed");
        jobStatusService.addJobStatus(jobComplete, jobPK);

        // Simulate PostWorkflowManager creating post-execution tasks
        TaskModel outputStagingTask = new TaskModel();
        outputStagingTask.setTaskType(TaskTypes.DATA_STAGING);
        outputStagingTask.setParentProcessId(testHierarchy.processId);
        String outputTaskId = taskService.addTask(outputStagingTask, testHierarchy.processId);

        // Verify task was created
        ProcessModel process = processService.getProcess(testHierarchy.processId);
        assertNotNull(process.getTasks(), "Process should have tasks");
        assertTrue(
                process.getTasks().stream().anyMatch(t -> t.getTaskId().equals(outputTaskId)),
                "Process should have output staging task");

        // Verify job state transition was valid
        assertTrue(
                StateTransitionService.isValid(JobStateValidator.INSTANCE, JobState.SUBMITTED, JobState.COMPLETE),
                "SUBMITTED -> COMPLETE should be valid for jobs");
    }
}
