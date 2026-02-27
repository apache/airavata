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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An immutable directed acyclic graph defining the execution order of
 * {@link DagTask}s for a process phase (pre, post, or cancel).
 *
 * <p>Each node maps to a Spring bean implementing {@link DagTask}.
 * Edges are defined per-node as success/failure transitions.
 * The DAG is walked linearly by the Temporal workflow;
 * parallelism is not supported (each node has at most one successor).
 *
 * <p>Build DAGs via the fluent {@link Builder} API:
 * <pre>{@code
 * ProcessDAG dag = ProcessDAG.builder("provision")
 *     .node("provision", "slurmProvisioningTask")
 *         .onSuccess("stageIn").onFailure("fail")
 *     .node("stageIn", "sftpInputStagingTask")
 *         .onSuccess("submit").onFailure("fail")
 *     .node("submit", "slurmSubmitTask")
 *         .onSuccess(null).onFailure("fail")
 *     .node("fail", "markFailedTask")
 *         .terminal()
 *     .build();
 * }</pre>
 */
public class ProcessDAG {

    private final String entryNodeId;
    private final Map<String, TaskNode> nodes;

    private ProcessDAG(String entryNodeId, Map<String, TaskNode> nodes) {
        this.entryNodeId = entryNodeId;
        this.nodes = Map.copyOf(nodes);
    }

    public String entryNodeId() {
        return entryNodeId;
    }

    public TaskNode getNode(String id) {
        return nodes.get(id);
    }

    public Map<String, TaskNode> nodes() {
        return nodes;
    }

    public static Builder builder(String entryNodeId) {
        return new Builder(entryNodeId);
    }

    // -------------------------------------------------------------------------
    // Fluent builder
    // -------------------------------------------------------------------------

    public static class Builder {
        private final String entryNodeId;
        private final Map<String, TaskNode> nodes = new LinkedHashMap<>();
        private NodeBuilder currentNode;

        private Builder(String entryNodeId) {
            this.entryNodeId = entryNodeId;
        }

        public NodeBuilder node(String id, String taskBeanName) {
            flushCurrent();
            currentNode = new NodeBuilder(this, id, taskBeanName);
            return currentNode;
        }

        private void flushCurrent() {
            if (currentNode != null) {
                nodes.put(currentNode.id, currentNode.buildNode());
                currentNode = null;
            }
        }

        public ProcessDAG build() {
            flushCurrent();
            if (!nodes.containsKey(entryNodeId)) {
                throw new IllegalStateException("Entry node '" + entryNodeId + "' not defined in DAG");
            }
            return new ProcessDAG(entryNodeId, nodes);
        }
    }

    public static class NodeBuilder {
        private final Builder parent;
        private final String id;
        private final String taskBeanName;
        private String onSuccess;
        private String onFailure;
        private final Map<String, String> metadata = new LinkedHashMap<>();

        private NodeBuilder(Builder parent, String id, String taskBeanName) {
            this.parent = parent;
            this.id = id;
            this.taskBeanName = taskBeanName;
        }

        public NodeBuilder onSuccess(String nodeId) {
            this.onSuccess = nodeId;
            return this;
        }

        public NodeBuilder onFailure(String nodeId) {
            this.onFailure = nodeId;
            return this;
        }

        /**
         * Marks this node as terminal (no successors on either path).
         */
        public NodeBuilder terminal() {
            this.onSuccess = null;
            this.onFailure = null;
            return this;
        }

        public NodeBuilder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public NodeBuilder node(String id, String taskBeanName) {
            return parent.node(id, taskBeanName);
        }

        public ProcessDAG build() {
            return parent.build();
        }

        private TaskNode buildNode() {
            return new TaskNode(id, taskBeanName, onSuccess, onFailure, Map.copyOf(metadata));
        }
    }
}
