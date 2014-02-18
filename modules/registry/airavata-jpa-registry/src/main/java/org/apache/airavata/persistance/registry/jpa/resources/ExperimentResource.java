/*
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.apache.airavata.persistance.registry.jpa.resources;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.bouncycastle.jce.provider.JDKPSSSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentResource.class);
    private WorkerResource worker;
    private String expID;
    private Timestamp creationTime;
    private GatewayResource gateway;
    private ProjectResource project;
    private String expName;
    private String description;
    private String applicationId;
    private String applicationVersion;
    private String workflowTemplateId;
    private String workflowTemplateVersion;
    private String workflowExecutionId;

    /**
     *
     * @return  experiment ID
     */
    public String getExpID() {
        return expID;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getExpName() {
        return expName;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getWorkflowTemplateId() {
        return workflowTemplateId;
    }

    public void setWorkflowTemplateId(String workflowTemplateId) {
        this.workflowTemplateId = workflowTemplateId;
    }

    public String getWorkflowTemplateVersion() {
        return workflowTemplateVersion;
    }

    public void setWorkflowTemplateVersion(String workflowTemplateVersion) {
        this.workflowTemplateVersion = workflowTemplateVersion;
    }

    public String getWorkflowExecutionId() {
        return workflowExecutionId;
    }

    public void setWorkflowExecutionId(String workflowExecutionId) {
        this.workflowExecutionId = workflowExecutionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @return UnsupportedOperationException
     */
    public Resource create(ResourceType type) {
    	switch (type){
	        case EXPERIMENT_INPUT:
	        	ExperimentInputResource inputResource = new ExperimentInputResource();
	            inputResource.setExperimentResource(this);
	            return inputResource;
            case EXPERIMENT_OUTPUT:
                ExperimentOutputResource experimentOutputResource = new ExperimentOutputResource();
                experimentOutputResource.setExperimentResource(this);
                return experimentOutputResource;
            case WORKFLOW_NODE_DETAIL:
                WorkflowNodeDetailResource nodeDetailResource = new WorkflowNodeDetailResource();
                nodeDetailResource.setExperimentResource(this);
                return nodeDetailResource;
            case ERROR_DETAIL:
                ErrorDetailResource errorDetailResource = new ErrorDetailResource();
                errorDetailResource.setExperimentResource(this);
                return errorDetailResource;
            case STATUS:
                StatusResource statusResource = new StatusResource();
                statusResource.setExperimentResource(this);
                return statusResource;
            case CONFIG_DATA:
                ConfigDataResource configDataResource = new ConfigDataResource();
                configDataResource.setExperimentResource(this);
                return configDataResource;
            case COMPUTATIONAL_RESOURCE_SCHEDULING:
                ComputationSchedulingResource schedulingResource = new ComputationSchedulingResource();
                schedulingResource.setExperimentResource(this);
                return schedulingResource;
            case ADVANCE_INPUT_DATA_HANDLING:
                AdvanceInputDataHandlingResource dataHandlingResource = new AdvanceInputDataHandlingResource();
                dataHandlingResource.setExperimentResource(this);
                return dataHandlingResource;
            case ADVANCE_OUTPUT_DATA_HANDLING:
                AdvancedOutputDataHandlingResource outputDataHandlingResource = new AdvancedOutputDataHandlingResource();
                outputDataHandlingResource.setExperimentResource(this);
                return outputDataHandlingResource;
            case QOS_PARAM:
                QosParamResource qosParamResource = new QosParamResource();
                qosParamResource.setExperimentResource(this);
                return qosParamResource;
	        default:
                logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
	            throw new IllegalArgumentException("Unsupported resource type for experiment resource.");
	    }
    }

    /**
     *
     * @param type  child resource types
     * @param name name of the child resource
     * @return UnsupportedOperationException
     */
    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case EXPERIMENT_INPUT:
                generator = new QueryGenerator(EXPERIMENT_INPUT);
                generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case EXPERIMENT_OUTPUT:
                generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                generator.setParameter(ExperimentOutputConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case WORKFLOW_NODE_DETAIL:
                generator = new QueryGenerator(WORKFLOW_NODE_DETAIL);
                generator.setParameter(WorkflowNodeDetailsConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case ERROR_DETAIL:
                generator = new QueryGenerator(ERROR_DETAIL);
                generator.setParameter(ErrorDetailConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case CONFIG_DATA:
                generator = new QueryGenerator(CONFIG_DATA);
                generator.setParameter(ExperimentConfigurationDataConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case COMPUTATIONAL_RESOURCE_SCHEDULING:
                generator = new QueryGenerator(COMPUTATIONAL_RESOURCE_SCHEDULING);
                generator.setParameter(ComputationalResourceSchedulingConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case ADVANCE_INPUT_DATA_HANDLING:
                generator = new QueryGenerator(ADVANCE_INPUT_DATA_HANDLING);
                generator.setParameter(AdvancedInputDataHandlingConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case ADVANCE_OUTPUT_DATA_HANDLING:
                generator = new QueryGenerator(ADVANCE_OUTPUT_DATA_HANDLING);
                generator.setParameter(AdvancedOutputDataHandlingConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case QOS_PARAM:
                generator = new QueryGenerator(QOS_PARAMS);
                generator.setParameter(QosParamsConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            default:
                logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
                break;
        }
        em.getTransaction().commit();
        em.close();
    }

    /**
     *
     * @param type  child resource types
     * @param name name of the child resource
     * @return UnsupportedOperationException
     */
    public Resource get(ResourceType type, Object name) {
    	EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case EXPERIMENT_INPUT:
                generator = new QueryGenerator(EXPERIMENT_INPUT);
                generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment_Input experimentInput = (Experiment_Input)q.getSingleResult();
                ExperimentInputResource inputResource = (ExperimentInputResource)Utils.getResource(ResourceType.EXPERIMENT_INPUT, experimentInput);
                em.getTransaction().commit();
                em.close();
                return inputResource;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for experiment data resource.");
        }

    }

    /**
     * key should be the experiment ID
     * @param keys experiment ID
     * @return ExperimentResource
     */
    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator queryGenerator = new QueryGenerator(EXPERIMENT);
        queryGenerator.setParameter(ExperimentConstants.EXPERIMENT_ID, keys[0]);
        Query q = queryGenerator.selectQuery(em);
        Experiment experiment = (Experiment)q.getSingleResult();
        ExperimentResource experimentResource =
                (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
        em.getTransaction().commit();
        em.close();
        list.add(experimentResource);
        return list;

    }

    /**
     *
     * @param type  child resource types
     * @return UnsupportedOperationException
     */
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for experiment resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     * save experiment
     */
    public void save() {
//        EntityManager em = ResourceUtils.getEntityManager();
//        Experiment existingExp = em.find(Experiment.class, expID);
//        em.close();
//
//        em = ResourceUtils.getEntityManager();
//        em.getTransaction().begin();
//        Experiment experiment = new Experiment();
//        Project projectmodel = em.find(Project.class, project.getName());
//        experiment.setProject(projectmodel);
//        Users user = em.find(Users.class, getWorker().getUser());
//        Gateway gateway = em.find(Gateway.class, getGateway().getGatewayName());
//        experiment.setProject(projectmodel);
//        experiment.setExperiment_ID(getExpID());
//        experiment.setUser(user);
//        experiment.setGateway(gateway);
//        experiment.setSubmitted_date(submittedDate);
//        if(existingExp != null){
//            existingExp.setGateway(gateway);
//            existingExp.setProject(projectmodel);
//            existingExp.setUser(user);
//            existingExp.setSubmitted_date(submittedDate);
//            experiment = em.merge(existingExp);
//        } else{
//           em.merge(experiment);
//        }
//
//        em.getTransaction().commit();
//        em.close();
    }

    /**
     *
     * @param expID experiment ID
     */
    public void setExpID(String expID) {
		this.expID = expID;
	}

    /**
     *
     * @return gatewayResource
     */
    public GatewayResource getGateway() {
		return gateway;
	}

    /**
     *
     * @param gateway gateway
     */
    public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

    /**
     *
     * @return worker for the gateway
     */
    public WorkerResource getWorker() {
		return worker;
	}

    /**
     *
     * @param worker gateway worker
     */
    public void setWorker(WorkerResource worker) {
		this.worker = worker;
	}

    /**
     *
     * @return project
     */
    public ProjectResource getProject() {
		return project;
	}

    /**
     *
     * @param project  project
     */
    public void setProject(ProjectResource project) {
		this.project = project;
	}

//    public ExperimentDataResource getData(){
//    	if (isExists(ResourceType.EXPERIMENT_DATA, getExpID())){
//    		return (ExperimentDataResource) get(ResourceType.EXPERIMENT_DATA, getExpID());
//    	}else{
//    		ExperimentDataResource data = (ExperimentDataResource) create(ResourceType.EXPERIMENT_DATA);
//            data.save();
//			return data;
//    	}
//    }
}
