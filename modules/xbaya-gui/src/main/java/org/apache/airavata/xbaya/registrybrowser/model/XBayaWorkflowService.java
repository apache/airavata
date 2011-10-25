package org.apache.airavata.xbaya.registrybrowser.model;

public class XBayaWorkflowService {
	private InputParameters inputParameters;
	private OutputParameters outputParameters;
	private String serviceNodeId;
	
	public XBayaWorkflowService(String serviceNodeId, InputParameters inputParameters, OutputParameters outputParameters) {
		setServiceNodeId(serviceNodeId);
		setInputParameters(inputParameters);
		setOutputParameters(outputParameters);
	}

	public OutputParameters getOutputParameters() {
		return outputParameters;
	}

	public void setOutputParameters(OutputParameters outputParameters) {
		this.outputParameters = outputParameters;
	}

	public InputParameters getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(InputParameters inputParameters) {
		this.inputParameters = inputParameters;
	}

	public String getServiceNodeId() {
		return serviceNodeId;
	}

	public void setServiceNodeId(String serviceNodeId) {
		this.serviceNodeId = serviceNodeId;
	}
}
