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

public class WorkflowServiceIOData extends WorkflowIOData {
	private String experimentId;
    private String workflowName;
    private String workflowId;
    private String workflowInstanceId;
    private WorkflowNodeType nodeType;


    /** when you construct this object it set to STARTED state **/
    private WorkflowInstanceStatus nodeStatus = new
            WorkflowInstanceStatus(new WorkflowInstance(experimentId,workflowId,workflowName), WorkflowInstanceStatus.ExecutionStatus.STARTED);
    
    public WorkflowServiceIOData() {
	}
    
	public WorkflowServiceIOData(String data, String experimentId,String workflowInstanceID, String workflowId,
            String nodeId,String workflowName) {
		super(nodeId,data);
		setExperimentId(experimentId);
        setWorkflowInstanceId(workflowInstanceID);
		setWorkflowId(workflowId);
		setWorkflowName(workflowName);
	}

	public WorkflowServiceIOData(String data, String experimentId,String workflowInstanceID,
            String nodeId,String workflowName) {
		this(data, experimentId, experimentId,workflowInstanceID, nodeId, workflowName);
	}

    public WorkflowServiceIOData(String experimentId,String workflowInstanceID, String workflowName, String workflowId, WorkflowNodeType nodeType) {
        this.experimentId = experimentId;
        this.workflowInstanceId = workflowInstanceID;
        this.workflowName = workflowName;
        this.workflowId = workflowId;
        this.nodeType = nodeType;
    }

    public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
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

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public void setNodeType(WorkflowNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public WorkflowNodeType getNodeType() {
        return nodeType;
    }

    public WorkflowInstanceStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(WorkflowInstanceStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }
}
