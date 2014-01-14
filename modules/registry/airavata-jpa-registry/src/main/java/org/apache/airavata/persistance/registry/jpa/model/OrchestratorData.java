package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class OrchestratorData {
	
	@Id
    private String experiment_ID;
	@GeneratedValue
	private int orchestrator_ID;
	private String user;
	private String status;
	private String state;
	private String gfacEPR;
	
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
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
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
	

}
