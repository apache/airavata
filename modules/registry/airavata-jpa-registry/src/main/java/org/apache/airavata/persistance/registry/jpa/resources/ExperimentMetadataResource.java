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

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ExperimentMetadataResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentMetadataResource.class);
    private String expID;
    private String experimentName;
    private String description;
    private Timestamp submittedDate;
    private String executionUser;
    private GatewayResource gateway;
    private ProjectResource project;
    private boolean shareExp;

    public static Logger getLogger() {
        return logger;
    }

    public String getExpID() {
        return expID;
    }

    public void setExpID(String expID) {
        this.expID = expID;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(Timestamp submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getExecutionUser() {
        return executionUser;
    }

    public void setExecutionUser(String executionUser) {
        this.executionUser = executionUser;
    }

    public GatewayResource getGateway() {
        return gateway;
    }

    public void setGateway(GatewayResource gateway) {
        this.gateway = gateway;
    }

    public ProjectResource getProject() {
        return project;
    }

    public void setProject(ProjectResource project) {
        this.project = project;
    }

    public boolean isShareExp() {
        return shareExp;
    }

    public void setShareExp(boolean shareExp) {
        this.shareExp = shareExp;
    }

    public Resource create(ResourceType type) {
        switch (type) {
            case EXPERIMENT_CONFIG_DATA:
                ExperimentConfigDataResource configDataResource = new ExperimentConfigDataResource();
                configDataResource.setExMetadata(this);
                return configDataResource;
            case EXPERIMENT_SUMMARY:
                ExperimentSummaryResource summaryResource = new ExperimentSummaryResource();
                summaryResource.setExperimentMetadataResource(this);
                return summaryResource;
            case EXPERIMENT_INPUT:
                ExperimentInputResource exInputResource = new ExperimentInputResource();
                exInputResource.setExperimentMetadataResource(this);
                return exInputResource;
            case EXPERIMENT_OUTPUT:
                ExperimentOutputResource exOutputResouce = new ExperimentOutputResource();
                exOutputResouce.setExperimentMetadataResource(this);
                return exOutputResouce;
            case WORKFLOW_DATA:
                WorkflowDataResource workflowDataResource = new WorkflowDataResource();
                workflowDataResource.setExperimentID(expID);
                return workflowDataResource;
            case EXECUTION_ERROR:
                ExecutionErrorResource executionErrorResource = new ExecutionErrorResource();
                executionErrorResource.setMetadataResource(this);
                return executionErrorResource;
            case GFAC_JOB_DATA:
                GFacJobDataResource gFacJobDataResource = new GFacJobDataResource();
                gFacJobDataResource.setMetadataResource(this);
                return gFacJobDataResource;
            default:
                logger.error("Unsupported resource type for experiment metadata resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
            }
    }

    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case EXPERIMENT_CONFIG_DATA:
                generator = new QueryGenerator(EXPERIMENT_CONFIG_DATA);
                generator.setParameter(ExperimentConfigurationDataConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case EXPERIMENT_SUMMARY:
                generator = new QueryGenerator(EXPERIMENT_SUMMARY);
                generator.setParameter(ExperimentSummaryConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
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
            case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
                generator.setParameter(WorkflowDataConstants.WORKFLOW_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case GFAC_JOB_DATA:
                generator = new QueryGenerator(GFAC_JOB_DATA);
                generator.setParameter(GFacJobDataConstants.LOCAL_JOB_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            default:
                logger.error("Unsupported operation for experiment metadata resource "
                + "since there are no child resources generated by experiment metadata resource.. ",
                new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
        em.getTransaction().commit();
        em.close();
    }

    public Resource get(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case EXPERIMENT_CONFIG_DATA:
                generator = new QueryGenerator(EXPERIMENT_CONFIG_DATA);
                generator.setParameter(ExperimentConfigurationDataConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment_Configuration_Data exConfigData = (Experiment_Configuration_Data) q.getSingleResult();
                ExperimentConfigDataResource experimentConfigDataResource =
                        (ExperimentConfigDataResource)Utils.getResource(ResourceType.EXPERIMENT_CONFIG_DATA, exConfigData);
                em.getTransaction().commit();
                em.close();
                return experimentConfigDataResource;
            case EXPERIMENT_SUMMARY:
                generator = new QueryGenerator(EXPERIMENT_SUMMARY);
                generator.setParameter(ExperimentSummaryConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment_Summary exSummaryData = (Experiment_Summary) q.getSingleResult();
                ExperimentSummaryResource exSummary =
                        (ExperimentSummaryResource)Utils.getResource(ResourceType.EXPERIMENT_SUMMARY, exSummaryData);
                em.getTransaction().commit();
                em.close();
                return exSummary;
            case EXPERIMENT_INPUT:
                generator = new QueryGenerator(EXPERIMENT_INPUT);
                generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment_Input exInput = (Experiment_Input) q.getSingleResult();
                ExperimentInputResource experimentInput =
                        (ExperimentInputResource)Utils.getResource(ResourceType.EXPERIMENT_INPUT, exInput);
                em.getTransaction().commit();
                em.close();
                return experimentInput;
            case EXPERIMENT_OUTPUT:
                generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                generator.setParameter(ExperimentOutputConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment_Output exOutput = (Experiment_Output) q.getSingleResult();
                ExperimentOutputResource experimentOutput =
                        (ExperimentOutputResource)Utils.getResource(ResourceType.EXPERIMENT_OUTPUT, exOutput);
                em.getTransaction().commit();
                em.close();
                return experimentOutput;
            case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
//                generator.setParameter(WorkflowDataConstants.EXPERIMENT_ID, experimentID);
                generator.setParameter(WorkflowDataConstants.WORKFLOW_INSTANCE_ID, name);
                q = generator.selectQuery(em);
                Workflow_Data eworkflowData = (Workflow_Data)q.getSingleResult();
                WorkflowDataResource workflowDataResource = (WorkflowDataResource)Utils.getResource(ResourceType.WORKFLOW_DATA, eworkflowData);
                em.getTransaction().commit();
                em.close();
                return workflowDataResource;
            case GFAC_JOB_DATA:
                generator = new QueryGenerator(GFAC_JOB_DATA);
                generator.setParameter(GFacJobDataConstants.LOCAL_JOB_ID, name);
                q = generator.selectQuery(em);
                GFac_Job_Data gFacJobData = (GFac_Job_Data)q.getSingleResult();
                GFacJobDataResource gFacJobDataResource = (GFacJobDataResource)Utils.getResource(ResourceType.GFAC_JOB_DATA, gFacJobData);
                em.getTransaction().commit();
                em.close();
                return gFacJobDataResource;
            default:
                logger.error("Unsupported operation for experiment metadata resource "
                        + "since there are no child resources generated by experiment metadata resource.. ",
                        new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case EXPERIMENT_INPUT:
                generator = new QueryGenerator(EXPERIMENT_INPUT);
                generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, expID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Experiment_Input expInput = (Experiment_Input) result;
                        ExperimentInputResource experimentResource =
                                (ExperimentInputResource)Utils.getResource(ResourceType.EXPERIMENT_INPUT, expInput);
                        resourceList.add(experimentResource);
                    }
                }
                break;
            case EXPERIMENT_OUTPUT:
                generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                generator.setParameter(ExperimentOutputConstants.EXPERIMENT_ID, expID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Experiment_Output expOutput = (Experiment_Output) result;
                        ExperimentOutputResource experimentResource =
                                (ExperimentOutputResource)Utils.getResource(ResourceType.EXPERIMENT_INPUT, expOutput);
                        resourceList.add(experimentResource);
                    }
                }
                break;
            case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
//                generator.setParameter(WorkflowDataConstants.EXPERIMENT_ID, experimentID);
                Experiment_Metadata experiment_metadata = em.find(Experiment_Metadata.class, expID);
                generator.setParameter("experiment_metadata", experiment_metadata);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Workflow_Data workflowData = (Workflow_Data) result;
                        WorkflowDataResource workflowDataResource = (WorkflowDataResource)Utils.getResource(ResourceType.WORKFLOW_DATA, workflowData);
                        resourceList.add(workflowDataResource);
                    }
                }
                break;
            case EXECUTION_ERROR:
                generator = new QueryGenerator(EXECUTION_ERROR);
                generator.setParameter(ExecutionErrorConstants.EXPERIMENT_ID, expID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Execution_Error executionError = (Execution_Error)result;
                        ExecutionErrorResource executionErrorResource = (ExecutionErrorResource)Utils.getResource(ResourceType.EXECUTION_ERROR, executionError);
                        resourceList.add(executionErrorResource);
                    }
                }
                break;
            case GFAC_JOB_DATA:
                generator = new QueryGenerator(GFAC_JOB_DATA);
                generator.setParameter(GFacJobDataConstants.EXPERIMENT_ID, expID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GFac_Job_Data gFacJobData = (GFac_Job_Data)result;
                        GFacJobDataResource gFacJobDataResource = (GFacJobDataResource)Utils.getResource(ResourceType.GFAC_JOB_DATA, gFacJobData);
                        resourceList.add(gFacJobDataResource);
                    }
                }
                break;
            default:
                logger.error("Unsupported operation for experiment metadata resource "
                        + "since there are no child resources generated by experiment metadata resource.. ",
                        new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Experiment_Metadata existingExpMetaData = em.find(Experiment_Metadata.class, expID);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Experiment_Metadata experimentMetadata = new Experiment_Metadata();
        experimentMetadata.setExperiment_id(expID);
        experimentMetadata.setExperiment_name(experimentName);
        experimentMetadata.setShare_experiment(shareExp);
        experimentMetadata.setDescription(description);
        Gateway gatewayModel = em.find(Gateway.class, gateway.getGatewayName());
        experimentMetadata.setGateway(gatewayModel);
        Project projectModel = em.find(Project.class, project.getName());
        experimentMetadata.setProject(projectModel);
        experimentMetadata.setExecution_user(executionUser);
        experimentMetadata.setSubmitted_date(submittedDate);
        if (existingExpMetaData != null) {
            existingExpMetaData.setExperiment_id(expID);
            existingExpMetaData.setDescription(description);
            existingExpMetaData.setExperiment_name(experimentName);
            existingExpMetaData.setGateway(gatewayModel);
            existingExpMetaData.setExecution_user(executionUser);
            existingExpMetaData.setProject(projectModel);
            existingExpMetaData.setShare_experiment(shareExp);
            existingExpMetaData.setSubmitted_date(submittedDate);
            experimentMetadata = em.merge(existingExpMetaData);
        } else {
            em.persist(experimentMetadata);
        }
        em.getTransaction().commit();
        em.close();

    }

    public boolean isWorkflowInstancePresent(String workflowInstanceId){
        return isExists(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }

    public boolean isGFacJobPresent(String jobId){
        return isExists(ResourceType.GFAC_JOB_DATA, jobId);
    }

    public WorkflowDataResource getWorkflowInstance(String workflowInstanceId){
        return (WorkflowDataResource)get(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }

    public List<Resource> getGFacJobs(){
        return get(ResourceType.GFAC_JOB_DATA);
    }

    public List<WorkflowDataResource> getWorkflowInstances(){
        return getResourceList(get(ResourceType.WORKFLOW_DATA),WorkflowDataResource.class);
    }

    public WorkflowDataResource createWorkflowInstanceResource(String workflowInstanceID){
        WorkflowDataResource r=(WorkflowDataResource)create(ResourceType.WORKFLOW_DATA);
        r.setWorkflowInstanceID(workflowInstanceID);
        return r;
    }

    public GFacJobDataResource createGFacJob(String jobID){
        GFacJobDataResource r=(GFacJobDataResource)create(ResourceType.GFAC_JOB_DATA);
        r.setLocalJobID(jobID);
        return r;
    }

    public ExperimentMetadataResource createExperimentMetadata(){
        return (ExperimentMetadataResource)create(ResourceType.EXPERIMENT_METADATA);
    }

    public ExecutionErrorResource createExecutionError(){
        return (ExecutionErrorResource) create(ResourceType.EXECUTION_ERROR);
    }

    public void removeWorkflowInstance(String workflowInstanceId){
        remove(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }

    public List<ExecutionErrorResource> getExecutionErrors(String type, String experimentId, String workflowInstanceId, String nodeId, String gfacJobId){
        List<ExecutionErrorResource> resourceList = new ArrayList<ExecutionErrorResource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List<?> results;
        generator = new QueryGenerator(EXECUTION_ERROR);
        if (experimentId!=null){
            generator.setParameter(ExecutionErrorConstants.EXPERIMENT_ID, experimentId);
        }
        if (type!=null){
            generator.setParameter(ExecutionErrorConstants.SOURCE_TYPE, type);
        }
        if (workflowInstanceId!=null){
            generator.setParameter(ExecutionErrorConstants.WORKFLOW_ID, workflowInstanceId);
        }
        if (nodeId!=null){
            generator.setParameter(ExecutionErrorConstants.NODE_ID, nodeId);
        }
        if (gfacJobId!=null){
            generator.setParameter(ExecutionErrorConstants.GFAC_JOB_ID, gfacJobId);
        }
        q = generator.selectQuery(em);
        results = q.getResultList();
        if (results.size() != 0) {
            for (Object result : results) {
                Execution_Error executionError = (Execution_Error)result;
                ExecutionErrorResource executionErrorResource = (ExecutionErrorResource)Utils.getResource(ResourceType.EXECUTION_ERROR, executionError);
                resourceList.add(executionErrorResource);
            }
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }
}
