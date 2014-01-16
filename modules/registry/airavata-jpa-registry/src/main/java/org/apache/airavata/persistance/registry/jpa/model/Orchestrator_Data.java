package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "Orchestrator_Data")
public class Orchestrator_Data {
	
	@Id
    private String experiment_ID;
	@GeneratedValue
	private int orchestrator_ID;
	private String username;
	private String status;
	private String state;
	private String gfacEPR;
	private String applicationName;
	@Lob
	private String jobRequest;
	
	public String getExperiment_ID() {
		return experiment_ID;
	}
	public void setExperiment_ID(String experiment_ID) {
		this.experiment_ID = experiment_ID;
	}
	
	public int getOrchestrator_ID() {
		return orchestrator_ID;
	}
	public void setOrchestrator_ID(int orchestrator_ID) {
		this.orchestrator_ID = orchestrator_ID;
	}
	public String getUserName() {
		return username;
	}
	public void setUserName(String username) {
		this.username = username;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getGfacEPR() {
		return gfacEPR;
	}
	public void setGfacEPR(String gfacEPR) {
		this.gfacEPR = gfacEPR;
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
	

}
