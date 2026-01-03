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
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProcessStatusService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.service.integration.StateMachineTestUtils.TestHierarchy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
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
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            ProcessExecutionStateMachineIntegrationTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
            "services.airavata.enabled=true"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata-integration.properties")
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
                "org.apache.airavata.service.orchestrator"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessStatusService processStatusService;

    private TestHierarchy testHierarchy;

    public ProcessExecutionStateMachineIntegrationTest(
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
                null, // TaskService not needed for process tests
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
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        // Verify state transitions
        StateMachineTestUtils.verifyProcessStateTransition(
                processService, processStatusService, testHierarchy.processId, expectedStates);

        // Verify latest status
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
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
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        // Verify final state
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // Verify queuing states are in history
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
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        // Verify final state
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
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
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        // Verify final state
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
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
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        // Verify final state
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.COMPLETED, latest.getState(), "Final state should be COMPLETED");

        // Verify requeue states are in history
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
        processStatusService.addProcessStatus(started, testHierarchy.processId);

        // Note: In a real scenario, OrchestratorService.handleProcessStatusChange would be called
        // to update experiment state. For this test, we verify the process state is correct.
        ProcessStatus latest = processStatusService.getProcessStatus(testHierarchy.processId);
        assertEquals(ProcessState.STARTED, latest.getState(), "Process state should be STARTED");

        // Verify experiment state (would be updated by OrchestratorService in real flow)
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
            processStatusService.addProcessStatus(status, testHierarchy.processId);
        }

        // Verify all states are in history
        var process = processService.getProcess(testHierarchy.processId);
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
