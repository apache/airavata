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
package org.apache.airavata.execution.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.airavata.core.model.ResourceIdentifier;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.execution.model.TaskModel;
import org.apache.airavata.execution.model.TaskTypes;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.apache.airavata.status.model.ProcessStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for {@link ExperimentStatusManager}.
 *
 * <p>All collaborators ({@link ProcessService}, {@link ExperimentRepository},
 * {@link OrchestratorService}) are mocked via Mockito — no Spring context is loaded.
 *
 * <p>The test suite is organised into four {@link Nested} groups mirroring the public
 * API surface of the class under test:
 * <ul>
 *   <li>{@link HandleProcessStatusChangeTests} — the main event-routing method</li>
 *   <li>{@link UpdateExperimentStatusTests}    — direct state-transition + persistence</li>
 *   <li>{@link FailExperimentTests}            — error-path helper</li>
 *   <li>{@link GetExperimentStatusTests}       — read-only state query</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class ExperimentStatusManagerTest {

    // -------------------------------------------------------------------------
    // Shared fixture constants
    // -------------------------------------------------------------------------

    private static final String PROCESS_ID    = "proc-unit-001";
    private static final String EXPERIMENT_ID = "exp-unit-001";
    private static final String GATEWAY_ID    = "gw-unit-001";

    // -------------------------------------------------------------------------
    // Mocks
    // -------------------------------------------------------------------------

    @Mock
    private ProcessService processService;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private OrchestratorService orchestratorService;

    // -------------------------------------------------------------------------
    // System under test
    // -------------------------------------------------------------------------

    private ExperimentStatusManager manager;

    @BeforeEach
    void setUp() {
        // Construct directly — no Spring wiring needed. The @Lazy on orchestratorService
        // is a Spring hint only; the constructor itself has no special behaviour.
        manager = new ExperimentStatusManager(processService, experimentRepository, orchestratorService);
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link ProcessStatusChangedEvent} carrying the given process state, using the
     * shared fixture IDs.
     */
    private static ProcessStatusChangedEvent eventFor(ProcessState state) {
        ResourceIdentifier identity = ResourceIdentifier.forProcess(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        return new ProcessStatusChangedEvent(state, identity);
    }

    /**
     * Builds a {@link ResourceIdentifier} for the shared process fixture.
     */
    private static ResourceIdentifier processIdentity() {
        return ResourceIdentifier.forProcess(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
    }

    /**
     * Returns a {@link ProcessModel} with no tasks — represents a standard (non-output-fetching)
     * process.
     */
    private static ProcessModel standardProcess() {
        ProcessModel model = new ProcessModel();
        model.setProcessId(PROCESS_ID);
        model.setExperimentId(EXPERIMENT_ID);
        model.setTasks(Collections.emptyList());
        return model;
    }

    /**
     * Returns a {@link ProcessModel} whose task list contains an {@code OUTPUT_FETCHING} task,
     * representing an intermediate output-fetching process that must not drive experiment status.
     */
    private static ProcessModel outputFetchingProcess() {
        TaskModel ofTask = new TaskModel();
        ofTask.setTaskId("task-of-001");
        ofTask.setTaskType(TaskTypes.OUTPUT_FETCHING);
        ProcessModel model = new ProcessModel();
        model.setProcessId(PROCESS_ID);
        model.setExperimentId(EXPERIMENT_ID);
        model.setTasks(List.of(ofTask));
        return model;
    }

    /**
     * Creates and stubs an {@link ExperimentEntity} whose stored state string is set to
     * {@code stateString}.  The entity is registered with the repository mock so that a
     * {@code findById(EXPERIMENT_ID)} call returns it.
     */
    private ExperimentEntity stubExperimentInState(String stateString) {
        ExperimentEntity entity = new ExperimentEntity();
        entity.setExperimentId(EXPERIMENT_ID);
        entity.setGatewayId(GATEWAY_ID);
        entity.setState(stateString);
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(entity));
        return entity;
    }

    // =========================================================================
    // handleProcessStatusChange
    // =========================================================================

    @Nested
    class HandleProcessStatusChangeTests {

        // ------------------------------------------------------------------
        // 1. LAUNCHED -> experiment EXECUTING
        // ------------------------------------------------------------------

        @Test
        void processLaunched_setsExperimentToExecuting() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.LAUNCHED.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.LAUNCHED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.EXECUTING.name(), entity.getState(),
                    "Experiment must transition to EXECUTING when process LAUNCHED");
        }

        // ------------------------------------------------------------------
        // 2. COMPLETED -> experiment COMPLETED (normal path, from EXECUTING)
        // ------------------------------------------------------------------

        @Test
        void processCompleted_setsExperimentToCompleted() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.EXECUTING.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.COMPLETED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.COMPLETED.name(), entity.getState(),
                    "Experiment must transition to COMPLETED when process COMPLETED from EXECUTING");
        }

        // ------------------------------------------------------------------
        // 3. COMPLETED while CANCELING -> experiment CANCELED
        // ------------------------------------------------------------------

        @Test
        void processCompleted_whileCanceling_setsExperimentToCanceled() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.CANCELING.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.COMPLETED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.CANCELED.name(), entity.getState(),
                    "CANCELING takes precedence: experiment must become CANCELED even when process COMPLETED");
        }

        // ------------------------------------------------------------------
        // 4. FAILED -> experiment FAILED
        // ------------------------------------------------------------------

        @Test
        void processFailed_setsExperimentToFailed() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.EXECUTING.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.FAILED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.FAILED.name(), entity.getState(),
                    "Experiment must transition to FAILED when process FAILED");
        }

        // ------------------------------------------------------------------
        // 5. FAILED while CANCELING -> experiment CANCELED
        // ------------------------------------------------------------------

        @Test
        void processFailed_whileCanceling_setsExperimentToCanceled() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.CANCELING.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.FAILED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.CANCELED.name(), entity.getState(),
                    "CANCELING takes precedence: experiment must become CANCELED even when process FAILED");
        }

        // ------------------------------------------------------------------
        // 6. CANCELED -> experiment CANCELED
        // ------------------------------------------------------------------

        @Test
        void processCanceled_setsExperimentToCanceled() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.CANCELING.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.CANCELED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.CANCELED.name(), entity.getState(),
                    "Experiment must transition to CANCELED when process CANCELED");
        }

        // ------------------------------------------------------------------
        // 7. QUEUED -> experiment SCHEDULED
        //
        // The valid transitions that end at SCHEDULED are:
        //   CREATED -> SCHEDULED, EXECUTING -> SCHEDULED, SCHEDULED -> SCHEDULED
        // LAUNCHED -> SCHEDULED is NOT in the validator, so the experiment must
        // start from EXECUTING for the transition to be accepted.
        // ------------------------------------------------------------------

        @Test
        void processQueued_setsExperimentToScheduled() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.EXECUTING.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.QUEUED), processIdentity());

            verify(experimentRepository, atLeastOnce()).save(entity);
            assertEquals(ExperimentState.SCHEDULED.name(), entity.getState(),
                    "Experiment must transition to SCHEDULED when process QUEUED");
        }

        // ------------------------------------------------------------------
        // 8. OUTPUT_FETCHING process must NOT update experiment status
        // ------------------------------------------------------------------

        @Test
        void intermediateOutputProcess_doesNotUpdateExperimentStatus() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(outputFetchingProcess());

            // Repository is NOT stubbed — if it were called the mock would return Optional.empty()
            // and the state logic would still run.  What we assert is that save() is never called.
            manager.handleProcessStatusChange(eventFor(ProcessState.COMPLETED), processIdentity());

            verify(experimentRepository, never()).save(any());
        }

        // ------------------------------------------------------------------
        // 9. COMPLETED from LAUNCHED -> EXECUTING inferred first, then COMPLETED
        //
        // The state machine path:
        //   handleProcessStatusChange sees currentExpState == LAUNCHED
        //   -> calls updateExperimentStatus(EXECUTING)   [save #1]
        //   -> calls updateExperimentStatus(COMPLETED)   [save #2]
        //
        // Because ExperimentEntity is a single mutable object shared across both
        // updateExperimentStatus calls, all ArgumentCaptor entries reference the
        // same object and show the FINAL state (COMPLETED) after both mutations.
        // We therefore assert:
        //   (a) save() was called exactly twice — proving both the intermediate
        //       EXECUTING write and the final COMPLETED write occurred, and
        //   (b) the final persisted state is COMPLETED.
        // ------------------------------------------------------------------

        @Test
        void processCompleted_fromLaunched_infersExecutingFirst() throws Exception {
            when(processService.getProcess(PROCESS_ID)).thenReturn(standardProcess());
            ExperimentEntity entity = stubExperimentInState(ExperimentState.LAUNCHED.name());

            manager.handleProcessStatusChange(eventFor(ProcessState.COMPLETED), processIdentity());

            // Exactly 2 saves: one for the inferred EXECUTING step, one for COMPLETED.
            verify(experimentRepository, times(2)).save(entity);

            // After both mutations the entity must reflect the final COMPLETED state.
            assertEquals(ExperimentState.COMPLETED.name(), entity.getState(),
                    "Final persisted state must be COMPLETED after the inferred EXECUTING step");
        }
    }

    // =========================================================================
    // updateExperimentStatus
    // =========================================================================

    @Nested
    class UpdateExperimentStatusTests {

        // ------------------------------------------------------------------
        // 10. Valid transition -> entity saved with new state
        // ------------------------------------------------------------------

        @Test
        void validTransition_updatesEntity() {
            ExperimentEntity entity = stubExperimentInState(ExperimentState.LAUNCHED.name());

            StatusModel<ExperimentState> status = StatusModel.of(ExperimentState.EXECUTING, "test");
            manager.updateExperimentStatus(EXPERIMENT_ID, status, GATEWAY_ID);

            verify(experimentRepository, times(1)).save(entity);
            assertEquals(ExperimentState.EXECUTING.name(), entity.getState(),
                    "Entity state must reflect the requested transition target");
        }

        // ------------------------------------------------------------------
        // 11. Invalid transition -> entity NOT saved
        // ------------------------------------------------------------------

        @Test
        void invalidTransition_rejected() {
            // COMPLETED -> LAUNCHED is not in the valid-transitions table
            stubExperimentInState(ExperimentState.COMPLETED.name());

            StatusModel<ExperimentState> status = StatusModel.of(ExperimentState.LAUNCHED, "illegal");
            manager.updateExperimentStatus(EXPERIMENT_ID, status, GATEWAY_ID);

            verify(experimentRepository, never()).save(any());
        }

        // ------------------------------------------------------------------
        // 12. Experiment not found -> no crash, no save
        // ------------------------------------------------------------------

        @Test
        void experimentNotFound_logsError() {
            when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.empty());

            // The state transition from null -> EXECUTING is valid (initial state),
            // so validation passes but the entity is null — the code must log and not throw.
            StatusModel<ExperimentState> status = StatusModel.of(ExperimentState.EXECUTING, "no entity");
            manager.updateExperimentStatus(EXPERIMENT_ID, status, GATEWAY_ID);

            verify(experimentRepository, never()).save(any());
        }
    }

    // =========================================================================
    // failExperiment
    // =========================================================================

    @Nested
    class FailExperimentTests {

        // ------------------------------------------------------------------
        // 13. failExperiment sets FAILED state and returns OrchestratorException
        // ------------------------------------------------------------------

        @Test
        void failExperiment_setsFailedStatusAndReturnsException() {
            ExperimentEntity entity = stubExperimentInState(ExperimentState.LAUNCHED.name());
            Exception cause = new RuntimeException("disk full");

            OrchestratorException result =
                    manager.failExperiment(EXPERIMENT_ID, GATEWAY_ID, "disk error", cause);

            // Entity must be persisted as FAILED
            verify(experimentRepository, times(1)).save(entity);
            assertEquals(ExperimentState.FAILED.name(), entity.getState(),
                    "Entity must be saved with state FAILED");

            // Returned exception must wrap the cause and reference the experiment ID
            assertNotNull(result, "failExperiment must return a non-null OrchestratorException");
            assertInstanceOf(OrchestratorException.class, result);
            assertNotNull(result.getMessage());
            org.junit.jupiter.api.Assertions.assertTrue(
                    result.getMessage().contains(EXPERIMENT_ID),
                    "OrchestratorException message must contain the experiment ID");
            assertEquals(cause, result.getCause(),
                    "OrchestratorException must wrap the original cause");
        }
    }

    // =========================================================================
    // getExperimentStatus
    // =========================================================================

    @Nested
    class GetExperimentStatusTests {

        // ------------------------------------------------------------------
        // 14. Returns current state from entity
        // ------------------------------------------------------------------

        @Test
        void getExperimentStatus_returnsCurrentState() throws Exception {
            stubExperimentInState(ExperimentState.EXECUTING.name());

            StatusModel<ExperimentState> result = manager.getExperimentStatus(EXPERIMENT_ID);

            assertNotNull(result, "Status model must not be null when entity exists");
            assertEquals(ExperimentState.EXECUTING, result.getState(),
                    "Returned state must match the stored entity state");
        }

        // ------------------------------------------------------------------
        // 15. Returns null when entity not found
        // ------------------------------------------------------------------

        @Test
        void getExperimentStatus_returnsNull_whenNotFound() throws Exception {
            when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.empty());

            StatusModel<ExperimentState> result = manager.getExperimentStatus(EXPERIMENT_ID);

            assertNull(result, "getExperimentStatus must return null when the experiment entity does not exist");
        }

        // ------------------------------------------------------------------
        // Additional: Returns null when stored state string is unrecognised
        // ------------------------------------------------------------------

        @Test
        void getExperimentStatus_returnsNull_whenStateIsUnknown() throws Exception {
            stubExperimentInState("BOGUS_STATE");

            StatusModel<ExperimentState> result = manager.getExperimentStatus(EXPERIMENT_ID);

            assertNull(result, "getExperimentStatus must return null for an unrecognised state string");
        }

        // ------------------------------------------------------------------
        // Additional: Returns null when stored state is null
        // ------------------------------------------------------------------

        @Test
        void getExperimentStatus_returnsNull_whenEntityStateIsNull() throws Exception {
            stubExperimentInState(null);
            // Override the stub: the entity's state is null
            ExperimentEntity nullStateEntity = new ExperimentEntity();
            nullStateEntity.setExperimentId(EXPERIMENT_ID);
            nullStateEntity.setState(null);
            when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(nullStateEntity));

            StatusModel<ExperimentState> result = manager.getExperimentStatus(EXPERIMENT_ID);

            assertNull(result, "getExperimentStatus must return null when the entity's state field is null");
        }
    }
}
