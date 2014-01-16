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
	private int orchestratorID;
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

	public int getOrchestratorID() {
		return orchestratorID;
	}

	public String getUserName() {
		return userName;
	}

	public void setExperimentID(String experimentID) {
		this.experimentID = experimentID;
	}

	public void setOrchestratorID(int orchestratorID) {
		this.orchestratorID = orchestratorID;
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
		 if (type == ResourceType.ORCHESTRATOR_DATA) {
	            OrchestratorDataResource orchestratorResource = new OrchestratorDataResource();
	            orchestratorResource.setExperimentID(experimentID);
	            orchestratorResource.setUserName(userName);
	            orchestratorResource.setState(state);
	            orchestratorResource.setStatus(status);
	            orchestratorResource.setGfacEPR(gfacEPR);
	            orchestratorResource.setApplicationName(applicationName);
	            orchestratorResource.setJobRequest(jobRequest);
	            return orchestratorResource;
	        } else {
	            log.error("Unsupported resource type for orchestrator resource.", new IllegalArgumentException());
	            throw new IllegalArgumentException("Unsupported resource type for orchestrator resource.");
	        }
	}

	@Override
	public void remove(ResourceType type, Object name) {
		   log.error("Unsupported operation to remove orchestrator data.", new UnsupportedOperationException());
	       throw new UnsupportedOperationException();
	}

	@Override
	public Resource get(ResourceType type, Object name) {
		  EntityManager em = ResourceUtils.getEntityManager();
	      em.getTransaction().begin();
	      QueryGenerator generator;
	      Query q;
	        switch (type) {
	            case ORCHESTRATOR_DATA:
	                generator = new QueryGenerator(ORCHESTRATORDATA);
	                generator.setParameter(OrchestratorDataConstants.EXPERIMENT_ID, name.toString());
	                q = generator.selectQuery(em);
	                Orchestrator_Data orchestrator_data = (Orchestrator_Data)q.getSingleResult();
	                OrchestratorDataResource orchestratorDataResource = (OrchestratorDataResource)Utils.getResource(ResourceType.ORCHESTRATOR_DATA, orchestrator_data);
	                em.getTransaction().commit();
	                em.close();
	                return orchestratorDataResource;
	            default:
	                em.getTransaction().commit();
	                em.close();
	                log.error("Unsupported resource type for node data resource.", new IllegalArgumentException());
	                throw new IllegalArgumentException("Unsupported resource type for node data resource.");
	        }
	}
	public List<Resource> getDataWithStatus(ResourceType type,Object name) {
		 List<Resource> resourceList = new ArrayList<Resource>();

	        if (type == ResourceType.ORCHESTRATOR_DATA) {
	            EntityManager em = ResourceUtils.getEntityManager();
	            em.getTransaction().begin();
	        	QueryGenerator generator = new QueryGenerator(ORCHESTRATORDATA);
	        	generator.setParameter(OrchestratorDataConstants.STATUS, name);
	        	Query q = generator.selectQuery(em);
	            List<?> results = q.getResultList();
	            if (results.size() != 0) {
	                for (Object result : results) {
	                	Orchestrator_Data orchestratorData = (Orchestrator_Data) result;
	                	OrchestratorDataResource orchestratorDataResource = (OrchestratorDataResource)
	                            Utils.getResource(ResourceType.ORCHESTRATOR_DATA, orchestratorData);
	                    resourceList.add(orchestratorDataResource);
	                }
	            }
	            em.getTransaction().commit();
	            em.close();
	        } else {
	            log.error("Unsupported resource type for orchestrator data resource.", new IllegalArgumentException());
	            throw new IllegalArgumentException("Unsupported resource type for orchestrator data resource.");
	        }
	        return resourceList;
	}
	
	@Override
	public List<Resource> get(ResourceType type) {
		 List<Resource> resourceList = new ArrayList<Resource>();

	        if (type == ResourceType.ORCHESTRATOR_DATA) {
	            EntityManager em = ResourceUtils.getEntityManager();
	            em.getTransaction().begin();
	        	QueryGenerator generator = new QueryGenerator(ORCHESTRATORDATA);
	        	generator.setParameter(OrchestratorDataConstants.EXPERIMENT_ID, experimentID);
	        	Query q = generator.selectQuery(em);
	            List<?> results = q.getResultList();
	            if (results.size() != 0) {
	                for (Object result : results) {
	                	Orchestrator_Data orchestratorData = (Orchestrator_Data) result;
	                	OrchestratorDataResource orchestratorDataResource = (OrchestratorDataResource)
	                            Utils.getResource(ResourceType.ORCHESTRATOR_DATA, orchestratorData);
	                    resourceList.add(orchestratorDataResource);
	                }
	            }
	            em.getTransaction().commit();
	            em.close();
	        } else {
	            log.error("Unsupported resource type for orchestrator data resource.", new IllegalArgumentException());
	            throw new IllegalArgumentException("Unsupported resource type for orchestrator data resource.");
	        }
	        return resourceList;
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