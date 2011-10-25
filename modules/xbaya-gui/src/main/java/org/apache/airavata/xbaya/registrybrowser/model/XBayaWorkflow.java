package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;

public class XBayaWorkflow {
	private List<XBayaWorkflowService> workflowServices;
	private String workflowId;
	private String workflowName;
	
	public XBayaWorkflow(String workflowId, String workflowName, List<XBayaWorkflowService> workflowServices) {
		setWorkflowId(workflowId);
		setWorkflowName(workflowName);
		setWorkflowServices(workflowServices);
	}

	public List<XBayaWorkflowService> getWorkflowServices() {
		if (workflowServices==null){
			workflowServices=new ArrayList<XBayaWorkflowService>();
		}
		return workflowServices;
	}

	public void setWorkflowServices(List<XBayaWorkflowService> workflowServices) {
		this.workflowServices = workflowServices;
	}
	
	public void add(XBayaWorkflowService workflowService){
		getWorkflowServices().add(workflowService);
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
