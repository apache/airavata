package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;

public class XBayaWorkflowExperiment {
	private List<XBayaWorkflow> workflows;
	private String experimentId;
	
	public XBayaWorkflowExperiment(String experimentId, List<XBayaWorkflow> workflows) {
		setWorkflows(workflows);
		setExperimentId(experimentId);
	}

	public List<XBayaWorkflow> getWorkflows() {
		if (workflows==null){
			workflows=new ArrayList<XBayaWorkflow>();
		}
		return workflows;
	}

	public void setWorkflows(List<XBayaWorkflow> workflows) {
		this.workflows = workflows;
	}
	
	public void add(XBayaWorkflow workflow){
		getWorkflows().add(workflow);
	}

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}
}
