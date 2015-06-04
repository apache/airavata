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

package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TaskDetailExperimentCatResource extends AbstractExperimentCatResource {
    private static final Logger logger = LoggerFactory.getLogger(TaskDetailExperimentCatResource.class);
    private String taskId;
    private String nodeId;
    private Timestamp creationTime;
    private String applicationId;
    private String applicationVersion;
    private String applicationDeploymentId;
    private boolean enableEmailNotifications;
    private List<ApplicationInputExperimentCatResource> applicationInputs;
    private List<ApplicationOutputExperimentCatResource> applicationOutputs;
    private ComputationSchedulingExperimentCatResource schedulingResource;
    private AdvanceInputDataHandlingExperimentCatResource inputDataHandlingResource;
    private AdvancedOutputDataHandlingExperimentCatResource outputDataHandlingResource;
    private StatusExperimentCatResource taskStatus;
    private List<JobDetailExperimentCatResource> jobDetailResources;
    private List<DataTransferDetailExperimentCatResource> transferDetailResourceList;
    private List<NotificationEmailExperimentCatResource> emailResourceList;
    private List<ErrorDetailExperimentCatResource> errors;

    public List<JobDetailExperimentCatResource> getJobDetailResources() {
        return jobDetailResources;
    }

    public void setJobDetailResources(List<JobDetailExperimentCatResource> jobDetailResources) {
        this.jobDetailResources = jobDetailResources;
    }

    public void setApplicationInputs(List<ApplicationInputExperimentCatResource> applicationInputs) {
        this.applicationInputs = applicationInputs;
    }

    public void setApplicationOutputs(List<ApplicationOutputExperimentCatResource> applicationOutputs) {
        this.applicationOutputs = applicationOutputs;
    }

    public ComputationSchedulingExperimentCatResource getSchedulingResource() {
        return schedulingResource;
    }

    public void setSchedulingResource(ComputationSchedulingExperimentCatResource schedulingResource) {
        this.schedulingResource = schedulingResource;
    }

    public AdvanceInputDataHandlingExperimentCatResource getInputDataHandlingResource() {
        return inputDataHandlingResource;
    }

    public void setInputDataHandlingResource(AdvanceInputDataHandlingExperimentCatResource inputDataHandlingResource) {
        this.inputDataHandlingResource = inputDataHandlingResource;
    }

    public AdvancedOutputDataHandlingExperimentCatResource getOutputDataHandlingResource() {
        return outputDataHandlingResource;
    }

    public void setOutputDataHandlingResource(AdvancedOutputDataHandlingExperimentCatResource outputDataHandlingResource) {
        this.outputDataHandlingResource = outputDataHandlingResource;
    }

    public void setTaskStatus(StatusExperimentCatResource taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<DataTransferDetailExperimentCatResource> getTransferDetailResourceList() {
        return transferDetailResourceList;
    }

    public void setTransferDetailResourceList(List<DataTransferDetailExperimentCatResource> transferDetailResourceList) {
        this.transferDetailResourceList = transferDetailResourceList;
    }

    public List<NotificationEmailExperimentCatResource> getEmailResourceList() {
        return emailResourceList;
    }

    public void setEmailResourceList(List<NotificationEmailExperimentCatResource> emailResourceList) {
        this.emailResourceList = emailResourceList;
    }

    public List<ErrorDetailExperimentCatResource> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetailExperimentCatResource> errors) {
        this.errors = errors;
    }

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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    
    public ExperimentCatResource create(ResourceType type) throws RegistryException{
       switch (type){
           case ERROR_DETAIL:
               ErrorDetailExperimentCatResource errorDetailResource = new ErrorDetailExperimentCatResource();
               errorDetailResource.setTaskId(taskId);
               return errorDetailResource;
           case NOTIFICATION_EMAIL:
               NotificationEmailExperimentCatResource emailResource = new NotificationEmailExperimentCatResource();
               emailResource.setTaskId(taskId);
               return emailResource;
           case APPLICATION_INPUT:
               ApplicationInputExperimentCatResource applicationInputResource = new ApplicationInputExperimentCatResource();
               applicationInputResource.setTaskId(taskId);
               return applicationInputResource;
           case APPLICATION_OUTPUT:
               ApplicationOutputExperimentCatResource applicationOutputResource = new ApplicationOutputExperimentCatResource();
               applicationOutputResource.setTaskId(taskId);
               return applicationOutputResource;
           case JOB_DETAIL:
               JobDetailExperimentCatResource jobDetailResource = new JobDetailExperimentCatResource();
               jobDetailResource.setTaskId(taskId);
               return jobDetailResource;
           case DATA_TRANSFER_DETAIL:
               DataTransferDetailExperimentCatResource dataTransferDetailResource = new DataTransferDetailExperimentCatResource();
               dataTransferDetailResource.setTaskId(taskId);
               return dataTransferDetailResource;
           case STATUS:
               StatusExperimentCatResource statusResource = new StatusExperimentCatResource();
               statusResource.setTaskId(taskId);
               return statusResource;
           case COMPUTATIONAL_RESOURCE_SCHEDULING:
               ComputationSchedulingExperimentCatResource schedulingResource = new ComputationSchedulingExperimentCatResource();
               schedulingResource.setTaskId(taskId);
               return schedulingResource;
           case ADVANCE_INPUT_DATA_HANDLING:
               AdvanceInputDataHandlingExperimentCatResource inputDataHandlingResource = new AdvanceInputDataHandlingExperimentCatResource();
               inputDataHandlingResource.setTaskId(taskId);
               return inputDataHandlingResource;
           case ADVANCE_OUTPUT_DATA_HANDLING:
               AdvancedOutputDataHandlingExperimentCatResource outputDataHandlingResource = new AdvancedOutputDataHandlingExperimentCatResource();
               outputDataHandlingResource.setTaskId(taskId);
               return outputDataHandlingResource;
           case QOS_PARAM:
               QosParamExperimentCatResource qosParamResource = new QosParamExperimentCatResource();
               qosParamResource.setTaskId(taskId);
               return qosParamResource;
           default:
               logger.error("Unsupported resource type for task detail resource.", new IllegalArgumentException());
               throw new IllegalArgumentException("Unsupported resource type for task detail resource.");
       }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
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

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    ErrorDetail errorDetail = (ErrorDetail) q.getSingleResult();
                    ErrorDetailExperimentCatResource errorDetailResource = (ErrorDetailExperimentCatResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                    em.getTransaction().commit();
                    em.close();
                    return errorDetailResource;
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    Notification_Email notificationEmail = (Notification_Email) q.getSingleResult();
                    NotificationEmailExperimentCatResource emailResource = (NotificationEmailExperimentCatResource) Utils.getResource(ResourceType.NOTIFICATION_EMAIL, notificationEmail);
                    em.getTransaction().commit();
                    em.close();
                    return emailResource;
                case APPLICATION_INPUT:
                    generator = new QueryGenerator(APPLICATION_INPUT);
                    generator.setParameter(ApplicationInputConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    ApplicationInput applicationInput = (ApplicationInput) q.getSingleResult();
                    ApplicationInputExperimentCatResource inputResource = (ApplicationInputExperimentCatResource) Utils.getResource(ResourceType.APPLICATION_INPUT, applicationInput);
                    em.getTransaction().commit();
                    em.close();
                    return inputResource;
                case APPLICATION_OUTPUT:
                    generator = new QueryGenerator(APPLICATION_OUTPUT);
                    generator.setParameter(ApplicationOutputConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    ApplicationOutput applicationOutput = (ApplicationOutput) q.getSingleResult();
                    ApplicationOutputExperimentCatResource outputResource = (ApplicationOutputExperimentCatResource) Utils.getResource(ResourceType.APPLICATION_OUTPUT, applicationOutput);
                    em.getTransaction().commit();
                    em.close();
                    return outputResource;
                case JOB_DETAIL:
                    generator = new QueryGenerator(JOB_DETAIL);
                    generator.setParameter(JobDetailConstants.JOB_ID, name);
                    generator.setParameter(JobDetailConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    JobDetail jobDetail = (JobDetail) q.getSingleResult();
                    JobDetailExperimentCatResource jobDetailResource = (JobDetailExperimentCatResource) Utils.getResource(ResourceType.JOB_DETAIL, jobDetail);
                    em.getTransaction().commit();
                    em.close();
                    return jobDetailResource;
                case DATA_TRANSFER_DETAIL:
                    generator = new QueryGenerator(DATA_TRANSFER_DETAIL);
                    generator.setParameter(DataTransferDetailConstants.TRANSFER_ID, name);
                    q = generator.selectQuery(em);
                    DataTransferDetail transferDetail = (DataTransferDetail) q.getSingleResult();
                    DataTransferDetailExperimentCatResource transferDetailResource = (DataTransferDetailExperimentCatResource) Utils.getResource(ResourceType.DATA_TRANSFER_DETAIL, transferDetail);
                    em.getTransaction().commit();
                    em.close();
                    return transferDetailResource;
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TASK_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.TASK.toString());
                    q = generator.selectQuery(em);
                    Status status = (Status) q.getSingleResult();
                    StatusExperimentCatResource statusResource = (StatusExperimentCatResource) Utils.getResource(ResourceType.STATUS, status);
                    em.getTransaction().commit();
                    em.close();
                    return statusResource;
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    generator = new QueryGenerator(COMPUTATIONAL_RESOURCE_SCHEDULING);
                    generator.setParameter(ComputationalResourceSchedulingConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    Computational_Resource_Scheduling resourceScheduling = (Computational_Resource_Scheduling) q.getSingleResult();
                    ComputationSchedulingExperimentCatResource schedulingResource = (ComputationSchedulingExperimentCatResource) Utils.getResource(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, resourceScheduling);
                    em.getTransaction().commit();
                    em.close();
                    return schedulingResource;
                case ADVANCE_INPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_INPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedInputDataHandlingConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    AdvancedInputDataHandling dataHandling = (AdvancedInputDataHandling) q.getSingleResult();
                    AdvanceInputDataHandlingExperimentCatResource inputDataHandlingResource = (AdvanceInputDataHandlingExperimentCatResource) Utils.getResource(ResourceType.ADVANCE_INPUT_DATA_HANDLING, dataHandling);
                    em.getTransaction().commit();
                    em.close();
                    return inputDataHandlingResource;
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_OUTPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedOutputDataHandlingConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    AdvancedOutputDataHandling outputDataHandling = (AdvancedOutputDataHandling) q.getSingleResult();
                    AdvancedOutputDataHandlingExperimentCatResource outputDataHandlingResource = (AdvancedOutputDataHandlingExperimentCatResource) Utils.getResource(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, outputDataHandling);
                    em.getTransaction().commit();
                    em.close();
                    return outputDataHandlingResource;
                case QOS_PARAM:
                    generator = new QueryGenerator(QOS_PARAMS);
                    generator.setParameter(QosParamsConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    QosParam qosParam = (QosParam) q.getSingleResult();
                    QosParamExperimentCatResource qosParamResource = (QosParamExperimentCatResource) Utils.getResource(ResourceType.QOS_PARAM, qosParam);
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

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        List<ExperimentCatResource> resourceList = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
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
                            ErrorDetailExperimentCatResource errorDetailResource =
                                    (ErrorDetailExperimentCatResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
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
                            NotificationEmailExperimentCatResource emailResource =
                                    (NotificationEmailExperimentCatResource) Utils.getResource(ResourceType.NOTIFICATION_EMAIL, notificationEmail);
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
                            ApplicationInputExperimentCatResource inputResource =
                                    (ApplicationInputExperimentCatResource) Utils.getResource(ResourceType.APPLICATION_INPUT, applicationInput);
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
                            ApplicationOutputExperimentCatResource outputResource =
                                    (ApplicationOutputExperimentCatResource) Utils.getResource(ResourceType.APPLICATION_OUTPUT, applicationOutput);
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
                            JobDetailExperimentCatResource jobDetailResource =
                                    (JobDetailExperimentCatResource) Utils.getResource(ResourceType.JOB_DETAIL, jobDetail);
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
                            DataTransferDetailExperimentCatResource transferDetailResource =
                                    (DataTransferDetailExperimentCatResource) Utils.getResource(ResourceType.DATA_TRANSFER_DETAIL, transferDetail);
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
                            StatusExperimentCatResource statusResource =
                                    (StatusExperimentCatResource) Utils.getResource(ResourceType.STATUS, status);
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
            em = ExpCatResourceUtils.getEntityManager();
            TaskDetail taskDetail = em.find(TaskDetail.class, taskId);
            em.close();
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if (taskDetail != null) {
            	updateTaskDetail(taskDetail, nodeId);
                em.merge(taskDetail);
            } else {
                taskDetail = new TaskDetail();
                updateTaskDetail(taskDetail, nodeId);
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

	private void updateTaskDetail(TaskDetail taskDetail, String nodeId) {
		taskDetail.setTaskId(taskId);
		taskDetail.setNodeId(nodeId);
		taskDetail.setCreationTime(creationTime);
		taskDetail.setAppId(applicationId);
		taskDetail.setAppVersion(applicationVersion);
        taskDetail.setAllowNotification(enableEmailNotifications);
		taskDetail.setApplicationDeploymentId(getApplicationDeploymentId());
	}

    public List<ApplicationInputExperimentCatResource> getApplicationInputs() {
        return applicationInputs;
    }

    public List<ApplicationOutputExperimentCatResource> getApplicationOutputs() {
        return applicationOutputs;
    }

    public List<ApplicationInputExperimentCatResource> getApplicationInputs1() throws RegistryException{
        List<ApplicationInputExperimentCatResource> applicationInputResources = new ArrayList<ApplicationInputExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.APPLICATION_INPUT);
        for (ExperimentCatResource resource : resources) {
            ApplicationInputExperimentCatResource inputResource = (ApplicationInputExperimentCatResource) resource;
            applicationInputResources.add(inputResource);
        }
        return applicationInputResources;
    }

    public List<ApplicationOutputExperimentCatResource> getApplicationOutputs1() throws RegistryException{
        List<ApplicationOutputExperimentCatResource> outputResources = new ArrayList<ApplicationOutputExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.APPLICATION_OUTPUT);
        for (ExperimentCatResource resource : resources) {
            ApplicationOutputExperimentCatResource outputResource = (ApplicationOutputExperimentCatResource) resource;
            outputResources.add(outputResource);
        }
        return outputResources;
    }

    public StatusExperimentCatResource getTaskStatus() {
        return taskStatus;
    }

    public StatusExperimentCatResource getTaskStatus1() throws RegistryException{
        List<ExperimentCatResource> resources = get(ResourceType.STATUS);
        for (ExperimentCatResource resource : resources) {
            StatusExperimentCatResource taskStatus = (StatusExperimentCatResource) resource;
            if(taskStatus.getStatusType().equals(StatusType.TASK.toString())){
                if (taskStatus.getState() == null || taskStatus.getState().equals("") ){
                    taskStatus.setState("UNKNOWN");
                }
                return taskStatus;
            }
        }
        return null;
    }

    public List<JobDetailExperimentCatResource> getJobDetailList() throws RegistryException{
        List<JobDetailExperimentCatResource> jobDetailResources = new ArrayList<JobDetailExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.JOB_DETAIL);
        for (ExperimentCatResource resource : resources) {
            JobDetailExperimentCatResource jobDetailResource = (JobDetailExperimentCatResource) resource;
            jobDetailResources.add(jobDetailResource);
        }
        return jobDetailResources;
    }

    public List<DataTransferDetailExperimentCatResource> getDataTransferDetailList() throws RegistryException{
        List<DataTransferDetailExperimentCatResource> transferDetails = new ArrayList<DataTransferDetailExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.DATA_TRANSFER_DETAIL);
        for (ExperimentCatResource resource : resources) {
            DataTransferDetailExperimentCatResource transferDetailResource = (DataTransferDetailExperimentCatResource) resource;
            transferDetails.add(transferDetailResource);
        }
        return transferDetails;
    }

    public List<ErrorDetailExperimentCatResource> getErrorDetailList() throws RegistryException{
        List<ErrorDetailExperimentCatResource> errorDetailResources = new ArrayList<ErrorDetailExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.ERROR_DETAIL);
        for (ExperimentCatResource resource : resources) {
            ErrorDetailExperimentCatResource errorDetailResource = (ErrorDetailExperimentCatResource) resource;
            errorDetailResources.add(errorDetailResource);
        }
        return errorDetailResources;
    }

    public ComputationSchedulingExperimentCatResource getComputationScheduling (String taskId) throws RegistryException{
        return  (ComputationSchedulingExperimentCatResource)get(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, taskId);
    }

    public AdvanceInputDataHandlingExperimentCatResource getInputDataHandling (String taskId) throws RegistryException{
        return  (AdvanceInputDataHandlingExperimentCatResource)get(ResourceType.ADVANCE_INPUT_DATA_HANDLING, taskId);
    }

    public AdvancedOutputDataHandlingExperimentCatResource getOutputDataHandling (String taskId) throws RegistryException{
        return  (AdvancedOutputDataHandlingExperimentCatResource)get(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, taskId);
    }

    public JobDetailExperimentCatResource createJobDetail (String jobId) throws RegistryException{
        JobDetailExperimentCatResource resource = (JobDetailExperimentCatResource)create(ResourceType.JOB_DETAIL);
        resource.setJobId(jobId);
        return resource;
    }

    public JobDetailExperimentCatResource getJobDetail (String jobId) throws RegistryException{
        return (JobDetailExperimentCatResource)get(ResourceType.JOB_DETAIL, jobId);
    }

    public DataTransferDetailExperimentCatResource getDataTransferDetail (String dataTransferId) throws RegistryException{
        return (DataTransferDetailExperimentCatResource)get(ResourceType.DATA_TRANSFER_DETAIL, dataTransferId);
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

    public List<NotificationEmailExperimentCatResource> getNotificationEmails () throws RegistryException{
        List<NotificationEmailExperimentCatResource> emailResources = new ArrayList<NotificationEmailExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.NOTIFICATION_EMAIL);
        for (ExperimentCatResource resource : resources) {
            emailResources.add((NotificationEmailExperimentCatResource) resource);
        }
        return emailResources;
    }
}
