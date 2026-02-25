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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.entity.ExperimentOutputEntity;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for {@link SaveExperimentOutputsTask}.
 *
 * <p>No Spring application context or database is required. The
 * {@link ExperimentRepository} is mocked; {@link TaskContext} is constructed
 * with a real {@link ProcessModel} so that {@code getExperimentId()} and
 * {@code getDagState()} work naturally without additional stubbing.
 */
@ExtendWith(MockitoExtension.class)
public class SaveExperimentOutputsTaskTest {

    // -------------------------------------------------------------------------
    // Shared test fixtures
    // -------------------------------------------------------------------------

    private static final String EXPERIMENT_ID = "exp-test-001";
    private static final String PROCESS_ID    = "proc-test-001";
    private static final String GATEWAY_ID    = "gw-test-001";
    private static final String TASK_ID       = "task-test-001";

    @Mock
    private ExperimentRepository experimentRepository;

    private SaveExperimentOutputsTask task;

    @BeforeEach
    public void setUp() {
        task = new SaveExperimentOutputsTask(experimentRepository);
    }

    // -------------------------------------------------------------------------
    // 1. DAG state has 2 prefixed entries, experiment exists with empty outputs
    // -------------------------------------------------------------------------

    @Test
    public void execute_withOutputEntries_persistsToExperiment() {
        TaskContext context = buildContext();
        Map<String, String> dagState = context.getDagState();
        dagState.put("experimentOutput.stdout", "/data/out/stdout.txt");
        dagState.put("experimentOutput.result", "/data/out/result.csv");

        ExperimentEntity entity = buildExperimentEntity();
        entity.setOutputs(new ArrayList<>());
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(entity));

        DagTaskResult result = task.execute(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "Result must be a Success");

        ArgumentCaptor<ExperimentEntity> captor = ArgumentCaptor.forClass(ExperimentEntity.class);
        verify(experimentRepository).save(captor.capture());

        ExperimentEntity saved = captor.getValue();
        assertEquals(2, saved.getOutputs().size(),
                "Entity must have exactly 2 output entries after persistence");

        // Verify the output names and values are correct (order may vary, so check by name)
        boolean foundStdout = false;
        boolean foundResult = false;
        for (ExperimentOutputEntity output : saved.getOutputs()) {
            if ("stdout".equals(output.getName())) {
                assertEquals("/data/out/stdout.txt", output.getValue());
                assertEquals("STRING", output.getType());
                assertEquals(entity, output.getExperiment());
                foundStdout = true;
            } else if ("result".equals(output.getName())) {
                assertEquals("/data/out/result.csv", output.getValue());
                assertEquals("STRING", output.getType());
                assertEquals(entity, output.getExperiment());
                foundResult = true;
            }
        }
        assertTrue(foundStdout, "Output 'stdout' must be present");
        assertTrue(foundResult, "Output 'result' must be present");
    }

    // -------------------------------------------------------------------------
    // 2. DAG state has entries but none with the prefix
    // -------------------------------------------------------------------------

    @Test
    public void execute_withNoOutputEntries_returnsSuccessWithoutSaving() {
        TaskContext context = buildContext();
        Map<String, String> dagState = context.getDagState();
        dagState.put("jobId", "job-42");
        dagState.put("workingDir", "/scratch/proc-test-001");

        DagTaskResult result = task.execute(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "Result must be a Success even when no prefixed entries exist");
        DagTaskResult.Success success = (DagTaskResult.Success) result;
        assertTrue(success.message().contains("No experiment outputs"),
                "Message must indicate no outputs to persist; got: " + success.message());

        verify(experimentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // -------------------------------------------------------------------------
    // 3. DAG state has prefixed entries but experiment not found
    // -------------------------------------------------------------------------

    @Test
    public void execute_withMissingExperiment_returnsSuccessSkip() {
        TaskContext context = buildContext();
        Map<String, String> dagState = context.getDagState();
        dagState.put("experimentOutput.stdout", "/data/out/stdout.txt");

        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.empty());

        DagTaskResult result = task.execute(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "Result must be a Success even when experiment is not found");
        DagTaskResult.Success success = (DagTaskResult.Success) result;
        assertTrue(success.message().contains("skipped"),
                "Message must indicate that output persistence was skipped; got: " + success.message());

        verify(experimentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // -------------------------------------------------------------------------
    // 4. Experiment already has an output with matching name — update in place
    // -------------------------------------------------------------------------

    @Test
    public void execute_updatesExistingOutput() {
        TaskContext context = buildContext();
        Map<String, String> dagState = context.getDagState();
        dagState.put("experimentOutput.stdout", "/data/out/new-stdout.txt");

        ExperimentEntity entity = buildExperimentEntity();
        ExperimentOutputEntity existingOutput = new ExperimentOutputEntity();
        existingOutput.setOutputId("existing-output-id");
        existingOutput.setName("stdout");
        existingOutput.setValue("/data/out/old-stdout.txt");
        existingOutput.setType("STRING");
        existingOutput.setExperiment(entity);

        ArrayList<ExperimentOutputEntity> outputs = new ArrayList<>();
        outputs.add(existingOutput);
        entity.setOutputs(outputs);

        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(entity));

        DagTaskResult result = task.execute(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "Result must be a Success");

        ArgumentCaptor<ExperimentEntity> captor = ArgumentCaptor.forClass(ExperimentEntity.class);
        verify(experimentRepository).save(captor.capture());

        ExperimentEntity saved = captor.getValue();
        assertEquals(1, saved.getOutputs().size(),
                "Existing output must be updated, not duplicated — count should remain 1");
        assertEquals("/data/out/new-stdout.txt", saved.getOutputs().get(0).getValue(),
                "Existing output value must be updated to the new value");
        assertEquals("existing-output-id", saved.getOutputs().get(0).getOutputId(),
                "Existing output ID must be preserved (not regenerated)");
    }

    // -------------------------------------------------------------------------
    // 5. DAG state has both prefixed and non-prefixed entries
    // -------------------------------------------------------------------------

    @Test
    public void execute_withMixedDagState_onlyProcessesPrefixedEntries() {
        TaskContext context = buildContext();
        Map<String, String> dagState = context.getDagState();
        dagState.put("experimentOutput.stdout", "/data/out/stdout.txt");
        dagState.put("experimentOutput.result", "/data/out/result.csv");
        dagState.put("jobId", "job-42");
        dagState.put("workingDir", "/scratch/proc-test-001");
        dagState.put("exitCode", "0");

        ExperimentEntity entity = buildExperimentEntity();
        entity.setOutputs(new ArrayList<>());
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(entity));

        DagTaskResult result = task.execute(context);

        assertInstanceOf(DagTaskResult.Success.class, result,
                "Result must be a Success");
        DagTaskResult.Success success = (DagTaskResult.Success) result;
        assertTrue(success.message().contains("2"),
                "Message must indicate 2 outputs persisted; got: " + success.message());

        ArgumentCaptor<ExperimentEntity> captor = ArgumentCaptor.forClass(ExperimentEntity.class);
        verify(experimentRepository).save(captor.capture());

        ExperimentEntity saved = captor.getValue();
        assertEquals(2, saved.getOutputs().size(),
                "Only the 2 prefixed entries must be persisted, not the 3 non-prefixed ones");

        for (ExperimentOutputEntity output : saved.getOutputs()) {
            assertTrue("stdout".equals(output.getName()) || "result".equals(output.getName()),
                    "Output name must be 'stdout' or 'result', but got: " + output.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TaskContext buildContext() {
        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(EXPERIMENT_ID);
        processModel.setProcessId(PROCESS_ID);
        return new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, processModel);
    }

    private ExperimentEntity buildExperimentEntity() {
        ExperimentEntity entity = new ExperimentEntity();
        entity.setExperimentId(EXPERIMENT_ID);
        entity.setGatewayId(GATEWAY_ID);
        entity.setExperimentName("Test Experiment");
        entity.setApplicationId("app-test-001");
        entity.setBindingId("binding-test-001");
        entity.setUserName("testuser");
        return entity;
    }
}
