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

import java.util.Date;
import java.util.List;

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;

public interface NodeExecutionData {

	/**
	 * Get id of the Node
	 * @return
	 */
	public abstract String getId();

	/**
	 * Get the id of the experiment which the workflow which has this node 
	 * @return
	 */
	public abstract String getExperimentId();

	/**
	 * Get the id of the workflow execution this node belongs to
	 * @return
	 */
	public abstract String getWorkflowExecutionId();

	/**
	 * @deprecated
	 * @return
	 */
	public abstract WorkflowInstanceNode getWorkflowInstanceNode();

	/**
	 * @deprecated
	 * @param workflowInstanceNode
	 */
	public abstract void setWorkflowInstanceNode(WorkflowInstanceNode workflowInstanceNode);

	
    /**
     * Get current state of the execution of this workflow
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public WorkflowExecutionStatus.State getState();
    
    /**
     * Get current state updated time
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public Date getStatusUpdateTime();
    
	/**
	 * @deprecated
	 * Get the status of the execution of this node
	 * @return
	 */
	public abstract NodeExecutionStatus getStatus();

	/**
	 * Update the execution status of the node
	 * @param status
	 */
	public abstract void setStatus(NodeExecutionStatus status);

	/**
	 * Update the execution status of the node
	 * @param status
	 * @param date
	 */
	public abstract void setStatus(WorkflowExecutionStatus.State status,
			Date date);

	/**
	 * Retrieve the input data to the node
	 * @return
	 */
	public abstract List<InputData> getInputData();

	/**
	 * Retrieve the output data to the node
	 * @return
	 */
	public abstract List<OutputData> getOutputData();
	
	/**
	 * Setup input data for the node
	 * @param inputData
	 */
	public abstract void setInputData(List<InputData> inputData);

	/**
	 * Setup output data for the node
	 * @param outputData
	 */
	public abstract void setOutputData(List<OutputData> outputData);

	/**
	 * Get node input as comma separated name value pairs
	 * @return
	 */
	public String getInput();
	
	/**
	 * Get node output as comma separated name value pairs
	 * @return
	 */
	public String getOutput();
	
	/**
	 * Set input as comma separated name value pairs
	 * @param input
	 */
	public void setInput(String input);

	/**
	 * Set output as comma separated name value pairs
	 * @param output
	 */
	public void setOutput(String output);

	/**
	 * Get node type
	 * @return
	 */
	public abstract WorkflowNodeType.WorkflowNode getType();

	/**
	 * Set node type
	 * @param type
	 */
	public abstract void setType(WorkflowNodeType.WorkflowNode type);

}