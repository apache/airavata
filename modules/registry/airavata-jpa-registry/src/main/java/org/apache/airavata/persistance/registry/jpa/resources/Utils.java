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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.persistance.registry.jpa.JPAConstants;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


public class Utils {
    private final static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getJDBCFullURL(){
		String jdbcUrl = getJDBCURL();
		String jdbcUser = getJDBCUser();
		String jdbcPassword = getJDBCPassword();
        jdbcUrl = jdbcUrl + "?"  + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;
        return jdbcUrl;
    }

    public static String getJDBCURL(){
    	try {
            return ServerSettings.getSetting(JPAConstants.KEY_JDBC_URL);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getHost(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getHost();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static int getPort(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getPort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public static int getJPACacheSize (){
        try {
            String cache = ServerSettings.getSetting(JPAConstants.JPA_CACHE_SIZE, "5000");
            return Integer.parseInt(cache);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public static String isCachingEnabled (){
        try {
            return ServerSettings.getSetting(JPAConstants.ENABLE_CACHING, "true");
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            return "true";
        }
    }

    public static String getDBType(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getScheme();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static boolean isDerbyStartEnabled(){
        try {
            String s = ServerSettings.getSetting(JPAConstants.KEY_DERBY_START_ENABLE);
            if("true".equals(s)){
                return true;
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return false;
    }

    public static String getJDBCUser(){
    	try {
		    return ServerSettings.getSetting(JPAConstants.KEY_JDBC_USER);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    public static String getValidationQuery(){
    	try {
            return ServerSettings.getSetting(JPAConstants.VALIDATION_QUERY);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    public static String getJDBCPassword(){
    	try {
            return ServerSettings.getSetting(JPAConstants.KEY_JDBC_PASSWORD);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}

    }

    public static String getJDBCDriver(){
    	try {
            return ServerSettings.getSetting(JPAConstants.KEY_JDBC_DRIVER);
		} catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    /**
     *
     * @param type model type
     * @param o model type instance
     * @return corresponding resource object
     */
    public static Resource getResource(ResourceType type, Object o) {
        switch (type){
            case GATEWAY:
                if (o instanceof Gateway) {
                    return createGateway((Gateway) o);
                } else {
                    logger.error("Object should be a Gateway.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Gateway.");
                }
            case PROJECT:
                if (o instanceof Project){
                    return createProject((Project) o);
                } else {
                    logger.error("Object should be a Project.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Project.");
                }
            case PROJECT_USER:
                if (o instanceof  ProjectUser){
                    return createProjectUser((ProjectUser)o);
                }else {
                    logger.error("Object should be a ProjectUser.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ProjectUser.");
                }
            case CONFIGURATION:
                if(o instanceof Configuration){
                    return createConfiguration((Configuration) o);
                }else {
                    logger.error("Object should be a Configuration.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Configuration.");
                }
            case USER:
                if(o instanceof Users) {
                    return createUser((Users) o);
                }else {
                    logger.error("Object should be a User.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a User.");
                }
            case GATEWAY_WORKER:
                if (o instanceof Gateway_Worker){
                    return createGatewayWorker((Gateway_Worker)o);
                } else {
                    logger.error("Object should be a Gateway Worker.", new IllegalArgumentException());
                    throw  new IllegalArgumentException("Object should be a Gateway Worker.");
                }
            case EXPERIMENT:
                if (o instanceof  Experiment){
                    return createExperiment((Experiment)o);
                }else {
                    logger.error("Object should be a Experiment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment.");
                }
            case EXPERIMENT_SUMMARY:
                if (o instanceof  Experiment){
                    return createExperimentSummary((Experiment)o);
                }else {
                    logger.error("Object should be a ExperimentSummary.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ExperimentSummary.");
                }
            case NOTIFICATION_EMAIL:
                if (o instanceof  Notification_Email){
                    return createNotificationEmail((Notification_Email)o);
                }else {
                    logger.error("Object should be a Experiment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment.");
                }
            case EXPERIMENT_INPUT:
                if (o instanceof  Experiment_Input){
                    return createExperimentInput((Experiment_Input)o);
                }else {
                    logger.error("Object should be a Experiment input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment input data.");
                }
            case EXPERIMENT_OUTPUT:
                if (o instanceof  Experiment_Output){
                    return createExperimentOutput((Experiment_Output)o);
                }else {
                    logger.error("Object should be a Experiment output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment output data.");
                }
            case WORKFLOW_NODE_DETAIL:
                 if (o instanceof  WorkflowNodeDetail){
                     return createWorkflowNodeDetail((WorkflowNodeDetail)o);
                 }else {
                     logger.error("Object should be a Workflow node data.", new IllegalArgumentException());
                     throw new IllegalArgumentException("Object should be a Workflow node data.");
                 }
            case TASK_DETAIL:
                if (o instanceof  TaskDetail){
                    return createTaskDetail((TaskDetail)o);
                }else {
                    logger.error("Object should be a task detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a task detail data.");
                }
            case ERROR_DETAIL:
                if (o instanceof  ErrorDetail){
                    return createErrorDetail((ErrorDetail)o);
                }else {
                    logger.error("Object should be a error detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a error detail data.");
                }
            case APPLICATION_INPUT:
                if (o instanceof  ApplicationInput){
                    return createApplicationInput((ApplicationInput)o);
                }else {
                    logger.error("Object should be a application input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a application input data.");
                }
            case APPLICATION_OUTPUT:
                if (o instanceof  ApplicationOutput){
                    return createApplicationOutput((ApplicationOutput)o);
                }else {
                    logger.error("Object should be a application output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a application output data.");
                }
            case NODE_INPUT:
                if (o instanceof  NodeInput){
                    return createNodeInput((NodeInput)o);
                }else {
                    logger.error("Object should be a node input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a node input data.");
                }
            case NODE_OUTPUT:
                if (o instanceof  NodeOutput){
                    return createNodeOutput((NodeOutput)o);
                }else {
                    logger.error("Object should be a node output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a node output data.");
                }
            case JOB_DETAIL:
                if (o instanceof  JobDetail){
                    return createJobDetail((JobDetail)o);
                }else {
                    logger.error("Object should be a job detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a job detail data.");
                }
            case DATA_TRANSFER_DETAIL:
                if (o instanceof  DataTransferDetail){
                    return createDataTransferResource((DataTransferDetail)o);
                }else {
                    logger.error("Object should be a data transfer detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a data transfer detail data.");
                }
            case STATUS:
                if (o instanceof  Status){
                    return createStatusResource((Status)o);
                }else {
                    logger.error("Object should be a status data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a status data.");
                }
            case CONFIG_DATA:
                if (o instanceof  ExperimentConfigData){
                    return createExConfigDataResource((ExperimentConfigData)o);
                }else {
                    logger.error("Object should be a experiment config data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be experiment config data.");
                }
            case COMPUTATIONAL_RESOURCE_SCHEDULING:
                if (o instanceof  Computational_Resource_Scheduling){
                    return createComputationalScheduling((Computational_Resource_Scheduling)o);
                }else {
                    logger.error("Object should be a scheduling resource data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be scheduling resource data.");
                }
            case ADVANCE_INPUT_DATA_HANDLING:
                if (o instanceof  AdvancedInputDataHandling){
                    return createAdvancedInputDataResource((AdvancedInputDataHandling)o);
                }else {
                    logger.error("Object should be a advanced input data handling data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be advanced input data handling data.");
                }
            case ADVANCE_OUTPUT_DATA_HANDLING:
                if (o instanceof  AdvancedOutputDataHandling){
                    return createAdvancedOutputDataResource((AdvancedOutputDataHandling)o);
                }else {
                    logger.error("Object should be a advanced output data handling data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be advanced output data handling data.");
                }
            case QOS_PARAM:
                if (o instanceof  QosParam){
                    return createQosParamResource((QosParam)o);
                }else {
                    logger.error("Object should be a QOSparam data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be QOSparam data.");
                }
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }

    /**
     *
     * @param o  Gateway model object
     * @return  GatewayResource object
     */
    private static Resource createGateway(Gateway o) {
        GatewayResource gatewayResource = new GatewayResource();
        gatewayResource.setGatewayName(o.getGateway_name());
        gatewayResource.setGatewayId(o.getGateway_id());
        gatewayResource.setDomain(o.getDomain());
        gatewayResource.setEmailAddress(o.getEmailAddress());
        return gatewayResource;
    }

    /**
     *
     * @param o Project model object
     * @return ProjectResource object
     */
    private static Resource createProject(Project o) {
        ProjectResource projectResource = new ProjectResource();
        if (o != null){
            projectResource.setId(o.getProject_id());
            projectResource.setName(o.getProject_name());
            GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
            projectResource.setGateway(gatewayResource);
            Gateway_Worker gateway_worker = new Gateway_Worker();
            gateway_worker.setGateway(o.getGateway());
            gateway_worker.setUser(o.getUsers());
            gateway_worker.setUser_name(o.getUsers().getUser_name());
            WorkerResource workerResource = (WorkerResource) createGatewayWorker(gateway_worker);
            projectResource.setWorker(workerResource);
            projectResource.setDescription(o.getDescription());
            projectResource.setCreationTime(o.getCreationTime());
        }

        return projectResource;
    }

    private static Resource createProjectUser(ProjectUser o) {
        ProjectUserResource projectUserResource = new ProjectUserResource();
        if (o != null){
            projectUserResource.setUserName(o.getUser().getUser_name());
            projectUserResource.setProjectId(o.getProjectID());
        }
        return projectUserResource;
    }

    /**
     *
     * @param o configuration model object
     * @return configuration resource object
     */
    private static Resource createConfiguration (Configuration o){
        ConfigurationResource configurationResource = new ConfigurationResource();
        if (o != null){
            configurationResource.setConfigKey(o.getConfig_key());
            configurationResource.setConfigVal(o.getConfig_val());
            configurationResource.setExpireDate(o.getExpire_date());
            configurationResource.setCategoryID(o.getCategory_id());
        }

        return configurationResource;
    }

    /**
     *
     * @param o Gateway_Worker model object
     * @return  Gateway_Worker resource object
     */
    private static Resource createGatewayWorker(Gateway_Worker o) {
        if (o != null){
            GatewayResource gatewayResource = new GatewayResource(o.getGateway().getGateway_id());
            gatewayResource.setDomain(o.getGateway().getGateway_name());
            gatewayResource.setDomain(o.getGateway().getDomain());
            gatewayResource.setEmailAddress(o.getGateway().getEmailAddress());
            return new WorkerResource(o.getUser_name(), gatewayResource);
        }
        return null;
    }

    /**
     *
     * @param o  Users model object
     * @return  UserResource object
     */
    private static Resource createUser(Users o) {
        UserResource userResource = new UserResource();
        if (o != null){
            userResource.setUserName(o.getUser_name());
            userResource.setPassword(o.getPassword());
        }

        return userResource;
    }

    /**
     * @param o Experiment model object
     * @return  Experiment resource object
     */
    private static Resource createExperiment(Experiment o) {
        ExperimentResource experimentResource = new ExperimentResource();
        if (o != null){
            GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
            experimentResource.setGateway(gatewayResource);
            experimentResource.setExecutionUser(o.getExecutionUser());
            if (o.getProject() != null){
                ProjectResource projectResource = (ProjectResource)createProject(o.getProject());
                experimentResource.setProject(projectResource);
            }
            experimentResource.setExpID(o.getExpId());
            experimentResource.setExpName(o.getExpName());
            experimentResource.setCreationTime(o.getCreationTime());
            experimentResource.setDescription(o.getExpDesc());
            experimentResource.setApplicationId(o.getApplicationId());
            experimentResource.setApplicationVersion(o.getAppVersion());
            experimentResource.setWorkflowTemplateId(o.getWorkflowTemplateId());
            experimentResource.setWorkflowTemplateVersion(o.getWorkflowTemplateVersion());
            experimentResource.setWorkflowExecutionId(o.getWorkflowExecutionId());
            experimentResource.setEnableEmailNotifications(o.isAllowNotification());
            experimentResource.setGatewayExecutionId(o.getGatewayExecutionId());
        }

        return experimentResource;
    }

    private static ExperimentSummaryResource createExperimentSummary(Experiment o) {
        ExperimentSummaryResource experimentSummaryResource = new ExperimentSummaryResource();
        if (o != null){
            experimentSummaryResource.setExecutionUser(o.getExecutionUser());
            experimentSummaryResource.setExpID(o.getExpId());
            experimentSummaryResource.setExpName(o.getExpName());
            experimentSummaryResource.setProjectID(o.getProjectId());
            experimentSummaryResource.setCreationTime(o.getCreationTime());
            experimentSummaryResource.setDescription(o.getExpDesc());
            experimentSummaryResource.setApplicationId(o.getApplicationId());

            if(o.getStatuses() != null && !o.getStatuses().isEmpty()) {
                Status experimentStatus = o.getStatuses().iterator().next();
                if (experimentStatus != null) {
                    StatusResource statusResource = new StatusResource();
                    statusResource.setStatusId(experimentStatus.getStatusId());
                    statusResource.setJobId(experimentStatus.getJobId());
                    statusResource.setState(experimentStatus.getState());
                    statusResource.setStatusUpdateTime(experimentStatus.getStatusUpdateTime());
                    statusResource.setStatusType(experimentStatus.getStatusType());
                    experimentSummaryResource.setStatus(statusResource);
                }
            }
        }

        return experimentSummaryResource;
    }

    private static Resource createNotificationEmail (Notification_Email o){
        NotificationEmailResource emailResource = new NotificationEmailResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            emailResource.setExperimentResource(experimentResource);
            TaskDetailResource taskDetailResource =  (TaskDetailResource)createTaskDetail(o.getTaskDetail());
            emailResource.setTaskDetailResource(taskDetailResource);
            emailResource.setEmailAddress(o.getEmailAddress());
        }
        return emailResource;
    }

    private static Resource createExperimentInput (Experiment_Input o){
        ExperimentInputResource eInputResource = new ExperimentInputResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            eInputResource.setExperimentResource(experimentResource);
            eInputResource.setDataType(o.getDataType());
            eInputResource.setMetadata(o.getMetadata());
            eInputResource.setExperimentKey(o.getEx_key());
            eInputResource.setAppArgument(o.getAppArgument());
            eInputResource.setInputOrder(o.getInputOrder());
            eInputResource.setStandardInput(o.isStandardInput());
            eInputResource.setUserFriendlyDesc(o.getUserFriendlyDesc());
            eInputResource.setRequired(o.isRequired());
            eInputResource.setRequiredToCMD(o.isRequiredToCMD());
            eInputResource.setDataStaged(o.isDataStaged());

            if (o.getValue() != null){
                eInputResource.setValue(new String(o.getValue()));
            }

        }
        return eInputResource;
    }

    private static Resource createExperimentOutput (Experiment_Output o){
        ExperimentOutputResource eOutputResource = new ExperimentOutputResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            eOutputResource.setExperimentResource(experimentResource);
            eOutputResource.setExperimentKey(o.getEx_key());
            if (o.getValue() != null){
                eOutputResource.setValue(new String(o.getValue()));
            }
            eOutputResource.setDataType(o.getDataType());
            eOutputResource.setRequired(o.isRequired());
            eOutputResource.setRequiredToCMD(o.isRequiredToCMD());
            eOutputResource.setDataMovement(o.isDataMovement());
            eOutputResource.setDataNameLocation(o.getDataNameLocation());
            eOutputResource.setSearchQuery(o.getSearchQuery());
            eOutputResource.setAppArgument(o.getApplicationArgument());
        }
        return eOutputResource;
    }

    private static Resource createWorkflowNodeDetail (WorkflowNodeDetail o){
        WorkflowNodeDetailResource nodeDetailResource = new WorkflowNodeDetailResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            nodeDetailResource.setExperimentResource(experimentResource);
            nodeDetailResource.setCreationTime(o.getCreationTime());
            nodeDetailResource.setNodeInstanceId(o.getNodeId());
            nodeDetailResource.setNodeName(o.getNodeName());
            nodeDetailResource.setExecutionUnit(o.getExecutionUnit());
            nodeDetailResource.setExecutionUnitData(o.getExecutionUnitData());

        }
        return nodeDetailResource;
    }

    private static Resource createTaskDetail(TaskDetail o){
        TaskDetailResource taskDetailResource = new TaskDetailResource();
        if ( o != null){
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetail());
            taskDetailResource.setWorkflowNodeDetailResource(nodeDetailResource);
            taskDetailResource.setCreationTime(o.getCreationTime());
            taskDetailResource.setTaskId(o.getTaskId());
            taskDetailResource.setApplicationId(o.getAppId());
            taskDetailResource.setApplicationVersion(o.getAppVersion());
            taskDetailResource.setApplicationDeploymentId(o.getApplicationDeploymentId());
            taskDetailResource.setEnableEmailNotifications(o.isAllowNotification());
        }
        return taskDetailResource;
    }

    private static Resource createErrorDetail (ErrorDetail o){
        ErrorDetailResource errorDetailResource = new ErrorDetailResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            errorDetailResource.setExperimentResource(experimentResource);
            if (o.getTask() != null){
                TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
                errorDetailResource.setTaskDetailResource(taskDetailResource);
            }
            if (o.getNodeDetails() != null){
                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetails());
                errorDetailResource.setNodeDetail(nodeDetailResource);
            }
            errorDetailResource.setErrorId(o.getErrorID());
            errorDetailResource.setJobId(o.getJobId());
            errorDetailResource.setCreationTime(o.getCreationTime());
            if (o.getActualErrorMsg() != null){
                errorDetailResource.setActualErrorMsg(new String(o.getActualErrorMsg()));
            }
            errorDetailResource.setUserFriendlyErrorMsg(o.getUserFriendlyErrorMsg());
            errorDetailResource.setTransientPersistent(o.isTransientPersistent());
            errorDetailResource.setErrorCategory(o.getErrorCategory());
            errorDetailResource.setCorrectiveAction(o.getCorrectiveAction());
            errorDetailResource.setActionableGroup(o.getActionableGroup());
        }

        return errorDetailResource;
    }

    private static Resource createApplicationInput (ApplicationInput o){
        ApplicationInputResource inputResource = new ApplicationInputResource();
        if (o != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            inputResource.setTaskDetailResource(taskDetailResource);
            inputResource.setInputKey(o.getInputKey());
            inputResource.setDataType(o.getDataType());
            inputResource.setAppArgument(o.getAppArgument());
            inputResource.setInputOrder(o.getInputOrder());
            inputResource.setStandardInput(o.isStandardInput());
            inputResource.setUserFriendlyDesc(o.getUserFriendlyDesc());
            inputResource.setRequired(o.isRequired());
            inputResource.setRequiredToCMD(o.isRequiredToCMD());
            inputResource.setDataStaged(o.isDataStaged());
            if (o.getValue() != null){
                inputResource.setValue(new String(o.getValue()));
            }
            inputResource.setMetadata(o.getMetadata());
        }
        return inputResource;
    }

    private static Resource createApplicationOutput (ApplicationOutput o){
        ApplicationOutputResource outputResource = new ApplicationOutputResource();
        if (o != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            outputResource.setTaskDetailResource(taskDetailResource);
            outputResource.setDataType(o.getDataType());
            outputResource.setOutputKey(o.getOutputKey());
            if (o.getValue() != null){
                outputResource.setValue(new String(o.getValue()));
            }
            outputResource.setRequired(o.isRequired());
            outputResource.setRequiredToCMD(o.isAddedToCmd());
            outputResource.setDataMovement(o.isDataMovement());
            outputResource.setDataNameLocation(o.getDataNameLocation());
            outputResource.setSearchQuery(o.getSearchQuery());
            outputResource.setAppArgument(o.getApplicationArgument());
        }
        return outputResource;
    }

    private static Resource createNodeInput (NodeInput o){
        NodeInputResource inputResource = new NodeInputResource();
        if (o != null){
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetails());
            inputResource.setNodeDetailResource(nodeDetailResource);
            inputResource.setInputKey(o.getInputKey());
            inputResource.setDataType(o.getDataType());
            inputResource.setValue(o.getValue());
            inputResource.setMetadata(o.getMetadata());
            inputResource.setAppArgument(o.getAppArgument());
            inputResource.setInputOrder(o.getInputOrder());
            inputResource.setStandardInput(o.isStandardInput());
            inputResource.setUserFriendlyDesc(o.getUserFriendlyDesc());
            inputResource.setRequired(o.getIsRequired());
            inputResource.setRequiredToCMD(o.getRequiredToCMD());
            inputResource.setDataStaged(o.isDataStaged());
        }
        return inputResource;
    }

    private static Resource createNodeOutput (NodeOutput o){
        NodeOutputResource outputResource = new NodeOutputResource();
        if (o != null){
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNode());
            outputResource.setNodeDetailResource(nodeDetailResource);
            outputResource.setDataType(o.getDataType());
            outputResource.setOutputKey(o.getOutputKey());
            outputResource.setValue(o.getValue());
            outputResource.setRequired(o.isRequired());
            outputResource.setRequiredToCMD(o.isRequiredToCMD());
            outputResource.setDataMovement(o.isDataMovement());
            outputResource.setDataNameLocation(o.getDataNameLocation());
            outputResource.setSearchQuery(o.getSearchQuery());
            outputResource.setAppArgument(o.getApplicationArgument());
        }

        return outputResource;
    }

    private static Resource createJobDetail (JobDetail o){
        JobDetailResource jobDetailResource = new JobDetailResource();
        if (o != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            jobDetailResource.setTaskDetailResource(taskDetailResource);
            if (o.getJobDescription() != null){
                jobDetailResource.setJobDescription(new String(o.getJobDescription()));
            }
            jobDetailResource.setJobId(o.getJobId());
            jobDetailResource.setCreationTime(o.getCreationTime());
            jobDetailResource.setComputeResourceConsumed(o.getComputeResourceConsumed());
            jobDetailResource.setJobName(o.getJobName());
            jobDetailResource.setWorkingDir(o.getWorkingDir());

        }

        return jobDetailResource;
    }

    private static Resource createDataTransferResource (DataTransferDetail o){
        DataTransferDetailResource transferDetailResource = new DataTransferDetailResource();
        if (o != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            transferDetailResource.setTaskDetailResource(taskDetailResource);
            transferDetailResource.setTransferId(o.getTransferId());
            transferDetailResource.setCreationTime(o.getCreationTime());
            if (o.getTransferDesc() != null){
                transferDetailResource.setTransferDescription(new String(o.getTransferDesc()));
            }

        }
        return transferDetailResource;
    }

    private static Resource createStatusResource (Status o){
        StatusResource statusResource = new StatusResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            statusResource.setExperimentResource(experimentResource);
            if (o.getTask() != null){
                TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
                statusResource.setTaskDetailResource(taskDetailResource);
            }
            if (o.getNode() != null){
                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNode());
                statusResource.setWorkflowNodeDetail(nodeDetailResource);
            }
            if (o.getTransferDetail() != null){
                DataTransferDetailResource transferDetailResource = (DataTransferDetailResource)createDataTransferResource(o.getTransferDetail());
                statusResource.setDataTransferDetail(transferDetailResource);
            }
            statusResource.setStatusId(o.getStatusId());
            statusResource.setJobId(o.getJobId());
            statusResource.setState(o.getState());
            statusResource.setStatusUpdateTime(o.getStatusUpdateTime());
            statusResource.setStatusType(o.getStatusType());
        }

        return statusResource;
    }

    private static Resource createExConfigDataResource (ExperimentConfigData o){
        ConfigDataResource configDataResource = new ConfigDataResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            configDataResource.setExperimentResource(experimentResource);
            configDataResource.setAiravataAutoSchedule(o.isAiravataAutoSchedule());
            configDataResource.setOverrideManualParams(o.isOverrideManualParams());
            configDataResource.setShareExp(o.isShareExp());
            configDataResource.setUserDn(o.getUserDn());
            configDataResource.setGenerateCert(o.isGenerateCert());
        }
        return configDataResource;
    }

    private static Resource createComputationalScheduling (Computational_Resource_Scheduling o){
        ComputationSchedulingResource schedulingResource = new ComputationSchedulingResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            schedulingResource.setExperimentResource(experimentResource);
            if (o.getTask() != null){
                TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
                schedulingResource.setTaskDetailResource(taskDetailResource);
            }
            schedulingResource.setSchedulingId(o.getSchedulingId());
            schedulingResource.setResourceHostId(o.getResourceHostId());
            schedulingResource.setCpuCount(o.getCpuCount());
            schedulingResource.setNodeCount(o.getNodeCount());
            schedulingResource.setNumberOfThreads(o.getNumberOfThreads());
            schedulingResource.setQueueName(o.getQueueName());
            schedulingResource.setWalltimeLimit(o.getWallTimeLimit());
            schedulingResource.setJobStartTime(o.getJobStartTime());
            schedulingResource.setPhysicalMemory(o.getTotalPhysicalmemory());
            schedulingResource.setProjectName(o.getProjectName());
            schedulingResource.setChessisName(o.getChessisName());
        }

        return schedulingResource;
    }

    private static Resource createAdvancedInputDataResource (AdvancedInputDataHandling o){
        AdvanceInputDataHandlingResource dataHandlingResource = new AdvanceInputDataHandlingResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            dataHandlingResource.setExperimentResource(experimentResource);
            if (o.getTask() != null){
                TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
                dataHandlingResource.setTaskDetailResource(taskDetailResource);
            }
            dataHandlingResource.setDataHandlingId(o.getDataHandlingId());
            dataHandlingResource.setWorkingDirParent(o.getParentWorkingDir());
            dataHandlingResource.setWorkingDir(o.getWorkingDir());
            dataHandlingResource.setStageInputFiles(o.isStageInputsToWorkingDir());
            dataHandlingResource.setCleanAfterJob(o.isCleanAfterJob());
        }

        return dataHandlingResource;
    }

    private static Resource createAdvancedOutputDataResource (AdvancedOutputDataHandling o){
        AdvancedOutputDataHandlingResource dataHandlingResource = new AdvancedOutputDataHandlingResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            dataHandlingResource.setExperimentResource(experimentResource);
            if (o.getTask() != null){
                TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
                dataHandlingResource.setTaskDetailResource(taskDetailResource);
            }
            dataHandlingResource.setOutputDataHandlingId(o.getOutputDataHandlingId());
            dataHandlingResource.setOutputDataDir(o.getOutputDataDir());
            dataHandlingResource.setDataRegUrl(o.getDataRegUrl());
            dataHandlingResource.setPersistOutputData(o.isPersistOutputData());
        }
        return dataHandlingResource;
    }

    private static Resource createQosParamResource (QosParam o){
        QosParamResource qosParamResource = new QosParamResource();
        if (o != null){
            ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
            qosParamResource.setExperimentResource(experimentResource);
            if (o.getTask() != null){
                TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
                qosParamResource.setTaskDetailResource(taskDetailResource);
            }
            qosParamResource.setQosId(o.getQosId());
            qosParamResource.setExecuteBefore(o.getExecuteBefore());
            qosParamResource.setStartExecutionAt(o.getStartExecutionAt());
            qosParamResource.setNoOfRetries(o.getNoOfRetries());
        }

        return qosParamResource;
    }
}
