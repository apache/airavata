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
