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

package org.apache.airavata.workflow.core;

import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;

public class WorkflowContext {
    private WorkflowNode workflowNode;
    private WorkflowNodeDetails wfNodeDetails;
    private TaskDetails taskDetails;

    public WorkflowContext(WorkflowNode workflowNode, WorkflowNodeDetails wfNodeDetails, TaskDetails taskDetails) {
        this.workflowNode = workflowNode;
        this.wfNodeDetails = wfNodeDetails;
        this.taskDetails = taskDetails;
    }

    public WorkflowNode getWorkflowNode() {
        return workflowNode;
    }

    public void setWorkflowNode(WorkflowNode workflowNode) {
        this.workflowNode = workflowNode;
    }

    public WorkflowNodeDetails getWfNodeDetails() {
        return wfNodeDetails;
    }

    public void setWfNodeDetails(WorkflowNodeDetails wfNodeDetails) {
        this.wfNodeDetails = wfNodeDetails;
    }

    public TaskDetails getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(TaskDetails taskDetails) {
        this.taskDetails = taskDetails;
    }
}
