package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;

@Entity
public class OrchestratorData {
	private String orchestratorId;
	private String experimentId;
	private String user;
	private String status;
	private String state;
	private String gfacEPR;

	public String getOrchestratorId() {
		return orchestratorId;
	}

	public String getExperimentId() {
		return experimentId;
	}

	public String getUser() {
		return user;
	}

	public String getGFACServiceEPR() {
		return gfacEPR;
	}

	public String getState() {
		return state;
	}

	public String getStatus() {
		return status.toString();
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public void setUser(String user) {
		this.user = user;

	}

	public void setGFACServiceEPR(String gfacEPR) {
		this.gfacEPR = gfacEPR;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
