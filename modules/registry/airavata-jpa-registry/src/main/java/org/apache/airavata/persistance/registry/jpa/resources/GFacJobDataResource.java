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
import org.apache.airavata.persistance.registry.jpa.model.GFac_Job_Data;
import org.apache.airavata.persistance.registry.jpa.model.GFac_Job_Status;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GFacJobDataResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GFacJobDataResource.class);
    private ExperimentDataResource experimentDataResource;
    private WorkflowDataResource workflowDataResource;
    private String nodeID;
    private String applicationDescID;
    private String hostDescID;
    private String serviceDescID;
    private String jobData;
    private String localJobID;
    private Timestamp submittedTime;
    private Timestamp statusUpdateTime;
    private String status;
    private String metadata;

    public ExperimentDataResource getExperimentDataResource() {
        return experimentDataResource;
    }

    public WorkflowDataResource getWorkflowDataResource() {
        return workflowDataResource;
    }

    public String getNodeID() {
        return nodeID;
    }

    public String getApplicationDescID() {
        return applicationDescID;
    }

    public String getHostDescID() {
        return hostDescID;
    }

    public String getServiceDescID() {
        return serviceDescID;
    }

    public String getJobData() {
        return jobData;
    }

    public String getLocalJobID() {
        return localJobID;
    }

    public Timestamp getSubmittedTime() {
        return submittedTime;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setExperimentDataResource(ExperimentDataResource experimentDataResource) {
        this.experimentDataResource = experimentDataResource;
    }

    public void setWorkflowDataResource(WorkflowDataResource workflowDataResource) {
        this.workflowDataResource = workflowDataResource;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public void setApplicationDescID(String applicationDescID) {
        this.applicationDescID = applicationDescID;
    }

    public void setHostDescID(String hostDescID) {
        this.hostDescID = hostDescID;
    }

    public void setServiceDescID(String serviceDescID) {
        this.serviceDescID = serviceDescID;
    }

    public void setJobData(String jobData) {
        this.jobData = jobData;
    }

    public void setLocalJobID(String localJobID) {
        this.localJobID = localJobID;
    }

    public void setSubmittedTime(Timestamp submittedTime) {
        this.submittedTime = submittedTime;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public Resource create(ResourceType type) {
        switch (type){
            case GFAC_JOB_STATUS:
                GFacJobStatusResource gFacJobStatusResource = new GFacJobStatusResource();
                gFacJobStatusResource.setLocalJobID(localJobID);
                gFacJobStatusResource.setgFacJobDataResource(this);
                return gFacJobStatusResource;
            default:
                logger.error("Unsupported resource type for GFac Job status resource" ,new UnsupportedOperationException() );
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for GFac Job data resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for GFac Job data resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case GFAC_JOB_STATUS:
                generator = new QueryGenerator(GFAC_JOB_STATUS);
                generator.setParameter(GFacJobStatusConstants.LOCAL_JOB_ID, localJobID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        GFac_Job_Status gFacJobStatus = (GFac_Job_Status) result;
                        GFacJobStatusResource gFacJobStatusResource =
                                (GFacJobStatusResource)Utils.getResource(ResourceType.GFAC_JOB_STATUS, gFacJobStatus);
                        resourceList.add(gFacJobStatusResource);
                    }
                }
                break;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for gfac job data resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gfac job data resource.");
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        GFac_Job_Data existingGfacJobData = em.find(GFac_Job_Data.class, localJobID);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        GFac_Job_Data gFacJobData = new GFac_Job_Data();
        Experiment_Data experiment_data = em.find(Experiment_Data.class, experimentDataResource.getExperimentID());
        gFacJobData.setExperiment_data(experiment_data);
        gFacJobData.setExperiment_ID(experimentDataResource.getExperimentID());
        Workflow_Data workflow_data = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
        gFacJobData.setWorkflow_Data(workflow_data);
        gFacJobData.setWorkflow_instanceID(workflowDataResource.getWorkflowInstanceID());
        gFacJobData.setNode_id(nodeID);
        gFacJobData.setApplication_descriptor_ID(applicationDescID);
        gFacJobData.setLocal_Job_ID(localJobID);
        gFacJobData.setService_descriptor_ID(serviceDescID);
        gFacJobData.setHost_descriptor_ID(hostDescID);
        gFacJobData.setJob_data(jobData);
        gFacJobData.setSubmitted_time(submittedTime);
        gFacJobData.setStatus_update_time(statusUpdateTime);
        gFacJobData.setStatus(status);
        gFacJobData.setMetadata(metadata);
        if(existingGfacJobData != null){
            Experiment_Data experiment_data1 = em.find(Experiment_Data.class, experimentDataResource.getExperimentID());
            existingGfacJobData.setExperiment_data(experiment_data1);
            existingGfacJobData.setExperiment_ID(experimentDataResource.getExperimentID());
            Workflow_Data workflow_data1 = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
            existingGfacJobData.setWorkflow_Data(workflow_data1);
            existingGfacJobData.setWorkflow_instanceID(workflowDataResource.getWorkflowInstanceID());
            existingGfacJobData.setNode_id(nodeID);
            existingGfacJobData.setApplication_descriptor_ID(applicationDescID);
            existingGfacJobData.setLocal_Job_ID(localJobID);
            existingGfacJobData.setService_descriptor_ID(serviceDescID);
            existingGfacJobData.setHost_descriptor_ID(hostDescID);
            existingGfacJobData.setJob_data(jobData);
            existingGfacJobData.setSubmitted_time(submittedTime);
            existingGfacJobData.setStatus_update_time(statusUpdateTime);
            existingGfacJobData.setStatus(status);
            existingGfacJobData.setMetadata(metadata);
            gFacJobData = em.merge(existingGfacJobData);
        }  else {
            em.persist(gFacJobData);
        }
        em.getTransaction().commit();
        em.close();
    }

}
