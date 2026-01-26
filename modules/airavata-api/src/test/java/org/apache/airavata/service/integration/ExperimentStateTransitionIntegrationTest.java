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
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.orchestrator.state.ExperimentStateValidator;
import org.apache.airavata.orchestrator.state.StateTransitionService;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.service.registry.RegistryService;
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
 * Comprehensive integration tests for ExperimentStateValidator.
 * Tests verify that all valid state transitions work correctly and invalid transitions are rejected.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            ExperimentStateTransitionIntegrationTest.TestConfiguration.class
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
public class ExperimentStateTransitionIntegrationTest extends ServiceIntegrationTestBase {

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
    private final RegistryService registryService;
    private final StatusService statusService;

    private String testExperimentId;
    private String testProjectId;

    public ExperimentStateTransitionIntegrationTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            RegistryService registryService,
            StatusService statusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.registryService = registryService;
        this.statusService = statusService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        super.setUpBase();

        Gateway gateway = TestDataFactory.createTestGateway(TEST_GATEWAY_ID);
        if (!gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            gatewayService.addGateway(gateway);
        }

        Project project = TestDataFactory.createTestProject("State Transition Test Project", TEST_GATEWAY_ID);
        testProjectId = projectService.addProject(project, TEST_GATEWAY_ID);

        ExperimentModel experiment =
                TestDataFactory.createTestExperiment("State Transition Experiment", testProjectId, TEST_GATEWAY_ID);
        experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        testExperimentId = experimentService.addExperiment(experiment);
    }

    @Test
    @DisplayName("Test all valid transitions from CREATED state")
    public void testValidTransitionsFromCreated() throws RegistryException {
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

        // Test actual transitions through service
        ExperimentStatus scheduled = createExperimentStatus(ExperimentState.SCHEDULED, "Scheduled");
        registryService.updateExperimentStatus(scheduled, testExperimentId);
        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.SCHEDULED, status.getState(), "Experiment should be in SCHEDULED state");
    }

    @Test
    @DisplayName("Test all valid transitions from SCHEDULED state")
    public void testValidTransitionsFromScheduled() throws RegistryException {
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

        // Test transition: CREATED -> SCHEDULED -> LAUNCHED
        ExperimentStatus scheduled = createExperimentStatus(ExperimentState.SCHEDULED, "Scheduled");
        registryService.updateExperimentStatus(scheduled, testExperimentId);

        ExperimentStatus launched = createExperimentStatus(ExperimentState.LAUNCHED, "Launched");
        registryService.updateExperimentStatus(launched, testExperimentId);
        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.LAUNCHED, status.getState(), "Experiment should be in LAUNCHED state");
    }

    @Test
    @DisplayName("Test all valid transitions from LAUNCHED state")
    public void testValidTransitionsFromLaunched() throws RegistryException {
        // LAUNCHED can transition to EXECUTING or CANCELING
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
                "LAUNCHED -> EXECUTING should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.LAUNCHED, ExperimentState.CANCELING),
                "LAUNCHED -> CANCELING should be valid");

        // Test transition: CREATED -> LAUNCHED -> EXECUTING
        ExperimentStatus launched = createExperimentStatus(ExperimentState.LAUNCHED, "Launched");
        registryService.updateExperimentStatus(launched, testExperimentId);

        ExperimentStatus executing = createExperimentStatus(ExperimentState.EXECUTING, "Executing");
        registryService.updateExperimentStatus(executing, testExperimentId);
        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.EXECUTING, status.getState(), "Experiment should be in EXECUTING state");
    }

    @Test
    @DisplayName("Test all valid transitions from EXECUTING state")
    public void testValidTransitionsFromExecuting() throws RegistryException {
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

        // Test successful completion path
        ExperimentStatus launched = createExperimentStatus(ExperimentState.LAUNCHED, "Launched");
        registryService.updateExperimentStatus(launched, testExperimentId);

        ExperimentStatus executing = createExperimentStatus(ExperimentState.EXECUTING, "Executing");
        registryService.updateExperimentStatus(executing, testExperimentId);

        ExperimentStatus completed = createExperimentStatus(ExperimentState.COMPLETED, "Completed");
        registryService.updateExperimentStatus(completed, testExperimentId);
        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.COMPLETED, status.getState(), "Experiment should be in COMPLETED state");
    }

    @Test
    @DisplayName("Test cancellation flow: CANCELING -> CANCELED")
    public void testCancellationFlow() throws RegistryException {
        // CANCELING can transition to CANCELING (self-loop) or CANCELED
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELING, ExperimentState.CANCELING),
                "CANCELING -> CANCELING (self-loop) should be valid");
        assertTrue(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELING, ExperimentState.CANCELED),
                "CANCELING -> CANCELED should be valid");

        // Test cancellation from SCHEDULED
        ExperimentStatus scheduled = createExperimentStatus(ExperimentState.SCHEDULED, "Scheduled");
        registryService.updateExperimentStatus(scheduled, testExperimentId);

        ExperimentStatus canceling = createExperimentStatus(ExperimentState.CANCELING, "Canceling");
        registryService.updateExperimentStatus(canceling, testExperimentId);

        ExperimentStatus canceled = createExperimentStatus(ExperimentState.CANCELED, "Canceled");
        registryService.updateExperimentStatus(canceled, testExperimentId);
        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.CANCELED, status.getState(), "Experiment should be in CANCELED state");
    }

    @Test
    @DisplayName("Test invalid transitions from terminal states")
    public void testInvalidTransitionsFromTerminalStates() {
        // Terminal states: COMPLETED, FAILED, CANCELED (no transitions out)
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.COMPLETED, ExperimentState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.COMPLETED, ExperimentState.SCHEDULED),
                "COMPLETED -> SCHEDULED should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.FAILED, ExperimentState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.FAILED, ExperimentState.SCHEDULED),
                "FAILED -> SCHEDULED should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELED, ExperimentState.EXECUTING),
                "CANCELED -> EXECUTING should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CANCELED, ExperimentState.SCHEDULED),
                "CANCELED -> SCHEDULED should be invalid");
    }

    @Test
    @DisplayName("Test invalid transitions (skipping required states)")
    public void testInvalidTransitionsSkippingStates() {
        // Invalid jumps
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.EXECUTING),
                "CREATED -> EXECUTING (skipping SCHEDULED/LAUNCHED) should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, ExperimentState.COMPLETED),
                "CREATED -> COMPLETED should be invalid");
        assertFalse(
                ExperimentStateValidator.INSTANCE.isValid(ExperimentState.SCHEDULED, ExperimentState.COMPLETED),
                "SCHEDULED -> COMPLETED (skipping LAUNCHED/EXECUTING) should be invalid");
    }

    @Test
    @DisplayName("Test StateTransitionService validates transitions correctly")
    public void testStateTransitionServiceValidation() {
        // Test that StateTransitionService.validateAndLog() correctly validates transitions
        assertTrue(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.CREATED, ExperimentState.SCHEDULED),
                "StateTransitionService should validate CREATED -> SCHEDULED");
        assertFalse(
                StateTransitionService.isValid(
                        ExperimentStateValidator.INSTANCE, ExperimentState.COMPLETED, ExperimentState.EXECUTING),
                "StateTransitionService should reject COMPLETED -> EXECUTING");
    }

    @Test
    @DisplayName("Test state history preservation")
    public void testStateHistoryPreservation() throws RegistryException {
        List<ExperimentState> expectedStates = new ArrayList<>();
        expectedStates.add(ExperimentState.CREATED);
        expectedStates.add(ExperimentState.SCHEDULED);
        expectedStates.add(ExperimentState.LAUNCHED);
        expectedStates.add(ExperimentState.EXECUTING);
        expectedStates.add(ExperimentState.COMPLETED);

        // Add statuses in sequence
        for (ExperimentState state : expectedStates) {
            ExperimentStatus status = createExperimentStatus(state, "State: " + state.name());
            registryService.updateExperimentStatus(status, testExperimentId);
        }

        // Verify all states are in history using StatusService (not Experiment entity which may be stale)
        List<ExperimentStatus> statuses = statusService.getExperimentStatuses(testExperimentId);
        assertNotNull(statuses, "Experiment should have status history");
        assertTrue(
                statuses.size() >= expectedStates.size(),
                "Experiment should have at least " + expectedStates.size() + " status entries");

        // Verify all expected states are present
        for (ExperimentState expectedState : expectedStates) {
            boolean found = statuses.stream().anyMatch(s -> s.getState() == expectedState);
            assertTrue(found, "Experiment state history should contain " + expectedState);
        }

        // Verify latest state
        ExperimentStatus latest = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.COMPLETED, latest.getState(), "Latest state should be COMPLETED");
    }

    @Test
    @DisplayName("Test requeue flow: EXECUTING -> SCHEDULED -> LAUNCHED -> EXECUTING")
    public void testRequeueFlow() throws RegistryException {
        // Test requeue scenario: EXECUTING -> SCHEDULED -> LAUNCHED -> EXECUTING
        ExperimentStatus launched = createExperimentStatus(ExperimentState.LAUNCHED, "Launched");
        registryService.updateExperimentStatus(launched, testExperimentId);

        ExperimentStatus executing1 = createExperimentStatus(ExperimentState.EXECUTING, "Executing");
        registryService.updateExperimentStatus(executing1, testExperimentId);

        // Requeue
        ExperimentStatus scheduled = createExperimentStatus(ExperimentState.SCHEDULED, "Requeued");
        registryService.updateExperimentStatus(scheduled, testExperimentId);

        ExperimentStatus launched2 = createExperimentStatus(ExperimentState.LAUNCHED, "Re-launched");
        registryService.updateExperimentStatus(launched2, testExperimentId);

        ExperimentStatus executing2 = createExperimentStatus(ExperimentState.EXECUTING, "Re-executing");
        registryService.updateExperimentStatus(executing2, testExperimentId);

        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(
                ExperimentState.EXECUTING, status.getState(), "Experiment should be in EXECUTING state after requeue");

        // Verify all states are in history using StatusService (not Experiment entity which may be stale)
        List<ExperimentStatus> statuses = statusService.getExperimentStatuses(testExperimentId);
        assertTrue(
                statuses.stream().anyMatch(s -> s.getState() == ExperimentState.SCHEDULED),
                "History should contain SCHEDULED state from requeue");
    }

    @Test
    @DisplayName("Test failure path: CREATED -> FAILED")
    public void testFailurePath() throws RegistryException {
        // Test immediate failure
        ExperimentStatus failed = createExperimentStatus(ExperimentState.FAILED, "Failed immediately");
        registryService.updateExperimentStatus(failed, testExperimentId);

        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.FAILED, status.getState(), "Experiment should be in FAILED state");
    }

    @Test
    @DisplayName("Test failure during execution: EXECUTING -> FAILED")
    public void testFailureDuringExecution() throws RegistryException {
        ExperimentStatus launched = createExperimentStatus(ExperimentState.LAUNCHED, "Launched");
        registryService.updateExperimentStatus(launched, testExperimentId);

        ExperimentStatus executing = createExperimentStatus(ExperimentState.EXECUTING, "Executing");
        registryService.updateExperimentStatus(executing, testExperimentId);

        ExperimentStatus failed = createExperimentStatus(ExperimentState.FAILED, "Execution failed");
        registryService.updateExperimentStatus(failed, testExperimentId);

        ExperimentStatus status = registryService.getExperimentStatus(testExperimentId);
        assertEquals(ExperimentState.FAILED, status.getState(), "Experiment should be in FAILED state");
    }

    @Test
    @DisplayName("Test null handling in StateTransitionService")
    public void testNullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                StateTransitionService.isValid(ExperimentStateValidator.INSTANCE, null, ExperimentState.CREATED),
                "null -> CREATED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                StateTransitionService.isValid(ExperimentStateValidator.INSTANCE, ExperimentState.CREATED, null),
                "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(
                StateTransitionService.isValid(ExperimentStateValidator.INSTANCE, null, null),
                "null -> null should be invalid");
    }

    /**
     * Helper method to create an ExperimentStatus with the specified state.
     */
    private ExperimentStatus createExperimentStatus(ExperimentState state, String reason) {
        ExperimentStatus status = new ExperimentStatus();
        status.setState(state);
        status.setReason(reason);
        status.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp().getTime());
        return status;
    }
}
