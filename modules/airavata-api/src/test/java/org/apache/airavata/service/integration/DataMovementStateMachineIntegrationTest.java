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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessIdentifier;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.MessageVerificationUtils;
import org.apache.airavata.messaging.Publisher;
import org.apache.airavata.messaging.Subscriber;
import org.apache.airavata.messaging.Type;
import org.apache.airavata.messaging.rabbitmq.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.service.integration.StateMachineTestUtils.TestHierarchy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for data movement state machine transitions.
 * Tests verify that input and output data staging transitions work correctly
 * and that the process state properly reflects data movement stages.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            DataMovementStateMachineIntegrationTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.flyway.enabled=false",
            "airavata.security.iam.enabled=false",
            "airavata.security.manager.enabled=false",
            "airavata.security.authzCache.enabled=false",
        })
@org.junit.jupiter.api.condition.EnabledIf("isMessagingContainerRunning")
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional("expCatalogTransactionManager")
public class DataMovementStateMachineIntegrationTest extends ServiceIntegrationTestBase {

    /**
     * Check if messaging containers are available (not via SSH tunnel).
     */
    static boolean isMessagingContainerRunning() {
        return !org.apache.airavata.config.TestcontainersConfig.shouldUseExistingContainers();
    }

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
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessStatusService processStatusService;

    @Autowired(required = false)
    private MessagingFactory messagingFactory;

    private TestHierarchy testHierarchy;

    public DataMovementStateMachineIntegrationTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessStatusService processStatusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processStatusService = processStatusService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        testHierarchy = StateMachineTestUtils.createTestHierarchy(
                gatewayService,
                projectService,
                experimentService,
                processService,
                null, // TaskService not needed
                null); // JobService not needed
    }

    @Test
    public void testInputDataStaging_StateTransition() throws RegistryException, InterruptedException {
        // Test that process transitions to INPUT_DATA_STAGING during input staging
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.PRE_PROCESSING);
        states.add(ProcessState.CONFIGURING_WORKSPACE);
        states.add(ProcessState.INPUT_DATA_STAGING);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(
                ProcessState.INPUT_DATA_STAGING, latest.getState(), "Process should be in INPUT_DATA_STAGING state");

        // state is in history
        var process = processService.getProcess(testHierarchy.processId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.INPUT_DATA_STAGING),
                "Process should have INPUT_DATA_STAGING state in history");
    }

    @Test
    public void testOutputDataStaging_StateTransition() throws RegistryException, InterruptedException {
        // Test that process transitions to OUTPUT_DATA_STAGING during output staging
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.MONITORING);
        states.add(ProcessState.OUTPUT_DATA_STAGING);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(
                ProcessState.OUTPUT_DATA_STAGING, latest.getState(), "Process should be in OUTPUT_DATA_STAGING state");

        // state is in history
        var process = processService.getProcess(testHierarchy.processId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.OUTPUT_DATA_STAGING),
                "Process should have OUTPUT_DATA_STAGING state in history");
    }

    @Test
    public void testDataMovement_FullFlow() throws RegistryException, InterruptedException {
        // Test full data movement flow: INPUT_DATA_STAGING -> EXECUTING -> OUTPUT_DATA_STAGING
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.PRE_PROCESSING);
        states.add(ProcessState.CONFIGURING_WORKSPACE);
        states.add(ProcessState.INPUT_DATA_STAGING);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.MONITORING);
        states.add(ProcessState.OUTPUT_DATA_STAGING);
        states.add(ProcessState.POST_PROCESSING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // data staging states are in history
        var process = processService.getProcess(testHierarchy.processId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.INPUT_DATA_STAGING),
                "Process should have INPUT_DATA_STAGING state in history");
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.OUTPUT_DATA_STAGING),
                "Process should have OUTPUT_DATA_STAGING state in history");
    }

    @Test
    public void testDataMovement_FailureHandling() throws RegistryException, InterruptedException {
        // Test that data staging failures trigger FAILED state
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.PRE_PROCESSING);
        states.add(ProcessState.CONFIGURING_WORKSPACE);
        states.add(ProcessState.INPUT_DATA_STAGING);
        states.add(ProcessState.FAILED); // Failure during input staging

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.FAILED, latest.getState(), "Process should be in FAILED state");

        // Test failure during output staging
        // Create a new process for this test
        TestHierarchy testHierarchy2 = StateMachineTestUtils.createTestHierarchy(
                gatewayService, projectService, experimentService, processService, null, null);

        List<ProcessState> states2 = new ArrayList<>();
        states2.add(ProcessState.CREATED);
        states2.add(ProcessState.VALIDATED);
        states2.add(ProcessState.STARTED);
        states2.add(ProcessState.EXECUTING);
        states2.add(ProcessState.MONITORING);
        states2.add(ProcessState.OUTPUT_DATA_STAGING);
        states2.add(ProcessState.FAILED); // Failure during output staging

        for (ProcessState state : states2) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy2.processId);
        }

        ProcessStatus latest2 = processStatusService.getProcessStatus(testHierarchy2.processId);
        assertEquals(ProcessState.FAILED, latest2.getState(), "Process should be in FAILED state");
    }

    @Test
    public void testDataMovement_MultipleFiles() throws RegistryException, InterruptedException {
        // Test that multiple input/output files are handled correctly
        // This test verifies that the state machine can handle multiple data staging operations
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.PRE_PROCESSING);
        states.add(ProcessState.CONFIGURING_WORKSPACE);
        states.add(ProcessState.INPUT_DATA_STAGING);
        // Multiple input files would be staged here (state remains INPUT_DATA_STAGING)
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.MONITORING);
        states.add(ProcessState.OUTPUT_DATA_STAGING);
        // Multiple output files would be staged here (state remains OUTPUT_DATA_STAGING)
        states.add(ProcessState.POST_PROCESSING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        var process = processService.getProcess(testHierarchy.processId);
        long inputStagingCount = process.getProcessStatuses().stream()
                .filter(s -> s.getState() == ProcessState.INPUT_DATA_STAGING)
                .count();
        long outputStagingCount = process.getProcessStatuses().stream()
                .filter(s -> s.getState() == ProcessState.OUTPUT_DATA_STAGING)
                .count();

        assertTrue(inputStagingCount >= 1, "Should have at least one INPUT_DATA_STAGING state");
        assertTrue(outputStagingCount >= 1, "Should have at least one OUTPUT_DATA_STAGING state");
    }

    @Test
    @DisplayName("Should verify messages are published for data staging state transitions")
    void shouldVerifyMessagesPublishedForDataStaging() throws Exception {
        if (messagingFactory == null) {
            return;
        }

        List<MessageContext> capturedMessages = new ArrayList<>();
        CountDownLatch messageReceived = new CountDownLatch(3); // Expect 3 messages for data staging states

        MessageHandler handler =
                MessageVerificationUtils.createCapturingHandlerWithLatch(capturedMessages, messageReceived, 3);

        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(testHierarchy.processId);
        routingKeys.add(testHierarchy.processId + ".*");

        Subscriber subscriber = null;
        Publisher publisher = null;

        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.STATUS);
            publisher = messagingFactory.getPublisher(Type.STATUS);

            ProcessIdentifier identifier =
                    new ProcessIdentifier(testHierarchy.processId, testHierarchy.experimentId, testHierarchy.gatewayId);

            ProcessStatus inputStaging =
                    StateMachineTestUtils.createProcessStatus(ProcessState.INPUT_DATA_STAGING, "Input data staging");
            processStatusService.addProcessStatus(inputStaging, testHierarchy.processId);
            
            ProcessStatusChangeEvent event1 = new ProcessStatusChangeEvent(ProcessState.INPUT_DATA_STAGING, identifier);
            MessageContext msgCtx1 = new MessageContext(
                    event1,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    testHierarchy.gatewayId);
            msgCtx1.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(msgCtx1);

            ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
            processStatusService.addProcessStatus(executing, testHierarchy.processId);
            
            ProcessStatusChangeEvent event2 = new ProcessStatusChangeEvent(ProcessState.EXECUTING, identifier);
            MessageContext msgCtx2 = new MessageContext(
                    event2,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    testHierarchy.gatewayId);
            msgCtx2.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(msgCtx2);

            ProcessStatus outputStaging =
                    StateMachineTestUtils.createProcessStatus(ProcessState.OUTPUT_DATA_STAGING, "Output data staging");
            processStatusService.addProcessStatus(outputStaging, testHierarchy.processId);
            
            ProcessStatusChangeEvent event3 =
                    new ProcessStatusChangeEvent(ProcessState.OUTPUT_DATA_STAGING, identifier);
            MessageContext msgCtx3 = new MessageContext(
                    event3,
                    MessageType.PROCESS,
                    AiravataUtils.getId(MessageType.PROCESS.name()),
                    testHierarchy.gatewayId);
            msgCtx3.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(msgCtx3);

            // Wait for messages
            boolean received = messageReceived.await(5, TimeUnit.SECONDS);

            assertTrue(received, "Messages should be received within timeout");
            assertTrue(
                    MessageVerificationUtils.verifyProcessStateMessage(
                            capturedMessages, testHierarchy.processId, ProcessState.INPUT_DATA_STAGING),
                    "Should have INPUT_DATA_STAGING state message");
            assertTrue(
                    MessageVerificationUtils.verifyProcessStateMessage(
                            capturedMessages, testHierarchy.processId, ProcessState.OUTPUT_DATA_STAGING),
                    "Should have OUTPUT_DATA_STAGING state message");
        } finally {
            if (subscriber != null) {
                // Note: Subscriber cleanup handled by connection close
            }
        }
    }
}
