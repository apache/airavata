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
package org.apache.airavata.workflow.dapr;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dapr.durabletask.Task;
import io.dapr.workflows.WorkflowContext;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessSubmitEvent;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.workflow.process.pre.ProcessPreWorkflow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for ProcessPreWorkflow.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProcessPreWorkflow Unit Tests")
class ProcessPreWorkflowTest {

    @Mock
    private WorkflowContext workflowContext;

    @Mock
    private RegistryService registryService;

    private ProcessPreWorkflow workflow;
    private MockedStatic<WorkflowRuntimeHolder> runtimeHolderMock;

    @BeforeEach
    void setUp() {
        workflow = new ProcessPreWorkflow();
        runtimeHolderMock = mockStatic(WorkflowRuntimeHolder.class);
        runtimeHolderMock
                .when(() -> WorkflowRuntimeHolder.getBean(RegistryService.class))
                .thenReturn(registryService);
    }

    @AfterEach
    void tearDown() {
        if (runtimeHolderMock != null) {
            runtimeHolderMock.close();
        }
    }

    @Test
    @DisplayName("Should execute all activities in sequence")
    void shouldExecuteAllActivitiesInSequence() throws Exception {
        ProcessSubmitEvent input = createTestProcessSubmitEvent();
        when(workflowContext.getInstanceId()).thenReturn("test-workflow-id");
        when(workflowContext.getInput(ProcessSubmitEvent.class)).thenReturn(input);

        // Mock activity calls - Dapr's Task type is difficult to mock, so we mock the await() chain
        mockActivityCallWithResult(
                workflowContext, "org.apache.airavata.activities.process.pre.EnvSetupActivity", "Env setup completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.InputDataStagingActivity",
                "Input staging completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.JobSubmissionActivity",
                "Job submission completed");

        // Mock process model with no OUTPUT_FETCHING tasks
        ProcessModel processModel = createProcessModelWithoutIntermediateTransfers();
        when(registryService.getProcess(input.getProcessId())).thenReturn(processModel);

        // Execute workflow using create() pattern
        workflow.create().run(workflowContext);

        verify(workflowContext)
                .callActivity(
                        eq("org.apache.airavata.activities.process.pre.EnvSetupActivity"),
                        any(BaseActivityInput.class),
                        eq(String.class));
        verify(workflowContext)
                .callActivity(
                        eq("org.apache.airavata.activities.process.pre.InputDataStagingActivity"),
                        any(BaseActivityInput.class),
                        eq(String.class));
        verify(workflowContext)
                .callActivity(
                        eq("org.apache.airavata.activities.process.pre.JobSubmissionActivity"),
                        any(BaseActivityInput.class),
                        eq(String.class));
        verify(workflowContext, never())
                .callActivity(
                        eq("org.apache.airavata.activities.shared.CompletingActivity"),
                        any(BaseActivityInput.class),
                        eq(String.class));
    }

    @Test
    @DisplayName("Should execute completing activity when intermediate transfers exist")
    void shouldExecuteCompletingActivityWhenIntermediateTransfersExist() throws Exception {
        ProcessSubmitEvent input = createTestProcessSubmitEvent();
        when(workflowContext.getInstanceId()).thenReturn("test-workflow-id");
        when(workflowContext.getInput(ProcessSubmitEvent.class)).thenReturn(input);

        // Mock activity calls
        mockActivityCallWithResult(
                workflowContext, "org.apache.airavata.activities.process.pre.EnvSetupActivity", "Env setup completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.InputDataStagingActivity",
                "Input staging completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.JobSubmissionActivity",
                "Job submission completed");
        mockActivityCallWithResult(
                workflowContext, "org.apache.airavata.activities.shared.CompletingActivity", "Completing completed");

        // Mock process model with OUTPUT_FETCHING task
        ProcessModel processModel = createProcessModelWithIntermediateTransfers();
        when(registryService.getProcess(input.getProcessId())).thenReturn(processModel);

        // Execute workflow using create() pattern
        workflow.create().run(workflowContext);

        verify(workflowContext)
                .callActivity(
                        eq("org.apache.airavata.activities.shared.CompletingActivity"),
                        any(BaseActivityInput.class),
                        eq(String.class));
    }

    @Test
    @DisplayName("Should skip completing activity when no intermediate transfers")
    void shouldSkipCompletingActivityWhenNoIntermediateTransfers() throws Exception {
        ProcessSubmitEvent input = createTestProcessSubmitEvent();
        when(workflowContext.getInstanceId()).thenReturn("test-workflow-id");
        when(workflowContext.getInput(ProcessSubmitEvent.class)).thenReturn(input);

        mockActivityCallWithResult(
                workflowContext, "org.apache.airavata.activities.process.pre.EnvSetupActivity", "Env setup completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.InputDataStagingActivity",
                "Input staging completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.JobSubmissionActivity",
                "Job submission completed");

        ProcessModel processModel = createProcessModelWithoutIntermediateTransfers();
        when(registryService.getProcess(input.getProcessId())).thenReturn(processModel);

        // Execute workflow using create() pattern
        workflow.create().run(workflowContext);

        verify(workflowContext, never())
                .callActivity(eq("org.apache.airavata.activities.shared.CompletingActivity"), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should handle RegistryException when checking for intermediate transfers")
    void shouldHandleRegistryExceptionWhenCheckingForIntermediateTransfers() throws Exception {
        ProcessSubmitEvent input = createTestProcessSubmitEvent();
        when(workflowContext.getInstanceId()).thenReturn("test-workflow-id");
        when(workflowContext.getInput(ProcessSubmitEvent.class)).thenReturn(input);

        mockActivityCallWithResult(
                workflowContext, "org.apache.airavata.activities.process.pre.EnvSetupActivity", "Env setup completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.InputDataStagingActivity",
                "Input staging completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.JobSubmissionActivity",
                "Job submission completed");

        // Simulate RegistryException when getting process
        when(registryService.getProcess(input.getProcessId())).thenThrow(new RegistryException("Process not found"));

        // Should continue without completing activity
        workflow.create().run(workflowContext);

        verify(workflowContext, never())
                .callActivity(eq("org.apache.airavata.activities.shared.CompletingActivity"), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should handle activity failure and throw RuntimeException")
    void shouldHandleActivityFailure() throws Exception {
        ProcessSubmitEvent input = createTestProcessSubmitEvent();
        when(workflowContext.getInput(ProcessSubmitEvent.class)).thenReturn(input);

        // Mock first activity to fail - mock callActivity to throw
        when(workflowContext.callActivity(
                        eq("org.apache.airavata.activities.process.pre.EnvSetupActivity"),
                        any(BaseActivityInput.class),
                        eq(String.class)))
                .thenThrow(new RuntimeException("Activity failed"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> workflow.create().run(workflowContext))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ProcessPreWorkflow failed");
    }

    @Test
    @DisplayName("Should complete workflow with instance ID")
    void shouldCompleteWorkflowWithInstanceId() throws Exception {
        ProcessSubmitEvent input = createTestProcessSubmitEvent();
        String expectedWorkflowId = "workflow-123";
        when(workflowContext.getInstanceId()).thenReturn(expectedWorkflowId);
        when(workflowContext.getInput(ProcessSubmitEvent.class)).thenReturn(input);

        mockActivityCallWithResult(
                workflowContext, "org.apache.airavata.activities.process.pre.EnvSetupActivity", "Env setup completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.InputDataStagingActivity",
                "Input staging completed");
        mockActivityCallWithResult(
                workflowContext,
                "org.apache.airavata.activities.process.pre.JobSubmissionActivity",
                "Job submission completed");

        ProcessModel processModel = createProcessModelWithoutIntermediateTransfers();
        when(registryService.getProcess(input.getProcessId())).thenReturn(processModel);

        // Execute workflow - it should call ctx.complete() with the workflow ID
        workflow.create().run(workflowContext);

        // Verify complete was called with the workflow ID
        verify(workflowContext).complete(any(String.class));
    }

    // Helper methods

    private ProcessSubmitEvent createTestProcessSubmitEvent() {
        ProcessSubmitEvent event = new ProcessSubmitEvent();
        event.setProcessId("test-process-id");
        event.setExperimentId("test-experiment-id");
        event.setGatewayId("test-gateway-id");
        event.setTokenId("test-token-id");
        return event;
    }

    private ProcessModel createProcessModelWithoutIntermediateTransfers() {
        ProcessModel model = new ProcessModel();
        model.setProcessId("test-process-id");
        List<TaskModel> tasks = new ArrayList<>();
        TaskModel task1 = new TaskModel();
        task1.setTaskType(TaskTypes.ENV_SETUP);
        tasks.add(task1);
        model.setTasks(tasks);
        return model;
    }

    private ProcessModel createProcessModelWithIntermediateTransfers() {
        ProcessModel model = new ProcessModel();
        model.setProcessId("test-process-id");
        List<TaskModel> tasks = new ArrayList<>();
        TaskModel task1 = new TaskModel();
        task1.setTaskType(TaskTypes.ENV_SETUP);
        tasks.add(task1);
        TaskModel task2 = new TaskModel();
        task2.setTaskType(TaskTypes.OUTPUT_FETCHING);
        tasks.add(task2);
        model.setTasks(tasks);
        return model;
    }

    /**
     * Mock activity call - creates a mock that simulates Dapr's Task.await() behavior.
     */
    @SuppressWarnings("unchecked")
    private void mockActivityCallWithResult(WorkflowContext ctx, String activityName, String result) {
        // Mock the Task returned by callActivity
        Task<String> taskMock = mock(Task.class);
        when(taskMock.await()).thenReturn(result);
        when(ctx.callActivity(eq(activityName), any(), eq(String.class))).thenReturn(taskMock);
    }

    @SuppressWarnings("unchecked")
    private void verifyActivityCalled(WorkflowContext ctx, String activityName) {
        verify(ctx).callActivity(eq(activityName), any(), eq(String.class));
    }
}
