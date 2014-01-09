package org.apache.airavata.registry.api.orchestrator.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.airavata.registry.api.orchestrator.OrchestratorData;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class OrchestratorDataImpl implements OrchestratorData{

	private String orchestratorId;
	private String experimentId;
	private String user;
	private String status;
	private String state;
	private String gfacEPR;
	private boolean lazyLoaded=false;

    public OrchestratorDataImpl() {
        this(false);
    }

    public OrchestratorDataImpl(boolean lazyLoaded) {
        this.lazyLoaded = lazyLoaded;
    }
	@Override
	public String getOrchestratorId() {
		return orchestratorId;
	}

	@Override
	public String getExperimentId() {
		return experimentId;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getGFACServiceEPR() {
		return gfacEPR;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public String getStatus() {
		return status.toString();
	}

	@Override
	public void setExperimentId(String experimentId) {
	this.experimentId =  experimentId;	
	}

	@Override
	public void setUser(String user) {
		this.user = user;
		
	}

	@Override
	public void setGFACServiceEPR(String gfacEPR) {
		this.gfacEPR = gfacEPR;
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

}
