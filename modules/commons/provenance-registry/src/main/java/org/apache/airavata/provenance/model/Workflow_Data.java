package org.apache.airavata.provenance.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Workflow_Data {

	@ManyToOne()
	@JoinColumn(name="experiment_ID")
	private Experiment_Data experiment_Data;
	@Id
	private String workflow_instanceID;
	private String template_name;
	private String status;
	private Timestamp start_time;
	private Timestamp last_update_time;

	public Experiment_Data getExperiment_Data() {
		return experiment_Data;
	}

	public void setExperiment_Data(Experiment_Data experiment_Data) {
		this.experiment_Data = experiment_Data;
	}

	public String getWorkflow_instanceID() {
		return workflow_instanceID;
	}

	public void setWorkflow_instanceID(String workflow_instanceID) {
		this.workflow_instanceID = workflow_instanceID;
	}

	public String getTemplate_name() {
		return template_name;
	}

	public void setTemplate_name(String template_name) {
		this.template_name = template_name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getStart_time() {
		return start_time;
	}

	public void setStart_time(Timestamp start_time) {
		this.start_time = start_time;
	}

	public Timestamp getLast_update_time() {
		return last_update_time;
	}

	public void setLast_update_time(Timestamp last_update_time) {
		this.last_update_time = last_update_time;
	}
}
