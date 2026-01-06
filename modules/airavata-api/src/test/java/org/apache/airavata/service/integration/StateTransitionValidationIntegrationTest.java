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
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.MessageVerificationUtils;
import org.apache.airavata.messaging.Publisher;
import org.apache.airavata.messaging.Subscriber;
import org.apache.airavata.messaging.Type;
import org.apache.airavata.messaging.rabbitmq.MessagingFactory;
import org.apache.airavata.monitor.JobStateValidator;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
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
            org.apache.airavata.config.AiravataServerProperties.class,
            StateTransitionValidationIntegrationTest.TestConfiguration.class
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
@Transactional
public class StateTransitionValidationIntegrationTest extends ServiceIntegrationTestBase {

    @org.junit.jupiter.api.BeforeAll
    public static void setupMessagingServices() {
        // Initialize Kafka and RabbitMQ containers via TestcontainersConfig
        org.apache.airavata.config.TestcontainersConfig.getKafkaBootstrapServers();
        org.apache.airavata.config.TestcontainersConfig.getRabbitMQUrl();
        org.apache.airavata.config.TestcontainersConfig.getZookeeperConnectionString();
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.monitor"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessStatusService processStatusService;
    private final TaskService taskService;
    private final JobService jobService;
    private final JobStatusService jobStatusService;

    @Autowired(required = false)
    private MessagingFactory messagingFactory;

    private TestHierarchy testHierarchy;

    public StateTransitionValidationIntegrationTest(
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
    public void testJobStateValidator_AllValidTransitions() {
        // Test all valid JobState transitions according to JobStateValidator
        // SUBMITTED can transition to all other states
        assertTrue(
                JobStateValidator.isValid(JobState.SUBMITTED, JobState.QUEUED), "SUBMITTED -> QUEUED should be valid");
        assertTrue(
                JobStateValidator.isValid(JobState.SUBMITTED, JobState.ACTIVE), "SUBMITTED -> ACTIVE should be valid");
        assertTrue(
                JobStateValidator.isValid(JobState.SUBMITTED, JobState.COMPLETE),
                "SUBMITTED -> COMPLETE should be valid");
        assertTrue(
                JobStateValidator.isValid(JobState.SUBMITTED, JobState.FAILED), "SUBMITTED -> FAILED should be valid");
        assertTrue(
                JobStateValidator.isValid(JobState.SUBMITTED, JobState.CANCELED),
                "SUBMITTED -> CANCELED should be valid");

        // QUEUED can transition to multiple states
        assertTrue(JobStateValidator.isValid(JobState.QUEUED, JobState.ACTIVE), "QUEUED -> ACTIVE should be valid");
        assertTrue(JobStateValidator.isValid(JobState.QUEUED, JobState.COMPLETE), "QUEUED -> COMPLETE should be valid");
        assertTrue(JobStateValidator.isValid(JobState.QUEUED, JobState.FAILED), "QUEUED -> FAILED should be valid");

        assertTrue(JobStateValidator.isValid(JobState.ACTIVE, JobState.COMPLETE), "ACTIVE -> COMPLETE should be valid");
        assertTrue(JobStateValidator.isValid(JobState.ACTIVE, JobState.FAILED), "ACTIVE -> FAILED should be valid");
        assertTrue(JobStateValidator.isValid(JobState.ACTIVE, JobState.CANCELED), "ACTIVE -> CANCELED should be valid");

        // NON_CRITICAL_FAIL can recover
        assertTrue(
                JobStateValidator.isValid(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
                "NON_CRITICAL_FAIL -> QUEUED should be valid");
        assertTrue(
                JobStateValidator.isValid(JobState.NON_CRITICAL_FAIL, JobState.ACTIVE),
                "NON_CRITICAL_FAIL -> ACTIVE should be valid");
    }

    @Test
    public void testJobStateValidator_InvalidTransitions() {
        // Test invalid JobState transitions
        // COMPLETE cannot transition to other states
        assertFalse(
                JobStateValidator.isValid(JobState.COMPLETE, JobState.SUBMITTED),
                "COMPLETE -> SUBMITTED should be invalid");
        assertFalse(
                JobStateValidator.isValid(JobState.COMPLETE, JobState.ACTIVE), "COMPLETE -> ACTIVE should be invalid");

        // FAILED cannot transition to SUBMITTED
        assertFalse(
                JobStateValidator.isValid(JobState.FAILED, JobState.SUBMITTED),
                "FAILED -> SUBMITTED should be invalid");

        // CANCELED cannot transition to active states
        assertFalse(
                JobStateValidator.isValid(JobState.CANCELED, JobState.ACTIVE), "CANCELED -> ACTIVE should be invalid");
        assertFalse(
                JobStateValidator.isValid(JobState.CANCELED, JobState.QUEUED), "CANCELED -> QUEUED should be invalid");
    }

    @Test
    public void testProcessState_SequentialTransitions() throws RegistryException, InterruptedException {
        // Test that process states follow logical sequence
        List<ProcessState> sequentialStates = StateMachineTestUtils.getSuccessfulProcessStateSequence();

        // Add statuses in sequence
        for (ProcessState state : sequentialStates) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy.processId);
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
        jobStatusService.addJobStatus(status1, testHierarchy.jobPK);

        JobStatus status2 = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Queued");
        jobStatusService.addJobStatus(status2, testHierarchy.jobPK);

        JobStatus status3 = StateMachineTestUtils.createJobStatus(JobState.ACTIVE, "Active");
        jobStatusService.addJobStatus(status3, testHierarchy.jobPK);

 // timestamps
        var job = jobService.getJob(testHierarchy.jobPK);
        StateMachineTestUtils.verifyJobStateTimestamps(new ArrayList<JobStatus>(job.getJobStatuses()));

        // Test process state timestamps
        ProcessStatus pStatus1 = StateMachineTestUtils.createProcessStatus(ProcessState.CREATED, "Created");
        processStatusService.addProcessStatus(pStatus1, testHierarchy.processId);

        ProcessStatus pStatus2 = StateMachineTestUtils.createProcessStatus(ProcessState.VALIDATED, "Validated");
        processStatusService.addProcessStatus(pStatus2, testHierarchy.processId);

        ProcessStatus pStatus3 = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        processStatusService.addProcessStatus(pStatus3, testHierarchy.processId);

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
            jobStatusService.addJobStatus(status, testHierarchy.jobPK);
        }

 // all states are recorded
        var job = jobService.getJob(testHierarchy.jobPK);
        assertNotNull(job.getJobStatuses(), "Job should have status history");
        assertTrue(job.getJobStatuses().size() >= rapidStates.size(), "All rapid state transitions should be recorded");

 // latest state is correct
        JobStatus latest = jobStatusService.getJobStatus(testHierarchy.jobPK);
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
                JobStateValidator.isValid(null, JobState.SUBMITTED),
                "null -> SUBMITTED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(JobStateValidator.isValid(JobState.SUBMITTED, null), "SUBMITTED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(JobStateValidator.isValid(null, null), "null -> null should be invalid");
    }

    @Test
    @DisplayName("Should verify messages are published for valid state transitions")
    void shouldVerifyMessagesPublishedForValidTransitions() throws Exception {
        if (messagingFactory == null) {
            return;
        }

        List<MessageContext> capturedMessages = new ArrayList<>();
        CountDownLatch messageReceived = new CountDownLatch(2);

        MessageHandler handler = MessageVerificationUtils.createCapturingHandlerWithLatch(
                capturedMessages, messageReceived, 2);

        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(testHierarchy.processId);
        routingKeys.add(testHierarchy.jobId);

        Subscriber subscriber = null;
        Publisher publisher = null;

        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            publisher = messagingFactory.getPublisher(Type.STATUS);

            // Valid process transition: CREATED -> VALIDATED
            ProcessStatus validated = StateMachineTestUtils.createProcessStatus(ProcessState.VALIDATED, "Validated");
            processStatusService.addProcessStatus(validated, testHierarchy.processId);
            ProcessIdentifier processIdentifier = new ProcessIdentifier(
                    testHierarchy.processId, testHierarchy.experimentId, testHierarchy.gatewayId);
            ProcessStatusChangeEvent processEvent = new ProcessStatusChangeEvent(ProcessState.VALIDATED, processIdentifier);
            MessageContext processMsg = new MessageContext(
                    processEvent, MessageType.PROCESS, AiravataUtils.getId(MessageType.PROCESS.name()), testHierarchy.gatewayId);
            processMsg.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(processMsg);

            // Valid job transition: SUBMITTED -> QUEUED
            JobStatus queued = StateMachineTestUtils.createJobStatus(JobState.QUEUED, "Queued");
            jobStatusService.addJobStatus(queued, testHierarchy.jobPK);
            JobIdentifier jobIdentifier = new JobIdentifier(
                    testHierarchy.jobId, testHierarchy.taskId, testHierarchy.processId,
                    testHierarchy.experimentId, testHierarchy.gatewayId);
            JobStatusChangeEvent jobEvent = new JobStatusChangeEvent(JobState.QUEUED, jobIdentifier);
            MessageContext jobMsg = new MessageContext(
                    jobEvent, MessageType.JOB, AiravataUtils.getId(MessageType.JOB.name()), testHierarchy.gatewayId);
            jobMsg.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(jobMsg);

            commitTransaction();

            // Wait for messages
            boolean received = messageReceived.await(5, TimeUnit.SECONDS);

            assertTrue(received, "Messages should be received within timeout");
            assertTrue(capturedMessages.size() >= 2, "Should capture at least 2 messages");
 // both process and job messages were received
            assertTrue(
                    capturedMessages.stream().anyMatch(msg -> msg.getType() == MessageType.PROCESS),
                    "Should have process status message");
            assertTrue(
                    capturedMessages.stream().anyMatch(msg -> msg.getType() == MessageType.JOB),
                    "Should have job status message");
        } finally {
            if (subscriber != null) {
                // Note: Subscriber cleanup handled by connection close
            }
        }
    }
}
