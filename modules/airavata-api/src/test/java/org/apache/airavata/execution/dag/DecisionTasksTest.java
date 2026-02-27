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
package org.apache.airavata.execution.dag;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.orchestration.ExperimentStatusManager;
import org.apache.airavata.execution.process.ProcessModel;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.status.service.StatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the DAG decision tasks:
 * {@link CheckOutputsTask}, {@link CheckDataMovementTask},
 * and {@link MarkFailedTask}.
 *
 * <p>Each task is tested in its own {@link Nested} class. All tests are pure
 * unit tests using JUnit 5 and Mockito — no Spring context is required.
 */
@ExtendWith(MockitoExtension.class)
public class DecisionTasksTest {

    // -------------------------------------------------------------------------
    // Shared test fixture constants
    // -------------------------------------------------------------------------

    private static final String PROCESS_ID = "proc-test-001";
    private static final String EXPERIMENT_ID = "exp-test-001";
    private static final String GATEWAY_ID = "gw-test-001";
    private static final String TASK_ID = "task-test-001";

    // -------------------------------------------------------------------------
    // Shared helper: build a minimal TaskContext backed by a given ProcessModel
    // -------------------------------------------------------------------------

    /**
     * Constructs a {@link TaskContext} whose {@code getProcessId()} returns
     * {@link #PROCESS_ID} and whose {@code getExperimentId()} delegates to
     * {@code processModel.getExperimentId()}.
     */
    private static TaskContext contextFor(ProcessModel processModel) {
        return new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, processModel);
    }

    /**
     * Creates a {@link ProcessModel} pre-wired with the shared experiment ID.
     */
    private static ProcessModel newProcessModel() {
        ProcessModel model = new ProcessModel();
        model.setProcessId(PROCESS_ID);
        model.setExperimentId(EXPERIMENT_ID);
        return model;
    }

    // =========================================================================
    // CheckOutputsTask
    // =========================================================================

    @Nested
    class CheckOutputsTaskTests {

        private CheckOutputsTask task;

        @BeforeEach
        void setUp() {
            task = new CheckOutputsTask();
        }

        @Test
        void execute_returnsSuccess_whenProcessHasOutputs() {
            ProcessModel model = newProcessModel();
            ApplicationOutput out1 = new ApplicationOutput();
            out1.setName("stdout");
            ApplicationOutput out2 = new ApplicationOutput();
            out2.setName("result.txt");

            TaskContext context = contextFor(model);
            context.setProcessOutputs(List.of(out1, out2));

            DagTaskResult result = task.execute(context);

            assertInstanceOf(DagTaskResult.Success.class, result, "Expected Success when process has outputs");
            DagTaskResult.Success success = (DagTaskResult.Success) result;
            assertTrue(success.message().contains("2"), "Success message should contain the output count");
        }

        @Test
        void execute_returnsFailure_whenProcessOutputsIsNull() {
            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);
            context.setProcessOutputs(null);

            DagTaskResult result = task.execute(context);

            assertInstanceOf(DagTaskResult.Failure.class, result, "Expected Failure when processOutputs is null");
            DagTaskResult.Failure failure = (DagTaskResult.Failure) result;
            assertTrue(
                    failure.reason().contains("No outputs defined"),
                    "Failure reason must indicate no outputs are defined");
        }

        @Test
        void execute_returnsFailure_whenProcessOutputsIsEmpty() {
            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);
            context.setProcessOutputs(Collections.emptyList());

            DagTaskResult result = task.execute(context);

            assertInstanceOf(DagTaskResult.Failure.class, result, "Expected Failure when processOutputs is empty");
            DagTaskResult.Failure failure = (DagTaskResult.Failure) result;
            assertTrue(
                    failure.reason().contains("No outputs defined"),
                    "Failure reason must indicate no outputs are defined");
        }
    }

    // =========================================================================
    // CheckDataMovementTask
    // =========================================================================

    @Nested
    class CheckDataMovementTaskTests {

        private CheckDataMovementTask task;

        @BeforeEach
        void setUp() {
            task = new CheckDataMovementTask();
        }

        @Test
        void execute_returnsSuccess_whenAtLeastOneOutputHasDataMovement() {
            ProcessModel model = newProcessModel();
            ApplicationOutput noMovement = new ApplicationOutput();
            noMovement.setName("stdout");
            noMovement.setDataMovement(false);
            ApplicationOutput withMovement = new ApplicationOutput();
            withMovement.setName("result.txt");
            withMovement.setDataMovement(true);

            TaskContext context = contextFor(model);
            context.setProcessOutputs(List.of(noMovement, withMovement));

            DagTaskResult result = task.execute(context);

            assertInstanceOf(
                    DagTaskResult.Success.class,
                    result,
                    "Expected Success when at least one output has dataMovement=true");
            DagTaskResult.Success success = (DagTaskResult.Success) result;
            assertTrue(
                    success.message().contains("Data movement outputs found"),
                    "Success message must confirm data movement outputs were found");
        }

        @Test
        void execute_returnsFailure_whenAllOutputsHaveDataMovementFalse() {
            ProcessModel model = newProcessModel();
            ApplicationOutput out1 = new ApplicationOutput();
            out1.setName("stdout");
            out1.setDataMovement(false);
            ApplicationOutput out2 = new ApplicationOutput();
            out2.setName("stderr");
            out2.setDataMovement(false);

            TaskContext context = contextFor(model);
            context.setProcessOutputs(List.of(out1, out2));

            DagTaskResult result = task.execute(context);

            assertInstanceOf(
                    DagTaskResult.Failure.class, result, "Expected Failure when no outputs have dataMovement=true");
            DagTaskResult.Failure failure = (DagTaskResult.Failure) result;
            assertTrue(
                    failure.reason().contains("No data movement outputs"),
                    "Failure reason must indicate no data movement outputs exist");
        }

        @Test
        void execute_returnsFailure_whenProcessOutputsIsNull() {
            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);
            context.setProcessOutputs(null);

            DagTaskResult result = task.execute(context);

            assertInstanceOf(DagTaskResult.Failure.class, result, "Expected Failure when processOutputs is null");
            DagTaskResult.Failure failure = (DagTaskResult.Failure) result;
            assertTrue(
                    failure.reason().contains("No outputs defined"),
                    "Failure reason must indicate that no outputs are defined");
        }
    }

    // =========================================================================
    // MarkFailedTask
    // =========================================================================

    @Nested
    class MarkFailedTaskTests {

        @Mock
        private StatusService statusService;

        @Mock
        private ExperimentStatusManager experimentStatusManager;

        private MarkFailedTask task;

        @BeforeEach
        void setUp() {
            task = new MarkFailedTask(statusService, experimentStatusManager);
        }

        @Test
        void execute_publishesFailedStatusForBothProcessAndExperiment_andReturnsSuccess() throws Exception {
            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);

            DagTaskResult result = task.execute(context);

            assertInstanceOf(
                    DagTaskResult.Success.class,
                    result,
                    "MarkFailedTask must always return Success regardless of what it marks");
            DagTaskResult.Success success = (DagTaskResult.Success) result;
            assertTrue(
                    success.message().contains("Marked as FAILED"),
                    "Success message must confirm the FAILED mark was applied");

            verify(statusService, times(1)).addProcessStatus(any(), eq(PROCESS_ID));
            verify(experimentStatusManager, times(1)).updateExperimentStatus(eq(EXPERIMENT_ID), any(), eq(GATEWAY_ID));
        }

        @Test
        void execute_returnsSuccess_evenWhenStatusServiceThrows() throws Exception {
            doThrow(new RegistryException("Status DB unavailable"))
                    .when(statusService)
                    .addProcessStatus(any(), any());

            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);

            DagTaskResult result = task.execute(context);

            assertInstanceOf(
                    DagTaskResult.Success.class,
                    result,
                    "MarkFailedTask must swallow StatusService exceptions and still return Success");
            DagTaskResult.Success success = (DagTaskResult.Success) result;
            assertTrue(
                    success.message().contains("Marked as FAILED"),
                    "Success message must be returned even after a StatusService failure");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_setsCorrectStateAndReasonOnProcessStatus() throws Exception {
            ArgumentCaptor<StatusModel<ProcessState>> processStatusCaptor = ArgumentCaptor.forClass(StatusModel.class);

            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);

            task.execute(context);

            verify(statusService).addProcessStatus(processStatusCaptor.capture(), eq(PROCESS_ID));
            StatusModel<ProcessState> captured = processStatusCaptor.getValue();

            org.junit.jupiter.api.Assertions.assertEquals(
                    ProcessState.FAILED, captured.getState(), "Process status state must be FAILED");
            org.junit.jupiter.api.Assertions.assertEquals(
                    "DAG execution failed",
                    captured.getReason(),
                    "Process status reason must be 'DAG execution failed'");
            assertTrue(
                    captured.getTimeOfStateChange() > 0,
                    "Process status timeOfStateChange must be set to a positive timestamp");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_setsCorrectStateAndReasonOnExperimentStatus() throws Exception {
            ArgumentCaptor<StatusModel<ExperimentState>> experimentStatusCaptor =
                    ArgumentCaptor.forClass(StatusModel.class);

            ProcessModel model = newProcessModel();
            TaskContext context = contextFor(model);

            task.execute(context);

            verify(experimentStatusManager)
                    .updateExperimentStatus(eq(EXPERIMENT_ID), experimentStatusCaptor.capture(), eq(GATEWAY_ID));
            StatusModel<ExperimentState> captured = experimentStatusCaptor.getValue();

            org.junit.jupiter.api.Assertions.assertEquals(
                    ExperimentState.FAILED, captured.getState(), "Experiment status state must be FAILED");
            org.junit.jupiter.api.Assertions.assertEquals(
                    "Process execution failed",
                    captured.getReason(),
                    "Experiment status reason must be 'Process execution failed'");
            assertTrue(
                    captured.getTimeOfStateChange() > 0,
                    "Experiment status timeOfStateChange must be set to a positive timestamp");
        }
    }
}
