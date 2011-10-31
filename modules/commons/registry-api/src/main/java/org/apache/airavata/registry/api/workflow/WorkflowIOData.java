/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.registry.api.workflow;

public class WorkflowIOData {
    private String data;
    private String experimentId;
    private String nodeId;
    private String workflowName;
    private String workflowId;

    public WorkflowIOData() {
    }

    public WorkflowIOData(String data, String experimentId, String workflowId, String nodeId, String workflowName) {
        setData(data);
        setExperimentId(experimentId);
        setWorkflowId(workflowId);
        setNodeId(nodeId);
        setWorkflowName(workflowName);
    }

    public WorkflowIOData(String data, String experimentId, String nodeId, String workflowName) {
        this(data, experimentId, experimentId, nodeId, workflowName);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}
