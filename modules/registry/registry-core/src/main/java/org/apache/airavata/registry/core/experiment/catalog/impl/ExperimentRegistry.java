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

package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class ExperimentRegistry {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRegistry.class);

    public ExperimentRegistry(GatewayResource gateway, UserResource user) throws RegistryException {
        gatewayResource = gateway;
        if (!gatewayResource.isExists(ResourceType.GATEWAY_WORKER, user.getUserName())) {
            workerResource = ExpCatResourceUtils.addGatewayWorker(gateway, user);
        } else {
            workerResource = (WorkerResource) ExpCatResourceUtils.getWorker(gateway.getGatewayId(), user.getUserName());
        }

    }

    //CPI Add Methods

    public String addExperiment(ExperimentModel experiment) throws RegistryException {
        String experimentId;
        try {
            if (!ExpCatResourceUtils.isUserExist(experiment.getUserName())) {
                ExpCatResourceUtils.addUser(experiment.getUserName(), null);
            }
            if (!workerResource.isProjectExists(experiment.getProjectId())) {
                logger.error("Project does not exist in the system..");
                throw new Exception("Project does not exist in the system, Please create the project first...");
            }
            experimentId = getExperimentID(experiment.getExperimentName());
            experiment.setExperimentId(experimentId);
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setExperimentId(experimentId);
            experimentResource.setProjectId(experiment.getProjectId());
            experimentResource.setExperimentType(experiment.getExperimentType().toString());
            experimentResource.setUserName(experiment.getUserName());
            experimentResource.setExperimentName(experiment.getExperimentName());
            experimentResource.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
            experimentResource.setDescription(experiment.getDescription());
            experimentResource.setExecutionId(experiment.getExecutionId());
            experimentResource.setGatewayExecutionId(experiment.getGatewayExecutionId());
            if(experiment.isEnableEmailNotification()){
                experimentResource.setEnableEmailNotification(true);
                if(experiment.getEmailAddresses() != null){
                    experimentResource.setEmailAddresses(StringUtils.join(experiment.getEmailAddresses(), ","));
                }
            }else{
                experimentResource.setEnableEmailNotification(false);
            }
            experimentResource.save();
            if(experiment.getUserConfigurationData() != null) {
                addUserConfigData(experiment.getUserConfigurationData(), experimentId);
            }
            if(experiment.getExperimentInputs() != null && experiment.getExperimentInputs().size() > 0) {
                addExpInputs(experiment.getExperimentInputs(), experimentId);
            }
            if(experiment.getExperimentOutputs() != null && experiment.getExperimentOutputs().size() > 0) {
                addExpOutputs(experiment.getExperimentOutputs(), experimentId);
            }

            ExperimentStatus experimentStatus = new ExperimentStatus();
            experimentStatus.setState(ExperimentState.CREATED);
            addExperimentStatus(experimentStatus, experimentId);

            List<ErrorModel> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (ErrorModel errror : errors) {
                    addExperimentError(errror, experimentId);
                }
            }
        } catch (Exception e) {
            logger.error("Error while saving experiment to registry", e);
            throw new RegistryException(e);
        }
        return experimentId;
    }

    public String addUserConfigData(UserConfigurationDataModel configurationData, String experimentId) throws RegistryException {
        try {
            UserConfigurationDataResource configDataResource = new UserConfigurationDataResource();
            configDataResource.setExperimentId(experimentId);
            configDataResource.setAiravataAutoSchedule(configurationData.isAiravataAutoSchedule());
            configDataResource.setOverrideManualScheduledParams(configurationData.isOverrideManualScheduledParams());
            configDataResource.setShareExperimentPublically(configurationData.isShareExperimentPublicly());
            configDataResource.setThrottleResources(configurationData.isThrottleResources());
            configDataResource.setUserDn(configurationData.getUserDN());
            configDataResource.setGenerateCert(configurationData.isGenerateCert());
            configDataResource.setResourceHostId(configurationData.getComputationalResourceScheduling().getResourceHostId());
            configDataResource.setTotalCpuCount(configurationData.getComputationalResourceScheduling().getTotalCPUCount());
            configDataResource.setNodeCount(configurationData.getComputationalResourceScheduling().getNodeCount());
            configDataResource.setNumberOfThreads(configurationData.getComputationalResourceScheduling().getNumberOfThreads());
            configDataResource.setQueueName(configurationData.getComputationalResourceScheduling().getQueueName());
            configDataResource.setWallTimeLimit(configurationData.getComputationalResourceScheduling().getWallTimeLimit());
            configDataResource.setTotalPhysicalMemory(configurationData.getComputationalResourceScheduling().getTotalPhysicalMemory());
            configDataResource.save();
        } catch (Exception e) {
            logger.error("Unable to save user config data", e);
            throw new RegistryException(e);
        }
        return experimentId;
    }

    public String addExpInputs(List<InputDataObjectType> exInputs, String experimentId) throws RegistryException {
        try {
            for (InputDataObjectType input : exInputs) {
                ExperimentInputResource resource = new ExperimentInputResource();
                resource.setExperimentId(experimentId);
                resource.setInputName(input.getName());
                resource.setInputValue(input.getValue());
                if (input.getType() != null) {
                    resource.setDataType(input.getType().toString());
                }
                resource.setMetadata(input.getMetaData());
                resource.setApplicationArgument(input.getApplicationArgument());
                resource.setInputOrder(input.getInputOrder());
                resource.setIsRequired(input.isIsRequired());
                resource.setIsRequired(input.isRequiredToAddedToCommandLine());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Unable to save experiment inputs", e);
            throw new RegistryException(e);
        }
        return experimentId;
    }

    public String addExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {
        try {
            for (OutputDataObjectType output : exOutput) {
                ExperimentOutputResource resource = new ExperimentOutputResource();
                resource.setExperimentId(expId);
                resource.setOutputName(output.getName());
                resource.setOutputValue(output.getValue());
                if (output.getType() != null) {
                    resource.setDataType(output.getType().toString());
                }
                resource.setApplicationArgument(output.getApplicationArgument());
                resource.setIsRequired(output.isIsRequired());
                resource.setRequiredToAddedToCmd(output.isRequiredToAddedToCommandLine());
                resource.setDataMovement(output.isDataMovement());
                resource.setLocation(output.getLocation());
                resource.setSearchQuery(output.getSearchQuery());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding experiment outputs...", e);
            throw new RegistryException(e);
        }
        return expId;
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String expId) throws RegistryException {
        try {
            ExperimentResource experiment = new ExperimentResource();
            experiment.setExperimentId(expId);
            ExperimentStatusResource status = experiment.getExperimentStatus();
            if (status == null) {
                status = (ExperimentStatusResource) experiment.create(ResourceType.EXPERIMENT_STATUS);
            }
            if (isValidStatusTransition(ExperimentState.valueOf(status.getState()), experimentStatus.getState())) {
                status.setStatusId(getStatusID(expId));
                status.setExperimentId(expId);
                status.setTimeOfStateChange(AiravataUtils.getTime(experimentStatus.getTimeOfStateChange()));
                status.setState(experimentStatus.getState().toString());
                status.setReason(experimentStatus.getReason());
                status.save();
                logger.debug(expId, "Added experiment {} status to {}.", expId, experimentStatus.toString());
            }
        } catch (Exception e) {
            logger.error(expId, "Error while adding experiment status...", e);
            throw new RegistryException(e);
        }
        return expId;
    }

    public String addExperimentError(ErrorModel experimentError, String expId) throws RegistryException {
        try {
            ExperimentErrorResource error = new ExperimentErrorResource();
            error.setErrorId(getErrorID(expId));
            error.setExperimentId(expId);
            error.setCreationTime(AiravataUtils.getTime(experimentError.getCreationTime()));
            error.setActualErrorMessage(experimentError.getActualErrorMessage());
            error.setUserFriendlyMessage(experimentError.getUserFriendlyMessage());
            error.setTransientOrPersistent(experimentError.isTransientOrPersistent());
            if(experimentError.getRootCauseErrorIdList() != null) {
                error.setRootCauseErrorIdList(StringUtils.join(experimentError.getRootCauseErrorIdList(), ","));
            }
            error.save();
        } catch (Exception e) {
            logger.error(expId, "Error while updating experiment status...", e);
            throw new RegistryException(e);
        }
        return expId;
    }

    public String addProcess(ProcessModel process, String expId) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            processResource.setProcessId(getProcessID(expId));
            processResource.setExperimentId(expId);
            processResource.setCreationTime(AiravataUtils.getTime(process.getCreationTime()));
            processResource.setLastUpdateTime(AiravataUtils.getTime(process.getLastUpdateTime()));
            processResource.setProcessDetail(process.getProcessDetail());
            processResource.setApplicationInterfaceId(process.getApplicationInterfaceId());
            processResource.setTaskDag(process.getTaskDag());
            processResource.save();

            if(process.getResourceSchedule() != null) {
                addProcessResourceSchedule(process.getResourceSchedule(), process.getProcessId());
            }
            if(process.getProcessInputs() !=  null && process.getProcessInputs().size() > 0) {
                addProcessInputs(process.getProcessInputs(), process.getProcessId());
            }
            if(process.getProcessOutputs() != null && process.getProcessOutputs().size() > 0) {
                addProcessOutputs(process.getProcessOutputs(), process.getProcessId());
            }

            ProcessStatus processStatus = new ProcessStatus();
            processStatus.setState(ProcessState.CREATED);
            addProcessStatus(processStatus, process.getProcessId());

            if(process.getProcessError() != null) {
                addProcessError(process.getProcessError(), process.getProcessId());
            }
        } catch (Exception e) {
            logger.error(expId, "Error while adding process...", e);
            throw new RegistryException(e);
        }
        return expId;
    }


    public String addProcessResourceSchedule(ComputationalResourceSchedulingModel resourceSchedule, String processID) throws RegistryException {
        try {
            ProcessResourceScheduleResource processResourceSchedule = new ProcessResourceScheduleResource();
            processResourceSchedule.setProcessId(processID);
            processResourceSchedule.setResourceHostId(resourceSchedule.getResourceHostId());
            processResourceSchedule.setTotalCpuCount(resourceSchedule.getTotalCPUCount());
            processResourceSchedule.setNodeCount(resourceSchedule.getNodeCount());
            processResourceSchedule.setNumberOfThreads(resourceSchedule.getNumberOfThreads());
            processResourceSchedule.setQueueName(resourceSchedule.getQueueName());
            processResourceSchedule.setWallTimeLimit(resourceSchedule.getWallTimeLimit());
            processResourceSchedule.setTotalPhysicalMemory(resourceSchedule.getTotalPhysicalMemory());
            processResourceSchedule.save();
        } catch (Exception e) {
            logger.error("Unable to save user config data", e);
            throw new RegistryException(e);
        }
        return processID;
    }

    public String addProcessInputs(List<InputDataObjectType> processInputs, String processID) throws RegistryException {
        try {
            for (InputDataObjectType input : processInputs) {
                ProcessInputResource resource = new ProcessInputResource();
                resource.setProcessId(processID);
                resource.setInputName(input.getName());
                resource.setInputValue(input.getValue());
                if (input.getType() != null) {
                    resource.setDataType(input.getType().toString());
                }
                resource.setMetadata(input.getMetaData());
                resource.setApplicationArgument(input.getApplicationArgument());
                resource.setInputOrder(input.getInputOrder());
                resource.setIsRequired(input.isIsRequired());
                resource.setIsRequired(input.isRequiredToAddedToCommandLine());
                resource.save();
            }
            return processID;
        } catch (Exception e) {
            logger.error("Unable to save process inputs", e);
            throw new RegistryException(e);
        }
    }

    public String addProcessOutputs(List<OutputDataObjectType> processOutput, String processID) throws RegistryException {
        try {
            for (OutputDataObjectType output : processOutput) {
                ProcessOutputResource resource = new ProcessOutputResource();
                resource.setProcessId(processID);
                resource.setOutputName(output.getName());
                resource.setOutputValue(output.getValue());
                if (output.getType() != null) {
                    resource.setDataType(output.getType().toString());
                }
                resource.setApplicationArgument(output.getApplicationArgument());
                resource.setIsRequired(output.isIsRequired());
                resource.setRequiredToAddedToCmd(output.isRequiredToAddedToCommandLine());
                resource.setDataMovement(output.isDataMovement());
                resource.setLocation(output.getLocation());
                resource.setSearchQuery(output.getSearchQuery());
                resource.save();
            }
            return processID;
        } catch (Exception e) {
            logger.error("Error while adding process outputs...", e);
            throw new RegistryException(e);
        }
    }

    public String addProcessStatus(ProcessStatus processStatus, String processID) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            processResource.setProcessId(processID);
            ProcessStatusResource status = processResource.getProcessStatus();
            if (status == null) {
                status = (ProcessStatusResource) processResource.create(ResourceType.EXPERIMENT_STATUS);
            }
            if (isValidStatusTransition(ProcessState.valueOf(status.getState()), processStatus.getState())) {
                status.setStatusId(getStatusID(processID));
                status.setProcessId(processID);
                status.setTimeOfStateChange(AiravataUtils.getTime(processStatus.getTimeOfStateChange()));
                status.setState(processStatus.getState().toString());
                status.setReason(processStatus.getReason());
                status.save();
                logger.debug(processID, "Added process {} status to {}.", processID, processStatus.toString());
            }
        } catch (Exception e) {
            logger.error(processID, "Error while adding process status...", e);
            throw new RegistryException(e);
        }
        return processID;
    }

    public String addProcessError(ErrorModel processError, String processID) throws RegistryException {
        try {
            ProcessErrorResource error = new ProcessErrorResource();
            error.setProcessId(processID);
            error.setErrorId(getErrorID(processID));
            error.setCreationTime(AiravataUtils.getTime(processError.getCreationTime()));
            error.setActualErrorMessage(processError.getActualErrorMessage());
            error.setUserFriendlyMessage(processError.getUserFriendlyMessage());
            error.setTransientOrPersistent(processError.isTransientOrPersistent());
            if(processError.getRootCauseErrorIdList() != null) {
                error.setRootCauseErrorIdList(StringUtils.join(processError.getRootCauseErrorIdList(), ","));
            }
            error.save();
        } catch (Exception e) {
            logger.error(processID, "Error while adding process status...", e);
            throw new RegistryException(e);
        }
        return processID;
    }

    public String addTask(TaskModel task, String processID) throws RegistryException {
        try {
            TaskResource taskResource = new TaskResource();
            taskResource.setParentProcessId(getProcessID(processID));
            taskResource.setTaskType(task.getTaskType().toString());
            taskResource.setCreationTime(AiravataUtils.getTime(task.getCreationTime()));
            taskResource.setLastUpdateTime(AiravataUtils.getTime(task.getLastUpdateTime()));
            taskResource.setTaskDetail(task.getTaskDetail());
            taskResource.setTaskInternalStore(task.getTaskInternalStore());
            taskResource.save();

            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setState(TaskState.CREATED);
            addTaskStatus(taskStatus, task.getTaskId());

            if(task.getTaskError() != null) {
                addTaskError(task.getTaskError(), task.getTaskId());
            }
        } catch (Exception e) {
            logger.error(processID, "Error while adding task...", e);
            throw new RegistryException(e);
        }
        return processID;
    }

    public String addTaskStatus(TaskStatus taskStatus, String taskID) throws RegistryException {
        try {
            TaskResource taskResource = new TaskResource();
            taskResource.setTaskId(taskID);
            TaskStatusResource status = taskResource.getTaskStatus();
            if (status == null) {
                status = new TaskStatusResource();
            }
            if (isValidStatusTransition(ProcessState.valueOf(status.getState()), taskStatus.getState())) {
                status.setStatusId(getStatusID(taskID));
                status.setTaskId(taskID);
                status.setTimeOfStateChange(AiravataUtils.getTime(taskStatus.getTimeOfStateChange()));
                status.setState(taskStatus.getState().toString());
                status.setReason(taskStatus.getReason());
                status.save();
                logger.debug(taskID, "Added task {} status to {}.", taskID, taskStatus.toString());
            }
        } catch (Exception e) {
            logger.error(taskID, "Error while adding task status...", e);
            throw new RegistryException(e);
        }
        return taskID;
    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        try {
            TaskErrorResource error = new TaskErrorResource();
            error.setTaskId(taskId);
            error.setErrorId(getErrorID(taskId));
            error.setCreationTime(AiravataUtils.getTime(taskError.getCreationTime()));
            error.setActualErrorMessage(taskError.getActualErrorMessage());
            error.setUserFriendlyMessage(taskError.getUserFriendlyMessage());
            error.setTransientOrPersistent(taskError.isTransientOrPersistent());
            if(taskError.getRootCauseErrorIdList() != null) {
                error.setRootCauseErrorIdList(StringUtils.join(taskError.getRootCauseErrorIdList(), ","));
            }
            error.save();
        } catch (Exception e) {
            logger.error(taskId, "Error while adding task status...", e);
            throw new RegistryException(e);
        }
        return taskId;
    }


    //CPI Update Methods
    public void updateExperiment(ExperimentModel experiment, String expId) throws RegistryException {
        try {
            if (!workerResource.isProjectExists(experiment.getProjectId())) {
                logger.error("Project does not exist in the system..");
                throw new Exception("Project does not exist in the system, Please create the project first...");
            }
            ExperimentResource existingExperiment = gatewayResource.getExperiment(expId);
            existingExperiment.setExperimentName(experiment.getExperimentName());
            existingExperiment.setUserName(experiment.getUserName());
            existingExperiment.setGatewayExecutionId(gatewayResource.getGatewayId());
            existingExperiment.setGatewayExecutionId(experiment.getGatewayExecutionId());
            existingExperiment.setProjectId(experiment.getProjectId());
            existingExperiment.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
            existingExperiment.setDescription(experiment.getDescription());
            existingExperiment.setApplicationId(experiment.getExecutionId());
            existingExperiment.setEnableEmailNotification(experiment.isEnableEmailNotification());

            existingExperiment.save();

            UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
            if (userConfigurationData != null) {
                updateUserConfigData(userConfigurationData, expId);
            }

            List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
            if (experimentInputs != null && !experimentInputs.isEmpty()) {
                updateExpInputs(experimentInputs, expId);
            }

            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()) {
                updateExpOutputs(experimentOutputs, expId);
            }
            ExperimentStatus experimentStatus = experiment.getExperimentStatus();
            if (experimentStatus != null) {
                updateExperimentStatus(experimentStatus, expId);
            }
            List<ErrorModel> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (ErrorModel errror : errors) {
                    updateExperimentError(errror, expId);
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating experiment...", e);
            throw new RegistryException(e);
        }
    }

    public void updateExpInputs(List<InputDataObjectType> exInputs, String expID) throws RegistryException {
        try {
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setExperimentId(expID);
            List<ExperimentInputResource> experimentInputs = experimentResource.getExperimentInputs();
            for (InputDataObjectType input : exInputs) {
                for (ExperimentInputResource exinput : experimentInputs) {
                    if (exinput.getInputName().equals(input.getName())) {
                        exinput.setInputValue(input.getValue());
                        exinput.setExperimentId(expID);
                        if (input.getType() != null) {
                            exinput.setDataType(input.getType().toString());
                        }
                        exinput.setMetadata(input.getMetaData());
                        exinput.setApplicationArgument(input.getApplicationArgument());
                        exinput.setInputOrder(input.getInputOrder());
                        exinput.setIsRequired(input.isIsRequired());
                        exinput.setRequiredToAddedToCmd(input.isRequiredToAddedToCommandLine());
                        exinput.save();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to update experiment inputs", e);
            throw new RegistryException(e);
        }
    }

    public void updateExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expId);
            List<ExperimentOutputResource> existingExpOutputs = experiment.getExperimentOutputs();
            for (OutputDataObjectType output : exOutput) {
                for (ExperimentOutputResource resource : existingExpOutputs) {
                    if (resource.getOutputName().equals(output.getName())) {
                        resource.setExperimentId(expId);
                        resource.setOutputName(output.getName());
                        resource.setOutputValue(output.getValue());
                        if (output.getType() != null) {
                            resource.setDataType(output.getType().toString());
                        }
                        resource.setIsRequired(output.isIsRequired());
                        resource.setRequiredToAddedToCmd(output.isRequiredToAddedToCommandLine());
                        resource.setDataMovement(output.isDataMovement());
                        resource.setLocation(output.getLocation());
                        resource.setApplicationArgument(output.getApplicationArgument());
                        resource.setSearchQuery(output.getSearchQuery());
                        resource.save();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating experiment outputs", e);
            throw new RegistryException(e);
        }
    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String expId) throws RegistryException {
        return addExperimentStatus(experimentStatus, expId);
    }

    public String updateExperimentError(ErrorModel experimentError, String expId) throws RegistryException {
        return addExperimentError(experimentError, expId);
    }

    public String updateUserConfigData(UserConfigurationDataModel configurationData, String experimentId) throws RegistryException {
        try {
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setExperimentId(experimentId);
            UserConfigurationDataResource configDataResource = experimentResource.getUserConfigurationDataResource();
            configDataResource.setExperimentId(experimentId);
            configDataResource.setAiravataAutoSchedule(configurationData.isAiravataAutoSchedule());
            configDataResource.setOverrideManualScheduledParams(configurationData.isOverrideManualScheduledParams());
            configDataResource.setShareExperimentPublically(configurationData.isShareExperimentPublicly());
            configDataResource.setThrottleResources(configurationData.isThrottleResources());
            configDataResource.setUserDn(configurationData.getUserDN());
            configDataResource.setGenerateCert(configurationData.isGenerateCert());
            configDataResource.setResourceHostId(configurationData.getComputationalResourceScheduling().getResourceHostId());
            configDataResource.setTotalCpuCount(configurationData.getComputationalResourceScheduling().getTotalCPUCount());
            configDataResource.setNodeCount(configurationData.getComputationalResourceScheduling().getNodeCount());
            configDataResource.setNumberOfThreads(configurationData.getComputationalResourceScheduling().getNumberOfThreads());
            configDataResource.setQueueName(configurationData.getComputationalResourceScheduling().getQueueName());
            configDataResource.setWallTimeLimit(configurationData.getComputationalResourceScheduling().getWallTimeLimit());
            configDataResource.setTotalPhysicalMemory(configurationData.getComputationalResourceScheduling().getTotalPhysicalMemory());
            configDataResource.save();
        } catch (Exception e) {
            logger.error("Unable to save user config data", e);
            throw new RegistryException(e);
        }
        return experimentId;
    }

    public void updateProcess(ProcessModel process, String processId) throws RegistryException {
        try {
            ExperimentResource experimentResource = new ExperimentResource();
            ProcessResource processResource = experimentResource.getProcess(processId);
            processResource.setProcessId(process.getProcessId());
            processResource.setExperimentId(process.getExperimentId());
            processResource.setCreationTime(AiravataUtils.getTime(process.getCreationTime()));
            processResource.setLastUpdateTime(AiravataUtils.getTime(process.getLastUpdateTime()));
            processResource.setProcessDetail(process.getProcessDetail());
            processResource.setApplicationInterfaceId(process.getApplicationInterfaceId());
            processResource.setTaskDag(process.getTaskDag());
            processResource.save();

            if(process.getResourceSchedule() != null) {
                updateProcessResourceSchedule(process.getResourceSchedule(), process.getProcessId());
            }
            if(process.getProcessInputs() !=  null && process.getProcessInputs().size() > 0) {
                updateProcessInputs(process.getProcessInputs(), process.getProcessId());
            }
            if(process.getProcessOutputs() != null && process.getProcessOutputs().size() > 0) {
                updateProcessOutputs(process.getProcessOutputs(), process.getProcessId());
            }
            if(process.getProcessStatus() != null) {
                updateProcessStatus(process.getProcessStatus(), process.getProcessId());
            }
            if(process.getProcessError() != null) {
                updateProcessError(process.getProcessError(), process.getProcessId());
            }
            if(process.getTasks() != null && process.getTasks().size() > 0){
                for(TaskModel task : process.getTasks()){
                    updateTask(task, process.getProcessId());
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating experiment...", e);
            throw new RegistryException(e);
        }
    }

    public String updateProcessResourceSchedule(ComputationalResourceSchedulingModel resourceSchedule, String processID) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            processResource.setProcessId(processID);
            ProcessResourceScheduleResource processResourceSchedule = processResource.getProcessResourceSchedule();
            processResourceSchedule.setProcessId(processID);
            processResourceSchedule.setResourceHostId(resourceSchedule.getResourceHostId());
            processResourceSchedule.setTotalCpuCount(resourceSchedule.getTotalCPUCount());
            processResourceSchedule.setNodeCount(resourceSchedule.getNodeCount());
            processResourceSchedule.setNumberOfThreads(resourceSchedule.getNumberOfThreads());
            processResourceSchedule.setQueueName(resourceSchedule.getQueueName());
            processResourceSchedule.setWallTimeLimit(resourceSchedule.getWallTimeLimit());
            processResourceSchedule.setTotalPhysicalMemory(resourceSchedule.getTotalPhysicalMemory());
            processResourceSchedule.save();
        } catch (Exception e) {
            logger.error("Unable to save process resource schedule data", e);
            throw new RegistryException(e);
        }
        return processID;
    }

    public void updateProcessInputs(List<InputDataObjectType> processInputs, String processID) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            processResource.setProcessId(processID);
            List<ProcessInputResource> existingProcessInputs = processResource.getProcessInputs();
            for (InputDataObjectType input : processInputs) {
                for (ProcessInputResource exinput : existingProcessInputs) {
                    if (exinput.getInputName().equals(input.getName())) {
                        exinput.setProcessId(processID);
                        exinput.setInputValue(input.getValue());
                        if (input.getType() != null) {
                            exinput.setDataType(input.getType().toString());
                        }
                        exinput.setMetadata(input.getMetaData());
                        exinput.setApplicationArgument(input.getApplicationArgument());
                        exinput.setInputOrder(input.getInputOrder());
                        exinput.setIsRequired(input.isIsRequired());
                        exinput.setRequiredToAddedToCmd(input.isRequiredToAddedToCommandLine());
                        exinput.save();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to update experiment inputs", e);
            throw new RegistryException(e);
        }
    }

    public void updateProcessOutputs(List<OutputDataObjectType> processOutput, String processID) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            processResource.setProcessId(processID);
            List<ProcessOutputResource> existingProcessOutputs = processResource.getProcessOutputs();
            for (OutputDataObjectType output : processOutput) {
                for (ProcessOutputResource resource : existingProcessOutputs) {
                    if (resource.getOutputName().equals(output.getName())) {
                        resource.setProcessId(processID);
                        resource.setOutputName(output.getName());
                        resource.setOutputValue(output.getValue());
                        if (output.getType() != null) {
                            resource.setDataType(output.getType().toString());
                        }
                        resource.setIsRequired(output.isIsRequired());
                        resource.setRequiredToAddedToCmd(output.isRequiredToAddedToCommandLine());
                        resource.setDataMovement(output.isDataMovement());
                        resource.setLocation(output.getLocation());
                        resource.setApplicationArgument(output.getApplicationArgument());
                        resource.setSearchQuery(output.getSearchQuery());
                        resource.save();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating process outputs", e);
            throw new RegistryException(e);
        }
    }

    public String updateProcessStatus(ProcessStatus processStatus, String processID) throws RegistryException {
        return addProcessStatus(processStatus, processID);
    }

    public String updateProcessError(ErrorModel processError, String processID) throws RegistryException {
        return addProcessError(processError, processID);
    }

    public String updateTask(TaskModel task, String taskID) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            TaskResource taskResource = processResource.getTask(taskID);
            taskResource.setParentProcessId(task.getParentProcessId());
            taskResource.setTaskType(task.getTaskType().toString());
            taskResource.setCreationTime(AiravataUtils.getTime(task.getCreationTime()));
            taskResource.setLastUpdateTime(AiravataUtils.getTime(task.getLastUpdateTime()));
            taskResource.setTaskDetail(task.getTaskDetail());
            taskResource.setTaskInternalStore(task.getTaskInternalStore());
            taskResource.save();

            if(task.getTaskError() != null) {
                updateTaskError(task.getTaskError(), task.getTaskId());
            }
            if(task.getTaskError() != null) {
                updateTaskError(task.getTaskError(), task.getTaskId());
            }
        } catch (Exception e) {
            logger.error(taskID, "Error while adding task...", e);
            throw new RegistryException(e);
        }
        return taskID;
    }

    public String updateTaskStatus(TaskStatus taskStatus, String taskID) throws RegistryException {
        return addTaskStatus(taskStatus, taskID);
    }

    public String updateTaskError(ErrorModel taskError, String taskID) throws RegistryException {
        return addTaskError(taskError, taskID);
    }

    //TODO
    public void updateExperimentField(String expID, String fieldName, Object value) throws RegistryException {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expID);
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                experiment.setExperimentName((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                experiment.setUserName((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.DESCRIPTION)) {
                experiment.setDescription((String) value);
                experiment.save();
            } else {
                logger.error("Unsupported field type for Experiment");
            }
        } catch (Exception e) {
            logger.error("Error while updating fields in experiment...", e);
            throw new RegistryException(e);
        }
    }

    public void updateUserConfigDataField(String expID, String fieldName, Object value) throws RegistryException {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expID);
            UserConfigurationDataResource exConfigData = (UserConfigurationDataResource)
                    experiment.get(ResourceType.USER_CONFIGURATION_DATA, expID);
            if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                exConfigData.setAiravataAutoSchedule((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                exConfigData.setOverrideManualScheduledParams((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.SHARE_EXP)) {
                exConfigData.setShareExperimentPublically((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
                updateSchedulingData((ComputationalResourceSchedulingModel) value, experiment);
            } else {
                logger.error("Unsupported field type for Experiment config data");
            }

        } catch (Exception e) {
            logger.error("Error while updating fields in experiment config...", e);
            throw new RegistryException(e);
        }
    }

    //CPI get methods
    public Object getExperiment(String expId, String fieldName) throws RegistryException {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getExperiment(resource);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                return resource.getUserName();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_EXECUTION_ID)) {
                return resource.getGatewayExecutionId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                return resource.getExperimentName();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.DESCRIPTION)) {
                return resource.getDescription();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID)) {
                return resource.getExecutionId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                return resource.getProjectId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_INPUTS)) {
                return ThriftDataModelConversion.getExpInputs(resource.getExperimentInputs());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_OUTPUTS)) {
                return ThriftDataModelConversion.getExpOutputs(resource.getExperimentOutputs());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
                return ThriftDataModelConversion.getExperimentStatus(resource.getExperimentStatus());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ERRORS)) {
                return ThriftDataModelConversion.getExperimentErrorList(resource.getExperimentErrors());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_CONFIGURATION_DATA)) {
                return ThriftDataModelConversion.getUserConfigData(resource.getUserConfigurationDataResource());
            } else {
                logger.error("Unsupported field name for experiment basic data..");
            }
        } catch (Exception e) {
            logger.error("Error while getting experiment info...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getExperimentInputs(String expID) throws RegistryException {
        return getExperiment(expID, Constants.FieldConstants.ExperimentConstants.EXPERIMENT_INPUTS);
    }

    public Object getExperimentOutputs(String expID) throws RegistryException {
        return getExperiment(expID, Constants.FieldConstants.ExperimentConstants.EXPERIMENT_OUTPUTS);
    }

    public Object getExperimentErrors(String expID) throws RegistryException {
        return getExperiment(expID, Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ERRORS);
    }

    public Object getExperimentStatus(String expID) throws RegistryException {
        return getExperiment(expID, Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS);
    }

    public Object getUserConfigData(String expId, String fieldName) throws RegistryException {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            UserConfigurationDataResource userConfigData = resource.getUserConfigurationDataResource();
            if (fieldName == null) {
                return ThriftDataModelConversion.getUserConfigData(userConfigData);
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                return userConfigData.getAiravataAutoSchedule();
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                return userConfigData.getOverrideManualScheduledParams();
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.SHARE_EXP)) {
                return userConfigData.getShareExperimentPublically();
            } else if (fieldName.equals(Constants.FieldConstants.UserConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)){
                return ThriftDataModelConversion.getComputationalResourceScheduling(userConfigData);
            } else {
                logger.error("Unsupported field name for experiment configuration data..");
            }
        } catch (Exception e) {
            logger.error("Error while getting config data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    //Todo
    public Object getProcess(String processId, String fieldName) throws RegistryException {
        try {
            ExperimentResource experimentResource = new ExperimentResource();
            ProcessResource resource = experimentResource.getProcess(processId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getProcesModel(resource);
            } else {
                logger.error("Unsupported field name for experiment basic data..");
            }
        }catch (Exception e) {
            logger.error("Error while getting process data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getProcessError(String identifier) {
        return null;
    }

    public Object getProcessStatus(String identifier) {
        return null;
    }

    public Object getProcessInputs(String identifier) {
        return null;
    }

    public Object getProcessOutputs(String identifier) {
        return null;
    }

    public Object getProcessResourceSchedule(String identifier, Object o) {
        return null;
    }

    public Object getTask(String taskId, String fieldName) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            TaskResource resource = processResource.getTask(taskId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getTaskModel(resource);
            } else {
                logger.error("Unsupported field name for experiment basic data..");
            }
        }catch (Exception e) {
            logger.error("Error while getting process data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getTaskError(String identifier) {
        return null;
    }


    public List<ExperimentModel> getExperimentList(String fieldName, Object value) throws RegistryException {
        List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                WorkerResource resource = (WorkerResource) gatewayResource.create(ResourceType.GATEWAY_WORKER);
                resource.setUser((String) value);
                List<ExperimentResource> resources = resource.getExperiments();
                for (ExperimentResource experimentResource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(experimentResource);
                    experiments.add(experiment);
                }
                return experiments;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                ProjectResource project = workerResource.getProject((String) value);
                List<ExperimentResource> resources = project.getExperiments();
                for (ExperimentResource resource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_EXECUTION_ID)) {
                List<ExperimentResource> resources = gatewayResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            } else {
                logger.error("Unsupported field name to retrieve experiment list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting experiment list...", e);
            throw new RegistryException(e);
        }
        return experiments;
    }

    //Todo
    public List<ProcessModel> getProcessList(String fieldName, Object value) {
        return null;
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) {
        return null;
    }

    public List<ExperimentModel> getExperimentList(String fieldName, Object value, int limit, int offset,
                                                   Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                WorkerResource resource = (WorkerResource) gatewayResource.create(ResourceType.GATEWAY_WORKER);
                resource.setUser((String) value);
                List<ExperimentResource> resources = resource.getExperiments(limit, offset,
                        orderByIdentifier, resultOrderType);
                for (ExperimentResource experimentResource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(experimentResource);
                    experiments.add(experiment);
                }
                return experiments;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                ProjectResource project = workerResource.getProject((String) value);
                List<ExperimentResource> resources = project.getExperiments(limit, offset,
                        Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
                for (ExperimentResource resource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            }
            logger.error("Unsupported field name to retrieve experiment list...");
        } catch (Exception e) {
            logger.error("Error while getting experiment list...", e);
            throw new RegistryException(e);
        }
        return experiments;
    }


    //CPI Search Methods

    public List<ExperimentSummaryModel> searchExperiments(Map<String, String> filters, int limit,
                                                          int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        Map<String, String> fil = new HashMap<String, String>();
        if (filters != null && filters.size() != 0) {
            List<ExperimentSummaryModel> experimentSummaries = new ArrayList<>();
            long fromTime = 0;
            long toTime = 0;
            try {
                for (String field : filters.keySet()) {
                    if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.EXPERIMENT_NAME, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.USER_NAME, filters.get(field));
                    }else if (field.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_EXECUTION_ID)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.GATEWAY_ID, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.DESCRIPTION)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.DESCRIPTION, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.EXECUTION_ID, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
                        fil.put(AbstractExpCatResource.ExperimentStatusConstants.STATE, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.FROM_DATE)) {
                        fromTime = Long.parseLong(filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.TO_DATE)) {
                        toTime = Long.parseLong(filters.get(field));
                    }
                }
                List<ExperimentSummaryResource> experimentSummaryResources;
                if (fromTime != 0 && toTime != 0) {
                    experimentSummaryResources = workerResource.searchExperiments(new Timestamp(fromTime), new Timestamp(toTime), fil
                            ,limit , offset, orderByIdentifier, resultOrderType);
                } else {
                    experimentSummaryResources = workerResource
                            .searchExperiments(null, null, fil, limit, offset, orderByIdentifier, resultOrderType);
                }
                if (experimentSummaryResources != null && !experimentSummaryResources.isEmpty()) {
                    for (ExperimentSummaryResource ex : experimentSummaryResources) {
                        experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
                    }
                }
                return experimentSummaries;

            } catch (Exception e) {
                logger.error("Error while retrieving experiment summary from registry", e);
                throw new RegistryException(e);
            }
        }
        return null;
    }

    public ExperimentStatistics getExperimentStatistics(Map<String,String> filters) throws RegistryException {
        try {
            ExperimentStatistics experimentStatistics = new ExperimentStatistics();
            ExperimentStatisticsResource experimentStatisticsResource = workerResource.getExperimentStatistics(
                    filters.get(Constants.FieldConstants.ExperimentConstants.GATEWAY_EXECUTION_ID),
                    new Timestamp(Long.parseLong(filters.get(Constants.FieldConstants.ExperimentConstants.FROM_DATE))),
                    new Timestamp(Long.parseLong(filters.get(Constants.FieldConstants.ExperimentConstants.TO_DATE)))
            );

            experimentStatistics.setAllExperimentCount(experimentStatisticsResource.getAllExperimentCount());
            experimentStatistics.setCompletedExperimentCount(experimentStatisticsResource.getCompletedExperimentCount());
            experimentStatistics.setFailedExperimentCount(experimentStatisticsResource.getFailedExperimentCount());
            experimentStatistics.setCancelledExperimentCount(experimentStatisticsResource.getCancelledExperimentCount());

            ArrayList<ExperimentSummaryModel> experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getAllExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setAllExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getCompletedExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setCompletedExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getFailedExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setFailedExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getCancelledExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setCancelledExperiments(experimentSummaries);

            return experimentStatistics;
        } catch (RegistryException e) {
            logger.error("Error while retrieving experiment statistics from registry", e);
            throw new RegistryException(e);
        }
    }

    //CPI getIds method
    public List<String> getExperimentIDs(String fieldName, Object value) throws RegistryException {
        List<String> expIDs = new ArrayList<String>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_EXECUTION_ID)) {
                if (gatewayResource == null) {
                    logger.error("You should use an existing gateway in order to retrieve experiments..");
                    return null;
                } else {
                    List<ExperimentResource> resources = gatewayResource.getExperiments();
                    for (ExperimentResource resource : resources) {
                        String expID = resource.getExperimentId();
                        expIDs.add(expID);
                    }
                }
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                List<ExperimentResource> resources = workerResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    expIDs.add(resource.getExperimentId());
                }
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                List<ExperimentResource> resources = workerResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    expIDs.add(resource.getExperimentId());
                }
            }
        } catch (Exception e) {
            logger.error("Error while retrieving experiment ids..", e);
            throw new RegistryException(e);
        }
        return expIDs;
    }


    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        List<String> processIds = new ArrayList<String>();
        List<ProcessModel> processes = getProcessList(fieldName, value);
        for (ProcessModel td : processes) {
            processIds.add(td.getProcessId());
        }
        return processIds;
    }

    public List<String> getTaskIds(String fieldName, Object value) throws RegistryException {
        List<String> taskIds = new ArrayList<String>();
        List<TaskModel> tasks = getTaskList(fieldName, value);
        for (TaskModel task : tasks) {
            taskIds.add(task.getTaskId());
        }
        return taskIds;
    }


    //Remove CPI methods
    public void removeExperiment(String experimentId) throws RegistryException {
        try {
            gatewayResource.remove(ResourceType.EXPERIMENT, experimentId);
        } catch (Exception e) {
            logger.error("Error while removing experiment..", e);
            throw new RegistryException(e);
        }
    }

    public void removeUserConfigData(String experimentId) throws RegistryException {
        try {
            ExperimentResource experiment = new ExperimentResource();
            experiment.setExperimentId(experimentId);
            experiment.remove(ResourceType.USER_CONFIGURATION_DATA, experimentId);
        } catch (Exception e) {
            logger.error("Error while removing experiment config..", e);
            throw new RegistryException(e);
        }
    }


    public void removeProcess(String processId) throws RegistryException {
        try {
            ExperimentResource experiment = new ExperimentResource();
            experiment.remove(ResourceType.PROCESS, processId);
        } catch (Exception e) {
            logger.error("Error while removing workflow node..", e);
            throw new RegistryException(e);
        }
    }

    public void removeProcessResourceSchedule(String processId) throws RegistryException {
        try {
            ExperimentResource experiment = new ExperimentResource();
            experiment.remove(ResourceType.PROCESS_RESOURCE_SCHEDULE, processId);
        } catch (Exception e) {
            logger.error("Error while removing workflow node..", e);
            throw new RegistryException(e);
        }
    }

    public void removeTask(String taskId) throws RegistryException {
        try {
            ProcessResource process = new ProcessResource();
            process.remove(ResourceType.TASK, taskId);
        } catch (Exception e) {
            logger.error("Error while removing task details..", e);
            throw new RegistryException(e);
        }
    }


    // ids - taskId + jobid
    public void updateJobDetails(JobModel jobDetails, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            String taskId = (String) ids.getTopLevelIdentifier();
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            String jobId = (String) ids.getSecondLevelIdentifier();
//            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
//            jobDetail.setTaskId(taskDetail.getTaskId());
//            jobDetail.setJobDescription(jobDetails.getJobDescription());
//            jobDetail.setCreationTime(AiravataUtils.getTime(jobDetails.getCreationTime()));
//            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
//            jobDetail.setJobName(jobDetails.getJobName());
//            jobDetail.setWorkingDir(jobDetails.getWorkingDir());
//            jobDetail.save();
//            JobStatus jobStatus = jobDetails.getJobStatus();
//            if (jobStatus != null) {
//                JobStatus status = getJobStatus(ids);
//                if (status != null) {
//                    updateJobStatus(jobStatus, ids);
//                } else {
//                    addJobStatus(jobStatus, ids);
//                }
//            }
//            JobStatus applicationStatus = jobDetails.getJobStatus();
//            if (applicationStatus != null) {
//                JobStatus appStatus = getJobStatus(ids);
//                if (appStatus != null) {
//                    updateJobStatus(applicationStatus, ids);
//                } else {
//                    addJobStatus(applicationStatus, ids);
//                }
//            }
////            List<ErrorDetails> errors = jobDetails.getErrors();
////            if (errors != null && !errors.isEmpty()) {
////                for (ErrorDetails error : errors) {
////                    addErrorDetails(error, jobId);
////                }
////            }
//        } catch (Exception e) {
//            logger.error("Error while updating job details...", e);
//            throw new RegistryException(e);
//        }
    }

//    public String addDataTransferDetails(DataTransferDetails transferDetails, String taskId) throws RegistryException {
//        try {
//            if (transferDetails.getTransferDescription() == null){
//                throw new RegistryException("Data transfer description cannot be empty");
//            }
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            DataTransferDetailResource resource = (DataTransferDetailResource) taskDetail.create(ResourceType.DATA_TRANSFER_DETAIL);
//            resource.setTaskId(taskId);
//            resource.setTransferId(getDataTransferID(taskId));
//
//            resource.setTransferDescription(transferDetails.getTransferDescription());
//            resource.setCreationTime(AiravataUtils.getTime(transferDetails.getCreationTime()));
//            resource.save();
//            String transferId = resource.getTransferId();
//            TransferStatus transferStatus = transferDetails.getTransferStatus();
//            if (transferStatus != null) {
//                TransferStatus status = getDataTransferStatus(transferId);
//                if (status != null) {
//                    updateTransferStatus(transferStatus, transferId);
//                } else {
//                    CompositeIdentifier ids = new CompositeIdentifier(taskId, transferId);
//                    addTransferStatus(transferStatus, ids);
//                }
//            }
//            return resource.getTransferId();
//        } catch (Exception e) {
//            logger.error("Error while adding transfer details...", e);
//            throw new RegistryException(e);
//        }
//    }

//    public String updateDataTransferDetails(DataTransferDetails transferDetails, String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource resource = taskDetail.getDataTransferDetail(transferId);
////            resource.setTaskDetailResource(taskDetail);
//            resource.setTransferDescription(transferDetails.getTransferDescription());
//            resource.setCreationTime(AiravataUtils.getTime(transferDetails.getCreationTime()));
//            resource.save();
//            String taskId = resource.getTaskId();
//            TransferStatus transferStatus = transferDetails.getTransferStatus();
//            if (transferStatus != null) {
//                TransferStatus status = getDataTransferStatus(transferId);
//                if (status != null) {
//                    updateTransferStatus(transferStatus, transferId);
//                } else {
//                    CompositeIdentifier ids = new CompositeIdentifier(taskId, transferId);
//                    addTransferStatus(transferStatus, ids);
//                }
//            }
//            return resource.getTransferId();
//        } catch (Exception e) {
//            logger.error("Error while updating transfer details...", e);
//            throw new RegistryException(e);
//        }
//    }

    /**
     * @param scheduling computational resource object
     * @param ids        contains expId and taskId, if it is an experiment, task id can be null
     * @return scheduling id
     */
    public String addComputationalResourceScheduling(ComputationalResourceSchedulingModel scheduling, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            ComputationSchedulingResource schedulingResource = (ComputationSchedulingResource) experiment.create(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                schedulingResource.setTaskId(taskDetail.getTaskId());
//            }
//            schedulingResource.setExperimentId(experiment.getExpID());
//            schedulingResource.setResourceHostId(scheduling.getResourceHostId());
//            schedulingResource.setCpuCount(scheduling.getTotalCPUCount());
//            schedulingResource.setNodeCount(scheduling.getNodeCount());
//            schedulingResource.setNumberOfThreads(scheduling.getNumberOfThreads());
//            schedulingResource.setQueueName(scheduling.getQueueName());
//            schedulingResource.setWalltimeLimit(scheduling.getWallTimeLimit());
//            schedulingResource.setPhysicalMemory(scheduling.getTotalPhysicalMemory());
//            schedulingResource.setChessisName(scheduling.getChessisNumber());
//            schedulingResource.save();
//            return String.valueOf(schedulingResource.getSchedulingId());
//        } catch (Exception e) {
//            logger.error("Error while adding scheduling parameters...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    /**
//     * @param dataHandling advanced input data handling object
//     * @param ids          contains expId and taskId
//     * @return data handling id
//     */
//    public String addInputDataHandling(AdvancedInputDataHandling dataHandling, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            AdvanceInputDataHandlingResource dataHandlingResource = (AdvanceInputDataHandlingResource) experiment.create(ResourceType.ADVANCE_INPUT_DATA_HANDLING);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                dataHandlingResource.setTaskId(taskDetail.getTaskId());
//            }
//            dataHandlingResource.setProcessId(experiment.getExpID());
//            dataHandlingResource.setWorkingDir(dataHandling.getUniqueWorkingDirectory());
//            dataHandlingResource.setWorkingDirParent(dataHandling.getParentWorkingDirectory());
//            dataHandlingResource.setStageInputFiles(dataHandling.isStageInputFilesToWorkingDir());
//            dataHandlingResource.setCleanAfterJob(dataHandling.isCleanUpWorkingDirAfterJob());
//            dataHandlingResource.save();
//            return String.valueOf(dataHandlingResource.getDataHandlingId());
//        } catch (Exception e) {
//            logger.error("Error while adding input data handling...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    /**
//     * @param dataHandling advanced output data handling object
//     * @param ids          contains expId and taskId
//     * @return data handling id
//     */
//    public String addOutputDataHandling(AdvancedOutputDataHandling dataHandling, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            AdvancedOutputDataHandlingResource dataHandlingResource = (AdvancedOutputDataHandlingResource) experiment.create(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                dataHandlingResource.setTaskId(taskDetail.getTaskId());
//            }
//            dataHandlingResource.setProcessId(experiment.getExpID());
//            dataHandlingResource.setOutputDataDir(dataHandling.getOutputDataDir());
//            dataHandlingResource.setDataRegUrl(dataHandling.getDataRegistryURL());
//            dataHandlingResource.setPersistOutputData(dataHandling.isPersistOutputData());
//            dataHandlingResource.save();
//            return String.valueOf(dataHandlingResource.getOutputDataHandlingId());
//        } catch (Exception e) {
//            logger.error("Error while adding output data handling...", e);
//            throw new RegistryException(e);
//        }
//    }

//    public String addQosParams(QualityOfServiceParams qosParams, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            QosParamResource qosParamResource = (QosParamResource) experiment.create(ResourceType.QOS_PARAM);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                qosParamResource.setTaskId(taskDetail.getTaskId());
//            }
//            qosParamResource.setProcessId(experiment.getExpID());
//            qosParamResource.setStartExecutionAt(qosParams.getStartExecutionAt());
//            qosParamResource.setExecuteBefore(qosParams.getExecuteBefore());
//            qosParamResource.setNoOfRetries(qosParams.getNumberofRetries());
//            qosParamResource.save();
//            return String.valueOf(qosParamResource.getQosId());
//        } catch (Exception e) {
//            logger.error("Error while adding QOS params...", e);
//            throw new RegistryException(e);
//        }
//    }

    public String addErrorDetails(ErrorModel error, Object id) throws RegistryException {
//        try {
//
//            ErrorDetailResource errorResource = null;
//            ExperimentResource experiment;
//            TaskDetailResource taskDetail;
//            WorkflowNodeDetailResource workflowNode;
//            // figure out the id is an experiment, node task or job
//            if (id instanceof String) {
//                // FIXME : for .12 we only save task related errors
////                if (isExperimentExist((String) id)) {
////                    experiment = gatewayResource.getExperiment((String) id);
////                    errorResource = (ErrorDetailResource) experiment.create(ResourceType.ERROR_DETAIL);
////                } else if (isWFNodeExist((String) id)) {
////                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
////                    workflowNode = experiment.getWorkflowNode((String) id);
////                    errorResource = (ErrorDetailResource) workflowNode.create(ResourceType.ERROR_DETAIL);
////                    errorResource.setExperimentResource(workflowNode.getExperimentResource());
////                } else
//                if (isTaskDetailExist((String) id)) {
//                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    taskDetail = workflowNode.getTaskDetail((String) id);
//                    errorResource = (ErrorDetailResource) taskDetail.create(ResourceType.ERROR_DETAIL);
//                    if (error.getErrorId() != null && !error.getErrorId().equals(airavata_commonsConstants.DEFAULT_ID)) {
//                        List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
//                        if (errorDetailList != null && !errorDetailList.isEmpty()) {
//                            for (ErrorDetailResource errorDetailResource : errorDetailList) {
//                                if (errorDetailResource.getErrorId() == Integer.parseInt(error.getErrorId())) {
//                                    errorResource = errorDetailResource;
//                                }
//                            }
//                        }
//                    }
//                    errorResource.setTaskId(taskDetail.getTaskId());
//                    errorResource.setNodeId(taskDetail.getNodeId());
//                    workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//                    errorResource.setExperimentId(workflowNode.getExperimentId());
//                } else {
////                    logger.error("The id provided is not an experiment id or a workflow id or a task id..");
//                }
//            } else if (id instanceof CompositeIdentifier) {
//                CompositeIdentifier cid = (CompositeIdentifier) id;
//                if (isJobDetailExist(cid)) {
//                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    taskDetail = workflowNode.getTaskDetail((String) cid.getTopLevelIdentifier());
//                    JobDetailResource jobDetail = taskDetail.getJobDetail((String) cid.getSecondLevelIdentifier());
//                    errorResource = (ErrorDetailResource) jobDetail.create(ResourceType.ERROR_DETAIL);
//                    if (error.getErrorId() != null && !error.getErrorId().equals(airavata_commonsConstants.DEFAULT_ID)) {
//                        List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
//                        if (errorDetailList != null && !errorDetailList.isEmpty()) {
//                            for (ErrorDetailResource errorDetailResource : errorDetailList) {
//                                if (errorDetailResource.getErrorId() == Integer.parseInt(error.getErrorId())) {
//                                    errorResource = errorDetailResource;
//                                }
//                            }
//                        }
//                    }
//                    errorResource.setTaskId(taskDetail.getTaskId());
//                    errorResource.setNodeId(taskDetail.getNodeId());
//                    workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//                    errorResource.setExperimentId(workflowNode.getExperimentId());
//                } else {
//                    logger.error("The id provided is not a job in the system..");
//                }
//            } else {
////                logger.error("The id provided is not an experiment id or a workflow id or a task id or a composite " +
////                        "identifier for job..");
//            }
//            if (errorResource != null) {
//                errorResource.setCreationTime(AiravataUtils.getTime(error.getCreationTime()));
//                errorResource.setActualErrorMsg(error.getActualErrorMessage());
//                errorResource.setUserFriendlyErrorMsg(error.getUserFriendlyMessage());
//                errorResource.setTransientPersistent(error.isTransientOrPersistent());
//                errorResource.save();
//                return String.valueOf(errorResource.getErrorId());
//            }
//        } catch (Exception e) {
//            logger.error("Unable to add error details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }


    public String getExperimentID(String experimentName) {
        String exp = experimentName.replaceAll("\\s", "");
        return exp + "_" + UUID.randomUUID();
    }

    public String getProcessID(String experimentId) {
        String process = experimentId.replaceAll("\\s", "");
        return process + "_" + UUID.randomUUID();
    }

    public String getTaskID(String processId) {
        String taskId = processId.replaceAll("\\s", "");
        return taskId + "_" + UUID.randomUUID();
    }

    public String getStatusID(String parentId) {
        String status = parentId.replaceAll("\\s", "");
        return status + "_" + UUID.randomUUID();
    }

    public String getErrorID(String parentId) {
        String error = parentId.replaceAll("\\s", "");
        return error + "_" + UUID.randomUUID();
    }

//    public void updateQosParams(QualityOfServiceParams qosParams, ExperimentCatResource resource) throws RegistryException {
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                QosParamResource qosr = expResource.getQOSparams(expResource.getExpID());
//                qosr.setProcessId(expResource.getExpID());
//                qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
//                qosr.setExecuteBefore(qosParams.getExecuteBefore());
//                qosr.setNoOfRetries(qosParams.getNumberofRetries());
//                qosr.save();
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating QOS data...", e);
//            throw new RegistryException(e);
//        }
//
//    }

//    public void updateOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, ExperimentCatResource resource) throws RegistryException {
//        AdvancedOutputDataHandlingResource adodh;
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                adodh = expResource.getOutputDataHandling(expResource.getExpID());
//                adodh.setProcessId(expResource.getExpID());
//            } else {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                ExperimentResource experimentResource = new ExperimentResource();
//                adodh = taskDetailResource.getOutputDataHandling(taskDetailResource.getTaskId());
//                adodh.setTaskId(taskDetailResource.getTaskId());
//                WorkflowNodeDetailResource nodeDetailResource = experimentResource.getWorkflowNode(taskDetailResource.getNodeId());
//                adodh.setProcessId(nodeDetailResource.getProcessId());
//            }
//            adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
//            adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
//            adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
//            adodh.save();
//        } catch (Exception e) {
//            logger.error("Error while updating output data handling...", e);
//            throw new RegistryException(e);
//        }
//
//    }
//
//    public void updateInputDataHandling(AdvancedInputDataHandling inputDataHandling, ExperimentCatResource resource) throws RegistryException {
//        AdvanceInputDataHandlingResource adidh;
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                adidh = expResource.getInputDataHandling(expResource.getExpID());
//                adidh.setProcessId(expResource.getExpID());
//            } else {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                ExperimentResource experimentResource = new ExperimentResource();
//                adidh = taskDetailResource.getInputDataHandling(taskDetailResource.getTaskId());
//                adidh.setTaskId(taskDetailResource.getTaskId());
//                WorkflowNodeDetailResource nodeDetailResource = experimentResource.getWorkflowNode(taskDetailResource.getNodeId());
//                adidh.setProcessId(nodeDetailResource.getProcessId());
//            }
//            adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
//            adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
//            adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
//            adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
//            adidh.save();
//        } catch (Exception e) {
//            logger.error("Error while updating input data handling...", e);
//            throw new RegistryException(e);
//        }
//
//    }

    public void updateSchedulingData(ComputationalResourceSchedulingModel resourceScheduling, ExperimentCatResource resource) throws RegistryException {
//        ComputationSchedulingResource cmsr;
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                cmsr = expResource.getComputationScheduling(expResource.getExpID());
//                cmsr.setExperimentId(expResource.getExpID());
//            } else {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                ExperimentResource experimentResource = new ExperimentResource();
//                cmsr = taskDetailResource.getComputationScheduling(taskDetailResource.getTaskId());
//                cmsr.setTaskId(taskDetailResource.getTaskId());
//                WorkflowNodeDetailResource nodeDetailResource = experimentResource.getWorkflowNode(taskDetailResource.getNodeId());
//                cmsr.setExperimentId(nodeDetailResource.getExperimentId());
//            }
//            cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
//            cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
//            cmsr.setNodeCount(resourceScheduling.getNodeCount());
//            cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
//            cmsr.setQueueName(resourceScheduling.getQueueName());
//            cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
//            cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
//            cmsr.save();
//        } catch (Exception e) {
//            logger.error("Error while updating scheduling data...", e);
//            throw new RegistryException(e);
//        }
    }


    /**
     * Method to get matching experiment list with pagination and ordering
     * @param fieldName
     * @param value
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */

    public List<TaskModel> getTaskDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.TaskDetailConstants.NODE_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) value);
//                List<TaskDetailResource> taskDetails = workflowNode.getTaskDetails();
//                return ThriftDataModelConversion.getTaskDetailsList(taskDetails);
//            } else {
//                logger.error("Unsupported field name to retrieve task detail list...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting task details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public List<JobModel> getJobDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.JobDetaisConstants.TASK_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
//                List<JobDetailResource> jobDetailList = taskDetail.getJobDetailList();
//                return ThriftDataModelConversion.getJobDetailsList(jobDetailList);
//            } else {
//                logger.error("Unsupported field name to retrieve job details list...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while job details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }
//
//    public List<DataTransferDetails> getDataTransferDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.DataTransferDetailConstants.TASK_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
//                List<DataTransferDetailResource> dataTransferDetailList = taskDetail.getDataTransferDetailList();
//                return ThriftDataModelConversion.getDataTransferlList(dataTransferDetailList);
//            } else {
//                logger.error("Unsupported field name to retrieve job details list...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting data transfer details...", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }

    public List<ErrorModel> getErrorDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.EXPERIMENT_ID)) {
//                ExperimentResource experiment = gatewayResource.getExperiment((String) value);
//                List<ErrorDetailResource> errorDetails = experiment.getErrorDetails();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
//            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.NODE_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) value);
//                List<ErrorDetailResource> errorDetails = workflowNode.getErrorDetails();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
//            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.TASK_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
//                List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetailList);
//            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.JOB_ID)) {
//                CompositeIdentifier cid = (CompositeIdentifier) value;
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) cid.getTopLevelIdentifier());
//                JobDetailResource jobDetail = taskDetail.getJobDetail((String) cid.getSecondLevelIdentifier());
//                List<ErrorDetailResource> errorDetails = jobDetail.getErrorDetails();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
//            } else {
//                logger.error("Unsupported field name to retrieve job details list...");
//            }
//        } catch (Exception e) {
//            logger.error("Unable to get error details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public ComputationalResourceSchedulingModel getComputationalScheduling(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            ComputationSchedulingResource computationScheduling = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    computationScheduling = resource.getComputationScheduling(id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    computationScheduling = taskDetail.getComputationScheduling(id);
//                    break;
//            }
//            if (computationScheduling != null) {
//                return ThriftDataModelConversion.getComputationalResourceScheduling(computationScheduling);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting scheduling data..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    public AdvancedInputDataHandling getInputDataHandling(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            AdvanceInputDataHandlingResource dataHandlingResource = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    dataHandlingResource = resource.getInputDataHandling(id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    dataHandlingResource = taskDetail.getInputDataHandling(id);
//                    break;
//            }
//            if (dataHandlingResource != null) {
//                return ThriftDataModelConversion.getAdvanceInputDataHandling(dataHandlingResource);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting input data handling..", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }
//
//    public AdvancedOutputDataHandling getOutputDataHandling(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            AdvancedOutputDataHandlingResource dataHandlingResource = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    dataHandlingResource = resource.getOutputDataHandling(id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    dataHandlingResource = taskDetail.getOutputDataHandling(id);
//                    break;
//            }
//            if (dataHandlingResource != null) {
//                return ThriftDataModelConversion.getAdvanceOutputDataHandling(dataHandlingResource);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting output data handling...", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }
//
//    public QualityOfServiceParams getQosParams(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            QosParamResource qosParamResource = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    qosParamResource = resource.getQOSparams(id);
//                    break;
//            }
//            if (qosParamResource != null) {
//                return ThriftDataModelConversion.getQOSParams(qosParamResource);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting qos params..", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }

//    private WorkflowNodeDetailResource getWorkflowNodeDetailResource(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            return resource.getWorkflowNode(nodeId);
//        } catch (Exception e) {
//            logger.error("Error while getting workflow node details...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public WorkflowNodeDetails getWorkflowNodeDetails(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
//            return ThriftDataModelConversion.getWorkflowNodeDetails(workflowNode);
//        } catch (Exception e) {
//            logger.error("Error while getting workflow node details...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public WorkflowNodeStatus getWorkflowNodeStatus(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
//            StatusResource workflowNodeStatus = workflowNode.getWorkflowNodeStatus();
//            return ThriftDataModelConversion.getWorkflowNodeStatus(workflowNodeStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting workflow node status..", e);
//            throw new RegistryException(e);
//        }
//    }

    public List<OutputDataObjectType> getNodeOutputs(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
//            List<NodeOutputResource> nodeOutputs = workflowNode.getNodeOutputs();
//            return ThriftDataModelConversion.getNodeOutputs(nodeOutputs);
//        } catch (Exception e) {
//            logger.error("Error while getting node outputs..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public TaskModel getTaskDetails(String taskId) throws RegistryException {
//        try {
//            TaskDetailResource taskDetail = getTaskDetailResource(taskId);
//            return ThriftDataModelConversion.getTaskModel(taskDetail);
//        } catch (Exception e) {
//            logger.error("Error while getting task details..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    private TaskResource getTaskDetailResource(String taskId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            return workflowNode.getTaskDetail(taskId);
//        } catch (Exception e) {
//            logger.error("Error while getting task details..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public List<OutputDataObjectType> getApplicationOutputs(String taskId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            List<ApplicationOutputResource> applicationOutputs = taskDetail.getApplicationOutputs();
//            return ThriftDataModelConversion.getApplicationOutputs(applicationOutputs);
//        } catch (Exception e) {
//            logger.error("Error while getting application outputs..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            StatusResource taskStatus = taskDetail.getTaskStatus();
//            return ThriftDataModelConversion.getTaskStatus(taskStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting experiment outputs..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }


    // ids contains task id + job id
    public JobModel getJobDetails(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            return ThriftDataModelConversion.getJobDetail(jobDetail);
//        } catch (Exception e) {
//            logger.error("Error while getting job details..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    // ids contains task id + job id
    public JobStatus getJobStatus(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource jobStatus = jobDetail.getJobStatus();
//            return ThriftDataModelConversion.getJobStatus(jobStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting job status..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    public ApplicationStatus getApplicationStatus(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource applicationStatus = jobDetail.getApplicationStatus();
//            return ThriftDataModelConversion.getApplicationStatus(applicationStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting application status..", e);
//            throw new RegistryException(e);
//        }
//    }

//    public DataTransferDetails getDataTransferDetails(String transferId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
//            return ThriftDataModelConversion.getDataTransferDetail(dataTransferDetail);
//        } catch (Exception e) {
//            logger.error("Error while getting data transfer details..", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public TransferStatus getDataTransferStatus(String transferId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
//            StatusResource dataTransferStatus = dataTransferDetail.getDataTransferStatus();
//            return ThriftDataModelConversion.getTransferStatus(dataTransferStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting data transfer status..", e);
//            throw new RegistryException(e);
//        }
//    }

//    public List<String> getWorkflowNodeIds(String fieldName, Object value) throws RegistryException {
//        List<String> wfIds = new ArrayList<String>();
//        List<WorkflowNodeDetails> wfNodeDetails = getWFNodeDetails(fieldName, value);
//        for (WorkflowNodeDetails wf : wfNodeDetails) {
//            wfIds.add(wf.getNodeInstanceId());
//        }
//        return wfIds;
//    }

//
//    public List<String> getTransferDetailIds(String fieldName, Object value) throws RegistryException {
//        List<String> transferIds = new ArrayList<String>();
//        List<DataTransferDetails> dataTransferDetails = getDataTransferDetails(fieldName, value);
//        for (DataTransferDetails dtd : dataTransferDetails) {
//            transferIds.add(dtd.getTransferID());
//        }
//        return transferIds;
//    }


    public void removeJobDetails(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetailResource = nodeDetailResource.getTaskDetail((String) ids.getTopLevelIdentifier());
//            taskDetailResource.remove(ResourceType.JOB_DETAIL, (String) ids.getSecondLevelIdentifier());
//        } catch (Exception e) {
//            logger.error("Error while removing job details..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeDataTransferDetails(String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) nodeDetailResource.create(ResourceType.TASK_DETAIL);
//            taskDetail.remove(ResourceType.DATA_TRANSFER_DETAIL, transferId);
//        } catch (Exception e) {
//            logger.error("Error while removing transfer details..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeComputationalScheduling(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    taskDetail.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing scheduling data..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeInputDataHandling(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    taskDetail.remove(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing input data handling..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeOutputDataHandling(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    taskDetail.remove(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing output data handling..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeQOSParams(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.QOS_PARAM, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing QOS params", e);
//            throw new RegistryException(e);
//        }
    }

    public boolean isExperimentExist(String expID) throws RegistryException {
        try {
            return gatewayResource.isExists(ResourceType.EXPERIMENT, expID);
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isExperimentConfigDataExist(String expID) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expID);
//            experiment.isExists(ResourceType.CONFIG_DATA, expID);
//            return true;
//        } catch (Exception e) {
//            logger.error("Error while retrieving experiment...", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isWFNodeExist(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            return experiment.isExists(ResourceType.WORKFLOW_NODE_DETAIL, nodeId);
//        } catch (Exception e) {
//            logger.error("Error while retrieving workflow...", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isTaskDetailExist(String taskId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            return wf.isExists(ResourceType.TASK_DETAIL, taskId);
//        } catch (Exception e) {
//            logger.error("Error while retrieving task.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isJobDetailExist(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = wf.getTaskDetail((String) ids.getTopLevelIdentifier());
//            return taskDetail.isExists(ResourceType.JOB_DETAIL, (String) ids.getSecondLevelIdentifier());
//        } catch (Exception e) {
//            logger.error("Error while retrieving job details.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isTransferDetailExist(String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) wf.create(ResourceType.TASK_DETAIL);
//            return taskDetail.isExists(ResourceType.DATA_TRANSFER_DETAIL, transferId);
//        } catch (Exception e) {
//            logger.error("Error while retrieving transfer details.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isComputationalSchedulingExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                default:
//                    logger.error("Unsupported data type...");
//
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving scheduling data.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isInputDataHandlingExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving input data handling.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isOutputDataHandlingExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving output data handling..", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isQOSParamsExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.QOS_PARAM, id);
////                case TASK_DETAIL:
////                    ExperimentResource exp = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
////                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource)exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
////                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
////                    return taskDetail.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving qos params..", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public void updateScheduling(ComputationalResourceSchedulingModel scheduling, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateSchedulingData(scheduling, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateSchedulingData(scheduling, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating scheduling..", e);
//            throw new RegistryException(e);
//        }
    }

//    public void updateInputDataHandling(AdvancedInputDataHandling dataHandling, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateInputDataHandling(dataHandling, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateInputDataHandling(dataHandling, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating input data handling..", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateOutputDataHandling(AdvancedOutputDataHandling dataHandling, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateOutputDataHandling(dataHandling, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateOutputDataHandling(dataHandling, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating output data handling", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateQOSParams(QualityOfServiceParams params, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateQosParams(params, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateQosParams(params, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating QOS data..", e);
//            throw new RegistryException(e);
//        }
//    }

    /**
     * To search the experiments of user with the given filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC.
     *
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */

    public boolean isValidStatusTransition(Object object1, Object object2) {
        if (object1 instanceof ExperimentState && object2 instanceof ExperimentState) {
            ExperimentState oldState = (ExperimentState) object1;
            ExperimentState nextState = (ExperimentState) object2;
            if (nextState == null) {
                return false;
            }
            switch (oldState) {
                case CREATED:
                    return true;
                case VALIDATED:
                    return nextState != ExperimentState.CREATED;
                case SCHEDULED:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED;
                case LAUNCHED:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED
                            || nextState != ExperimentState.SCHEDULED;
                case EXECUTING:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED
                            || nextState != ExperimentState.SCHEDULED
                            || nextState != ExperimentState.LAUNCHED;

                case CANCELING:
                    return nextState == ExperimentState.CANCELING
                            || nextState == ExperimentState.CANCELED
                            || nextState == ExperimentState.COMPLETED
                            || nextState == ExperimentState.FAILED;
                case CANCELED:
                    return nextState == ExperimentState.CANCELED;
                case COMPLETED:
                    return nextState == ExperimentState.COMPLETED;
                case FAILED:
                    return nextState == ExperimentState.FAILED;
                default:
                    return false;
            }
        } else if (object1 instanceof ProcessState && object2 instanceof ProcessState) {
            ProcessState oldState = (ProcessState) object1;
            ProcessState nextState = (ProcessState) object2;
            if (nextState == null) {
                return false;
            }
            //TODO
            switch (oldState) {
                case CREATED:
                    return true;
                default:
                    return false;
            }
        } else if (object1 instanceof TaskState && object2 instanceof TaskState) {
            TaskState oldState = (TaskState) object1;
            TaskState nextState = (TaskState) object2;
            if (nextState == null) {
                return false;
            }
            //TODO
            switch (oldState) {
                case CREATED:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }
}
