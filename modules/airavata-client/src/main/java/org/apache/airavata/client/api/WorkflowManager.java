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

package org.apache.airavata.client.api;

import java.util.List;

import org.apache.airavata.workflow.model.wf.Workflow;

public interface WorkflowManager {

	//privilledged API
	public boolean saveWorkflow(String workflowAsString, String owner) throws AiravataAPIInvocationException;
	
    public boolean saveWorkflow(Workflow workflow, String owner) throws AiravataAPIInvocationException;

    public List<Workflow> getWorkflows(String owner) throws AiravataAPIInvocationException;
    
    public List<String> getWorkflowTemplateIds(String owner) throws AiravataAPIInvocationException;

    public Workflow getWorkflow(String workflowName, String owner) throws AiravataAPIInvocationException;

    public String getWorkflowAsString(String workflowName, String owner) throws AiravataAPIInvocationException;

    public boolean deleteWorkflow(String workflowName, String owner) throws AiravataAPIInvocationException;
    
    //user api
	public boolean saveWorkflow(String workflowAsString) throws AiravataAPIInvocationException;

	public boolean saveWorkflowAsPublic(String workflowAsString) throws AiravataAPIInvocationException;
	
    public boolean saveWorkflow(Workflow workflow) throws AiravataAPIInvocationException;
    
    public boolean saveWorkflowAsPublic(Workflow workflow) throws AiravataAPIInvocationException;

    public List<Workflow> getWorkflows() throws AiravataAPIInvocationException;
    
    public List<String> getWorkflowTemplateIds() throws AiravataAPIInvocationException;

    public Workflow getWorkflow(String workflowName) throws AiravataAPIInvocationException;

    public String getWorkflowAsString(String workflowName) throws AiravataAPIInvocationException;

    public boolean deleteWorkflow(String workflowName) throws AiravataAPIInvocationException;
    
    public Workflow getWorkflowFromString(String workflowAsString) throws AiravataAPIInvocationException;
    
    public String getWorkflowAsString(Workflow workflow) throws AiravataAPIInvocationException;

}
