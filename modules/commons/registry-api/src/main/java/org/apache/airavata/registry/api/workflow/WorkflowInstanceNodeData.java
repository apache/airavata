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

import java.util.List;

public class WorkflowInstanceNodeData{
	private WorkflowInstanceNode workflowInstanceNode;
	private List<WorkflowInstanceNodePortData> inputData;
	private List<WorkflowInstanceNodePortData> outputData;
	private String input;
	private String output;
	
	public WorkflowInstanceNodeData(WorkflowInstanceNode workflowInstanceNode) {
		setWorkflowInstanceNode(workflowInstanceNode);
	}

	public WorkflowInstanceNode getWorkflowInstanceNode() {
		return workflowInstanceNode;
	}

	public void setWorkflowInstanceNode(WorkflowInstanceNode workflowInstanceNode) {
		this.workflowInstanceNode = workflowInstanceNode;
	}

	public List<WorkflowInstanceNodePortData> getInputData() {
		return inputData;
	}

	public void setInputData(List<WorkflowInstanceNodePortData> inputData) {
		this.inputData = inputData;
	}

	public List<WorkflowInstanceNodePortData> getOutputData() {
		return outputData;
	}

	public void setOutputData(List<WorkflowInstanceNodePortData> outputData) {
		this.outputData = outputData;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
}
