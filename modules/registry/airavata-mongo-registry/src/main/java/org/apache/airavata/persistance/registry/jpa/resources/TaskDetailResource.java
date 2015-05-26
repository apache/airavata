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
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TaskDetailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(TaskDetailResource.class);
    private String taskId;
    private WorkflowNodeDetailResource workflowNodeDetailResource;
    private Timestamp creationTime;
    private String applicationId;
    private String applicationVersion;
    private String applicationDeploymentId;
    private boolean enableEmailNotifications;

    public boolean isEnableEmailNotifications() {
        return enableEmailNotifications;
    }

    public void setEnableEmailNotifications(boolean enableEmailNotifications) {
        this.enableEmailNotifications = enableEmailNotifications;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public WorkflowNodeDetailResource getWorkflowNodeDetailResource() {
        return workflowNodeDetailResource;
    }

    public void setWorkflowNodeDetailResource(WorkflowNodeDetailResource workflowNodeDetailResource) {
        this.workflowNodeDetailResource = workflowNodeDetailResource;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
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

    
    public Resource create(ResourceType type) throws RegistryException{
       switch (type){
           case ERROR_DETAIL:
               ErrorDetailResource errorDetailResource = new ErrorDetailResource();
               errorDetailResource.setTaskDetailResource(this);
               return errorDetailResource;
           case NOTIFICATION_EMAIL:
               NotificationEmailResource emailResource = new NotificationEmailResource();
               emailResource.setTaskDetailResource(this);
               return emailResource;
           case APPLICATION_INPUT:
               ApplicationInputResource applicationInputResource = new ApplicationInputResource();
               applicationInputResource.setTaskDetailResource(this);
               return applicationInputResource;
           case APPLICATION_OUTPUT:
               ApplicationOutputResource applicationOutputResource = new ApplicationOutputResource();
               applicationOutputResource.setTaskDetailResource(this);
               return applicationOutputResource;
           case JOB_DETAIL:
               JobDetailResource jobDetailResource = new JobDetailResource();
               jobDetailResource.setTaskDetailResource(this);
               return jobDetailResource;
           case DATA_TRANSFER_DETAIL:
               DataTransferDetailResource dataTransferDetailResource = new DataTransferDetailResource();
               dataTransferDetailResource.setTaskDetailResource(this);
               return dataTransferDetailResource;
           case STATUS:
               StatusResource statusResource = new StatusResource();
               statusResource.setTaskDetailResource(this);
               return statusResource;
           case COMPUTATIONAL_RESOURCE_SCHEDULING:
               ComputationSchedulingResource schedulingResource = new ComputationSchedulingResource();
               schedulingResource.setTaskDetailResource(this);
               return schedulingResource;
           case ADVANCE_INPUT_DATA_HANDLING:
               AdvanceInputDataHandlingResource inputDataHandlingResource = new AdvanceInputDataHandlingResource();
               inputDataHandlingResource.setTaskDetailResource(this);
               return inputDataHandlingResource;
           case ADVANCE_OUTPUT_DATA_HANDLING:
               AdvancedOutputDataHandlingResource outputDataHandlingResource = new AdvancedOutputDataHandlingResource();
               outputDataHandlingResource.setTaskDetailResource(this);
               return outputDataHandlingResource;
           case QOS_PARAM:
               QosParamResource qosParamResource = new QosParamResource();
               qosParamResource.setTaskDetailResource(this);
               return qosParamResource;
           default:
               logger.error("Unsupported resource type for task detail resource.", new IllegalArgumentException());
               throw new IllegalArgumentException("Unsupported resource type for task detail resource.");
       }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case APPLICATION_INPUT:
                    generator = new QueryGenerator(APPLICATION_INPUT);
                    generator.setParameter(ApplicationInputConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case APPLICATION_OUTPUT:
                    generator = new QueryGenerator(APPLICATION_OUTPUT);
                    generator.setParameter(ApplicationOutputConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case JOB_DETAIL:
                    generator = new QueryGenerator(JOB_DETAIL);
                    generator.setParameter(JobDetailConstants.TASK_ID, taskId);
                    generator.setParameter(JobDetailConstants.JOB_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case DATA_TRANSFER_DETAIL:
                    generator = new QueryGenerator(DATA_TRANSFER_DETAIL);
                    generator.setParameter(DataTransferDetailConstants.TRANSFER_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TASK_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.TASK.toString());
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    generator = new QueryGenerator(COMPUTATIONAL_RESOURCE_SCHEDULING);
                    generator.setParameter(ComputationalResourceSchedulingConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case ADVANCE_INPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_INPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedInputDataHandlingConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_OUTPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedOutputDataHandlingConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case QOS_PARAM:
                    generator = new QueryGenerator(QOS_PARAMS);
                    generator.setParameter(QosParamsConstants.TASK_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for task detail resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    ErrorDetail errorDetail = (ErrorDetail) q.getSingleResult();
                    ErrorDetailResource errorDetailResource = (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                    em.getTransaction().commit();
                    em.close();
                    return errorDetailResource;
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    Notification_Email notificationEmail = (Notification_Email) q.getSingleResult();
                    NotificationEmailResource emailResource = (NotificationEmailResource) Utils.getResource(ResourceType.NOTIFICATION_EMAIL, notificationEmail);
                    em.getTransaction().commit();
                    em.close();
                    return emailResource;
                case APPLICATION_INPUT:
                    generator = new QueryGenerator(APPLICATION_INPUT);
                    generator.setParameter(ApplicationInputConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    ApplicationInput applicationInput = (ApplicationInput) q.getSingleResult();
                    ApplicationInputResource inputResource = (ApplicationInputResource) Utils.getResource(ResourceType.APPLICATION_INPUT, applicationInput);
                    em.getTransaction().commit();
                    em.close();
                    return inputResource;
                case APPLICATION_OUTPUT:
                    generator = new QueryGenerator(APPLICATION_OUTPUT);
                    generator.setParameter(ApplicationOutputConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    ApplicationOutput applicationOutput = (ApplicationOutput) q.getSingleResult();
                    ApplicationOutputResource outputResource = (ApplicationOutputResource) Utils.getResource(ResourceType.APPLICATION_OUTPUT, applicationOutput);
                    em.getTransaction().commit();
                    em.close();
                    return outputResource;
                case JOB_DETAIL:
                    generator = new QueryGenerator(JOB_DETAIL);
                    generator.setParameter(JobDetailConstants.JOB_ID, name);
                    generator.setParameter(JobDetailConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    JobDetail jobDetail = (JobDetail) q.getSingleResult();
                    JobDetailResource jobDetailResource = (JobDetailResource) Utils.getResource(ResourceType.JOB_DETAIL, jobDetail);
                    em.getTransaction().commit();
                    em.close();
                    return jobDetailResource;
                case DATA_TRANSFER_DETAIL:
                    generator = new QueryGenerator(DATA_TRANSFER_DETAIL);
                    generator.setParameter(DataTransferDetailConstants.TRANSFER_ID, name);
                    q = generator.selectQuery(em);
                    DataTransferDetail transferDetail = (DataTransferDetail) q.getSingleResult();
                    DataTransferDetailResource transferDetailResource = (DataTransferDetailResource) Utils.getResource(ResourceType.DATA_TRANSFER_DETAIL, transferDetail);
                    em.getTransaction().commit();
                    em.close();
                    return transferDetailResource;
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TASK_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.TASK.toString());
                    q = generator.selectQuery(em);
                    Status status = (Status) q.getSingleResult();
                    StatusResource statusResource = (StatusResource) Utils.getResource(ResourceType.STATUS, status);
                    em.getTransaction().commit();
                    em.close();
                    return statusResource;
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    generator = new QueryGenerator(COMPUTATIONAL_RESOURCE_SCHEDULING);
                    generator.setParameter(ComputationalResourceSchedulingConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    Computational_Resource_Scheduling resourceScheduling = (Computational_Resource_Scheduling) q.getSingleResult();
                    ComputationSchedulingResource schedulingResource = (ComputationSchedulingResource) Utils.getResource(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, resourceScheduling);
                    em.getTransaction().commit();
                    em.close();
                    return schedulingResource;
                case ADVANCE_INPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_INPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedInputDataHandlingConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    AdvancedInputDataHandling dataHandling = (AdvancedInputDataHandling) q.getSingleResult();
                    AdvanceInputDataHandlingResource inputDataHandlingResource = (AdvanceInputDataHandlingResource) Utils.getResource(ResourceType.ADVANCE_INPUT_DATA_HANDLING, dataHandling);
                    em.getTransaction().commit();
                    em.close();
                    return inputDataHandlingResource;
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_OUTPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedOutputDataHandlingConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    AdvancedOutputDataHandling outputDataHandling = (AdvancedOutputDataHandling) q.getSingleResult();
                    AdvancedOutputDataHandlingResource outputDataHandlingResource = (AdvancedOutputDataHandlingResource) Utils.getResource(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, outputDataHandling);
                    em.getTransaction().commit();
                    em.close();
                    return outputDataHandlingResource;
                case QOS_PARAM:
                    generator = new QueryGenerator(QOS_PARAMS);
                    generator.setParameter(QosParamsConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    QosParam qosParam = (QosParam) q.getSingleResult();
                    QosParamResource qosParamResource = (QosParamResource) Utils.getResource(ResourceType.QOS_PARAM, qosParam);
                    em.getTransaction().commit();
                    em.close();
                    return qosParamResource;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for workflow node resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for workflow node resource.");
            }
        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ErrorDetail errorDetail = (ErrorDetail) result;
                            ErrorDetailResource errorDetailResource =
                                    (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                            resourceList.add(errorDetailResource);
                        }
                    }
                    break;
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Notification_Email notificationEmail = (Notification_Email) result;
                            NotificationEmailResource emailResource =
                                    (NotificationEmailResource) Utils.getResource(ResourceType.NOTIFICATION_EMAIL, notificationEmail);
                            resourceList.add(emailResource);
                        }
                    }
                    break;
                case APPLICATION_INPUT:
                    generator = new QueryGenerator(APPLICATION_INPUT);
                    generator.setParameter(ApplicationInputConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ApplicationInput applicationInput = (ApplicationInput) result;
                            ApplicationInputResource inputResource =
                                    (ApplicationInputResource) Utils.getResource(ResourceType.APPLICATION_INPUT, applicationInput);
                            resourceList.add(inputResource);
                        }
                    }
                    break;
                case APPLICATION_OUTPUT:
                    generator = new QueryGenerator(APPLICATION_OUTPUT);
                    generator.setParameter(ApplicationOutputConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ApplicationOutput applicationOutput = (ApplicationOutput) result;
                            ApplicationOutputResource outputResource =
                                    (ApplicationOutputResource) Utils.getResource(ResourceType.APPLICATION_OUTPUT, applicationOutput);
                            resourceList.add(outputResource);
                        }
                    }
                    break;
                case JOB_DETAIL:
                    generator = new QueryGenerator(JOB_DETAIL);
                    generator.setParameter(JobDetailConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            JobDetail jobDetail = (JobDetail) result;
                            JobDetailResource jobDetailResource =
                                    (JobDetailResource) Utils.getResource(ResourceType.JOB_DETAIL, jobDetail);
                            resourceList.add(jobDetailResource);
                        }
                    }
                    break;
                case DATA_TRANSFER_DETAIL:
                    generator = new QueryGenerator(DATA_TRANSFER_DETAIL);
                    generator.setParameter(DataTransferDetailConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            DataTransferDetail transferDetail = (DataTransferDetail) result;
                            DataTransferDetailResource transferDetailResource =
                                    (DataTransferDetailResource) Utils.getResource(ResourceType.DATA_TRANSFER_DETAIL, transferDetail);
                            resourceList.add(transferDetailResource);
                        }
                    }
                    break;
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Status status = (Status) result;
                            StatusResource statusResource =
                                    (StatusResource) Utils.getResource(ResourceType.STATUS, status);
                            resourceList.add(statusResource);
                        }
                    }
                    break;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for workflow node details resource.", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            TaskDetail taskDetail = em.find(TaskDetail.class, taskId);
            em.close();
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowNodeDetail workflowNodeDetail = em.find(WorkflowNodeDetail.class, workflowNodeDetailResource.getNodeInstanceId());
            if (taskDetail != null) {
            	updateTaskDetail(taskDetail, workflowNodeDetail);
                em.merge(taskDetail);
            } else {
                taskDetail = new TaskDetail();
                updateTaskDetail(taskDetail, workflowNodeDetail);                
                em.persist(taskDetail);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

	private void updateTaskDetail(TaskDetail taskDetail,
			WorkflowNodeDetail workflowNodeDetail) {
		taskDetail.setTaskId(taskId);
		taskDetail.setNodeDetail(workflowNodeDetail);
		taskDetail.setNodeId(workflowNodeDetailResource.getNodeInstanceId());
		taskDetail.setCreationTime(creationTime);
		taskDetail.setAppId(applicationId);
		taskDetail.setAppVersion(applicationVersion);
        taskDetail.setAllowNotification(enableEmailNotifications);
		taskDetail.setApplicationDeploymentId(getApplicationDeploymentId());
	}

    public List<ApplicationInputResource> getApplicationInputs() throws RegistryException{
        List<ApplicationInputResource> applicationInputResources = new ArrayList<ApplicationInputResource>();
        List<Resource> resources = get(ResourceType.APPLICATION_INPUT);
        for (Resource resource : resources) {
            ApplicationInputResource inputResource = (ApplicationInputResource) resource;
            applicationInputResources.add(inputResource);
        }
        return applicationInputResources;
    }

    public List<ApplicationOutputResource> getApplicationOutputs() throws RegistryException{
        List<ApplicationOutputResource> outputResources = new ArrayList<ApplicationOutputResource>();
        List<Resource> resources = get(ResourceType.APPLICATION_OUTPUT);
        for (Resource resource : resources) {
            ApplicationOutputResource outputResource = (ApplicationOutputResource) resource;
            outputResources.add(outputResource);
        }
        return outputResources;
    }

    public StatusResource getTaskStatus() throws RegistryException{
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource taskStatus = (StatusResource) resource;
            if(taskStatus.getStatusType().equals(StatusType.TASK.toString())){
                if (taskStatus.getState() == null || taskStatus.getState().equals("") ){
                    taskStatus.setState("UNKNOWN");
                }
                return taskStatus;
            }
        }
        return null;
    }

    public List<JobDetailResource> getJobDetailList() throws RegistryException{
        List<JobDetailResource> jobDetailResources = new ArrayList<JobDetailResource>();
        List<Resource> resources = get(ResourceType.JOB_DETAIL);
        for (Resource resource : resources) {
            JobDetailResource jobDetailResource = (JobDetailResource) resource;
            jobDetailResources.add(jobDetailResource);
        }
        return jobDetailResources;
    }

    public List<DataTransferDetailResource> getDataTransferDetailList() throws RegistryException{
        List<DataTransferDetailResource> transferDetails = new ArrayList<DataTransferDetailResource>();
        List<Resource> resources = get(ResourceType.DATA_TRANSFER_DETAIL);
        for (Resource resource : resources) {
            DataTransferDetailResource transferDetailResource = (DataTransferDetailResource) resource;
            transferDetails.add(transferDetailResource);
        }
        return transferDetails;
    }

    public List<ErrorDetailResource> getErrorDetailList() throws RegistryException{
        List<ErrorDetailResource> errorDetailResources = new ArrayList<ErrorDetailResource>();
        List<Resource> resources = get(ResourceType.ERROR_DETAIL);
        for (Resource resource : resources) {
            ErrorDetailResource errorDetailResource = (ErrorDetailResource) resource;
            errorDetailResources.add(errorDetailResource);
        }
        return errorDetailResources;
    }

    public ComputationSchedulingResource getComputationScheduling (String taskId) throws RegistryException{
        return  (ComputationSchedulingResource)get(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, taskId);
    }

    public AdvanceInputDataHandlingResource getInputDataHandling (String taskId) throws RegistryException{
        return  (AdvanceInputDataHandlingResource)get(ResourceType.ADVANCE_INPUT_DATA_HANDLING, taskId);
    }

    public AdvancedOutputDataHandlingResource getOutputDataHandling (String taskId) throws RegistryException{
        return  (AdvancedOutputDataHandlingResource)get(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, taskId);
    }

    public JobDetailResource createJobDetail (String jobId) throws RegistryException{
        JobDetailResource resource = (JobDetailResource)create(ResourceType.JOB_DETAIL);
        resource.setJobId(jobId);
        return resource;
    }

    public JobDetailResource getJobDetail (String jobId) throws RegistryException{
        return (JobDetailResource)get(ResourceType.JOB_DETAIL, jobId);
    }

    public DataTransferDetailResource getDataTransferDetail (String dataTransferId) throws RegistryException{
        return (DataTransferDetailResource)get(ResourceType.DATA_TRANSFER_DETAIL, dataTransferId);
    }

    public  boolean isTaskStatusExist (String taskId) throws RegistryException{
        return isExists(ResourceType.STATUS, taskId);
    }

	public String getApplicationDeploymentId() {
		return applicationDeploymentId;
	}

	public void setApplicationDeploymentId(String applicationDeploymentId) {
		this.applicationDeploymentId = applicationDeploymentId;
	}

    public List<NotificationEmailResource> getNotificationEmails () throws RegistryException{
        List<NotificationEmailResource> emailResources = new ArrayList<NotificationEmailResource>();
        List<Resource> resources = get(ResourceType.NOTIFICATION_EMAIL);
        for (Resource resource : resources) {
            emailResources.add((NotificationEmailResource) resource);
        }
        return emailResources;
    }
}
