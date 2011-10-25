package org.apache.airavata.registry.api.workflow;

public class WorkflowIOData {
	private String data; 
	private String experimentId;
    private String nodeId;
    private String workflowName;
    private String workflowId;
    
    public WorkflowIOData() {
	}
    
	public WorkflowIOData(String data, String experimentId, String workflowId,
            String nodeId,String workflowName) {
		setData(data);
		setExperimentId(experimentId);
		setWorkflowId(workflowId);
		setNodeId(nodeId);
		setWorkflowName(workflowName);
	}

	public WorkflowIOData(String data, String experimentId,
            String nodeId,String workflowName) {
		this(data, experimentId, experimentId, nodeId, workflowName);
	}
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
}
