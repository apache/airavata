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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class WorkflowInstanceNode{
	private WorkflowExecution workflowInstance;
	private String nodeId;
    private String originalNodeID;
    private int executionIndex;

    public WorkflowInstanceNode() {
    }

    public WorkflowInstanceNode(WorkflowExecution workflowInstance, String nodeId) {
		setWorkflowInstance(workflowInstance);
		setNodeId(nodeId);
	}

	public WorkflowExecution getWorkflowInstance() {
		return workflowInstance;
	}

	public void setWorkflowInstance(WorkflowExecution workflowInstance) {
		this.workflowInstance = workflowInstance;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

    public String getOriginalNodeID() {
        return originalNodeID;
    }

    public int getExecutionIndex() {
        return executionIndex;
    }
}
