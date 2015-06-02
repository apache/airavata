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
package org.apache.airavata.experiment.catalog.resources;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.experiment.catalog.Resource;
import org.apache.airavata.experiment.catalog.ResourceType;
import org.apache.airavata.experiment.catalog.ResourceUtils;
import org.apache.airavata.experiment.catalog.model.*;
import org.apache.airavata.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentResource.class);
//    private WorkerResource worker;
    private String executionUser;
    private String expID;
    private Timestamp creationTime;
    private String gatewayId;
    private String projectId;
    private String expName;
    private String description;
    private String applicationId;
    private String applicationVersion;
    private String workflowTemplateId;
    private String workflowTemplateVersion;
    private String workflowExecutionId;
    private boolean enableEmailNotifications;
    private String gatewayExecutionId;
    private List<ExperimentInputResource> experimentInputResources;
    private List<ExperimentOutputResource> experimentOutputputResources;
    private ComputationSchedulingResource computationSchedulingResource;
    private ConfigDataResource userConfigDataResource;
    private List<WorkflowNodeDetailResource> workflowNodeDetailResourceList;
    private List<StatusResource> stateChangeList;
    private List<ErrorDetailResource> errorDetailList;
    private StatusResource experimentStatus;
    private List<NotificationEmailResource> emailResourceList;

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

    public boolean isEnableEmailNotifications() {
        return enableEmailNotifications;
    }

    public void setEnableEmailNotifications(boolean enableEmailNotifications) {
        this.enableEmailNotifications = enableEmailNotifications;
    }

    public String getGatewayExecutionId() {
        return gatewayExecutionId;
    }

    public void setGatewayExecutionId(String gatewayExecutionId) {
        this.gatewayExecutionId = gatewayExecutionId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<ExperimentInputResource> getExperimentInputResources() {
        return experimentInputResources;
    }

    public void setExperimentInputResources(List<ExperimentInputResource> experimentInputResources) {
        this.experimentInputResources = experimentInputResources;
    }

    public List<ExperimentOutputResource> getExperimentOutputputResources() {
        return experimentOutputputResources;
    }

    public void setExperimentOutputputResources(List<ExperimentOutputResource> experimentOutputputResources) {
        this.experimentOutputputResources = experimentOutputputResources;
    }

    public ComputationSchedulingResource getComputationSchedulingResource() {
        return computationSchedulingResource;
    }

    public void setComputationSchedulingResource(ComputationSchedulingResource computationSchedulingResource) {
        this.computationSchedulingResource = computationSchedulingResource;
    }

    public ConfigDataResource getUserConfigDataResource() {
        return userConfigDataResource;
    }

    public void setUserConfigDataResource(ConfigDataResource userConfigDataResource) {
        this.userConfigDataResource = userConfigDataResource;
    }

    public List<WorkflowNodeDetailResource> getWorkflowNodeDetailResourceList() {
        return workflowNodeDetailResourceList;
    }

    public void setWorkflowNodeDetailResourceList(List<WorkflowNodeDetailResource> workflowNodeDetailResourceList) {
        this.workflowNodeDetailResourceList = workflowNodeDetailResourceList;
    }

    public List<StatusResource> getStateChangeList() {
        return stateChangeList;
    }

    public void setStateChangeList(List<StatusResource> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

    public List<ErrorDetailResource> getErrorDetailList() {
        return errorDetailList;
    }

    public void setErrorDetailList(List<ErrorDetailResource> errorDetailList) {
        this.errorDetailList = errorDetailList;
    }

    public void setExperimentStatus(StatusResource experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public List<NotificationEmailResource> getEmailResourceList() {
        return emailResourceList;
    }

    public void setEmailResourceList(List<NotificationEmailResource> emailResourceList) {
        this.emailResourceList = emailResourceList;
    }


    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @return UnsupportedOperationException
     */
    public Resource create(ResourceType type) throws RegistryException {
    	switch (type){
	        case EXPERIMENT_INPUT:
	        	ExperimentInputResource inputResource = new ExperimentInputResource();
	            inputResource.setExperimentId(expID);
	            return inputResource;
            case EXPERIMENT_OUTPUT:
                ExperimentOutputResource experimentOutputResource = new ExperimentOutputResource();
                experimentOutputResource.setExperimentId(expID);
                return experimentOutputResource;
            case NOTIFICATION_EMAIL:
                NotificationEmailResource emailResource = new NotificationEmailResource();
                emailResource.setExperimentId(expID);
                return emailResource;
            case WORKFLOW_NODE_DETAIL:
                WorkflowNodeDetailResource nodeDetailResource = new WorkflowNodeDetailResource();
                nodeDetailResource.setExperimentId(expID);
                return nodeDetailResource;
            case ERROR_DETAIL:
                ErrorDetailResource errorDetailResource = new ErrorDetailResource();
                errorDetailResource.setExperimentId(expID);
                return errorDetailResource;
            case STATUS:
                StatusResource statusResource = new StatusResource();
                statusResource.setExperimentId(expID);
                return statusResource;
            case CONFIG_DATA:
                ConfigDataResource configDataResource = new ConfigDataResource();
                configDataResource.setExperimentId(expID);
                return configDataResource;
            case COMPUTATIONAL_RESOURCE_SCHEDULING:
                ComputationSchedulingResource schedulingResource = new ComputationSchedulingResource();
                schedulingResource.setExperimentId(expID);
                return schedulingResource;
            case ADVANCE_INPUT_DATA_HANDLING:
                AdvanceInputDataHandlingResource dataHandlingResource = new AdvanceInputDataHandlingResource();
                dataHandlingResource.setExperimentId(expID);
                return dataHandlingResource;
            case ADVANCE_OUTPUT_DATA_HANDLING:
                AdvancedOutputDataHandlingResource outputDataHandlingResource = new AdvancedOutputDataHandlingResource();
                outputDataHandlingResource.setExperimentId(expID);
                return outputDataHandlingResource;
            case QOS_PARAM:
                QosParamResource qosParamResource = new QosParamResource();
                qosParamResource.setExperimentId(expID);
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
    public void remove(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
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
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.EXPERIMENT_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case WORKFLOW_NODE_DETAIL:
                    generator = new QueryGenerator(WORKFLOW_NODE_DETAIL);
                    generator.setParameter(WorkflowNodeDetailsConstants.NODE_INSTANCE_ID, name);
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
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.EXPERIMENT.toString());
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

    /**
     *
     * @param type  child resource types
     * @param name name of the child resource
     * @return UnsupportedOperationException
     */
    public Resource get(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case EXPERIMENT_INPUT:
                    generator = new QueryGenerator(EXPERIMENT_INPUT);
                    generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    Experiment_Input experimentInput = (Experiment_Input) q.getSingleResult();
                    ExperimentInputResource inputResource = (ExperimentInputResource) Utils.getResource(ResourceType.EXPERIMENT_INPUT, experimentInput);
                    em.getTransaction().commit();
                    em.close();
                    return inputResource;
                case EXPERIMENT_OUTPUT:
                    generator = new QueryGenerator(EXPERIMENT_OUTPUT);
                    generator.setParameter(ExperimentOutputConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    Experiment_Output experimentOutput = (Experiment_Output) q.getSingleResult();
                    ExperimentOutputResource outputResource = (ExperimentOutputResource) Utils.getResource(ResourceType.EXPERIMENT_OUTPUT, experimentOutput);
                    em.getTransaction().commit();
                    em.close();
                    return outputResource;
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    Notification_Email notificationEmail = (Notification_Email) q.getSingleResult();
                    NotificationEmailResource notificationEmailResource = (NotificationEmailResource) Utils.getResource(ResourceType.NOTIFICATION_EMAIL, notificationEmail);
                    em.getTransaction().commit();
                    em.close();
                    return notificationEmailResource;
                case WORKFLOW_NODE_DETAIL:
                    generator = new QueryGenerator(WORKFLOW_NODE_DETAIL);
                    generator.setParameter(WorkflowNodeDetailsConstants.NODE_INSTANCE_ID, name);
                    q = generator.selectQuery(em);
                    WorkflowNodeDetail workflowNodeDetail = (WorkflowNodeDetail) q.getSingleResult();
                    WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) Utils.getResource(ResourceType.WORKFLOW_NODE_DETAIL, workflowNodeDetail);
                    em.getTransaction().commit();
                    em.close();
                    return nodeDetailResource;
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    ErrorDetail errorDetail = (ErrorDetail) q.getSingleResult();
                    ErrorDetailResource errorDetailResource = (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                    em.getTransaction().commit();
                    em.close();
                    return errorDetailResource;
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.EXPERIMENT_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.EXPERIMENT.toString());
                    q = generator.selectQuery(em);
                    Status status = (Status) q.getSingleResult();
                    StatusResource statusResource = (StatusResource) Utils.getResource(ResourceType.STATUS, status);
                    em.getTransaction().commit();
                    em.close();
                    return statusResource;
                case CONFIG_DATA:
                    generator = new QueryGenerator(CONFIG_DATA);
                    generator.setParameter(ExperimentConfigurationDataConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    ExperimentConfigData configData = (ExperimentConfigData) q.getSingleResult();
                    ConfigDataResource configDataResource = (ConfigDataResource) Utils.getResource(ResourceType.CONFIG_DATA, configData);
                    em.getTransaction().commit();
                    em.close();
                    return configDataResource;
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    generator = new QueryGenerator(COMPUTATIONAL_RESOURCE_SCHEDULING);
                    generator.setParameter(ComputationalResourceSchedulingConstants.EXPERIMENT_ID, name);
                    generator.setParameter(ComputationalResourceSchedulingConstants.TASK_ID, null);
                    q = generator.selectQuery(em);
                    Computational_Resource_Scheduling scheduling = (Computational_Resource_Scheduling) q.getSingleResult();
                    ComputationSchedulingResource schedulingResource = (ComputationSchedulingResource) Utils.getResource(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, scheduling);
                    em.getTransaction().commit();
                    em.close();
                    return schedulingResource;
                case ADVANCE_INPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_INPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedInputDataHandlingConstants.EXPERIMENT_ID, name);
                    generator.setParameter(AdvancedInputDataHandlingConstants.TASK_ID, null);
                    q = generator.selectQuery(em);
                    AdvancedInputDataHandling inputDataHandling = (AdvancedInputDataHandling) q.getSingleResult();
                    AdvanceInputDataHandlingResource dataHandlingResource = (AdvanceInputDataHandlingResource) Utils.getResource(ResourceType.ADVANCE_INPUT_DATA_HANDLING, inputDataHandling);
                    em.getTransaction().commit();
                    em.close();
                    return dataHandlingResource;
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    generator = new QueryGenerator(ADVANCE_OUTPUT_DATA_HANDLING);
                    generator.setParameter(AdvancedOutputDataHandlingConstants.EXPERIMENT_ID, name);
                    generator.setParameter(AdvancedOutputDataHandlingConstants.TASK_ID, null);
                    q = generator.selectQuery(em);
                    AdvancedOutputDataHandling outputDataHandling = (AdvancedOutputDataHandling) q.getSingleResult();
                    AdvancedOutputDataHandlingResource outputDataHandlingResource = (AdvancedOutputDataHandlingResource) Utils.getResource(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, outputDataHandling);
                    em.getTransaction().commit();
                    em.close();
                    return outputDataHandlingResource;
                case QOS_PARAM:
                    generator = new QueryGenerator(QOS_PARAMS);
                    generator.setParameter(QosParamsConstants.EXPERIMENT_ID, name);
                    generator.setParameter(QosParamsConstants.TASK_ID, null);
                    q = generator.selectQuery(em);
                    QosParam qosParam = (QosParam) q.getSingleResult();
                    QosParamResource qosParamResource = (QosParamResource) Utils.getResource(ResourceType.QOS_PARAM, qosParam);
                    em.getTransaction().commit();
                    em.close();
                    return qosParamResource;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for experiment data resource.");
            }
        } catch (Exception e) {
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

    /**
     *
     * @param type  child resource types
     * @return UnsupportedOperationException
     */
    public List<Resource> get(ResourceType type)  throws RegistryException{
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case EXPERIMENT_INPUT:
                    generator = new QueryGenerator(EXPERIMENT_INPUT);
                    generator.setParameter(ExperimentInputConstants.EXPERIMENT_ID, expID);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Experiment_Input exInput = (Experiment_Input) result;
                            ExperimentInputResource inputResource =
                                    (ExperimentInputResource) Utils.getResource(ResourceType.EXPERIMENT_INPUT, exInput);
                            resourceList.add(inputResource);
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
                            Experiment_Output output = (Experiment_Output) result;
                            ExperimentOutputResource outputResource =
                                    (ExperimentOutputResource) Utils.getResource(ResourceType.EXPERIMENT_OUTPUT, output);
                            resourceList.add(outputResource);
                        }
                    }
                    break;
                case NOTIFICATION_EMAIL:
                    generator = new QueryGenerator(NOTIFICATION_EMAIL);
                    generator.setParameter(NotificationEmailConstants.EXPERIMENT_ID, expID);
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
                case WORKFLOW_NODE_DETAIL:
                    generator = new QueryGenerator(WORKFLOW_NODE_DETAIL);
                    generator.setParameter(WorkflowNodeDetailsConstants.EXPERIMENT_ID, expID);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            WorkflowNodeDetail nodeDetail = (WorkflowNodeDetail) result;
                            WorkflowNodeDetailResource nodeDetailResource =
                                    (WorkflowNodeDetailResource) Utils.getResource(ResourceType.WORKFLOW_NODE_DETAIL, nodeDetail);
                            resourceList.add(nodeDetailResource);
                        }
                    }
                    break;
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.EXPERIMENT_ID, expID);
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
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.EXPERIMENT_ID, expID);
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
                    logger.error("Unsupported resource type for experiment resource.", new UnsupportedOperationException());
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

    /**
     * save experiment
     */
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Experiment existingExp = em.find(Experiment.class, expID);
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Experiment experiment = new Experiment();
            experiment.setProjectID(projectId);
            experiment.setExpId(expID);
            experiment.setExecutionUser(executionUser);
            experiment.setExecutionUser(executionUser);
            experiment.setGatewayId(gatewayId);
            experiment.setCreationTime(creationTime);
            experiment.setExpName(expName);
            experiment.setExpDesc(description);
            experiment.setApplicationId(applicationId);
            experiment.setAppVersion(applicationVersion);
            experiment.setWorkflowExecutionId(workflowExecutionId);
            experiment.setWorkflowTemplateVersion(workflowTemplateVersion);
            experiment.setWorkflowExecutionId(workflowExecutionId);
            experiment.setAllowNotification(enableEmailNotifications);
            experiment.setGatewayExecutionId(gatewayExecutionId);
            if (existingExp != null) {
                existingExp.setGatewayId(gatewayId);
                existingExp.setExecutionUser(executionUser);
                existingExp.setProjectID(projectId);
                existingExp.setCreationTime(creationTime);
                existingExp.setExpName(expName);
                existingExp.setExpDesc(description);
                existingExp.setApplicationId(applicationId);
                existingExp.setAppVersion(applicationVersion);
                existingExp.setWorkflowExecutionId(workflowExecutionId);
                existingExp.setWorkflowTemplateVersion(workflowTemplateVersion);
                existingExp.setWorkflowExecutionId(workflowExecutionId);
                existingExp.setAllowNotification(enableEmailNotifications);
                existingExp.setGatewayExecutionId(gatewayExecutionId);
                experiment = em.merge(existingExp);
            } else {
                em.persist(experiment);
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

    /**
     *
     * @param expID experiment ID
     */
    public void setExpID(String expID) {
		this.expID = expID;
	}

    public String getExecutionUser() {
        return executionUser;
    }

    public void setExecutionUser(String executionUser) {
        this.executionUser = executionUser;
    }

    public List<NotificationEmailResource> getNotificationEmails () throws RegistryException{
        List<NotificationEmailResource> emailResources = new ArrayList<NotificationEmailResource>();
        List<Resource> resources = get(ResourceType.NOTIFICATION_EMAIL);
        for (Resource resource : resources) {
            emailResources.add((NotificationEmailResource) resource);
        }
        return emailResources;
    }

    public List<ExperimentInputResource> getExperimentInputs () throws RegistryException{
        List<ExperimentInputResource> expInputs = new ArrayList<ExperimentInputResource>();
        List<Resource> resources = get(ResourceType.EXPERIMENT_INPUT);
        for (Resource resource : resources) {
            expInputs.add((ExperimentInputResource) resource);
        }
        return expInputs;
    }

    public List<ExperimentOutputResource> getExperimentOutputs () throws RegistryException{
        List<ExperimentOutputResource> expOutputs = new ArrayList<ExperimentOutputResource>();
        List<Resource> resources = get(ResourceType.EXPERIMENT_OUTPUT);
        for (Resource resource : resources) {
            expOutputs.add((ExperimentOutputResource) resource);
        }
        return expOutputs;
    }

    public StatusResource getExperimentStatus() throws RegistryException{
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource expStatus = (StatusResource) resource;
            if(expStatus.getStatusType().equals(StatusType.EXPERIMENT.toString())){
                if (expStatus.getState() == null || expStatus.getState().equals("") ){
                    expStatus.setState("UNKNOWN");
                }
                return expStatus;
            }
        }
        return null;
    }

    public List<StatusResource> getWorkflowNodeStatuses() throws RegistryException{
        List<StatusResource> statuses = new ArrayList<StatusResource>();
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource workflowNodeStatus = (StatusResource) resource;
            if(workflowNodeStatus.getStatusType().equals(StatusType.WORKFLOW_NODE.toString())){
                if (workflowNodeStatus.getState() == null || workflowNodeStatus.getState().equals("")){
                    workflowNodeStatus.setState("UNKNOWN");
                }
                statuses.add(workflowNodeStatus);
            }
        }
        return statuses;
    }

    public List<WorkflowNodeDetailResource> getWorkflowNodeDetails () throws RegistryException{
        List<WorkflowNodeDetailResource> workflowNodeDetailResourceList = new ArrayList<WorkflowNodeDetailResource>();
        List<Resource> resources = get(ResourceType.WORKFLOW_NODE_DETAIL);
        for (Resource resource : resources) {
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) resource;
            workflowNodeDetailResourceList.add(nodeDetailResource);
        }
        return workflowNodeDetailResourceList;
    }

    public List<ErrorDetailResource> getErrorDetails () throws RegistryException{
        List<ErrorDetailResource> errorDetailResources = new ArrayList<ErrorDetailResource>();
        List<Resource> resources = get(ResourceType.ERROR_DETAIL);
        for (Resource resource : resources) {
            ErrorDetailResource errorDetailResource = (ErrorDetailResource) resource;
            errorDetailResources.add(errorDetailResource);
        }
        return errorDetailResources;
    }

    public ComputationSchedulingResource getComputationScheduling (String expId) throws RegistryException{
        return  (ComputationSchedulingResource)get(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, expId);
    }

    public AdvanceInputDataHandlingResource getInputDataHandling (String expId) throws RegistryException{
        return  (AdvanceInputDataHandlingResource)get(ResourceType.ADVANCE_INPUT_DATA_HANDLING, expId);
    }

    public AdvancedOutputDataHandlingResource getOutputDataHandling (String expId) throws RegistryException{
        return  (AdvancedOutputDataHandlingResource)get(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, expId);
    }

    public QosParamResource getQOSparams (String expId) throws RegistryException{
        return  (QosParamResource)get(ResourceType.QOS_PARAM, expId);
    }

    public ConfigDataResource getUserConfigData(String expID) throws RegistryException{
        return (ConfigDataResource)get(ResourceType.CONFIG_DATA, expID);
    }
    public WorkflowNodeDetailResource getWorkflowNode (String nodeId) throws RegistryException{
        return (WorkflowNodeDetailResource)get(ResourceType.WORKFLOW_NODE_DETAIL, nodeId);
    }
}
