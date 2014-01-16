package org.apache.airavata.persistance.registry.jpa.resources;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.Orchestrator_Data;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorDataResource extends AbstractResource {
   
	private final static Logger log = LoggerFactory.getLogger(OrchestratorDataResource.class);
	private String experimentID;
	private String userName;
	private String applicationName;
	private String status;
	private String state;
	private String gfacEPR;
	private String jobRequest;
	private GatewayResource gateway;

	public String getExperimentID() {
		return experimentID;
	}

	public String getUserName() {
		return userName;
	}

	public void setExperimentID(String experimentID) {
		this.experimentID = experimentID;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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

	public String getJobRequest() {
		return jobRequest;
	}

	public void setJobRequest(String jobRequest) {
		this.jobRequest = jobRequest;
	}

	public GatewayResource getGateway() {
		return gateway;
	}

	public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

	@Override
	public Resource create(ResourceType type) {
        log.error("Unsupported resource type for orchestrator resource.", new IllegalArgumentException());
        throw new IllegalArgumentException("Unsupported resource type for orchestrator resource.");
    }

	@Override
	public void remove(ResourceType type, Object name) {
		   log.error("Unsupported operation to remove orchestrator data.", new UnsupportedOperationException());
	       throw new UnsupportedOperationException();
	}

	@Override
	public Resource get(ResourceType type, Object name) {
        log.error("Unsupported resource type for orchestrator data.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
	}

	@Override
	public List<Resource> get(ResourceType type) {
        log.error("Unsupported resource type for orchestrator data.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
	}

	@Override
	public void save() {
		EntityManager em = ResourceUtils.getEntityManager();
		Orchestrator_Data existingOrchestratorData = em.find(Orchestrator_Data.class,
				experimentID);
		em.close();
		em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		Orchestrator_Data orchestratorData = new Orchestrator_Data();
		orchestratorData.setExperiment_ID(experimentID);
		orchestratorData.setUserName(userName);
		orchestratorData.setGfacEPR(gfacEPR);
		orchestratorData.setState(state);
		orchestratorData.setStatus(status);
		orchestratorData.setApplicationName(applicationName);
		orchestratorData.setJobRequest(jobRequest);
		if (existingOrchestratorData != null) {
			existingOrchestratorData.setExperiment_ID(experimentID);
			existingOrchestratorData.setUserName(userName);
			existingOrchestratorData.setState(state);
			existingOrchestratorData.setStatus(status);
			existingOrchestratorData.setGfacEPR(gfacEPR);
			existingOrchestratorData.setApplicationName(applicationName);
			existingOrchestratorData.setJobRequest(jobRequest);
			orchestratorData = em.merge(existingOrchestratorData);
		} else {
			em.persist(orchestratorData);
		}
		em.getTransaction().commit();
		em.close();
	}

}