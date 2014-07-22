/**
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
 */

package org.apache.airavata.orchestrator.core.model;

import java.util.Map;

public class ExperimentConfigurationData {
	 private String experimentID;
	 private String applicationName;
	 private String jobRequest;
	 private ResourceScheduling resourceScheduling;
	 private Map<String,Object> inputParameters;
	 private Map<String,Object> outputParameters;
	public String getExperimentID() {
		return experimentID;
	}
	public void setExperimentID(String experimentID) {
		this.experimentID = experimentID;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getJobRequest() {
		return jobRequest;
	}
	public void setJobRequest(String jobRequest) {
		this.jobRequest = jobRequest;
	}
	public ResourceScheduling getResourceScheduling() {
		return resourceScheduling;
	}
	public void setResourceScheduling(ResourceScheduling resourceScheduling) {
		this.resourceScheduling = resourceScheduling;
	}
	public Map<String, Object> getInputParameters() {
		return inputParameters;
	}
	public void setInputParameters(Map<String, Object> inputParameters) {
		this.inputParameters = inputParameters;
	}
	public Map<String, Object> getOutputParameters() {
		return outputParameters;
	}
	public void setOutputParameters(Map<String, Object> outputParameters) {
		this.outputParameters = outputParameters;
	}
	 
}
