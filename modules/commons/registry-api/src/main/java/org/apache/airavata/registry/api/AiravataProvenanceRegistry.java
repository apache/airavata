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

package org.apache.airavata.registry.api;

import java.util.List;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;

public abstract class AiravataProvenanceRegistry implements DataRegistry{
	private String user;
	
	public AiravataProvenanceRegistry(String user) {
		setUser(user);
	}
	
	public abstract boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData workflowInputData) throws RegistryException;

    public abstract boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData workflowOutputData)throws RegistryException;
    
    public abstract List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;

    public abstract List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;
    
    public abstract boolean saveWorkflowExecutionName(String experimentId,String workflowIntanceName)throws RegistryException;
    
    public abstract boolean saveWorkflowExecutionStatus(String experimentId,WorkflowInstanceStatus status)throws RegistryException;
    
    public abstract boolean saveWorkflowExecutionStatus(String experimentId,ExecutionStatus status)throws RegistryException;

    public abstract WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)throws RegistryException;

    public abstract boolean saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException;
    
    public abstract boolean saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegistryException;

    public abstract WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException;
    
    public abstract List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException;

    public abstract String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException;

    public abstract boolean saveWorkflowExecutionUser(String experimentId, String user) throws RegistryException;
    
    public abstract String getWorkflowExecutionUser(String experimentId) throws RegistryException;
    
    public abstract String getWorkflowExecutionName(String experimentId) throws RegistryException;
    
    public abstract WorkflowExecution getWorkflowExecution(String experimentId) throws RegistryException;
    
    public abstract List<String> getWorkflowExecutionIdByUser(String user) throws RegistryException;

    public abstract List<WorkflowExecution> getWorkflowExecutionByUser(String user) throws RegistryException;
    
    public abstract List<WorkflowExecution> getWorkflowExecutionByUser(String user, int pageSize, int pageNo) throws RegistryException;
    
    public abstract String getWorkflowExecutionMetadata(String experimentId) throws RegistryException;
    
    public abstract boolean saveWorkflowExecutionMetadata(String experimentId, String metadata) throws RegistryException;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
