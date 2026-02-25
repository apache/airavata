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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.task.TaskContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * Unit tests for {@link ProcessDAGEngine}.
 *
 * <p>These tests exercise the engine's DAG traversal logic in isolation.
 * {@link ApplicationContext}, {@link TaskContextFactory}, {@link DagTask}, and
 * {@link TaskInterceptor} collaborators are all replaced with Mockito mocks so
 * no Spring context or database access is required.
 *
 * <p>The {@link TaskContext} used in each test is a real instance constructed
 * with a plain {@link ProcessModel}, matching the pattern used in production.
 */
@ExtendWith(MockitoExtension.class)
public class ProcessDAGEngineTest {

    // -------------------------------------------------------------------------
    // Shared constants
    // -------------------------------------------------------------------------

    private static final String PROCESS_ID = "proc-001";
    private static final String GATEWAY_ID = "default";

    // -------------------------------------------------------------------------
    // Mocked collaborators
    // -------------------------------------------------------------------------

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TaskContextFactory contextFactory;

    @Mock
    private TaskInterceptor interceptor;

    @Mock
    private TaskInterceptor interceptorA;

    @Mock
    private TaskInterceptor interceptorB;

    @Mock
    private DagTask taskAlpha;

    @Mock
    private DagTask taskBeta;

    @Mock
    private DagTask taskGamma;

    @Mock
    private DagTask failureHandlerTask;

    // -------------------------------------------------------------------------
    // Subject under test
    // -------------------------------------------------------------------------

    private ProcessDAGEngine engine;

    // -------------------------------------------------------------------------
    // Reusable real TaskContext
    // -------------------------------------------------------------------------

    private TaskContext taskContext;

    @BeforeEach
    void setUp() {
        ProcessModel processModel = new ProcessModel();
        processModel.setProcessId(PROCESS_ID);
        processModel.setExperimentId("exp-001");

        // contextFactory.buildContext() accepts any processId/gatewayId/taskId strings
        // and returns our pre-built real context. Using anyString() avoids having to
        // hard-code the randomly-generated taskId that the engine creates internally.
        taskContext = new TaskContext(PROCESS_ID, GATEWAY_ID, "task-id", processModel);

        when(contextFactory.buildContext(eq(PROCESS_ID), eq(GATEWAY_ID), anyString()))
                .thenReturn(taskContext);
    }

    // =========================================================================
    // 1. Happy path — single terminal node that succeeds
    // =========================================================================

    @Test
    void execute_singleSuccessNode_returnsSuccessMessage() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any(TaskContext.class)))
                .thenReturn(new DagTaskResult.Success("alpha completed"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")
                    .terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        assertEquals("alpha completed", result,
                "Engine must return the success message from the terminal node");
    }

    @Test
    void execute_singleSuccessNode_invokesContextFactory() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any(TaskContext.class)))
                .thenReturn(new DagTaskResult.Success("done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // Verify the factory is called exactly once; taskId is generated, so any string is fine
        verify(contextFactory, times(1)).buildContext(eq(PROCESS_ID), eq(GATEWAY_ID), anyString());
    }

    // =========================================================================
    // 2. Linear chain — 3 nodes, all succeed, onSuccess edges traversed in order
    // =========================================================================

    @Test
    void execute_linearChain_traversesAllSuccessEdges() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);
        when(applicationContext.getBean("gammaTask", DagTask.class)).thenReturn(taskGamma);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("alpha ok"));
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("beta ok"));
        when(taskGamma.execute(any())).thenReturn(new DagTaskResult.Success("gamma ok"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .onSuccess("gamma").onFailure(null)
                .node("gamma", "gammaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // All three task beans must be resolved and executed
        verify(taskAlpha, times(1)).execute(same(taskContext));
        verify(taskBeta,  times(1)).execute(same(taskContext));
        verify(taskGamma, times(1)).execute(same(taskContext));

        assertEquals("gamma ok", result,
                "Engine must return the message from the last node in the chain");
    }

    @Test
    void execute_linearChain_executesNodesInOrder() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);
        when(applicationContext.getBean("gammaTask", DagTask.class)).thenReturn(taskGamma);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("step 1"));
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("step 2"));
        when(taskGamma.execute(any())).thenReturn(new DagTaskResult.Success("step 3"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .onSuccess("gamma").onFailure(null)
                .node("gamma", "gammaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        InOrder ordered = inOrder(taskAlpha, taskBeta, taskGamma);
        ordered.verify(taskAlpha).execute(any());
        ordered.verify(taskBeta).execute(any());
        ordered.verify(taskGamma).execute(any());
    }

    // =========================================================================
    // 3. Failure edge navigation — node fails, engine follows onFailure edge
    // =========================================================================

    @Test
    void execute_nodeFailure_followsOnFailureEdge() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Failure("alpha failed"));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("handled"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha",       "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",        "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(failureHandlerTask, times(1)).execute(same(taskContext));
        assertEquals("handled", result,
                "Engine must return the message from the failure-handler node");
    }

    @Test
    void execute_nodeFailure_doesNotExecuteSuccessPath() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Failure("alpha failed"));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("handled"));

        // betaTask bean lookup should never happen; do not stub it so any accidental
        // call would blow up with a MissingBeanDefinition rather than silently succeeding.
        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha",   "alphaTask")  .onSuccess("beta").onFailure("fail")
                .node("beta",    "betaTask")   .terminal()
                .node("fail",    "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // betaTask is on the success path; it must NOT be executed when alpha fails
        verify(taskBeta, never()).execute(any());
    }

    // =========================================================================
    // 4. Uncaught exception — task throws RuntimeException, engine wraps as Failure
    // =========================================================================

    @Test
    void execute_taskThrowsRuntimeException_wrapsAsFailureAndFollowsOnFailureEdge() {
        RuntimeException boom = new RuntimeException("unexpected error");

        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenThrow(boom);
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("recovered"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // The engine must route to the failure handler, not propagate the raw exception
        verify(failureHandlerTask, times(1)).execute(same(taskContext));
        assertEquals("recovered", result);
    }

    @Test
    void execute_taskThrowsRuntimeException_nullOnFailure_propagatesRuntimeException() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenThrow(new RuntimeException("kaboom"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> engine.execute(dag, PROCESS_ID, GATEWAY_ID));

        assertTrue(ex.getMessage().contains("alpha"),
                "Exception message must reference the failing node id");
        assertTrue(ex.getMessage().contains(PROCESS_ID),
                "Exception message must reference the process id");
    }

    @Test
    void execute_taskThrowsException_wrapsMessageInFailureReason() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenThrow(new RuntimeException("disk full"));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("recovered"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));

        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // The interceptor must see a Failure whose reason contains the original exception message
        verify(interceptor).afterFailure(same(taskContext),
                any(TaskNode.class),
                any(DagTaskResult.Failure.class));
    }

    // =========================================================================
    // 5. Missing node — DAG references a node ID not in the graph
    // =========================================================================

    @Test
    void execute_missingNode_throwsIllegalStateException() {
        // Build a DAG that references "ghost" from the onSuccess edge but never defines it
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("ok"));

        // Manually craft a ProcessDAG whose success edge points to a non-existent node.
        // We build alpha -> "ghost" via onSuccess. The DAG builder does not validate
        // that referenced successors exist, so this is a legal (if broken) DAG.
        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("ghost").onFailure(null)
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> engine.execute(dag, PROCESS_ID, GATEWAY_ID));

        assertTrue(ex.getMessage().contains("ghost"),
                "Exception must identify the missing node id 'ghost'");
    }

    // =========================================================================
    // 6. Failure with null onFailure — engine throws RuntimeException
    // =========================================================================

    @Test
    void execute_failureWithNullOnFailure_throwsRuntimeException() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any()))
                .thenReturn(new DagTaskResult.Failure("alpha failed hard", true));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()   // onFailure = null
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> engine.execute(dag, PROCESS_ID, GATEWAY_ID));

        assertTrue(ex.getMessage().contains("alpha"),
                "Exception message must name the failing node");
        assertTrue(ex.getMessage().contains(PROCESS_ID),
                "Exception message must include the process id");
        assertTrue(ex.getMessage().contains("alpha failed hard"),
                "Exception message must include the failure reason");
    }

    @Test
    void execute_failureWithNullOnFailure_causeIsPreservedWhenPresent() {
        RuntimeException root = new RuntimeException("root cause");

        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any()))
                .thenReturn(new DagTaskResult.Failure("wrapped", false, root));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> engine.execute(dag, PROCESS_ID, GATEWAY_ID));

        assertEquals(root, ex.getCause(),
                "The RuntimeException thrown by the engine must chain the original Failure cause");
    }

    // =========================================================================
    // 7. DAG state propagation — Success output merged into TaskContext.dagState
    // =========================================================================

    @Test
    void execute_successOutput_mergedIntoDagState() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);

        Map<String, String> alphaOutput = Map.of("jobId", "job-42", "queue", "default");
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("alpha done", alphaOutput));
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("beta done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // After execution the DAG state on the context must contain alpha's output
        Map<String, String> dagState = taskContext.getDagState();
        assertEquals("job-42",  dagState.get("jobId"),  "dagState must contain 'jobId' from alpha's output");
        assertEquals("default", dagState.get("queue"),  "dagState must contain 'queue' from alpha's output");
    }

    @Test
    void execute_multipleSuccessOutputs_allMergedIntoDagState() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);
        when(applicationContext.getBean("gammaTask", DagTask.class)).thenReturn(taskGamma);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("a", Map.of("k1", "v1")));
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("b", Map.of("k2", "v2")));
        when(taskGamma.execute(any())).thenReturn(new DagTaskResult.Success("c"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .onSuccess("gamma").onFailure(null)
                .node("gamma", "gammaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        Map<String, String> dagState = taskContext.getDagState();
        assertEquals("v1", dagState.get("k1"), "k1 from alpha must be present");
        assertEquals("v2", dagState.get("k2"), "k2 from beta must be present");
    }

    @Test
    void execute_emptySuccessOutput_doesNotClearExistingDagState() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);

        // Pre-seed the dag state before execution starts
        taskContext.getDagState().put("pre-existing", "value");

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("alpha"));  // empty output
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("beta"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        assertEquals("value", taskContext.getDagState().get("pre-existing"),
                "Pre-existing dagState entries must not be removed by an empty output map");
    }

    // =========================================================================
    // 8. Interceptor ordering — before() called before task, afterSuccess/afterFailure called after
    // =========================================================================

    @Test
    void execute_interceptor_beforeCalledBeforeTaskExecute() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        InOrder order = inOrder(interceptor, taskAlpha);
        order.verify(interceptor).before(same(taskContext), any(TaskNode.class));
        order.verify(taskAlpha).execute(same(taskContext));
    }

    @Test
    void execute_interceptor_afterSuccessCalledAfterTaskExecute_onSuccess() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        InOrder order = inOrder(taskAlpha, interceptor);
        order.verify(taskAlpha).execute(same(taskContext));
        order.verify(interceptor).afterSuccess(same(taskContext), any(TaskNode.class), any(DagTaskResult.Success.class));
    }

    @Test
    void execute_interceptor_afterFailureCalledAfterTaskExecute_onFailure() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Failure("oops"));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("handled"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        InOrder order = inOrder(taskAlpha, interceptor);
        order.verify(taskAlpha).execute(same(taskContext));
        order.verify(interceptor).afterFailure(same(taskContext), any(TaskNode.class), any(DagTaskResult.Failure.class));
    }

    @Test
    void execute_interceptor_afterSuccessNotCalledOnFailure() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Failure("bad"));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("ok"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // afterSuccess must NOT be called for the alpha node that produced a Failure
        verify(interceptor, never()).afterSuccess(same(taskContext),
                argThatNodeId("alpha"), any(DagTaskResult.Success.class));
    }

    @Test
    void execute_interceptor_afterFailureNotCalledOnSuccess() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("fine"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(interceptor, never()).afterFailure(any(), any(), any());
    }

    // =========================================================================
    // 9. Multiple interceptors — all called in registration order
    // =========================================================================

    @Test
    void execute_multipleInterceptors_allReceiveBeforeCall() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptorA, interceptorB));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(interceptorA, times(1)).before(same(taskContext), any(TaskNode.class));
        verify(interceptorB, times(1)).before(same(taskContext), any(TaskNode.class));
    }

    @Test
    void execute_multipleInterceptors_beforeCalledInRegistrationOrder() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptorA, interceptorB));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        InOrder order = inOrder(interceptorA, interceptorB);
        order.verify(interceptorA).before(any(), any());
        order.verify(interceptorB).before(any(), any());
    }

    @Test
    void execute_multipleInterceptors_afterSuccessCalledInRegistrationOrder() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("done"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptorA, interceptorB));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        InOrder order = inOrder(interceptorA, interceptorB);
        order.verify(interceptorA).afterSuccess(any(), any(), any());
        order.verify(interceptorB).afterSuccess(any(), any(), any());
    }

    @Test
    void execute_multipleInterceptors_allReceiveAfterFailureCall() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Failure("fail"));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("handled"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptorA, interceptorB));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(interceptorA, times(1)).afterFailure(same(taskContext), any(TaskNode.class), any(DagTaskResult.Failure.class));
        verify(interceptorB, times(1)).afterFailure(same(taskContext), any(TaskNode.class), any(DagTaskResult.Failure.class));
    }

    @Test
    void execute_multipleInterceptors_calledOncePerNodeForLinearChain() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("a"));
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("b"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptorA, interceptorB));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // 2 nodes => before/afterSuccess called twice per interceptor
        verify(interceptorA, times(2)).before(same(taskContext), any(TaskNode.class));
        verify(interceptorB, times(2)).before(same(taskContext), any(TaskNode.class));
        verify(interceptorA, times(2)).afterSuccess(same(taskContext), any(TaskNode.class), any());
        verify(interceptorB, times(2)).afterSuccess(same(taskContext), any(TaskNode.class), any());
    }

    // =========================================================================
    // 10. Default return message — when lastMessage is null, returns default string
    // =========================================================================

    @Test
    void execute_emptyDag_returnDefaultMessage() {
        // A single-node DAG where the task returns a Success with null message
        // forces lastMessage to null after the node, resulting in the default message.
        // However, DagTaskResult.Success stores whatever String is passed, including null.
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success(null, Map.of()));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        // When lastMessage == null, the engine falls back to the default message
        assertNotNull(result, "Result must never be null");
        assertTrue(result.contains(PROCESS_ID),
                "Default message must embed the process id when lastMessage is null");
    }

    // =========================================================================
    // 11. Interceptor receives the correct TaskNode for each node
    // =========================================================================

    @Test
    void execute_interceptor_receivesCorrectNodeForEachStep() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("betaTask",  DagTask.class)).thenReturn(taskBeta);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("a"));
        when(taskBeta.execute(any())).thenReturn(new DagTaskResult.Success("b"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").onSuccess("beta").onFailure(null)
                .node("beta",  "betaTask") .terminal()
                .build();

        TaskNode alphaNode = dag.getNode("alpha");
        TaskNode betaNode  = dag.getNode("beta");

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(interceptor).before(same(taskContext), same(alphaNode));
        verify(interceptor).before(same(taskContext), same(betaNode));
        verify(interceptor).afterSuccess(same(taskContext), same(alphaNode), any(DagTaskResult.Success.class));
        verify(interceptor).afterSuccess(same(taskContext), same(betaNode),  any(DagTaskResult.Success.class));
    }

    // =========================================================================
    // 12. Empty interceptors list — engine executes without errors
    // =========================================================================

    @Test
    void execute_noInterceptors_completesNormally() {
        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Success("ok"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        assertEquals("ok", result);
    }

    // =========================================================================
    // 13. Failure followed by success — full path returning success message
    // =========================================================================

    @Test
    void execute_failureThenSuccessHandler_returnsFinalSuccessMessage() {
        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(new DagTaskResult.Failure("alpha failed", false));
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("cleanup complete"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of());

        String result = engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        assertEquals("cleanup complete", result,
                "Engine must return the final success message from the failure-handler node");
    }

    // =========================================================================
    // 14. Interceptor receives the correct DagTaskResult on afterSuccess
    // =========================================================================

    @Test
    void execute_interceptorAfterSuccess_receivesActualResultInstance() {
        DagTaskResult.Success expectedResult = new DagTaskResult.Success("precise message", Map.of("x", "1"));

        when(applicationContext.getBean("alphaTask", DagTask.class)).thenReturn(taskAlpha);
        when(taskAlpha.execute(any())).thenReturn(expectedResult);

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(interceptor).afterSuccess(same(taskContext), any(TaskNode.class), same(expectedResult));
    }

    @Test
    void execute_interceptorAfterFailure_receivesActualResultInstance() {
        DagTaskResult.Failure expectedFailure = new DagTaskResult.Failure("specific reason", true);

        when(applicationContext.getBean("alphaTask",   DagTask.class)).thenReturn(taskAlpha);
        when(applicationContext.getBean("failHandler", DagTask.class)).thenReturn(failureHandlerTask);

        when(taskAlpha.execute(any())).thenReturn(expectedFailure);
        when(failureHandlerTask.execute(any())).thenReturn(new DagTaskResult.Success("ok"));

        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")  .onSuccess(null).onFailure("fail")
                .node("fail",  "failHandler").terminal()
                .build();

        engine = new ProcessDAGEngine(applicationContext, contextFactory, List.of(interceptor));
        engine.execute(dag, PROCESS_ID, GATEWAY_ID);

        verify(interceptor).afterFailure(same(taskContext), any(TaskNode.class), same(expectedFailure));
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * Returns an argument matcher that accepts only a {@link TaskNode} whose
     * {@code id()} matches the given node identifier string. Used to assert that
     * a specific node — and not just any node — was passed to a mock method.
     */
    private static TaskNode argThatNodeId(String nodeId) {
        return org.mockito.ArgumentMatchers.argThat(
                node -> node != null && nodeId.equals(node.id()));
    }
}
