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
            if (!ExpCatResourceUtils.isUserExist(experiment.getUserName(), experiment.getGatewayId())) {
                ExpCatResourceUtils.addUser(experiment.getUserName(), null, experiment.getGatewayId());
            }
            if (!workerResource.isProjectExists(experiment.getProjectId())) {
                logger.error("Project does not exist in the system..");
                throw new Exception("Project does not exist in the system, Please create the project first...");
            }
	        experimentId = AiravataUtils.getId(experiment.getExperimentName());
	        experiment.setExperimentId(experimentId);
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setExperimentId(experimentId);
            experimentResource.setProjectId(experiment.getProjectId());
            experimentResource.setGatewayId(experiment.getGatewayId());
            experimentResource.setExperimentType(experiment.getExperimentType().toString());
            experimentResource.setUserName(experiment.getUserName());
            experimentResource.setExperimentName(experiment.getExperimentName());
            experimentResource.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
            experimentResource.setDescription(experiment.getDescription());
            experimentResource.setExecutionId(experiment.getExecutionId());
            experimentResource.setGatewayExecutionId(experiment.getGatewayExecutionId());
            experimentResource.setGatewayInstanceId(experiment.getGatewayInstanceId());
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
            experimentStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
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
            configDataResource.setStaticWorkingDir(configurationData.getComputationalResourceScheduling().getStaticWorkingDir());
            configDataResource.setStorageId(configurationData.getStorageId());
            configDataResource.setExperimentDataDir(configurationData.getExperimentDataDir());
            configDataResource.setUseUserCRPref(configurationData.isUseUserCRPref());
            configDataResource.setOverrideLoginUserName(configurationData.getComputationalResourceScheduling().getOverrideLoginUserName());
            configDataResource.setOverrideScratchLocation(configurationData.getComputationalResourceScheduling().getOverrideScratchLocation());
            configDataResource.setOverrideAllocationProjectNumber(configurationData.getComputationalResourceScheduling().getOverrideAllocationProjectNumber());
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
                resource.setRequiredToAddedToCmd(input.isRequiredToAddedToCommandLine());
                resource.setStorageResourceId(input.getStorageResourceId());
                resource.setIsReadOnly(input.isIsReadOnly());
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
                resource.setOutputStreaming(output.isOutputStreaming());
                resource.setStorageResourceId(output.getStorageResourceId());
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
            ExperimentState newState = experimentStatus.getState();
            if (status == null) {
                status = (ExperimentStatusResource) experiment.create(ResourceType.EXPERIMENT_STATUS);
                status.setStatusId(getStatusID("EXPERIMENT_STATE"));
            }else {
                String state = status.getState();
                if (newState != null && !state.equals(newState.toString())){
                    status.setStatusId(getStatusID("EXPERIMENT_STATE"));
                }
            }
            status.setExperimentId(expId);
            status.setTimeOfStateChange(AiravataUtils.getTime(experimentStatus.getTimeOfStateChange()));
            if (newState != null){
                status.setState(newState.toString());
            }
            status.setReason(experimentStatus.getReason());
            status.save();
	        logger.debug(expId, "Added experiment {} status to {}.", expId, experimentStatus.toString());
        } catch (Exception e) {
            logger.error(expId, "Error while adding experiment status...", e);
            throw new RegistryException(e);
        }
        return expId;
    }

    public String addExperimentError(ErrorModel experimentError, String expId) throws RegistryException {
        try {
            ExperimentErrorResource error = new ExperimentErrorResource();
            if (experimentError.getErrorId() == null){
                error.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            }else {
                error.setErrorId(experimentError.getErrorId());
            }
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
	        String processId = AiravataUtils.getId("PROCESS");
	        process.setProcessId(processId);
            processResource.setProcessId(processId);
            processResource.setExperimentId(expId);
            processResource.setCreationTime(AiravataUtils.getTime(process.getCreationTime()));
            processResource.setLastUpdateTime(AiravataUtils.getTime(process.getLastUpdateTime()));
            processResource.setProcessDetail(process.getProcessDetail());
            processResource.setApplicationInterfaceId(process.getApplicationInterfaceId());
            processResource.setTaskDag(process.getTaskDag());
            processResource.setGatewayExecutionId(process.getGatewayExecutionId());
            processResource.setComputeResourceId(process.getComputeResourceId());
            processResource.setApplicationInterfaceId(process.getApplicationInterfaceId());
            processResource.setStorageResourceId(process.getStorageResourceId());
            processResource.setUserDn(process.getUserDn());
            processResource.setGenerateCert(process.isGenerateCert());
            processResource.setExperimentDataDir(process.getExperimentDataDir());
            processResource.setUserName(process.getUserName());
            processResource.setUseUserCRPref(process.isUseUserCRPref());
            processResource.setProcessTypeValue(process.getProcessType().getValue());
            if(process.isEnableEmailNotification()){
                processResource.setEnableEmailNotification(true);
                if(process.getEmailAddresses() != null){
                    processResource.setEmailAddresses(StringUtils.join(process.getEmailAddresses(), ","));
                }
            }else{
                processResource.setEnableEmailNotification(false);
            }

            processResource.save();

            if(process.getProcessResourceSchedule() != null) {
                addProcessResourceSchedule(process.getProcessResourceSchedule(), process.getProcessId());
            }
            if(process.getProcessInputs() !=  null && process.getProcessInputs().size() > 0) {
                addProcessInputs(process.getProcessInputs(), process.getProcessId());
            }
            if(process.getProcessOutputs() != null && process.getProcessOutputs().size() > 0) {
                addProcessOutputs(process.getProcessOutputs(), process.getProcessId());
            }

            ProcessStatus processStatus = new ProcessStatus();
            processStatus.setState(ProcessState.CREATED);
            List<ProcessStatus> processStatuses = new ArrayList<>();
            processStatuses.add(processStatus);
            addProcessStatus(processStatuses.get(0), process.getProcessId());

            if(process.getProcessErrors() != null) {
                addProcessError(process.getProcessErrors().get(0), process.getProcessId());
            }
        } catch (Exception e) {
            logger.error(expId, "Error while adding process...", e);
            throw new RegistryException(e);
        }
        return process.getProcessId();
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
            processResourceSchedule.setOverrideAllocationProjectNumber(resourceSchedule.getOverrideAllocationProjectNumber());
            processResourceSchedule.setOverrideLoginUserName(resourceSchedule.getOverrideLoginUserName());
            processResourceSchedule.setOverrideScratchLocation(resourceSchedule.getOverrideScratchLocation());
            processResourceSchedule.setStaticWorkingDir(resourceSchedule.getStaticWorkingDir());
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
                resource.setRequiredToAddedToCmd(input.isRequiredToAddedToCommandLine());
                resource.setStorageResourceId(input.getStorageResourceId());
                resource.setIsReadOnly(input.isIsReadOnly());
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
                resource.setOutputStreaming(output.isOutputStreaming());
                resource.setStorageResourceId(output.getStorageResourceId());
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
                ProcessState newState = processStatus.getState();
                if (status == null) {
                    status = (ProcessStatusResource) processResource.create(ResourceType.PROCESS_STATUS);
                    status.setStatusId(getStatusID("PROCESS_STATE"));
                }else {
                    String state = status.getState();
                    if (newState != null && !state.equals(newState.toString())){
                        status.setStatusId(getStatusID("PROCESS_STATE"));
                    }
                }
                status.setProcessId(processID);
                status.setTimeOfStateChange(AiravataUtils.getTime(processStatus.getTimeOfStateChange()));
                if (newState != null){
                    status.setState(newState.toString());
                }
                status.setReason(processStatus.getReason());
                status.save();
                logger.debug(processID, "Added process {} status to {}.", processID, processStatus.toString());
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
            if (processError.getErrorId() == null){
                error.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            }else {
                error.setErrorId(processError.getErrorId());
            }
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
	        task.setTaskId(AiravataUtils.getId("TASK"));
            taskResource.setTaskId(task.getTaskId());
            taskResource.setParentProcessId(task.getParentProcessId());
            taskResource.setTaskType(task.getTaskType().toString());
            taskResource.setCreationTime(AiravataUtils.getTime(task.getCreationTime()));
            taskResource.setLastUpdateTime(AiravataUtils.getTime(task.getLastUpdateTime()));
            taskResource.setTaskDetail(task.getTaskDetail());
            taskResource.setSubTaskModel(task.getSubTaskModel());
            taskResource.save();

            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setState(TaskState.CREATED);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
	        addTaskStatus(taskStatus, task.getTaskId());

            if(task.getTaskErrors() != null) {
                addTaskError(task.getTaskErrors().get(0), task.getTaskId());
            }
        } catch (Exception e) {
            logger.error(processID, "Error while adding task...", e);
            throw new RegistryException(e);
        }
        return task.getTaskId();
    }

    public String addTaskStatus(TaskStatus taskStatus, String taskID) throws RegistryException {
        try {
            TaskResource taskResource = new TaskResource();
            taskResource.setTaskId(taskID);
            TaskStatusResource status = taskResource.getTaskStatus();
            TaskState newState = taskStatus.getState();
            if (status == null) {
                status = (TaskStatusResource) taskResource.create(ResourceType.TASK_STATUS);
                status.setStatusId(getStatusID("TASK_STATE"));
            }else {
                String state = status.getState();
                if (newState != null && !state.equals(newState.toString())){
                    status.setStatusId(getStatusID("TASK_STATE"));
                }
            }
            status.setTaskId(taskID);
            status.setTimeOfStateChange(AiravataUtils.getTime(taskStatus.getTimeOfStateChange()));
            if (newState != null){
                status.setState(newState.toString());
            }
            status.setReason(taskStatus.getReason());
            status.save();
	        logger.debug(taskID, "Added task {} status to {}.", taskID, taskStatus.toString());
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
            if (taskError.getErrorId() == null){
                error.setErrorId(AiravataUtils.getId("TASK_ERROR"));
            }else {
                error.setErrorId(taskError.getErrorId());
            }
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

    public String addJob(JobModel job, String processId) throws RegistryException {
        try {
            JobResource jobResource = new JobResource();
            jobResource.setJobId(job.getJobId());
            jobResource.setProcessId(processId);
	        jobResource.setTaskId(job.getTaskId());
            jobResource.setJobDescription(job.getJobDescription());
            jobResource.setCreationTime(AiravataUtils.getTime(job.getCreationTime()));
            jobResource.setComputeResourceConsumed(job.getComputeResourceConsumed());
            jobResource.setJobName(job.getJobName());
            jobResource.setWorkingDir(job.getWorkingDir());
            jobResource.setExitCode(job.getExitCode());
            jobResource.setStdOut(job.getStdOut());
            jobResource.setStdErr(job.getStdErr());
			jobResource.save();
        } catch (Exception e) {
            logger.error(processId, "Error while adding task...", e);
            throw new RegistryException(e);
        }
        return processId;
    }

    public String addJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {
        String taskId = (String)cis.getTopLevelIdentifier();
        String jobID = (String)cis.getSecondLevelIdentifier();
        try {
            JobResource jobResource = new JobResource();
            jobResource.setJobId(jobID);
            JobStatusResource status = jobResource.getJobStatus();
            if (status == null) {
                status = new JobStatusResource();
            }
	        status.setStatusId(getStatusID(jobID));
	        status.setJobId(jobID);
	        status.setTaskId(taskId);
	        status.setTimeOfStateChange(AiravataUtils.getTime(jobStatus.getTimeOfStateChange()));
	        status.setState(jobStatus.getJobState().toString());
	        status.setReason(jobStatus.getReason());
	        status.save();
	        logger.debug(jobID, "Added job {} status to {}.", jobID, jobStatus.toString());
        } catch (Exception e) {
            logger.error(jobID, "Error while adding job status...", e);
            throw new RegistryException(e);
        }
        return jobID;
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
            existingExperiment.setGatewayId(experiment.getGatewayId());
            existingExperiment.setGatewayExecutionId(experiment.getGatewayExecutionId());
            existingExperiment.setGatewayInstanceId(experiment.getGatewayInstanceId());
            existingExperiment.setProjectId(experiment.getProjectId());
            existingExperiment.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
            existingExperiment.setDescription(experiment.getDescription());
            existingExperiment.setExecutionId(experiment.getExecutionId());

            if(experiment.isEnableEmailNotification()){
                existingExperiment.setEnableEmailNotification(true);
                if(experiment.getEmailAddresses() != null){
                    existingExperiment.setEmailAddresses(StringUtils.join(experiment.getEmailAddresses(), ","));
                }
            }else{
                existingExperiment.setEnableEmailNotification(false);
            }

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

            List<ExperimentStatus> experimentStatuses = experiment.getExperimentStatus();
            if (experimentStatuses != null && experimentStatuses.size() > 0) {
                if (experimentStatuses.get(0) != null) {
                    updateExperimentStatus(experimentStatuses.get(0), expId);
                }
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
                        exinput.setStorageResourceId(input.getStorageResourceId());
                        exinput.setIsReadOnly(input.isIsReadOnly());
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
                        resource.setOutputStreaming(output.isOutputStreaming());
                        resource.setStorageResourceId(output.getStorageResourceId());
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
            configDataResource.setStorageId(configurationData.getStorageId());
            configDataResource.setResourceHostId(configurationData.getComputationalResourceScheduling().getResourceHostId());
            configDataResource.setTotalCpuCount(configurationData.getComputationalResourceScheduling().getTotalCPUCount());
            configDataResource.setNodeCount(configurationData.getComputationalResourceScheduling().getNodeCount());
            configDataResource.setNumberOfThreads(configurationData.getComputationalResourceScheduling().getNumberOfThreads());
            configDataResource.setQueueName(configurationData.getComputationalResourceScheduling().getQueueName());
            configDataResource.setWallTimeLimit(configurationData.getComputationalResourceScheduling().getWallTimeLimit());
            configDataResource.setTotalPhysicalMemory(configurationData.getComputationalResourceScheduling().getTotalPhysicalMemory());
            configDataResource.setStaticWorkingDir(configurationData.getComputationalResourceScheduling().getStaticWorkingDir());
            configDataResource.setExperimentDataDir(configurationData.getExperimentDataDir());
            configDataResource.setUseUserCRPref(configurationData.isUseUserCRPref());
            configDataResource.setOverrideLoginUserName(configurationData.getComputationalResourceScheduling().getOverrideLoginUserName());
            configDataResource.setOverrideScratchLocation(configurationData.getComputationalResourceScheduling().getOverrideScratchLocation());
            configDataResource.setOverrideAllocationProjectNumber(configurationData.getComputationalResourceScheduling().getOverrideAllocationProjectNumber());
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
            processResource.setGatewayExecutionId(process.getGatewayExecutionId());
            processResource.setComputeResourceId(process.getComputeResourceId());
            processResource.setApplicationDeploymentId(process.getApplicationDeploymentId());
            processResource.setStorageResourceId(process.getStorageResourceId());
            processResource.setUserDn(process.getUserDn());
            processResource.setGenerateCert(process.isGenerateCert());
            processResource.setExperimentDataDir(process.getExperimentDataDir());
            processResource.setUserName(process.getUserName());
            processResource.setUseUserCRPref(process.isUseUserCRPref());
            processResource.setProcessTypeValue(process.getProcessType().getValue());
            if(process.isEnableEmailNotification()){
                processResource.setEnableEmailNotification(true);
                if(process.getEmailAddresses() != null){
                    processResource.setEmailAddresses(StringUtils.join(process.getEmailAddresses(), ","));
                }
            }else{
                processResource.setEnableEmailNotification(false);
            }

            processResource.save();

            if(process.getProcessResourceSchedule() != null) {
                updateProcessResourceSchedule(process.getProcessResourceSchedule(), process.getProcessId());
            }
            if(process.getProcessInputs() !=  null && process.getProcessInputs().size() > 0) {
                updateProcessInputs(process.getProcessInputs(), process.getProcessId());
            }
            if(process.getProcessOutputs() != null && process.getProcessOutputs().size() > 0) {
                updateProcessOutputs(process.getProcessOutputs(), process.getProcessId());
            }
            if(process.getProcessStatuses() != null) {
                updateProcessStatus(process.getProcessStatuses().get(0), process.getProcessId());
            }
            if(process.getProcessErrors() != null) {
                updateProcessError(process.getProcessErrors().get(0), process.getProcessId());
            }
            if(process.getTasks() != null && process.getTasks().size() > 0){
                for(TaskModel task : process.getTasks()){
                    updateTask(task, task.getTaskId());
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating process...", e);
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
            processResourceSchedule.setOverrideAllocationProjectNumber(resourceSchedule.getOverrideAllocationProjectNumber());
            processResourceSchedule.setOverrideLoginUserName(resourceSchedule.getOverrideLoginUserName());
            processResourceSchedule.setOverrideScratchLocation(resourceSchedule.getOverrideScratchLocation());
            processResourceSchedule.setStaticWorkingDir(resourceSchedule.getStaticWorkingDir());
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
                        exinput.setStorageResourceId(input.getStorageResourceId());
                        exinput.setIsReadOnly(input.isIsReadOnly());
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
                        resource.setOutputStreaming(output.isOutputStreaming());
                        resource.setStorageResourceId(output.getStorageResourceId());
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

    public String updateProcessError(ErrorModel processErrors, String processID) throws RegistryException {
        return addProcessError(processErrors, processID);
    }

    public String updateTask(TaskModel task, String taskID) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            TaskResource taskResource = processResource.getTask(taskID);
            taskResource.setTaskId(task.getTaskId());
            taskResource.setParentProcessId(task.getParentProcessId());
            taskResource.setTaskType(task.getTaskType().toString());
            taskResource.setCreationTime(AiravataUtils.getTime(task.getCreationTime()));
            taskResource.setLastUpdateTime(AiravataUtils.getTime(task.getLastUpdateTime()));
            taskResource.setTaskDetail(task.getTaskDetail());
            taskResource.setSubTaskModel(task.getSubTaskModel());
            taskResource.save();

            if(task.getTaskErrors() != null) {
                updateTaskError(task.getTaskErrors().get(0), task.getTaskId());
            }
            if(task.getTaskErrors() != null) {
                updateTaskError(task.getTaskErrors().get(0), task.getTaskId());
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

    public String updateJob(JobModel job, CompositeIdentifier cis) throws RegistryException {
        String taskId = (String) cis.getTopLevelIdentifier();
        String jobId = (String) cis.getSecondLevelIdentifier();
        try {
	        TaskResource taskResource = new TaskResource();
	        taskResource.setTaskId(taskId);
	        JobResource jobResource = taskResource.getJob(jobId);
	        jobResource.setJobId(jobId);
	        jobResource.setTaskId(job.getTaskId());
	        jobResource.setProcessId(job.getProcessId());
	        jobResource.setJobDescription(job.getJobDescription());
	        jobResource.setCreationTime(AiravataUtils.getTime(job.getCreationTime()));
	        jobResource.setComputeResourceConsumed(job.getComputeResourceConsumed());
	        jobResource.setJobName(job.getJobName());
	        jobResource.setWorkingDir(job.getWorkingDir());
            jobResource.setStdOut(job.getStdOut());
            jobResource.setStdErr(job.getStdErr());
            jobResource.setExitCode(job.getExitCode());
	        jobResource.save();
        } catch (Exception e) {
            logger.error(jobId, "Error while adding job...", e);
            throw new RegistryException(e);
        }
        return jobId;
    }

    public String updateJobStatus(JobStatus jobStatus, CompositeIdentifier cis) throws RegistryException {
        return addJobStatus(jobStatus, cis);
    }


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
            ExperimentResource experiment = new ExperimentResource();
            experiment.setExperimentId(expID);
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
                updateComputeResourceScheduling((ComputationalResourceSchedulingModel) value, expID);
            } else {
                logger.error("Unsupported field type for Experiment config data");
            }

        } catch (Exception e) {
            logger.error("Error while updating fields in experiment config...", e);
            throw new RegistryException(e);
        }
    }

    public void updateComputeResourceScheduling(ComputationalResourceSchedulingModel value, String expID) throws RegistryException {
        ExperimentResource experiment = new ExperimentResource();
        experiment.setExperimentId(expID);
        UserConfigurationDataResource configDataResource = experiment.getUserConfigurationDataResource();
        configDataResource.setResourceHostId(value.getResourceHostId());
        configDataResource.setTotalCpuCount(value.getTotalCPUCount());
        configDataResource.setNodeCount(value.getNodeCount());
        configDataResource.setNumberOfThreads(value.getNumberOfThreads());
        configDataResource.setQueueName(value.getQueueName());
        configDataResource.setWallTimeLimit(value.getWallTimeLimit());
        configDataResource.setTotalPhysicalMemory(value.getTotalPhysicalMemory());
        configDataResource.setStaticWorkingDir(value.getStaticWorkingDir());
        configDataResource.setOverrideLoginUserName(value.getOverrideLoginUserName());
        configDataResource.setOverrideScratchLocation(value.getOverrideScratchLocation());
        configDataResource.setOverrideAllocationProjectNumber(value.getOverrideAllocationProjectNumber());
        configDataResource.save();
    }

    //CPI get methods
    public Object getExperiment(String expId, String fieldName) throws RegistryException {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getExperiment(resource);
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
                logger.error("Unsupported field name for experiment data..");
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

    public Object getProcess(String processId, String fieldName) throws RegistryException {
        try {
            ExperimentResource experimentResource = new ExperimentResource();
            ProcessResource resource = experimentResource.getProcess(processId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getProcessModel(resource);
            } else if (fieldName.equals(Constants.FieldConstants.ProcessConstants.PROCESS_ERROR)) {
                return ThriftDataModelConversion.getErrorModel(resource.getProcessError());
            } else if (fieldName.equals(Constants.FieldConstants.ProcessConstants.PROCESS_STATUS)) {
                return ThriftDataModelConversion.getProcessStatus(resource.getProcessStatus());
            } else if (fieldName.equals(Constants.FieldConstants.ProcessConstants.PROCESS_INPUTS)) {
                return ThriftDataModelConversion.getProcessInputs(resource.getProcessInputs());
            } else if (fieldName.equals(Constants.FieldConstants.ProcessConstants.PROCESS_OUTPUTS)) {
                return ThriftDataModelConversion.getProcessOutputs(resource.getProcessOutputs());
            } else if (fieldName.equals(Constants.FieldConstants.ProcessConstants.PROCESS_RESOURCE_SCHEDULE)) {
                return ThriftDataModelConversion.getProcessResourceSchedule(resource.getProcessResourceSchedule());
            } else {
                logger.error("Unsupported field name for process data..");
            }
        }catch (Exception e) {
            logger.error("Error while getting process data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getProcessError(String processId) throws RegistryException {
        return getProcess(processId, Constants.FieldConstants.ProcessConstants.PROCESS_ERROR);
    }

    public Object getProcessStatus(String processId) throws RegistryException {
        return getProcess(processId, Constants.FieldConstants.ProcessConstants.PROCESS_STATUS);
    }

    public Object getProcessInputs(String processId) throws RegistryException {
        return getProcess(processId, Constants.FieldConstants.ProcessConstants.PROCESS_INPUTS);
    }

    public Object getProcessOutputs(String processId) throws RegistryException {
        return getProcess(processId, Constants.FieldConstants.ProcessConstants.PROCESS_OUTPUTS);
    }

    public Object getProcessResourceSchedule(String processId) throws RegistryException {
        return getProcess(processId, Constants.FieldConstants.ProcessConstants.PROCESS_RESOURCE_SCHEDULE);
    }

    public Object getTask(String taskId, String fieldName) throws RegistryException {
        try {
            ProcessResource processResource = new ProcessResource();
            TaskResource resource = processResource.getTask(taskId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getTaskModel(resource);
            } else if (fieldName.equals(Constants.FieldConstants.TaskConstants.TASK_ERROR)) {
                return ThriftDataModelConversion.getErrorModel(resource.getTaskError());
            } else if (fieldName.equals(Constants.FieldConstants.TaskConstants.TASK_STATUS)) {
                return ThriftDataModelConversion.getTaskStatus(resource.getTaskStatus());
            } else {
                logger.error("Unsupported field name for task data..");
            }
        }catch (Exception e) {
            logger.error("Error while getting task data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getTaskError(String taskId) throws RegistryException {
        return getTask(taskId, Constants.FieldConstants.TaskConstants.TASK_ERROR);
    }

    public Object getTaskStatus(String taskId) throws RegistryException {
        return getTask(taskId, Constants.FieldConstants.TaskConstants.TASK_STATUS);
    }

    public Object getJob(CompositeIdentifier cis, String fieldName) throws RegistryException {
        String taskId = (String) cis.getTopLevelIdentifier();
        String jobId = (String) cis.getSecondLevelIdentifier();
        try {
	        TaskResource taskResource = new TaskResource();
	        taskResource.setTaskId(taskId);
	        JobResource resource = taskResource.getJob(jobId);
	        if (fieldName == null) {
		        return ThriftDataModelConversion.getJobModel(resource);
	        } else if (fieldName.equals(Constants.FieldConstants.JobConstants.JOB_STATUS)) {
		        return ThriftDataModelConversion.getJobStatus(resource.getJobStatus());
	        } else {
		        logger.error("Unsupported field name for job basic data..");
	        }
        }catch (Exception e) {
            logger.error("Error while getting job data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getJobStatus(CompositeIdentifier cis) throws RegistryException {
        return getJob(cis, Constants.FieldConstants.JobConstants.JOB_STATUS);
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
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID)) {
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

    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        List<ProcessModel> processes = new ArrayList<ProcessModel>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID)) {
                ExperimentResource experimentResource = new ExperimentResource();
                experimentResource.setExperimentId((String) value);
                List<ProcessResource> resources = experimentResource.getProcessList();
                for (ProcessResource processResource : resources) {
                    ProcessModel processModel = ThriftDataModelConversion.getProcessModel(processResource);
                    processes.add(processModel);
                }
                return processes;
            } else {
                logger.error("Unsupported field name to retrieve process list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting process list...", e);
            throw new RegistryException(e);
        }
        return processes;
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {
        List<TaskModel> tasks = new ArrayList<TaskModel>();
        try {
            if (fieldName.equals(Constants.FieldConstants.TaskConstants.PARENT_PROCESS_ID)) {
                ProcessResource processResource = new ProcessResource();
                processResource.setProcessId((String) value);
                List<TaskResource> resources = processResource.getTaskList();
                for (TaskResource taskResource : resources) {
                    TaskModel taskModel = ThriftDataModelConversion.getTaskModel(taskResource);
                    tasks.add(taskModel);
                }
                return tasks;
            } else {
                logger.error("Unsupported field name to retrieve task list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting task list...", e);
            throw new RegistryException(e);
        }
        return tasks;
    }

    public List<JobModel> getJobList(String fieldName, Object value) throws RegistryException {
        List<JobModel> jobs = new ArrayList<JobModel>();
        try {
            if (fieldName.equals(Constants.FieldConstants.JobConstants.PROCESS_ID)) {
                ProcessResource processResource = new ProcessResource();
                processResource.setProcessId((String) value);
                List<JobResource> resources = processResource.getJobList();
                for (JobResource jobResource : resources) {
                    JobModel jobModel = ThriftDataModelConversion.getJobModel(jobResource);
                    JobStatusResource latestSR = jobResource.getJobStatus();
	                if (latestSR != null) {
		                JobStatus jobStatus = new JobStatus(JobState.valueOf(latestSR.getState()));
		                jobStatus.setReason(latestSR.getReason());
                        List<JobStatus> statuses = new ArrayList<>();
                        statuses.add(jobStatus);
		                jobModel.setJobStatuses(statuses);
	                }
	                jobs.add(jobModel);
                }
                return jobs;
            }else if (fieldName.equals(Constants.FieldConstants.JobConstants.TASK_ID)) {
                TaskResource taskResource = new TaskResource();
                taskResource.setTaskId((String) value);
                List<JobResource> resources = taskResource.getJobList();
                for (JobResource jobResource : resources) {
                    JobModel jobModel = ThriftDataModelConversion.getJobModel(jobResource);
                    JobStatusResource latestSR = jobResource.getJobStatus();
                    if (latestSR != null) {
                        JobStatus jobStatus = new JobStatus(JobState.valueOf(latestSR.getState()));
                        jobStatus.setReason(latestSR.getReason());
                        List<JobStatus> statuses = new ArrayList<>();
                        statuses.add(jobStatus);
                        jobModel.setJobStatuses(statuses);
                    }
                    jobs.add(jobModel);
                }
                return jobs;
            }else {
                logger.error("Unsupported field name to retrieve job list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting job list...", e);
            throw new RegistryException(e);
        }
        return jobs;
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
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.PROJECT_ID, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID)) {
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
                    experimentSummaryResources = workerResource.searchExperiments(null, new Timestamp(fromTime), new Timestamp(toTime), fil
                            ,limit , offset, orderByIdentifier, resultOrderType);
                } else {
                    experimentSummaryResources = workerResource
                            .searchExperiments(null, null, null, fil, limit, offset, orderByIdentifier, resultOrderType);
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

    public List<ExperimentSummaryModel> searchAllAccessibleExperiments(List<String> accessibleIds, Map<String, String> filters, int limit,
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
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                        fil.put(AbstractExpCatResource.ExperimentConstants.PROJECT_ID, filters.get(field));
                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID)) {
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
                    experimentSummaryResources = workerResource.searchExperiments(accessibleIds, new Timestamp(fromTime), new Timestamp(toTime), fil
                            ,limit , offset, orderByIdentifier, resultOrderType);
                } else {
                    experimentSummaryResources = workerResource
                            .searchExperiments(accessibleIds, null, null, fil, limit, offset, orderByIdentifier, resultOrderType);
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
                    filters.get(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID),
                    new Timestamp(Long.parseLong(filters.get(Constants.FieldConstants.ExperimentConstants.FROM_DATE))),
                    new Timestamp(Long.parseLong(filters.get(Constants.FieldConstants.ExperimentConstants.TO_DATE))),
                    filters.get(Constants.FieldConstants.ExperimentConstants.USER_NAME),
                    filters.get(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID),
                    filters.get(Constants.FieldConstants.ExperimentConstants.RESOURCE_HOST_ID)
            );

            experimentStatistics.setAllExperimentCount(experimentStatisticsResource.getAllExperimentCount());
            experimentStatistics.setCreatedExperimentCount(experimentStatisticsResource.getCreatedExperimentCount());
            experimentStatistics.setRunningExperimentCount(experimentStatisticsResource.getRunningExperimentCount());
            experimentStatistics.setCompletedExperimentCount(experimentStatisticsResource.getCompletedExperimentCount());
            experimentStatistics.setFailedExperimentCount(experimentStatisticsResource.getFailedExperimentCount());
            experimentStatistics.setCancelledExperimentCount(experimentStatisticsResource.getCancelledExperimentCount());

            ArrayList<ExperimentSummaryModel> experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getAllExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setAllExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getCreatedExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setCreatedExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getRunningExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setRunningExperiments(experimentSummaries);

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
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID)) {
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

    public List<String> getJobIds(String fieldName, Object value) throws RegistryException {
        List<String> jobIds = new ArrayList<String>();
        List<JobModel> jobs = getJobList(fieldName, value);
        for (JobModel job : jobs) {
            jobIds.add(job.getJobId());
        }
        return jobIds;
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

    public void removeJob(CompositeIdentifier cis) throws RegistryException {
        try {
            String processId = (String) cis.getTopLevelIdentifier();
            String jobId = (String) cis.getSecondLevelIdentifier();
	        ProcessResource process = new ProcessResource();
	        process.setProcessId(processId);
	        process.remove(ResourceType.JOB, jobId);
        } catch (Exception e) {
            logger.error("Error while removing task details..", e);
            throw new RegistryException(e);
        }
    }


    //isExists CPI methods
    public boolean isExperimentExist(String expID) throws RegistryException {
        try {
            return gatewayResource.isExists(ResourceType.EXPERIMENT, expID);
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isUserConfigDataExist(String expID) throws RegistryException {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expID);
            experiment.isExists(ResourceType.USER_CONFIGURATION_DATA, expID);
            return true;
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isProcessExist(String processId) throws RegistryException {
        try {
            ExperimentResource experiment = new ExperimentResource();
            return experiment.isExists(ResourceType.PROCESS, processId);
        } catch (Exception e) {
            logger.error("Error while retrieving process...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isProcessResourceScheduleExist(String processId) throws RegistryException {
        try {
            ExperimentResource experiment = new ExperimentResource();
            return experiment.isExists(ResourceType.PROCESS_RESOURCE_SCHEDULE, processId);
        } catch (Exception e) {
            logger.error("Error while retrieving process...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isTaskExist(String taskId) throws RegistryException {
        try {
            ProcessResource process = new ProcessResource();
            return process.isExists(ResourceType.TASK, taskId);
        } catch (Exception e) {
            logger.error("Error while retrieving task.....", e);
            throw new RegistryException(e);
        }
    }


    public boolean isJobExist(CompositeIdentifier cis) throws RegistryException {
        String taskId = (String) cis.getTopLevelIdentifier();
        String jobId = (String) cis.getSecondLevelIdentifier();
        try {
            TaskResource taskResource = new TaskResource();
            taskResource.setTaskId(taskId);
            return taskResource.isExists(ResourceType.JOB, jobId);
        } catch (Exception e) {
            logger.error("Error while retrieving job.....", e);
            throw new RegistryException(e);
        }
    }

    public boolean createQueueStatuses(List<QueueStatusModel> queueStatusModels) throws RegistryException {
        for(QueueStatusModel qModel : queueStatusModels){
            QueueStatusResource queueStatusResource = new QueueStatusResource();
            queueStatusResource.setHostName(qModel.getHostName());
            queueStatusResource.setQueueName(qModel.getQueueName());
            queueStatusResource.setTime(qModel.getTime());
            queueStatusResource.setQueueUp(qModel.isQueueUp());
            queueStatusResource.setRunningJobs(qModel.getRunningJobs());
            queueStatusResource.setQueuedJobs(qModel.getQueuedJobs());

            queueStatusResource.save();
        }
        return true;
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        List<QueueStatusModel> queueStatusModels = new ArrayList<>();
        List<ExperimentCatResource> queueStatusResources =  (new QueueStatusResource()).get(ResourceType.QUEUE_STATUS);
        for(ExperimentCatResource r : queueStatusResources){
            QueueStatusResource qResource = (QueueStatusResource) r;
            QueueStatusModel queueStatusModel = new QueueStatusModel();
            queueStatusModel.setHostName(qResource.getHostName());
            queueStatusModel.setQueueName(qResource.getQueueName());
            queueStatusModel.setTime(qResource.getTime());
            queueStatusModel.setQueueUp(qResource.getQueueUp());
            queueStatusModel.setRunningJobs(qResource.getRunningJobs());
            queueStatusModel.setQueuedJobs(qResource.getQueuedJobs());
            queueStatusModels.add(queueStatusModel);
        }
        return queueStatusModels;
    }

    public String getStatusID(String parentId) {
        String status = parentId.replaceAll("\\s", "");
        return status + "_" + UUID.randomUUID();
    }

    public String getErrorID(String parentId) {
        String error = parentId.replaceAll("\\s", "");
        return error + "_" + UUID.randomUUID();
    }


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
            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                case CREATED:
//                    return true;
//                default:
//                    return false;
//            }
        } else if (object1 instanceof TaskState && object2 instanceof TaskState) {
            TaskState oldState = (TaskState) object1;
            TaskState nextState = (TaskState) object2;
            if (nextState == null) {
                return false;
            }
            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                case CREATED:
//                    return true;
//                default:
//                    return false;
//            }
        }else if (object1 instanceof JobState && object2 instanceof JobState) {
            JobState oldState = (JobState) object1;
            JobState nextState = (JobState) object2;
            if (nextState == null) {
                return false;
            }
            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                default:
//                    return false;
//            }
        }
        return false;
    }
}
