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

package org.apache.airavata.client.impl;

import java.util.UUID;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;

public class ExperimentAdavanceOptionsImpl implements ExperimentAdvanceOptions {
	private String executionUser;
	private String metadata;
	private String experimentName;
	private String customExperimentId;
	private WorkflowContextHeaderBuilder workflowContext;
	
	private AiravataAPI api;
	
	public ExperimentAdavanceOptionsImpl(AiravataAPI api) {
		this.api=api;
	}
	
	@Override
	public String getExperimentExecutionUser() {
		return executionUser;
	}

	@Override
	public String getExperimentMetadata() {
		return metadata;
	}

	@Override
	public String getExperimentName() {
		return experimentName;
	}

	@Override
	public String getCustomExperimentId() {
		return customExperimentId;
	}

	@Override
	public WorkflowContextHeaderBuilder getCustomWorkflowContext() {
		return workflowContext;
	}

	@Override
	public void setExperimentExecutioUser(String experimentExecutionUser) {
		this.executionUser=experimentExecutionUser;

	}

	@Override
	public void setExperimentCustomMetadata(String experimentMetadata) {
		this.metadata=experimentMetadata;
	}

	@Override
	public void setExperimentName(String experimentName) {
		this.experimentName=experimentName;
	}

	@Override
	public void setCustomExperimentId(String customExperimentId) {
		this.customExperimentId=customExperimentId;
	}

	@Override
	public void setCustomWorkflowContext(
			WorkflowContextHeaderBuilder workflowContext) {
		this.workflowContext=workflowContext;
	}

	@Override
	public WorkflowContextHeaderBuilder newCustomWorkflowContext() throws AiravataAPIInvocationException {
		return api.getExecutionManager().createWorkflowContextHeader();
	}

	@Override
	public String generatExperimentId() {
		return UUID.randomUUID().toString();
	}

}
