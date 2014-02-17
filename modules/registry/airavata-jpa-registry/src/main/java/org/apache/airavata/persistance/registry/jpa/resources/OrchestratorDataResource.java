///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.persistance.registry.jpa.resources;
//
//import java.sql.Timestamp;
//import java.util.List;
//
//import javax.persistence.EntityManager;
//
//import org.apache.airavata.persistance.registry.jpa.Resource;
//import org.apache.airavata.persistance.registry.jpa.ResourceType;
//import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
//import org.apache.airavata.persistance.registry.jpa.model.Orchestrator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class OrchestratorDataResource extends AbstractResource {
//
//	private final static Logger log = LoggerFactory.getLogger(OrchestratorDataResource.class);
//	private String experimentID;
//	private String userName;
//	private String applicationName;
//	private String status;
//	private String state;
//	private String gfacEPR;
//	private String jobRequest;
//	private GatewayResource gateway;
//    private Timestamp submittedTime;
//    private Timestamp statusUpdateTime;
//
//	public String getExperimentID() {
//		return experimentID;
//	}
//
//	public String getUserName() {
//		return userName;
//	}
//
//	public void setExperimentID(String experimentID) {
//		this.experimentID = experimentID;
//	}
//
//	public void setUserName(String userName) {
//		this.userName = userName;
//	}
//
//	public String getApplicationName() {
//		return applicationName;
//	}
//
//	public void setApplicationName(String applicationName) {
//		this.applicationName = applicationName;
//	}
//
//	public String getStatus() {
//		return status;
//	}
//
//	public void setStatus(String status) {
//		this.status = status;
//	}
//
//	public String getState() {
//		return state;
//	}
//
//	public void setState(String state) {
//		this.state = state;
//	}
//
//	public String getGfacEPR() {
//		return gfacEPR;
//	}
//
//	public void setGfacEPR(String gfacEPR) {
//		this.gfacEPR = gfacEPR;
//	}
//
//	public String getJobRequest() {
//		return jobRequest;
//	}
//
//	public void setJobRequest(String jobRequest) {
//		this.jobRequest = jobRequest;
//	}
//
//	public GatewayResource getGateway() {
//		return gateway;
//	}
//
//	public void setGateway(GatewayResource gateway) {
//		this.gateway = gateway;
//	}
//
//	public Timestamp getSubmittedTime() {
//		return submittedTime;
//	}
//
//	public void setSubmittedTime(Timestamp submittedTime) {
//		this.submittedTime = submittedTime;
//	}
//
//	public Timestamp getStatusUpdateTime() {
//		return statusUpdateTime;
//	}
//
//	public void setStatusUpdateTime(Timestamp statusUpdateTime) {
//		this.statusUpdateTime = statusUpdateTime;
//	}
//
//	@Override
//	public Resource create(ResourceType type) {
//        log.error("Unsupported resource type for orchestrator resource.", new IllegalArgumentException());
//        throw new IllegalArgumentException("Unsupported resource type for orchestrator resource.");
//    }
//
//	@Override
//	public void remove(ResourceType type, Object name) {
//		   log.error("Unsupported operation to remove orchestrator data.", new UnsupportedOperationException());
//	       throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public Resource get(ResourceType type, Object name) {
//        log.error("Unsupported resource type for orchestrator data.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public List<Resource> get(ResourceType type) {
//        log.error("Unsupported resource type for orchestrator data.", new UnsupportedOperationException());
//        throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public void save() {
//		EntityManager em = ResourceUtils.getEntityManager();
//		Orchestrator existingOrchestrator = em.find(Orchestrator.class,
//				experimentID);
//		em.close();
//		em = ResourceUtils.getEntityManager();
//		em.getTransaction().begin();
//		Orchestrator orchestrator = new Orchestrator();
//		orchestrator.setExperiment_ID(experimentID);
//		orchestrator.setUserName(userName);
//		orchestrator.setGfacEPR(gfacEPR);
//		orchestrator.setState(state);
//		orchestrator.setStatus(status);
//		orchestrator.setApplicationName(applicationName);
//		orchestrator.setJobRequest(jobRequest);
//		orchestrator.setSubmittedTime(submittedTime);
//		orchestrator.setStatusUpdateTime(statusUpdateTime);
//		if (existingOrchestrator != null) {
//			existingOrchestrator.setExperiment_ID(experimentID);
//			existingOrchestrator.setUserName(userName);
//			existingOrchestrator.setState(state);
//			existingOrchestrator.setStatus(status);
//			existingOrchestrator.setGfacEPR(gfacEPR);
//			existingOrchestrator.setApplicationName(applicationName);
//			existingOrchestrator.setJobRequest(jobRequest);
//			existingOrchestrator.setSubmittedTime(submittedTime);
//			existingOrchestrator.setStatusUpdateTime(statusUpdateTime);
//			orchestrator = em.merge(existingOrchestrator);
//		} else {
//			em.persist(orchestrator);
//		}
//		em.getTransaction().commit();
//		em.close();
//	}
//
//}