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
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Data;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Metadata;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class ExperimentDataResource extends AbstractResource{

    private String experimentID;
    private String expName;
    private String userName;

    public String getExperimentID() {
        return experimentID;
    }

    public String getExpName() {
        return expName;
    }

    public String getUserName() {
        return userName;
    }

    public void setExperimentID(String experimentID) {
        this.experimentID = experimentID;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Resource create(ResourceType type) {
        switch (type){
            case WORKFLOW_DATA:
                WorkflowDataResource workflowDataResource = new WorkflowDataResource();
                workflowDataResource.setExperimentID(experimentID);
                return workflowDataResource;
            case EXPERIMENT_METADATA:
                ExperimentMetadataResource experimentMetadataResource = new ExperimentMetadataResource();
                experimentMetadataResource.setExpID(experimentID);
                return experimentMetadataResource;
            default:
                throw new IllegalArgumentException("Unsupported resource type for experiment data resource.");
        }

    }

    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
                generator.setParameter(WorkflowDataConstants.EXPERIMENT_ID, experimentID);
                generator.setParameter(WorkflowDataConstants.WORKFLOW_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case EXPERIMENT_METADATA:
                generator = new QueryGenerator(EXPERIMENT_METADATA);
                generator.setParameter(ExperimentDataConstants.EXPERIMENT_ID, experimentID);
                generator.setParameter(ExperimentDataConstants.METADATA, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            default:
                break;
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
            case EXPERIMENT_METADATA:
                generator = new QueryGenerator(EXPERIMENT_METADATA);
                generator.setParameter(ExperimentDataConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment_Metadata expMetadata = (Experiment_Metadata)q.getSingleResult();
                ExperimentMetadataResource experimentMetadataResource = (ExperimentMetadataResource)Utils.getResource(ResourceType.EXPERIMENT_METADATA, expMetadata);
                em.getTransaction().commit();
                em.close();
                return experimentMetadataResource;
            default:
                em.getTransaction().commit();
                em.close();
                throw new IllegalArgumentException("Unsupported resource type for experiment data resource.");
        }
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List<?> results;
        switch (type){
            case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
//                generator.setParameter(WorkflowDataConstants.EXPERIMENT_ID, experimentID);
                Experiment_Data experiment_data = em.find(Experiment_Data.class, experimentID);
                generator.setParameter("experiment_data", experiment_data);
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
            case EXPERIMENT_METADATA:
                generator = new QueryGenerator(EXPERIMENT_METADATA);
                generator.setParameter(ExperimentDataConstants.EXPERIMENT_ID, experimentID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Experiment_Metadata expMetadata = (Experiment_Metadata) result;
                        ExperimentMetadataResource experimentMetadataResource = (ExperimentMetadataResource)Utils.getResource(ResourceType.EXPERIMENT_METADATA, expMetadata);
                        resourceList.add(experimentMetadataResource);
                    }
                }
                break;
            default:
                em.getTransaction().commit();
                em.close();
                throw new IllegalArgumentException("Unsupported resource type for experiment data resource.");
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Experiment_Data existingExpData = em.find(Experiment_Data.class, experimentID);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Experiment_Data experimentData = new Experiment_Data();
        experimentData.setExperiment_ID(experimentID);
        experimentData.setName(expName);
        experimentData.setUsername(userName);
        if(existingExpData != null){
            existingExpData.setName(expName);
            existingExpData.setUsername(userName);
            experimentData = em.merge(existingExpData);
        } else{
            em.persist(experimentData);
        }
        em.getTransaction().commit();
        em.close();

    }
    
    public boolean isWorkflowInstancePresent(String workflowInstanceId){
		return isExists(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }
    
    public boolean isExperimentMetadataPresent(){
		return isExists(ResourceType.EXPERIMENT_METADATA, getExperimentID());
    }
    
    public WorkflowDataResource getWorkflowInstance(String workflowInstanceId){
    	return (WorkflowDataResource)get(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }
    
    public ExperimentMetadataResource getExperimentMetadata(){
    	return (ExperimentMetadataResource)get(ResourceType.EXPERIMENT_METADATA,getExperimentID());
    }
    
    public List<WorkflowDataResource> getWorkflowInstances(){
    	return getResourceList(get(ResourceType.WORKFLOW_DATA),WorkflowDataResource.class);
    }
    
    public WorkflowDataResource createWorkflowInstanceResource(String workflowInstanceID){
    	WorkflowDataResource r=(WorkflowDataResource)create(ResourceType.WORKFLOW_DATA);
    	r.setWorkflowInstanceID(workflowInstanceID);
    	return r;
    }
    
    public ExperimentMetadataResource createExperimentMetadata(){
    	return (ExperimentMetadataResource)create(ResourceType.EXPERIMENT_METADATA);
    }
    
    public void removeWorkflowInstance(String workflowInstanceId){
    	remove(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }
    
    public void removeExperimentMetadata(){
    	remove(ResourceType.EXPERIMENT_METADATA,getExperimentID());
    }
}
