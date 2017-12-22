/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.JPAConstants;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.model.Process;
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
    public static ExperimentCatResource getResource(ResourceType type, Object o) {
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
            case NOTIFICATION:
                if (o instanceof Notification){
                    return createNotification((Notification) o);
                } else {
                    logger.error("Object should be a Project.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Project.");
                }
            case PROJECT_USER:
                if (o instanceof ProjectUser){
                    return createProjectUser((ProjectUser) o);
                }else {
                    logger.error("Object should be a ProjectUser.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ProjectUser.");
                }
            case USER:
                if(o instanceof Users) {
                    return createUser((Users) o);
                }else {
                    logger.error("Object should be a User.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a User.");
                }
            case GATEWAY_WORKER:
                if (o instanceof GatewayWorker){
                    return createGatewayWorker((GatewayWorker)o);
                } else {
                    logger.error("Object should be a Gateway Worker.", new IllegalArgumentException());
                    throw  new IllegalArgumentException("Object should be a Gateway Worker.");
                }
            case EXPERIMENT_SUMMARY:
                if (o instanceof  ExperimentSummary){
                    return createExperimentSummary((ExperimentSummary) o);
                }else {
                    logger.error("Object should be a ExperimentSummary.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ExperimentSummary.");
                }
            case EXPERIMENT:
                if (o instanceof  Experiment){
                    return createExperiment((Experiment) o);
                }else {
                    logger.error("Object should be a Experiment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment.");
                }
            case EXPERIMENT_INPUT:
                if (o instanceof  ExperimentInput){
                    return createExperimentInput((ExperimentInput) o);
                }else {
                    logger.error("Object should be a Experiment input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment input data.");
                }
            case EXPERIMENT_OUTPUT:
                if (o instanceof  ExperimentOutput){
                    return createExperimentOutput((ExperimentOutput) o);
                }else {
                    logger.error("Object should be a Experiment output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment output data.");
                }
            case EXPERIMENT_STATUS:
                if (o instanceof  ExperimentStatus){
                    return createExperimentStatusResource((ExperimentStatus) o);
                }else {
                    logger.error("Object should be a ExperimentStatus data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ExperimentStatus data.");
                }
            case EXPERIMENT_ERROR:
                if (o instanceof  ExperimentError){
                    return createExperimentError((ExperimentError) o);
                }else {
                    logger.error("Object should be a experiment error data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a experiment error data.");
                }
            case USER_CONFIGURATION_DATA:
                if (o instanceof  UserConfigurationData){
                    return createUserConfigData((UserConfigurationData) o);
                }else {
                    logger.error("Object should be a user config data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a user config data.");
                }
            case PROCESS:
                if (o instanceof Process){
                    return createProcess((Process) o);
                }else {
                    logger.error("Object should be a process error data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a process error data.");
                }
            case PROCESS_ERROR:
                if (o instanceof  ProcessError){
                    return createProcessError((ProcessError) o);
                }else {
                    logger.error("Object should be a process error data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a process error data.");
                }
            case PROCESS_STATUS:
                if (o instanceof  ProcessStatus){
                    return createProcessStatusResource((ProcessStatus) o);
                }else {
                    logger.error("Object should be a ProcessStatus data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a ProcessStatus data.");
                }
            case PROCESS_INPUT:
                if (o instanceof  ProcessInput){
                    return createProcessInput((ProcessInput)o);
                }else {
                    logger.error("Object should be a process input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a process input data.");
                }
            case PROCESS_OUTPUT:
                if (o instanceof  ProcessOutput){
                    return createProcessOutput((ProcessOutput)o);
                }else {
                    logger.error("Object should be a process output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a process output data.");
                }
            case PROCESS_RESOURCE_SCHEDULE:
                if (o instanceof  ProcessResourceSchedule){
                    return createProcessResourceSchedule((ProcessResourceSchedule) o);
                }else {
                    logger.error("Object should be a scheduling resource data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be scheduling resource data.");
                }
            case TASK:
                if (o instanceof  Task){
                    return createTask((Task)o);
                }else {
                    logger.error("Object should be a task data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a task data.");
                }
            case TASK_STATUS:
                if (o instanceof  TaskStatus){
                    return createTaskStatusResource((TaskStatus) o);
                }else {
                    logger.error("Object should be a TaskStatus data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a TaskStatus data.");
                }
            case TASK_ERROR:
                if (o instanceof  TaskError){
                    return createTaskError((TaskError)o);
                }else {
                    logger.error("Object should be a task error data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be task error data.");
                }
            case JOB:
                if (o instanceof  Job){
                    return createJobResource((Job) o);
                }else {
                    logger.error("Object should be a Job data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Job data.");
                }
            case JOB_STATUS:
                if (o instanceof  JobStatus){
                    return createJobStatusResource((JobStatus) o);
                }else {
                    logger.error("Object should be a JobStatus data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a JobStatus data.");
                }
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }

    private static ExperimentCatResource createGateway(Gateway o) {
        GatewayResource gatewayResource = new GatewayResource();
        gatewayResource.setGatewayName(o.getGatewayName());
        gatewayResource.setGatewayId(o.getGatewayId());
        gatewayResource.setDomain(o.getDomain());
        gatewayResource.setEmailAddress(o.getEmailAddress());
        gatewayResource.setGatewayApprovalStatus(o.getGatewayApprovalStatus());
        gatewayResource.setGatewayAcronym(o.getGatewayAcronym());
        gatewayResource.setGatewayUrl(o.getGatewayUrl());
        gatewayResource.setGatewayPublicAbstract(o.getGatewayPublicAbstract());
        gatewayResource.setReviewProposalDescription(o.getReviewProposalDescription());
        gatewayResource.setGatewayAdminFirstName(o.getGatewayAdminFirstName());
        gatewayResource.setGetGatewayAdminLastName(o.getGetGatewayAdminLastName());
        gatewayResource.setGatewayAdminEmail(o.getGatewayAdminEmail());
        gatewayResource.setIdentityServerUserName(o.getIdentityServerUserName());
        gatewayResource.setIdentityServerPasswordToken(o.getIdentityServerPasswordToken());
        gatewayResource.setDeclinedReason(o.getDeclinedReason());
        gatewayResource.setOauthClientId(o.getOauthClientId());
        gatewayResource.setRequestCreationTime(o.getRequestCreationTime());
        gatewayResource.setRequesterUsername(o.getRequesterUsername());

        gatewayResource.setOauthClientSecret(o.getGetOauthClientSecret());
        return gatewayResource;
    }


    private static ExperimentCatResource createProject(Project o) {
        ProjectResource projectResource = new ProjectResource();
        if (o != null){
            projectResource.setId(o.getProjectId());
            projectResource.setName(o.getProjectName());
            projectResource.setGatewayId(o.getGatewayId());
            GatewayWorker gatewayWorker = new GatewayWorker();
            gatewayWorker.setGateway(o.getGateway());
            gatewayWorker.setUserName(o.getUserName());
            WorkerResource workerResource = (WorkerResource) createGatewayWorker(gatewayWorker);
            projectResource.setWorker(workerResource);
            projectResource.setDescription(o.getDescription());
            projectResource.setCreationTime(o.getCreationTime());
        }

        return projectResource;
    }

    private static ExperimentCatResource createNotification(Notification o) {
        NotificationResource notificationResource = new NotificationResource();
        if (o != null){
            notificationResource.setNotificationId(o.getNotificationId());
            notificationResource.setGatewayId(o.getGatewayId());
            notificationResource.setTitle(o.getTitle());
            notificationResource.setNotificationMessage(o.getNotificationMessage());
            notificationResource.setPublishedTime(o.getPublishedDate());
            notificationResource.setExpirationTime(o.getExpirationDate());
            notificationResource.setCreationTime(o.getCreationDate());
            notificationResource.setPriority(o.getPriority());
        }

        return notificationResource;
    }

    private static ExperimentCatResource createProjectUser(ProjectUser o) {
        ProjectUserResource projectUserResource = new ProjectUserResource();
        if (o != null){
            projectUserResource.setUserName(o.getUser().getUserName());
            projectUserResource.setProjectId(o.getProjectId());
        }
        return projectUserResource;
    }

    private static ExperimentCatResource createGatewayWorker(GatewayWorker o) {
        if (o != null){
            WorkerResource workerResource = new WorkerResource();
            workerResource.setGatewayId(o.getGatewayId());
            workerResource.setUser(o.getUserName());
            return workerResource;
        }
        return null;
    }

    private static ExperimentCatResource createUser(Users o) {
        UserResource userResource = new UserResource();
        if (o != null){
            userResource.setUserName(o.getUserName());
            userResource.setPassword(o.getPassword());
            userResource.setGatewayId(o.getGatewayId());
        }
        return userResource;
    }


    private static ExperimentCatResource createExperimentSummary(ExperimentSummary o) {
        ExperimentSummaryResource experimentSummaryResource = new ExperimentSummaryResource();
        if (o != null){
            experimentSummaryResource.setExperimentId(o.getExperimentId());
            experimentSummaryResource.setProjectId(o.getProjectId());
            experimentSummaryResource.setUserName(o.getUserName());
            experimentSummaryResource.setGatewayId(o.getGatewayId());
            experimentSummaryResource.setExecutionId(o.getExecutionId());
            experimentSummaryResource.setExperimentName(o.getExperimentName());
            experimentSummaryResource.setCreationTime(o.getCreationTime());
            experimentSummaryResource.setDescription(o.getDescription());
            experimentSummaryResource.setState(o.getState());
            experimentSummaryResource.setResourceHostId(o.getResourceHostId());
            experimentSummaryResource.setTimeOfStateChange(o.getTimeOfStateChange());
        }
        return experimentSummaryResource;
    }

    private static ExperimentCatResource createExperiment(Experiment o) {
        ExperimentResource experimentResource = new ExperimentResource();
        if (o != null){
            experimentResource.setExperimentId(o.getExperimentId());
            experimentResource.setProjectId(o.getProjectId());
            experimentResource.setGatewayId(o.getGatewayId());
            experimentResource.setExperimentType(o.getExperimentType());
            experimentResource.setUserName(o.getUserName());
            experimentResource.setExperimentName(o.getExperimentName());
            experimentResource.setCreationTime(o.getCreationTime());
            experimentResource.setDescription(o.getDescription());
            experimentResource.setExecutionId(o.getExecutionId());
            experimentResource.setGatewayExecutionId(o.getGatewayExecutionId());
            experimentResource.setGatewayInstanceId(o.getGatewayInstanceId());
            experimentResource.setEnableEmailNotification(o.getEnableEmailNotification());
            experimentResource.setEmailAddresses(o.getEmailAddresses());
        }
        return experimentResource;
    }

    private static ExperimentCatResource createExperimentInput (ExperimentInput o){
        ExperimentInputResource inputResource = new ExperimentInputResource();
        if (o != null){
            inputResource.setExperimentId(o.getExperimentId());
            inputResource.setInputName(o.getInputName());
            inputResource.setInputValue(o.getInputValue());
            inputResource.setDataType(o.getDataType());
            inputResource.setApplicationArgument(o.getApplicationArgument());
            inputResource.setStandardInput(o.getStandardInput());
            inputResource.setUserFriendlyDescription(o.getUserFriendlyDescription());
            inputResource.setMetadata(o.getMetadata());
            inputResource.setInputOrder(o.getInputOrder());
            inputResource.setIsRequired(o.getIsRequired());
            inputResource.setRequiredToAddedToCmd(o.getRequiredToAddedToCmd());
            inputResource.setDataStaged(o.getDataStaged());
            inputResource.setIsReadOnly(o.isReadOnly());
        }
        return inputResource;
    }

    private static ExperimentCatResource createExperimentOutput (ExperimentOutput o){
        ExperimentOutputResource outputResource = new ExperimentOutputResource();
        if (o != null){
            outputResource.setExperimentId(o.getExperimentId());
            outputResource.setOutputName(o.getOutputName());
            outputResource.setOutputValue(o.getOutputValue());
            outputResource.setDataType(o.getDataType());
            outputResource.setApplicationArgument(o.getApplicationArgument());
            outputResource.setIsRequired(o.getIsRequired());
            outputResource.setRequiredToAddedToCmd(o.getRequiredToAddedToCmd());
            outputResource.setDataMovement(o.getDataMovement());
            outputResource.setLocation(o.getLocation());
            outputResource.setSearchQuery(o.getSearchQuery());
            outputResource.setOutputStreaming(o.isOutputStreaming());
        }
        return outputResource;
    }

    private static ExperimentCatResource createTaskError (TaskError o){
        TaskErrorResource taskErrorResource = new TaskErrorResource();
        if (o != null){
            taskErrorResource.setTaskId(o.getTaskId());
            taskErrorResource.setErrorId(o.getErrorId());
            taskErrorResource.setCreationTime(o.getCreationTime());
            taskErrorResource.setActualErrorMessage(o.getActualErrorMessage());
            taskErrorResource.setUserFriendlyMessage(o.getUserFriendlyMessage());
            taskErrorResource.setTransientOrPersistent(o.getTransientOrPersistent());
            taskErrorResource.setRootCauseErrorIdList(o.getRootCauseErrorIdList());
        }

        return taskErrorResource;
    }

    private static ExperimentCatResource createExperimentError (ExperimentError o){
        ExperimentErrorResource experimentErrorResource = new ExperimentErrorResource();
        if (o != null){
            experimentErrorResource.setExperimentId(o.getExperimentId());
            experimentErrorResource.setErrorId(o.getErrorId());
            experimentErrorResource.setCreationTime(o.getCreationTime());
            experimentErrorResource.setActualErrorMessage(o.getActualErrorMessage());
            experimentErrorResource.setUserFriendlyMessage(o.getUserFriendlyMessage());
            experimentErrorResource.setTransientOrPersistent(o.getTransientOrPersistent());
            experimentErrorResource.setRootCauseErrorIdList(o.getRootCauseErrorIdList());
        }
        return experimentErrorResource;
    }

    private static ExperimentCatResource createUserConfigData (UserConfigurationData o){
        UserConfigurationDataResource configurationDataResource = new UserConfigurationDataResource();
        if (o != null){
            configurationDataResource.setExperimentId(o.getExperimentId());
            configurationDataResource.setAiravataAutoSchedule(o.getAiravataAutoSchedule());
            configurationDataResource.setOverrideManualScheduledParams(o.getOverrideManualScheduledParams());
            configurationDataResource.setShareExperimentPublically(o.getShareExperimentPublically());
            configurationDataResource.setThrottleResources(o.getThrottleResources());
            configurationDataResource.setUserDn(o.getUserDn());
            configurationDataResource.setGenerateCert(o.getGenerateCert());
            configurationDataResource.setResourceHostId(o.getResourceHostId());
            configurationDataResource.setTotalCpuCount(o.getTotalCpuCount());
            configurationDataResource.setNodeCount(o.getNodeCount());
            configurationDataResource.setNumberOfThreads(o.getNumberOfThreads());
            configurationDataResource.setQueueName(o.getQueueName());
            configurationDataResource.setWallTimeLimit(o.getWallTimeLimit());
            configurationDataResource.setTotalPhysicalMemory(o.getTotalPhysicalMemory());
            configurationDataResource.setStaticWorkingDir(o.getStaticWorkingDir());
            configurationDataResource.setOverrideLoginUserName(o.getOverrideLoginUserName());
            configurationDataResource.setOverrideScratchLocation(o.getOverrideScratchLocation());
            configurationDataResource.setOverrideAllocationProjectNumber(o.getOverrideAllocationProjectNumber());
            configurationDataResource.setStorageId(o.getStorageId());
            configurationDataResource.setExperimentDataDir(o.getExperimentDataDir());
            configurationDataResource.setUseUserCRPref(o.isUseUserCRPref());
        }
        return configurationDataResource;
    }

    private static ExperimentCatResource createProcess (Process o){
        ProcessResource processResource = new ProcessResource();
        if (o != null){
            processResource.setProcessId(o.getProcessId());
            processResource.setExperimentId(o.getExperimentId());
            processResource.setCreationTime(o.getCreationTime());
            processResource.setLastUpdateTime(o.getLastUpdateTime());
            processResource.setProcessDetail(o.getProcessDetail());
            processResource.setApplicationInterfaceId(o.getApplicationInterfaceId());
            processResource.setTaskDag(o.getTaskDag());
            processResource.setGatewayExecutionId(o.getGatewayExecutionId());
            processResource.setComputeResourceId(o.getComputeResourceId());
            processResource.setApplicationDeploymentId(o.getApplicationDeploymentId());
            processResource.setEnableEmailNotification(o.getEnableEmailNotification());
            processResource.setEmailAddresses(o.getEmailAddresses());
            processResource.setStorageResourceId(o.getStorageId());
            processResource.setUserDn(o.getUserDn());
            processResource.setGenerateCert(o.getGenerateCert());
            processResource.setExperimentDataDir(o.getExperimentDataDir());
            processResource.setUserName(o.getUserName());
            processResource.setUseUserCRPref(o.isUseUserCRPref());
            processResource.setProcessTypeValue(o.getProcessTypeValue());
        }
        return processResource;
    }

    private static ExperimentCatResource createProcessError (ProcessError o){
        ProcessErrorResource processErrorResource = new ProcessErrorResource();
        if (o != null){
            processErrorResource.setProcessId(o.getProcessId());
            processErrorResource.setErrorId(o.getErrorId());
            processErrorResource.setCreationTime(o.getCreationTime());
            processErrorResource.setActualErrorMessage(o.getActualErrorMessage());
            processErrorResource.setUserFriendlyMessage(o.getUserFriendlyMessage());
            processErrorResource.setTransientOrPersistent(o.getTransientOrPersistent());
            processErrorResource.setRootCauseErrorIdList(o.getRootCauseErrorIdList());
        }
        return processErrorResource;
    }

    private static ExperimentCatResource createProcessInput (ProcessInput o){
        ProcessInputResource inputResource = new ProcessInputResource();
        if (o != null){
            inputResource.setProcessId(o.getProcessId());
            inputResource.setInputName(o.getInputName());
            inputResource.setInputValue(o.getInputValue());
            inputResource.setDataType(o.getDataType());
            inputResource.setApplicationArgument(o.getApplicationArgument());
            inputResource.setStandardInput(o.getStandardInput());
            inputResource.setUserFriendlyDescription(o.getUserFriendlyDescription());
            inputResource.setMetadata(o.getMetadata());
            inputResource.setInputOrder(o.getInputOrder());
            inputResource.setIsRequired(o.getIsRequired());
            inputResource.setRequiredToAddedToCmd(o.getRequiredToAddedToCmd());
            inputResource.setDataStaged(o.getDataStaged());
            inputResource.setIsReadOnly(o.getIsReadOnly());
        }
        return inputResource;
    }

    private static ExperimentCatResource createProcessOutput (ProcessOutput o){
        ProcessOutputResource outputResource = new ProcessOutputResource();
        if (o != null){
            outputResource.setProcessId(o.getProcessId());
            outputResource.setOutputName(o.getOutputName());
            outputResource.setOutputValue(o.getOutputValue());
            outputResource.setDataType(o.getDataType());
            outputResource.setApplicationArgument(o.getApplicationArgument());
            outputResource.setIsRequired(o.getIsRequired());
            outputResource.setRequiredToAddedToCmd(o.getRequiredToAddedToCmd());
            outputResource.setDataMovement(o.getDataMovement());
            outputResource.setLocation(o.getLocation());
            outputResource.setSearchQuery(o.getSearchQuery());
            outputResource.setOutputStreaming(o.isOutputStreaming());
        }
        return outputResource;
    }

    private static ExperimentCatResource createTask (Task o){
        TaskResource taskResource = new TaskResource();
        if (o != null){
            taskResource.setTaskId(o.getTaskId());
            taskResource.setTaskType(o.getTaskType());
            taskResource.setParentProcessId(o.getParentProcessId());
            taskResource.setCreationTime(o.getCreationTime());
            taskResource.setLastUpdateTime(o.getLastUpdateTime());
            taskResource.setTaskDetail(o.getTaskDetail());
            taskResource.setSubTaskModel(o.getSetSubTaskModel());
        }
        return taskResource;
    }

    private static ExperimentCatResource createTaskStatusResource (TaskStatus o){
        TaskStatusResource taskStatusResource = new TaskStatusResource();
        if (o != null){
            taskStatusResource.setTaskId(o.getTaskId());
            taskStatusResource.setStatusId(o.getStatusId());
            taskStatusResource.setState(o.getState());
	        taskStatusResource.setStatusId(o.getStatusId());
            taskStatusResource.setTimeOfStateChange(o.getTimeOfStateChange());
            taskStatusResource.setReason(o.getReason());
        }

        return taskStatusResource;
    }

    private static ExperimentCatResource createProcessStatusResource (ProcessStatus o){
        ProcessStatusResource processStatusResource = new ProcessStatusResource();
        if (o != null){
            processStatusResource.setProcessId(o.getProcessId());
            processStatusResource.setStatusId(o.getStatusId());
            processStatusResource.setState(o.getState());
            processStatusResource.setTimeOfStateChange(o.getTimeOfStateChange());
            processStatusResource.setReason(o.getReason());
        }
        return processStatusResource;
    }

    private static ExperimentCatResource createExperimentStatusResource (ExperimentStatus o){
        ExperimentStatusResource experimentStatusResource = new ExperimentStatusResource();
        if (o != null){
            experimentStatusResource.setExperimentId(o.getExperimentId());
            experimentStatusResource.setStatusId(o.getStatusId());
            experimentStatusResource.setState(o.getState());
            experimentStatusResource.setTimeOfStateChange(o.getTimeOfStateChange());
            experimentStatusResource.setReason(o.getReason());
        }
        return experimentStatusResource;
    }

    public static ExperimentCatResource createProcessResourceSchedule(ProcessResourceSchedule o){
        ProcessResourceScheduleResource resourceScheduleResource = new ProcessResourceScheduleResource();
        if(o != null){
            resourceScheduleResource.setProcessId(o.getProcessId());
            resourceScheduleResource.setResourceHostId(o.getResourceHostId());
            resourceScheduleResource.setTotalCpuCount(o.getTotalCpuCount());
            resourceScheduleResource.setNodeCount(o.getNodeCount());
            resourceScheduleResource.setNumberOfThreads(o.getNumberOfThreads());
            resourceScheduleResource.setQueueName(o.getQueueName());
            resourceScheduleResource.setWallTimeLimit(o.getWallTimeLimit());
            resourceScheduleResource.setTotalPhysicalMemory(o.getTotalPhysicalMemory());
            resourceScheduleResource.setStaticWorkingDir(o.getStaticWorkingDir());
            resourceScheduleResource.setOverrideLoginUserName(o.getOverrideLoginUserName());
            resourceScheduleResource.setOverrideScratchLocation(o.getOverrideScratchLocation());
            resourceScheduleResource.setOverrideAllocationProjectNumber(o.getOverrideAllocationProjectNumber());
        }
        return resourceScheduleResource;
    }

    private static ExperimentCatResource createJobResource (Job o){
        JobResource jobResource = new JobResource();
        if (o != null){
            jobResource.setJobId(o.getJobId());
	        jobResource.setProcessId(o.getProcessId());
            jobResource.setTaskId(o.getTaskId());
            jobResource.setCreationTime(o.getCreationTime());
            if (o.getJobDescription() != null){
                jobResource.setJobDescription(new String(o.getJobDescription()));
            }
            if (o.getStdErr() != null){
                jobResource.setStdErr(new String(o.getStdErr()));
            }
            if (o.getStdOut() != null){
                jobResource.setStdOut(new String(o.getStdOut()));
            }
            jobResource.setComputeResourceConsumed(o.getComputeResourceConsumed());
            jobResource.setJobName(o.getJobName());
            jobResource.setWorkingDir(o.getWorkingDir());
            jobResource.setExitCode(o.getExitCode());
        }
        return jobResource;
    }

    private static ExperimentCatResource createJobStatusResource (JobStatus o){
        JobStatusResource jobStatusResource = new JobStatusResource();
        if (o != null){
            jobStatusResource.setJobId(o.getJobId());
	        jobStatusResource.setStatusId(o.getStatusId());
	        jobStatusResource.setTaskId(o.getTaskId());
	        jobStatusResource.setState(o.getState());
            jobStatusResource.setTimeOfStateChange(o.getTimeOfStateChange());
            jobStatusResource.setReason(o.getReason());
        }

        return jobStatusResource;
    }
}