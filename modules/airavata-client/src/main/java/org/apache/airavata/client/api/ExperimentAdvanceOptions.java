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

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;

public interface ExperimentAdvanceOptions {
	
	/**
	 * Get the user who will be running the experiment if different from the experiment
	 * submitting user.
	 * @return String representing the execution user
	 */
	public String getExperimentExecutionUser();
	
	/**
	 * Get the metadata for the experiment.
	 * @return String representing the custom metadata.
	 */
	public String getExperimentMetadata();
	
	/**
	 * Get the name of the experiment
	 * @return String representing the experiment name.
	 */
	public String getExperimentName();
	
	/**
	 * Get the custom Id that will be used as the experiment Id.
	 * @return String representing the custom experiment Id.
	 */
	public String getCustomExperimentId();
	
	/**
	 * Get the custom workflow context settings.
	 * @return WorkflowContextHeaderBuilder object.
	 */
	public WorkflowContextHeaderBuilder getCustomWorkflowContext();
	
	/**
	 * Set a 3rd party user identity as the user who performed this experiment. If not specified 
	 * the experiment submission user will be used as the execution user.
	 * @param experimentExecutionUser - String representing the user.
	 */
	public void setExperimentExecutioUser(String experimentExecutionUser);
	
	/**
	 * Set custom metadata for the experiment.<br />
	 * <i><b>Note:</b> Users can store custom data related to experiment along with the experiment and 
	 * retrieve them later on.</i> 
	 * @param experimentMetadata - String representing the metadata.
	 */
	public void setExperimentCustomMetadata(String experimentMetadata);
	
	/**
	 * Set the name of the experiment. Must be unique. If not defined the name will be 
	 * auto-generated using the worklfow template Id & & time of experiment submission .
	 * @param experimentName - String representing experiment name.
	 */
	public void setExperimentName(String experimentName);
	
	/**
	 * Set a custom id as an experiment Id. If not specified the system will autogenerate an 
	 * experiment id.
	 * @param customExperimentId - String representing the experiment Id.
	 */
	public void setCustomExperimentId(String customExperimentId);
	
	/**
	 * Set a custom workflow context to the experiment. From this users can specify scheduling,
	 * output handling & security related custom settings. If not specified a default empty workflow
	 * context will be used.
	 * @param workflowContext - WorkflowContextHeaderBuilder object which contains the custom 
	 * settings for the workflow context.
	 */
	public void setCustomWorkflowContext(WorkflowContextHeaderBuilder workflowContext);
	
	/**
	 * Create a new Workflow Context object. <br />
	 * <i><b>Note:</b> This will not be set as the Custom Workflow Context in the ExperimentAdanceOptions. 
	 * Users should use the function </i><code><b>setCustomWorkflowContext(...)</b></code><i> to do 
	 * so</i>.
	 * @return A WorkflowContextHeaderBuilder object.
	 */
	public WorkflowContextHeaderBuilder newCustomWorkflowContext();
	
	/**
	 * Create a unique experiment Id.<br />
	 * <i><b>Note:</b> This will not be set as the experiment Id for the experiment. Users should use the
	 * function </i><code><b>setCustomExperimentId(...)</b></code><i> to do so.
	 * @return A string representing a unique id.
	 */
	public String generatExperimentId();
}
