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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for {@link ProcessDAG} and its fluent builder API.
 * No Spring context or external dependencies required.
 */
public class ProcessDAGTest {

    // ===========================================================================
    // Builder — entry node
    // ===========================================================================

    @Test
    public void builder_setsEntryNodeId() {
        ProcessDAG dag = ProcessDAG.builder("first")
                .node("first", "someTask")
                    .terminal()
                .build();

        assertEquals("first", dag.entryNodeId(),
                "entryNodeId must match the id passed to builder()");
    }

    @Test
    public void build_throwsIllegalState_whenEntryNodeNotDefined() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ProcessDAG.builder("missing")
                        .node("other", "someTask")
                            .terminal()
                        .build());

        assertTrue(ex.getMessage().contains("missing"),
                "Exception message must name the undefined entry node");
    }

    // ===========================================================================
    // Builder — node retrieval
    // ===========================================================================

    @Test
    public void getNode_returnsNode_byId() {
        ProcessDAG dag = ProcessDAG.builder("alpha")
                .node("alpha", "alphaTask")
                    .onSuccess("beta").onFailure("beta")
                .node("beta", "betaTask")
                    .terminal()
                .build();

        TaskNode alpha = dag.getNode("alpha");
        assertNotNull(alpha, "Node 'alpha' must be retrievable");
        assertEquals("alpha", alpha.id());
        assertEquals("alphaTask", alpha.taskBeanName());
    }

    @Test
    public void getNode_returnsNull_forUnknownId() {
        ProcessDAG dag = ProcessDAG.builder("only")
                .node("only", "onlyTask")
                    .terminal()
                .build();

        assertNull(dag.getNode("nonExistent"),
                "getNode must return null for an id not in the DAG");
    }

    @Test
    public void nodes_returnsAllDefinedNodes() {
        ProcessDAG dag = ProcessDAG.builder("a")
                .node("a", "taskA").onSuccess("b").onFailure("b")
                .node("b", "taskB").terminal()
                .build();

        Map<String, TaskNode> nodes = dag.nodes();
        assertEquals(2, nodes.size(), "DAG must contain exactly 2 nodes");
        assertTrue(nodes.containsKey("a"), "nodes map must contain 'a'");
        assertTrue(nodes.containsKey("b"), "nodes map must contain 'b'");
    }

    // ===========================================================================
    // Builder — edge wiring: onSuccess / onFailure
    // ===========================================================================

    @Test
    public void node_onSuccess_setsSuccessorId() {
        ProcessDAG dag = ProcessDAG.builder("start")
                .node("start", "startTask")
                    .onSuccess("next").onFailure("err")
                .node("next", "nextTask").terminal()
                .node("err", "errTask").terminal()
                .build();

        assertEquals("next", dag.getNode("start").onSuccess(),
                "onSuccess edge must resolve to 'next'");
    }

    @Test
    public void node_onFailure_setsFailureSuccessorId() {
        ProcessDAG dag = ProcessDAG.builder("start")
                .node("start", "startTask")
                    .onSuccess("next").onFailure("err")
                .node("next", "nextTask").terminal()
                .node("err", "errTask").terminal()
                .build();

        assertEquals("err", dag.getNode("start").onFailure(),
                "onFailure edge must resolve to 'err'");
    }

    @Test
    public void node_onSuccess_canBeNull_forPartialTerminal() {
        ProcessDAG dag = ProcessDAG.builder("gate")
                .node("gate", "gateTask")
                    .onSuccess(null).onFailure("fallback")
                .node("fallback", "fallbackTask")
                    .terminal()
                .build();

        assertNull(dag.getNode("gate").onSuccess(),
                "onSuccess may be null to express a terminal success path");
        assertEquals("fallback", dag.getNode("gate").onFailure());
    }

    @Test
    public void node_onFailure_canBeNull_forPartialTerminal() {
        ProcessDAG dag = ProcessDAG.builder("gate")
                .node("gate", "gateTask")
                    .onSuccess("happy").onFailure(null)
                .node("happy", "happyTask")
                    .terminal()
                .build();

        assertNull(dag.getNode("gate").onFailure(),
                "onFailure may be null to express a terminal failure path");
    }

    // ===========================================================================
    // Builder — terminal() helper
    // ===========================================================================

    @Test
    public void terminal_setsOnSuccessToNull() {
        ProcessDAG dag = ProcessDAG.builder("leaf")
                .node("leaf", "leafTask")
                    .terminal()
                .build();

        assertNull(dag.getNode("leaf").onSuccess(),
                "terminal() must set onSuccess to null");
    }

    @Test
    public void terminal_setsOnFailureToNull() {
        ProcessDAG dag = ProcessDAG.builder("leaf")
                .node("leaf", "leafTask")
                    .terminal()
                .build();

        assertNull(dag.getNode("leaf").onFailure(),
                "terminal() must set onFailure to null");
    }

    @Test
    public void terminal_overridesPreviousEdgeAssignments() {
        ProcessDAG dag = ProcessDAG.builder("leaf")
                .node("leaf", "leafTask")
                    .onSuccess("somewhere")
                    .onFailure("somewhere")
                    .terminal()   // must win
                .build();

        assertNull(dag.getNode("leaf").onSuccess(),
                "terminal() called after onSuccess must nullify the success edge");
        assertNull(dag.getNode("leaf").onFailure(),
                "terminal() called after onFailure must nullify the failure edge");
    }

    // ===========================================================================
    // Builder — metadata
    // ===========================================================================

    @Test
    public void node_metadata_isStoredOnTaskNode() {
        ProcessDAG dag = ProcessDAG.builder("work")
                .node("work", "workTask")
                    .metadata("processState", "EXECUTING")
                    .terminal()
                .build();

        TaskNode work = dag.getNode("work");
        assertEquals("EXECUTING", work.metadata().get("processState"),
                "metadata key 'processState' must be stored with value 'EXECUTING'");
    }

    @Test
    public void node_metadata_supportsMultipleEntries() {
        ProcessDAG dag = ProcessDAG.builder("work")
                .node("work", "workTask")
                    .metadata("processState", "EXECUTING")
                    .metadata("phase", "pre")
                    .terminal()
                .build();

        Map<String, String> meta = dag.getNode("work").metadata();
        assertEquals(2, meta.size(), "Both metadata entries must be stored");
        assertEquals("EXECUTING", meta.get("processState"));
        assertEquals("pre", meta.get("phase"));
    }

    @Test
    public void node_withNoMetadata_hasEmptyMetadataMap() {
        ProcessDAG dag = ProcessDAG.builder("bare")
                .node("bare", "bareTask")
                    .terminal()
                .build();

        Map<String, String> meta = dag.getNode("bare").metadata();
        assertNotNull(meta, "metadata map must never be null");
        assertTrue(meta.isEmpty(), "metadata map must be empty when none was set");
    }

    // ===========================================================================
    // Builder — chaining via NodeBuilder.node()
    // ===========================================================================

    @Test
    public void nodeBuilder_node_transitionsToNewNode() {
        ProcessDAG dag = ProcessDAG.builder("first")
                .node("first", "firstTask")
                    .onSuccess("second").onFailure("second")
                .node("second", "secondTask")   // chained from NodeBuilder
                    .onSuccess("third").onFailure("third")
                .node("third", "thirdTask")
                    .terminal()
                .build();

        assertEquals(3, dag.nodes().size(), "All three chained nodes must be registered");
        assertEquals("second", dag.getNode("first").onSuccess());
        assertEquals("third", dag.getNode("second").onSuccess());
    }

    @Test
    public void nodeBuilder_node_flushesCurrentNodeBeforeStartingNew() {
        // Calling .node() on a NodeBuilder must flush the in-progress node
        // so its edges are captured before the new node begins.
        ProcessDAG dag = ProcessDAG.builder("a")
                .node("a", "taskA")
                    .onSuccess("b").onFailure("b")
                .node("b", "taskB")
                    .terminal()
                .build();

        // If flushing was broken, node 'a' would lose its edges
        assertEquals("b", dag.getNode("a").onSuccess(),
                "Flushed node must retain its onSuccess edge");
        assertEquals("b", dag.getNode("a").onFailure(),
                "Flushed node must retain its onFailure edge");
    }

    // ===========================================================================
    // Builder — chaining via NodeBuilder.build()
    // ===========================================================================

    @Test
    public void nodeBuilder_build_flushesLastNodeAndReturnsDag() {
        ProcessDAG dag = ProcessDAG.builder("only")
                .node("only", "onlyTask")
                    .onSuccess(null).onFailure(null)
                    .build();   // build() called on NodeBuilder, not Builder

        assertNotNull(dag, "build() on NodeBuilder must return a non-null ProcessDAG");
        assertEquals("only", dag.entryNodeId());
        assertNotNull(dag.getNode("only"),
                "Last node must be flushed when build() is called on NodeBuilder");
    }

    @Test
    public void nodeBuilder_build_includesEdgeSetBeforeCall() {
        ProcessDAG dag = ProcessDAG.builder("head")
                .node("head", "headTask")
                    .onSuccess("tail").onFailure("tail")
                .node("tail", "tailTask")
                    .terminal()
                    .build();

        assertEquals("tail", dag.getNode("head").onSuccess(),
                "Edges set before NodeBuilder.build() must be preserved");
    }

    // ===========================================================================
    // ProcessDAG — immutability
    // ===========================================================================

    @Test
    public void nodes_map_isUnmodifiable() {
        ProcessDAG dag = ProcessDAG.builder("x")
                .node("x", "xTask").terminal()
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> dag.nodes().put("y", new TaskNode("y", "yTask", null, null)),
                "nodes() must return an unmodifiable map");
    }

    // ===========================================================================
    // ProcessDAG — linear chain smoke test
    // ===========================================================================

    @Test
    public void fullLinearChain_isWiredCorrectly() {
        ProcessDAG dag = ProcessDAG.builder("a")
                .node("a", "taskA").onSuccess("b").onFailure("f")
                .node("b", "taskB").onSuccess("c").onFailure("f")
                .node("c", "taskC").onSuccess(null).onFailure("f")
                .node("f", "failTask").terminal()
                .build();

        assertEquals("a", dag.entryNodeId());

        TaskNode a = dag.getNode("a");
        assertEquals("b", a.onSuccess());
        assertEquals("f", a.onFailure());

        TaskNode b = dag.getNode("b");
        assertEquals("c", b.onSuccess());
        assertEquals("f", b.onFailure());

        TaskNode c = dag.getNode("c");
        assertNull(c.onSuccess());
        assertEquals("f", c.onFailure());

        TaskNode f = dag.getNode("f");
        assertNull(f.onSuccess());
        assertNull(f.onFailure());
    }
}
