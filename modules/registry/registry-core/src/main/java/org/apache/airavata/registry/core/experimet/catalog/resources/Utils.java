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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.experiment.catalog.JPAConstants;
import org.apache.airavata.experiment.catalog.Resource;
import org.apache.airavata.experiment.catalog.ResourceType;
import org.apache.airavata.experiment.catalog.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


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
            projectResource.setGatewayId(o.getGateway_id());
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
            WorkerResource workerResource = new WorkerResource();
            workerResource.setGatewayId(o.getGateway_id());
            workerResource.setUser(o.getUser_name());
            return workerResource;
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
            experimentResource.setGatewayId(o.getGatewayId());
            experimentResource.setExecutionUser(o.getExecutionUser());
            experimentResource.setProjectId(o.getProjectID());
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
            if (o.getExperimentInputs() != null && !o.getExperimentInputs().isEmpty()){
                experimentResource.setExperimentInputResources(getExperimentInputs(o.getExperimentInputs()));
            }
            if (o.getExperimentOutputs() != null && !o.getExperimentOutputs().isEmpty()){
                experimentResource.setExperimentOutputputResources(getExperimentOutputs(o.getExperimentOutputs()));
            }
            if (o.getResourceScheduling() != null){
                experimentResource.setComputationSchedulingResource((ComputationSchedulingResource)createComputationalScheduling(o.getResourceScheduling()));
            }
            if (o.getUserConfigurationData() != null){
                experimentResource.setUserConfigDataResource((ConfigDataResource)createExConfigDataResource(o.getUserConfigurationData()));
            }

            if (o.getWorkflowNodeDetails() != null && !o.getWorkflowNodeDetails().isEmpty()){
                experimentResource.setWorkflowNodeDetailResourceList(getWorkflowNodeLit(o.getWorkflowNodeDetails()));
            }

            if (o.getStateChangeList() != null && !o.getStateChangeList().isEmpty()){
                experimentResource.setStateChangeList(getStateChangeList(o.getStateChangeList()));
            }

            if (o.getErrorDetails() != null && !o.getErrorDetails().isEmpty()){
                experimentResource.setErrorDetailList(getErrorList(o.getErrorDetails()));
            }

            if (o.getExperimentStatus() != null){
                experimentResource.setExperimentStatus((StatusResource)createStatusResource(o.getExperimentStatus()));
            }

            if (o.getNotificationEmails() != null && !o.getNotificationEmails().isEmpty()){
                experimentResource.setEmailResourceList(getEmailList(o.getNotificationEmails()));
            }
        }
        return experimentResource;
    }

    /**
     *
     * @param o ExperimentSummary model object
     * @return  ExperimentSummary Resource object
     */
    private static Resource createExperimentSummary(Experiment o) {
        ExperimentSummaryResource experimentSummaryResource = new ExperimentSummaryResource();
        if (o != null){
            experimentSummaryResource.setExecutionUser(o.getExecutionUser());
            experimentSummaryResource.setExpID(o.getExpId());
            experimentSummaryResource.setExpName(o.getExpName());
            experimentSummaryResource.setProjectID(o.getProjectID());
            experimentSummaryResource.setCreationTime(o.getCreationTime());
            experimentSummaryResource.setDescription(o.getExpDesc());
            experimentSummaryResource.setApplicationId(o.getApplicationId());

            Status experimentStatus = o.getExperimentStatus();
            if(experimentStatus != null) {
                StatusResource statusResource = new StatusResource();
                statusResource.setStatusId(experimentStatus.getStatusId());
                statusResource.setJobId(experimentStatus.getJobId());
                statusResource.setState(experimentStatus.getState());
                statusResource.setStatusUpdateTime(experimentStatus.getStatusUpdateTime());
                statusResource.setStatusType(experimentStatus.getStatusType());
                experimentSummaryResource.setStatus(statusResource);
            }
        }

        return experimentSummaryResource;
    }

    private static List<ExperimentInputResource> getExperimentInputs(List<Experiment_Input> inputs){
        List<ExperimentInputResource> inputResources = new ArrayList<ExperimentInputResource>();
        for (Experiment_Input input : inputs){
            inputResources.add((ExperimentInputResource)createExperimentInput(input));
        }
        return inputResources;
    }

    private static List<ExperimentOutputResource> getExperimentOutputs(List<Experiment_Output> outputs){
        List<ExperimentOutputResource> outputResources = new ArrayList<>();
        for (Experiment_Output output : outputs){
            outputResources.add((ExperimentOutputResource) createExperimentOutput(output));
        }
        return outputResources;
    }

    private static List<NodeInputResource> getNodeInputs(List<NodeInput> inputs){
        List<NodeInputResource> inputResources = new ArrayList<NodeInputResource>();
        for (NodeInput input : inputs){
            inputResources.add((NodeInputResource)createNodeInput(input));
        }
        return inputResources;
    }

    private static List<NodeOutputResource> getNodeOutputs(List<NodeOutput> outputs){
        List<NodeOutputResource> outputResources = new ArrayList<>();
        for (NodeOutput output : outputs){
            outputResources.add((NodeOutputResource) createNodeOutput(output));
        }
        return outputResources;
    }

    private static List<ApplicationInputResource> getApplicationInputs(List<ApplicationInput> inputs){
        List<ApplicationInputResource> inputResources = new ArrayList<ApplicationInputResource>();
        for (ApplicationInput input : inputs){
            inputResources.add((ApplicationInputResource)createApplicationInput(input));
        }
        return inputResources;
    }

    private static List<ApplicationOutputResource> getApplicationOutputs(List<ApplicationOutput> outputs){
        List<ApplicationOutputResource> outputResources = new ArrayList<>();
        for (ApplicationOutput output : outputs){
            outputResources.add((ApplicationOutputResource) createApplicationOutput(output));
        }
        return outputResources;
    }

    private static List<WorkflowNodeDetailResource> getWorkflowNodeLit(List<WorkflowNodeDetail> nodes){
        List<WorkflowNodeDetailResource> nodeList = new ArrayList<>();
        for (WorkflowNodeDetail node : nodes){
            nodeList.add((WorkflowNodeDetailResource) createWorkflowNodeDetail(node));
        }
        return nodeList;
    }

    private static List<StatusResource> getStateChangeList(List<Status> statusList){
        List<StatusResource> changeList = new ArrayList<>();
        for (Status status : statusList){
            changeList.add((StatusResource) createStatusResource(status));
        }
        return changeList;
    }

    private static List<ErrorDetailResource> getErrorList(List<ErrorDetail> errorDetails){
        List<ErrorDetailResource> errors = new ArrayList<>();
        for (ErrorDetail error : errorDetails){
            errors.add((ErrorDetailResource) createErrorDetail(error));
        }
        return errors;
    }

    private static List<JobDetailResource> getJobDetails(List<JobDetail> jobDetails){
        List<JobDetailResource> resources = new ArrayList<>();
        for (JobDetail jobDetail : jobDetails){
            resources.add((JobDetailResource) createJobDetail(jobDetail));
        }
        return resources;
    }

    private static List<DataTransferDetailResource> getDTDetails(List<DataTransferDetail> dataTransferDetails){
        List<DataTransferDetailResource> resources = new ArrayList<>();
        for (DataTransferDetail detail : dataTransferDetails){
            resources.add((DataTransferDetailResource) createDataTransferResource(detail));
        }
        return resources;
    }

    private static List<NotificationEmailResource> getEmailList(List<Notification_Email> emails){
        List<NotificationEmailResource> emailResources = new ArrayList<>();
        for (Notification_Email email : emails){
            emailResources.add((NotificationEmailResource) createNotificationEmail(email));
        }
        return emailResources;
    }

    private static Resource createNotificationEmail (Notification_Email o){
        NotificationEmailResource emailResource = new NotificationEmailResource();
        if (o != null){
            emailResource.setExperimentId(o.getExperiment_id());
            emailResource.setTaskId(o.getTaskId());
            emailResource.setEmailAddress(o.getEmailAddress());
        }
        return emailResource;
    }

    private static Resource createExperimentInput (Experiment_Input o){
        ExperimentInputResource eInputResource = new ExperimentInputResource();
        if (o != null){
            eInputResource.setExperimentId(o.getExperiment_id());
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
            eOutputResource.setExperimentId(o.getExperiment_id());
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
            nodeDetailResource.setExperimentId(o.getExpId());
            nodeDetailResource.setCreationTime(o.getCreationTime());
            nodeDetailResource.setNodeInstanceId(o.getNodeId());
            nodeDetailResource.setNodeName(o.getNodeName());
            nodeDetailResource.setExecutionUnit(o.getExecutionUnit());
            nodeDetailResource.setExecutionUnitData(o.getExecutionUnitData());
            if (o.getTaskDetails() != null && !o.getErrorDetails().isEmpty()){
                nodeDetailResource.setTaskDetailResourceList(getTaskDetails(o.getTaskDetails()));
            }

            if (o.getNodeInputs() != null && !o.getNodeInputs().isEmpty()){
                nodeDetailResource.setNodeInputs(getNodeInputs(o.getNodeInputs()));
            }

            if (o.getNodeOutputs() != null && !o.getNodeOutputs().isEmpty()){
                nodeDetailResource.setNodeOutputs(getNodeOutputs(o.getNodeOutputs()));
            }

            if (o.getNodeStatus() != null){
                nodeDetailResource.setNodeStatus((StatusResource) createStatusResource(o.getNodeStatus()));
            }

            if (o.getErrorDetails() != null && !o.getErrorDetails().isEmpty()){
                nodeDetailResource.setErros(getErrorList(o.getErrorDetails()));
            }
        }
        return nodeDetailResource;
    }

    private static List<TaskDetailResource> getTaskDetails (List<TaskDetail> taskDetails){
        List<TaskDetailResource> tasks = new ArrayList<>();
        for (TaskDetail detail : taskDetails){
            tasks.add((TaskDetailResource) createTaskDetail(detail));
        }
        return tasks;
    }

    private static Resource createTaskDetail(TaskDetail o){
        TaskDetailResource taskDetailResource = new TaskDetailResource();
        if ( o != null){
            taskDetailResource.setNodeId(o.getNodeId());
            taskDetailResource.setCreationTime(o.getCreationTime());
            taskDetailResource.setTaskId(o.getTaskId());
            taskDetailResource.setApplicationId(o.getAppId());
            taskDetailResource.setApplicationVersion(o.getAppVersion());
            taskDetailResource.setApplicationDeploymentId(o.getApplicationDeploymentId());
            taskDetailResource.setEnableEmailNotifications(o.isAllowNotification());
            if (o.getApplicationInputs() != null && !o.getApplicationInputs().isEmpty()){
                taskDetailResource.setApplicationInputs(getApplicationInputs(o.getApplicationInputs()));
            }
            if (o.getApplicationOutputs() != null && !o.getApplicationOutputs().isEmpty()){
                taskDetailResource.setApplicationOutputs(getApplicationOutputs(o.getApplicationOutputs()));
            }
            if (o.getResourceScheduling() != null){
                taskDetailResource.setSchedulingResource((ComputationSchedulingResource) createComputationalScheduling(o.getResourceScheduling()));

            }
            if (o.getInputDataHandling() != null){
                taskDetailResource.setInputDataHandlingResource((AdvanceInputDataHandlingResource) createAdvancedInputDataResource(o.getInputDataHandling()));
            }
            if (o.getOutputDataHandling() != null){
                taskDetailResource.setOutputDataHandlingResource((AdvancedOutputDataHandlingResource) createAdvancedOutputDataResource(o.getOutputDataHandling()));
            }
            if (o.getErrorDetails() != null && !o.getErrorDetails().isEmpty()){
                taskDetailResource.setErrors(getErrorList(o.getErrorDetails()));
            }
            if (o.getTaskStatus() != null){
                taskDetailResource.setTaskStatus((StatusResource) createStatusResource(o.getTaskStatus()));
            }
            if (o.getNotificationEmails() != null && !o.getNotificationEmails().isEmpty()){
                taskDetailResource.setEmailResourceList(getEmailList(o.getNotificationEmails()));
            }
            if (o.getJobDetails() != null && !o.getJobDetails().isEmpty()){
                taskDetailResource.setJobDetailResources(getJobDetails(o.getJobDetails()));
            }
            if (o.getDataTransferDetails() != null && !o.getDataTransferDetails().isEmpty()){
                taskDetailResource.setTransferDetailResourceList(getDTDetails(o.getDataTransferDetails()));
            }

        }
        return taskDetailResource;
    }

    private static Resource createErrorDetail (ErrorDetail o){
        ErrorDetailResource errorDetailResource = new ErrorDetailResource();
        if (o != null){
            errorDetailResource.setExperimentId(o.getExpId());
            errorDetailResource.setTaskId(o.getTaskId());
            errorDetailResource.setNodeId(o.getNodeId());
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
            inputResource.setTaskId(o.getTaskId());
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
            outputResource.setTaskId(o.getTaskId());
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
            inputResource.setNodeId(o.getNodeId());
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
            outputResource.setNodeId(o.getNodeId());
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
            jobDetailResource.setTaskId(o.getTaskId());
            if (o.getJobDescription() != null){
                jobDetailResource.setJobDescription(new String(o.getJobDescription()));
            }
            jobDetailResource.setJobId(o.getJobId());
            jobDetailResource.setCreationTime(o.getCreationTime());
            jobDetailResource.setComputeResourceConsumed(o.getComputeResourceConsumed());
            jobDetailResource.setJobName(o.getJobName());
            jobDetailResource.setWorkingDir(o.getWorkingDir());
            jobDetailResource.setJobStatus((StatusResource)createStatusResource(o.getJobStatus()));
            jobDetailResource.setErrors(getErrorList(o.getErrorDetails()));
        }

        return jobDetailResource;
    }

    private static Resource createDataTransferResource (DataTransferDetail o){
        DataTransferDetailResource transferDetailResource = new DataTransferDetailResource();
        if (o != null){
            transferDetailResource.setTaskId(o.getTaskId());
            transferDetailResource.setTransferId(o.getTransferId());
            transferDetailResource.setCreationTime(o.getCreationTime());
            if (o.getTransferDesc() != null){
                transferDetailResource.setTransferDescription(new String(o.getTransferDesc()));
            }
            if (o.getDataTransferStatus() != null){
                transferDetailResource.setDatatransferStatus((StatusResource)createStatusResource(o.getDataTransferStatus()));
            }
        }
        return transferDetailResource;
    }

    private static Resource createStatusResource (Status o){
        StatusResource statusResource = new StatusResource();
        if (o != null){
            statusResource.setExperimentId(o.getExpId());
            statusResource.setTaskId(o.getTaskId());
            statusResource.setNodeId(o.getNodeId());
            statusResource.setTransferId(o.getTransferId());
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
            configDataResource.setExperimentId(o.getExpId());
            configDataResource.setAiravataAutoSchedule(o.isAiravataAutoSchedule());
            configDataResource.setOverrideManualParams(o.isOverrideManualParams());
            configDataResource.setShareExp(o.isShareExp());
            configDataResource.setUserDn(o.getUserDn());
            configDataResource.setGenerateCert(o.isGenerateCert());
            if (o.getOutputDataHandling() != null){
                configDataResource.setAdvancedOutputDataHandlingResource((AdvancedOutputDataHandlingResource) createAdvancedOutputDataResource(o.getOutputDataHandling()));
            }
            if (o.getInputDataHandling() != null){
                configDataResource.setAdvanceInputDataHandlingResource((AdvanceInputDataHandlingResource) createAdvancedInputDataResource(o.getInputDataHandling()));
            }
            if (o.getResourceScheduling() != null){
                configDataResource.setComputationSchedulingResource((ComputationSchedulingResource) createComputationalScheduling(o.getResourceScheduling()));
            }
            if (o.getQosParam() != null){
                configDataResource.setQosParamResource((QosParamResource) createQosParamResource(o.getQosParam()));
            }

        }
        return configDataResource;
    }

    private static Resource createComputationalScheduling (Computational_Resource_Scheduling o){
        ComputationSchedulingResource schedulingResource = new ComputationSchedulingResource();
        if (o != null){
            schedulingResource.setExperimentId(o.getExpId());
            schedulingResource.setTaskId(o.getTaskId());
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
            dataHandlingResource.setExperimentId(o.getExpId());
            dataHandlingResource.setTaskId(o.getTaskId());
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
            dataHandlingResource.setExperimentId(o.getExpId());
            dataHandlingResource.setTaskId(o.getTaskId());
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
            qosParamResource.setExperimentId(o.getExpId());
            qosParamResource.setTaskId(o.getTaskId());
            qosParamResource.setQosId(o.getQosId());
            qosParamResource.setExecuteBefore(o.getExecuteBefore());
            qosParamResource.setStartExecutionAt(o.getStartExecutionAt());
            qosParamResource.setNoOfRetries(o.getNoOfRetries());
        }

        return qosParamResource;
    }
}
