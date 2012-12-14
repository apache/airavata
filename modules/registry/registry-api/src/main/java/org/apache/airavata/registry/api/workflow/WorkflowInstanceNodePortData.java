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

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowInstanceNodePortData {
	private WorkflowInstanceNode workflowInstanceNode;
	private String name;
	private String value;

    public WorkflowInstanceNodePortData() {
    }

    public WorkflowInstanceNodePortData(WorkflowInstanceNode workflowInstanceNode, String portName, String portValue) {
		setWorkflowInstanceNode(workflowInstanceNode);
		setName(portName);
		setValue(portValue);
	}
	
	public WorkflowInstanceNodePortData(WorkflowInstanceNode workflowInstanceNode, String data) {
		setWorkflowInstanceNode(workflowInstanceNode);
		setValue(data);
	}

	public WorkflowInstanceNode getWorkflowInstanceNode() {
		return workflowInstanceNode;
	}

	public void setWorkflowInstanceNode(WorkflowInstanceNode workflowInstanceNode) {
		this.workflowInstanceNode = workflowInstanceNode;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getId(){
		return getName();
	}
	

    public String getExperimentId(){
    	return getWorkflowInstanceNode().getWorkflowInstance().getExperimentId();
    }
    
    public String getWorkflowInstanceId(){
    	return getWorkflowInstanceNode().getWorkflowInstance().getWorkflowExecutionId();
    }
    
    public String getWorkflowInstanceNodeId(){
    	return getWorkflowInstanceNode().getNodeId();
    }
}
