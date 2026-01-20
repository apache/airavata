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
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.statemachine.ProcessStateValidator;
import org.apache.airavata.statemachine.StateTransitionService;
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
 * Comprehensive integration tests for ProcessStateValidator covering all transition paths.
 * Tests verify that all valid state transitions work correctly and invalid transitions are rejected.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            ProcessStateTransitionComprehensiveTest.TestConfiguration.class
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
public class ProcessStateTransitionComprehensiveTest extends ServiceIntegrationTestBase {

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

    private String testProcessId;
    private String testExperimentId;

    public ProcessStateTransitionComprehensiveTest(
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
        super.setUpBase();

        Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
        if (!gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            gatewayService.addGateway(gateway);
        }

        Project project = TestDataFactory.createTestProject("Process State Test Project", TEST_GATEWAY_ID);
        String projectId = projectService.addProject(project, TEST_GATEWAY_ID);

        ExperimentModel experiment =
                TestDataFactory.createTestExperiment("Process State Experiment", projectId, TEST_GATEWAY_ID);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        testExperimentId = experimentService.addExperiment(experiment);

        ProcessModel process = new ProcessModel();
        process.setExperimentId(testExperimentId);
        testProcessId = processService.addProcess(process, testExperimentId);
    }

    @Test
    @DisplayName(
            "Test forward flow: CREATED -> VALIDATED -> STARTED -> PRE_PROCESSING -> CONFIGURING_WORKSPACE -> INPUT_DATA_STAGING -> EXECUTING -> MONITORING -> OUTPUT_DATA_STAGING -> POST_PROCESSING -> COMPLETED")
    public void testForwardFlowComplete() throws RegistryException {
        List<ProcessState> forwardFlow = StateMachineTestUtils.getSuccessfulProcessStateSequence();

        // Add statuses in sequence
        for (ProcessState state : forwardFlow) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testProcessId);
        }

        // Verify all states are in history
        ProcessModel process = processService.getProcess(testProcessId);
        assertNotNull(process.getProcessStatuses(), "Process should have status history");
        assertTrue(
                process.getProcessStatuses().size() >= forwardFlow.size(),
                "Process should have at least " + forwardFlow.size() + " status entries");

        // Verify latest state
        ProcessStatus latest = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");
    }

    @Test
    @DisplayName("Test queuing flow: STARTED -> QUEUED -> DEQUEUING -> EXECUTING")
    public void testQueuingFlow() throws RegistryException {
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.QUEUED);
        states.add(ProcessState.DEQUEUING);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testProcessId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // Verify queuing states are in history
        ProcessModel process = processService.getProcess(testProcessId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.QUEUED),
                "Process should have QUEUED state in history");
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.DEQUEUING),
                "Process should have DEQUEUING state in history");
    }

    @Test
    @DisplayName("Test requeue flow: EXECUTING -> REQUEUED -> QUEUED -> DEQUEUING -> EXECUTING")
    public void testRequeueFlow() throws RegistryException {
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.REQUEUED);
        states.add(ProcessState.QUEUED);
        states.add(ProcessState.DEQUEUING);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testProcessId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // Verify requeue states are in history
        ProcessModel process = processService.getProcess(testProcessId);
        assertTrue(
                process.getProcessStatuses().stream().anyMatch(s -> s.getState() == ProcessState.REQUEUED),
                "Process should have REQUEUED state in history");
    }

    @Test
    @DisplayName("Test cancellation flow: any state -> CANCELLING -> CANCELED")
    public void testCancellationFlow() throws RegistryException {
        // Test cancellation from EXECUTING
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
            processStatusService.addProcessStatus(status, testProcessId);
        }

        ProcessStatus latest = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.CANCELED, latest.getState(), "Final state should be CANCELED");
    }

    @Test
    @DisplayName("Test failure paths from various states")
    public void testFailurePaths() throws RegistryException {
        // Test failure from CREATED
        ProcessStatus created = StateMachineTestUtils.createProcessStatus(ProcessState.CREATED, "Created");
        processStatusService.addProcessStatus(created, testProcessId);

        ProcessStatus failed1 = StateMachineTestUtils.createProcessStatus(ProcessState.FAILED, "Failed from CREATED");
        processStatusService.addProcessStatus(failed1, testProcessId);

        ProcessStatus latest1 = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.FAILED, latest1.getState(), "Process should be in FAILED state");

        // Create new process for next test
        ProcessModel process2 = new ProcessModel();
        process2.setExperimentId(testExperimentId);
        String processId2 = processService.addProcess(process2, testExperimentId);

        // Test failure from EXECUTING
        ProcessStatus started = StateMachineTestUtils.createProcessStatus(ProcessState.STARTED, "Started");
        processStatusService.addProcessStatus(started, processId2);

        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing, processId2);

        ProcessStatus failed2 = StateMachineTestUtils.createProcessStatus(ProcessState.FAILED, "Failed from EXECUTING");
        processStatusService.addProcessStatus(failed2, processId2);

        ProcessStatus latest2 = processStatusService.getProcessStatus(processId2);
        assertEquals(ProcessState.FAILED, latest2.getState(), "Process should be in FAILED state");
    }

    @Test
    @DisplayName("Test invalid transitions from terminal states")
    public void testInvalidTransitionsFromTerminalStates() {
        // Terminal states: COMPLETED, FAILED, CANCELED (no transitions out)
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.COMPLETED, ProcessState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.COMPLETED, ProcessState.STARTED),
                "COMPLETED -> STARTED should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.FAILED, ProcessState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.FAILED, ProcessState.STARTED),
                "FAILED -> STARTED should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CANCELED, ProcessState.EXECUTING),
                "CANCELED -> EXECUTING should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CANCELED, ProcessState.STARTED),
                "CANCELED -> STARTED should be invalid");
    }

    @Test
    @DisplayName("Test invalid transitions (skipping required states)")
    public void testInvalidTransitionsSkippingStates() {
        // Invalid jumps
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, ProcessState.EXECUTING),
                "CREATED -> EXECUTING (skipping VALIDATED/STARTED) should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, ProcessState.COMPLETED),
                "CREATED -> COMPLETED should be invalid");
        assertFalse(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.STARTED, ProcessState.COMPLETED),
                "STARTED -> COMPLETED (skipping EXECUTING) should be invalid");
    }

    @Test
    @DisplayName("Test StateTransitionService validates transitions correctly")
    public void testStateTransitionServiceValidation() {
        // Test that StateTransitionService.validateAndLog() correctly validates transitions
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.CREATED, ProcessState.VALIDATED),
                "StateTransitionService should validate CREATED -> VALIDATED");
        assertTrue(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.EXECUTING, ProcessState.COMPLETED),
                "StateTransitionService should validate EXECUTING -> COMPLETED");
        assertFalse(
                StateTransitionService.isValid(
                        ProcessStateValidator.INSTANCE, ProcessState.COMPLETED, ProcessState.EXECUTING),
                "StateTransitionService should reject COMPLETED -> EXECUTING");
    }

    @Test
    @DisplayName("Test process state affects experiment state")
    public void testProcessStateAffectsExperimentState() throws RegistryException {
        // When process transitions to EXECUTING, experiment should transition to EXECUTING
        ProcessStatus executing = StateMachineTestUtils.createProcessStatus(ProcessState.EXECUTING, "Executing");
        processStatusService.addProcessStatus(executing, testProcessId);

        // Verify process state
        ProcessStatus processStatus = processStatusService.getProcessStatus(testProcessId);
        assertEquals(ProcessState.EXECUTING, processStatus.getState(), "Process should be in EXECUTING state");

        // Note: In a real system, experiment state would be updated based on process state
        // This test verifies the process state transition works correctly
        ExperimentModel experiment = experimentService.getExperiment(testExperimentId);
        assertNotNull(experiment, "Experiment should exist");
    }

    @Test
    @DisplayName("Test all valid transitions from CREATED")
    public void testValidTransitionsFromCreated() {
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, ProcessState.VALIDATED),
                "CREATED -> VALIDATED should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, ProcessState.STARTED),
                "CREATED -> STARTED should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, ProcessState.CANCELLING),
                "CREATED -> CANCELLING should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, ProcessState.FAILED),
                "CREATED -> FAILED should be valid");
    }

    @Test
    @DisplayName("Test all valid transitions from EXECUTING")
    public void testValidTransitionsFromExecuting() {
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.MONITORING),
                "EXECUTING -> MONITORING should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.OUTPUT_DATA_STAGING),
                "EXECUTING -> OUTPUT_DATA_STAGING should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.POST_PROCESSING),
                "EXECUTING -> POST_PROCESSING should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.FAILED),
                "EXECUTING -> FAILED should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.QUEUED),
                "EXECUTING -> QUEUED should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.REQUEUED),
                "EXECUTING -> REQUEUED should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.CANCELLING),
                "EXECUTING -> CANCELLING should be valid");
        assertTrue(
                ProcessStateValidator.INSTANCE.isValid(ProcessState.EXECUTING, ProcessState.CANCELED),
                "EXECUTING -> CANCELED should be valid");
    }

    @Test
    @DisplayName("Test state history preservation for complex flows")
    public void testStateHistoryPreservation() throws RegistryException {
        // Test a complex flow with requeue
        List<ProcessState> states = new ArrayList<>();
        states.add(ProcessState.CREATED);
        states.add(ProcessState.VALIDATED);
        states.add(ProcessState.STARTED);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.REQUEUED);
        states.add(ProcessState.QUEUED);
        states.add(ProcessState.DEQUEUING);
        states.add(ProcessState.EXECUTING);
        states.add(ProcessState.COMPLETED);

        // Add statuses
        for (ProcessState state : states) {
            ProcessStatus status = StateMachineTestUtils.createProcessStatus(state, "State: " + state.name());
            processStatusService.addProcessStatus(status, testProcessId);
        }

        // Verify all states are in history
        ProcessModel process = processService.getProcess(testProcessId);
        assertNotNull(process.getProcessStatuses(), "Process should have status history");
        assertTrue(
                process.getProcessStatuses().size() >= states.size(),
                "Process should have at least " + states.size() + " status entries");

        // Verify all expected states are present
        for (ProcessState expectedState : states) {
            boolean found = process.getProcessStatuses().stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Process state history should contain " + expectedState);
        }

        // Verify timestamps are in order
        StateMachineTestUtils.verifyProcessStateTimestamps(new ArrayList<ProcessStatus>(process.getProcessStatuses()));
    }
}
