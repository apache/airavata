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
package org.apache.airavata.orchestrator.cpi.impl;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.*;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.impl.GFACPassiveJobSubmitter;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class SimpleOrchestratorImpl extends AbstractOrchestrator{
    private final static Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;

    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;


    public SimpleOrchestratorImpl() throws OrchestratorException, TException {
        try {
            try {
                // We are only going to use GFacPassiveJobSubmitter
                jobSubmitter = new GFACPassiveJobSubmitter();
                jobSubmitter.initialize(this.orchestratorContext);

            } catch (Exception e) {
                String error = "Error creating JobSubmitter in non threaded mode ";
                logger.error(error);
                throw new OrchestratorException(error, e);
            }
        } catch (OrchestratorException e) {
            logger.error("Error Constructing the Orchestrator");
            throw e;
        }
    }

    public boolean launchProcess(ProcessModel processModel, String tokenId) throws OrchestratorException {
        try {
	        return jobSubmitter.submit(processModel.getExperimentId(), processModel.getProcessId(), tokenId);
        } catch (Exception e) {
            throw new OrchestratorException("Error launching the job", e);
        }
    }

    public ValidationResults validateExperiment(ExperimentModel experiment)
            throws OrchestratorException,LaunchValidationException {
        org.apache.airavata.model.error.ValidationResults validationResults =
                new org.apache.airavata.model.error.ValidationResults();
        validationResults.setValidationState(true); // initially making it to success, if atleast one failed them simply mark it failed.
        String errorMsg = "Validation Errors : ";
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClasses = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClasses) {
                try {
                    Class<? extends JobMetadataValidator> vClass =
                            Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                    JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                    validationResults = jobMetadataValidator.validate(experiment, null);
                    if (validationResults.isValidationState()) {
                        logger.info("Validation of " + validator + " is SUCCESSFUL");
                    } else {
                        List<ValidatorResult> validationResultList = validationResults.getValidationResultList();
                        for (ValidatorResult result : validationResultList){
                            if (!result.isResult()){
                                String validationError = result.getErrorDetails();
                                if (validationError != null){
                                    errorMsg += validationError + " ";
                                }
                            }
                        }
                        logger.error("Validation of " + validator + " for experiment Id " +
                                experiment.getExperimentId() + " is FAILED:[error]. " + errorMsg);
                        validationResults.setValidationState(false);
                        try {
                            ErrorModel details = new ErrorModel();
                            details.setActualErrorMessage(errorMsg);
                            details.setCreationTime(Calendar.getInstance().getTimeInMillis());
                            final RegistryService.Client registryClient = getRegistryServiceClient();
                            try {
                                registryClient
                                        .addErrors(OrchestratorConstants.EXPERIMENT_ERROR, details, experiment.getExperimentId());
                            } finally {
                                if (registryClient != null) {
                                    ThriftUtils.close(registryClient);
                                }
                            }
                        } catch (RegistryServiceException e) {
                            logger.error("Error while saving error details to registry", e);
                            throw new RuntimeException("Error while saving error details to registry", e);
                        } catch (TException e) {
                            throw new RuntimeException("Error while saving error details to registry", e);
                        }
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                } catch (InstantiationException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                } catch (IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                }
            }
        }
        if(validationResults.isValidationState()){
            return validationResults;
        }else {
            //atleast one validation has failed, so we throw an exception
            LaunchValidationException launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults list for " +
                    "detail error. Validation errors : " + errorMsg);
            throw launchValidationException;
        }
    }

    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel)
            throws OrchestratorException, LaunchValidationException {

        org.apache.airavata.model.error.ValidationResults validationResults = new org.apache.airavata.model.error.ValidationResults();
        validationResults.setValidationState(true); // initially making it to success, if atleast one failed them simply mark it failed.
        String errorMsg = "Validation Errors : ";
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClzzez = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClzzez) {
                try {
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                    JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                    validationResults = jobMetadataValidator.validate(experiment, processModel);
                    if (validationResults.isValidationState()) {
                        logger.info("Validation of " + validator + " is SUCCESSFUL");
                    } else {
                        List<ValidatorResult> validationResultList = validationResults.getValidationResultList();
                        for (ValidatorResult result : validationResultList) {
                            if (!result.isResult()) {
                                String validationError = result.getErrorDetails();
                                if (validationError != null) {
                                    errorMsg += validationError + " ";
                                }
                            }
                        }
                        logger.error("Validation of " + validator + " for experiment Id " +
                                experiment.getExperimentId() + " is FAILED:[error]. " + errorMsg);
                        validationResults.setValidationState(false);
                        try {
                            ErrorModel details = new ErrorModel();
                            details.setActualErrorMessage(errorMsg);
                            details.setCreationTime(Calendar.getInstance().getTimeInMillis());
                            final RegistryService.Client registryClient = getRegistryServiceClient();
                            try {
                                registryClient
                                        .addErrors(OrchestratorConstants.PROCESS_ERROR, details, processModel.getProcessId());
                            } finally {
                                if (registryClient != null) {
                                    ThriftUtils.close(registryClient);
                                }
                            }
                        } catch (RegistryServiceException e) {
                            logger.error("Error while saving error details to registry", e);
                            throw new RuntimeException("Error while saving error details to registry", e);
                        } catch (TException e) {
                            throw new RuntimeException("Error while saving error details to registry", e);
                        }
                        break;
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                }
            }
        }
        if (validationResults.isValidationState()) {
            return validationResults;
        } else {
            //atleast one validation has failed, so we throw an exception
            LaunchValidationException launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults " +
                    "list for detail error. Validation errors : " + errorMsg);
            throw launchValidationException;
        }
    }


    public void cancelExperiment(ExperimentModel experiment, String tokenId) throws OrchestratorException {
        logger.info("Terminating experiment " + experiment.getExperimentId());
        RegistryService.Client registryServiceClient = getRegistryServiceClient();

        try {
            List<String> processIds = registryServiceClient.getProcessIds(experiment.getExperimentId());
            if (processIds != null && processIds.size() > 0) {
                for (String processId : processIds) {
                    logger.info("Terminating process " + processId + " of experiment " + experiment.getExperimentId());
                    jobSubmitter.terminate(experiment.getExperimentId(), processId, tokenId);
                }
            } else {
                logger.warn("No processes found for experiment " + experiment.getExperimentId() + " to cancel");
            }
        } catch (TException e) {
            logger.error("Failed to fetch process ids for experiment " + experiment.getExperimentId(), e);
            throw new OrchestratorException("Failed to fetch process ids for experiment " + experiment.getExperimentId(), e);
        }
    }


    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public JobSubmitter getJobSubmitter() {
        return jobSubmitter;
    }

    public void setJobSubmitter(JobSubmitter jobSubmitter) {
        this.jobSubmitter = jobSubmitter;
    }

    public void initialize() throws OrchestratorException {

    }

    public List<ProcessModel> createProcesses (String experimentId, String gatewayId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            ExperimentModel experimentModel = registryClient.getExperiment(experimentId);
            List<ProcessModel> processModels = registryClient.getProcessList(experimentId);
            if (processModels == null || processModels.isEmpty()){
                ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
                String processId = registryClient.addProcess(processModel, experimentId);
                processModel.setProcessId(processId);
                processModels = new ArrayList<>();
                processModels.add(processModel);
            }
            return processModels;
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public String createAndSaveTasks(String gatewayId, ProcessModel processModel, boolean autoSchedule) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            ComputationalResourceSchedulingModel resourceSchedule = processModel.getProcessResourceSchedule();
            String userGivenQueueName = resourceSchedule.getQueueName();
            int userGivenWallTime = resourceSchedule.getWallTimeLimit();
            String resourceHostId = resourceSchedule.getResourceHostId();
            if (resourceHostId == null){
                throw new OrchestratorException("Compute Resource Id cannot be null at this point");
            }
            ComputeResourceDescription computeResource = registryClient.getComputeResource(resourceHostId);
            JobSubmissionInterface preferredJobSubmissionInterface =
                    OrchestratorUtils.getPreferredJobSubmissionInterface(processModel, gatewayId);
            JobSubmissionProtocol preferredJobSubmissionProtocol = OrchestratorUtils.getPreferredJobSubmissionProtocol(processModel, gatewayId);
            List<String> taskIdList = new ArrayList<>();

            if (preferredJobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
                // TODO - breakdown unicore all in one task to multiple tasks, then we don't need to handle UNICORE here.
                taskIdList.addAll(createAndSaveSubmissionTasks(registryClient, gatewayId, preferredJobSubmissionInterface, processModel, userGivenWallTime));
            } else {
                taskIdList.addAll(createAndSaveEnvSetupTask(registryClient, gatewayId, processModel));
                taskIdList.addAll(createAndSaveInputDataStagingTasks(processModel, gatewayId));
                if (autoSchedule) {
                    List<BatchQueue> definedBatchQueues = computeResource.getBatchQueues();
                    for (BatchQueue batchQueue : definedBatchQueues) {
                        if (batchQueue.getQueueName().equals(userGivenQueueName)) {
                            int maxRunTime = batchQueue.getMaxRunTime();
                            if (maxRunTime < userGivenWallTime) {
                                resourceSchedule.setWallTimeLimit(maxRunTime);
                                // need to create more job submissions
                                int numOfMaxWallTimeJobs = ((int) Math.floor(userGivenWallTime / maxRunTime));
                                for (int i = 1; i <= numOfMaxWallTimeJobs; i++) {
                                    taskIdList.addAll(
                                            createAndSaveSubmissionTasks(registryClient, gatewayId, preferredJobSubmissionInterface, processModel, maxRunTime));
                                }
                                int leftWallTime = userGivenWallTime % maxRunTime;
                                if (leftWallTime != 0) {
                                    taskIdList.addAll(
                                            createAndSaveSubmissionTasks(registryClient, gatewayId, preferredJobSubmissionInterface, processModel, leftWallTime));
                                }
                            } else {
                                taskIdList.addAll(
                                        createAndSaveSubmissionTasks(registryClient, gatewayId, preferredJobSubmissionInterface, processModel, userGivenWallTime));
                            }
                        }
                    }
                } else {
                    taskIdList.addAll(createAndSaveSubmissionTasks(registryClient, gatewayId, preferredJobSubmissionInterface, processModel, userGivenWallTime));
                }
                taskIdList.addAll(createAndSaveOutputDataStagingTasks(processModel, gatewayId));
            }
            // update process scheduling
            registryClient.updateProcess(processModel, processModel.getProcessId());
            return getTaskDag(taskIdList);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public String createAndSaveIntermediateOutputFetchingTasks(String gatewayId, ProcessModel processModel,
                                                               ProcessModel parentProcess) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            List<String> taskIdList = new ArrayList<>();

            taskIdList.addAll(createAndSaveIntermediateOutputDataStagingTasks(processModel, gatewayId, parentProcess));
            // update process scheduling
            registryClient.updateProcess(processModel, processModel.getProcessId());
            return getTaskDag(taskIdList);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    private String getTaskDag(List<String> taskIdList) {
        if (taskIdList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : taskIdList) {
            sb.append(s).append(","); // comma separated values
        }
        String dag = sb.toString();
        return dag.substring(0, dag.length() - 1); // remove last comma
    }

    private List<String> createAndSaveEnvSetupTask(RegistryService.Client registryClient, String gatewayId,
                                                   ProcessModel processModel)
            throws TException, AiravataException, OrchestratorException {
        List<String> envTaskIds = new ArrayList<>();
        TaskModel envSetupTask = new TaskModel();
        envSetupTask.setTaskType(TaskTypes.ENV_SETUP);
        envSetupTask.setTaskStatuses(Arrays.asList(new TaskStatus(TaskState.CREATED)));
        envSetupTask.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        envSetupTask.setLastUpdateTime(AiravataUtils.getCurrentTimestamp().getTime());
        envSetupTask.setParentProcessId(processModel.getProcessId());
        EnvironmentSetupTaskModel envSetupSubModel = new EnvironmentSetupTaskModel();
        envSetupSubModel.setProtocol(OrchestratorUtils.getSecurityProtocol(processModel, gatewayId));
        String scratchLocation = OrchestratorUtils.getScratchLocation(processModel, gatewayId);
        String workingDir = scratchLocation + File.separator + processModel.getProcessId();
        envSetupSubModel.setLocation(workingDir);
        byte[] envSetupSub = ThriftUtils.serializeThriftObject(envSetupSubModel);
        envSetupTask.setSubTaskModel(envSetupSub);
        envSetupTask.setMaxRetry(3);
        envSetupTask.setCurrentRetry(0);
        String envSetupTaskId = (String) registryClient.addTask(envSetupTask, processModel.getProcessId());
        envSetupTask.setTaskId(envSetupTaskId);
        envTaskIds.add(envSetupTaskId);
        return envTaskIds;
    }

    public List<String> createAndSaveInputDataStagingTasks(ProcessModel processModel, String gatewayId)
            throws AiravataException, OrchestratorException {

        List<String> dataStagingTaskIds = new ArrayList<>();
        List<InputDataObjectType> processInputs = processModel.getProcessInputs();

        sortByInputOrder(processInputs);
        if (processInputs != null) {
            for (InputDataObjectType processInput : processInputs) {
                DataType type = processInput.getType();
                switch (type) {
                    case STDERR:
                        break;
                    case STDOUT:
                        break;
                    case URI:
                    case URI_COLLECTION:
                        if ((processInput.getValue() == null || processInput.getValue().equals("")) && !processInput.isIsRequired()) {
                            logger.debug("Skipping input data staging task for {} since value is empty and not required", processInput.getName());
                            break;
                        }
                        final RegistryService.Client registryClient = getRegistryServiceClient();
                        try {
                            TaskModel inputDataStagingTask = getInputDataStagingTask(registryClient, processModel, processInput, gatewayId);
                            String taskId = registryClient
                                    .addTask( inputDataStagingTask, processModel.getProcessId());
                            inputDataStagingTask.setTaskId(taskId);
                            dataStagingTaskIds.add(inputDataStagingTask.getTaskId());
                        } catch (Exception e) {
                            throw new AiravataException("Error while serializing data staging sub task model", e);
                        } finally {
                            if (registryClient != null) {
                                ThriftUtils.close(registryClient);
                            }
                        }
                        break;
                    default:
                        // nothing to do
                        break;
                }
            }
        }
        return dataStagingTaskIds;
    }

    public List<String> createAndSaveOutputDataStagingTasks(ProcessModel processModel, String gatewayId)
            throws AiravataException, TException, OrchestratorException {

        final RegistryService.Client registryClient = getRegistryServiceClient();
        List<String> dataStagingTaskIds = new ArrayList<>();
        try {
            List<OutputDataObjectType> processOutputs = processModel.getProcessOutputs();
            String appName = OrchestratorUtils.getApplicationInterfaceName(processModel);
            if (processOutputs != null) {
                for (OutputDataObjectType processOutput : processOutputs) {
                    DataType type = processOutput.getType();
                    switch (type) {
                        case STDOUT:
                            if (null == processOutput.getValue() || processOutput.getValue().trim().isEmpty()) {
                                processOutput.setValue(appName + ".stdout");
                            }
                            createOutputDataSatagingTasks(registryClient, processModel, gatewayId, dataStagingTaskIds, processOutput);
                            break;
                        case STDERR:
                            if (null == processOutput.getValue() || processOutput.getValue().trim().isEmpty()) {
                                processOutput.setValue(appName + ".stderr");
                            }
                            createOutputDataSatagingTasks(registryClient, processModel, gatewayId, dataStagingTaskIds, processOutput);
                            break;
                        case URI:
                        case URI_COLLECTION:
                            createOutputDataSatagingTasks(registryClient, processModel, gatewayId, dataStagingTaskIds, processOutput);
                            break;
                        default:
                            // nothing to do
                            break;
                    }
                }
            }

            try {
                if (isArchive(registryClient, processModel, orchestratorContext)) {
                    createArchiveDataStatgingTask(registryClient, processModel, gatewayId, dataStagingTaskIds);
                }
            } catch (Exception e) {
                throw new AiravataException("Error! Application interface retrieval failed", e);
            }
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
        return dataStagingTaskIds;
    }

    public List<String> createAndSaveIntermediateOutputDataStagingTasks(ProcessModel processModel, String gatewayId,
                                                                        ProcessModel parentProcess)
            throws AiravataException, TException, OrchestratorException {

        final RegistryService.Client registryClient = getRegistryServiceClient();
        List<String> dataStagingTaskIds = new ArrayList<>();
        try {
            List<OutputDataObjectType> processOutputs = processModel.getProcessOutputs();
            String appName = OrchestratorUtils.getApplicationInterfaceName(processModel);
            if (processOutputs != null) {
                for (OutputDataObjectType processOutput : processOutputs) {
                    DataType type = processOutput.getType();
                    switch (type) {
                        case STDOUT:
                            if (null == processOutput.getValue() || processOutput.getValue().trim().isEmpty()) {
                                processOutput.setValue(appName + ".stdout");
                            }
                            createIntermediateOutputDataStagingTasks(registryClient, processModel, gatewayId,
                                    parentProcess, dataStagingTaskIds, processOutput);
                            break;
                        case STDERR:
                            if (null == processOutput.getValue() || processOutput.getValue().trim().isEmpty()) {
                                processOutput.setValue(appName + ".stderr");
                            }
                            createIntermediateOutputDataStagingTasks(registryClient, processModel, gatewayId,
                                    parentProcess, dataStagingTaskIds, processOutput);
                            break;
                        case URI:
                        case URI_COLLECTION:
                            createIntermediateOutputDataStagingTasks(registryClient, processModel, gatewayId,
                                    parentProcess, dataStagingTaskIds, processOutput);
                            break;
                        default:
                            // nothing to do
                            break;
                    }
                }
            }

        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
        return dataStagingTaskIds;
    }
    private boolean isArchive(RegistryService.Client registryClient, ProcessModel processModel, OrchestratorContext orchestratorContext) throws TException {
        ApplicationInterfaceDescription appInterface = registryClient
                .getApplicationInterface(processModel.getApplicationInterfaceId());
        return appInterface.isArchiveWorkingDirectory();
    }

    private void createArchiveDataStatgingTask(RegistryService.Client registryClient, ProcessModel processModel,
                                               String gatewayId,
                                               List<String> dataStagingTaskIds) throws AiravataException, TException, OrchestratorException {
        TaskModel archiveTask = null;
        try {
            archiveTask = getOutputDataStagingTask(registryClient, processModel, null, gatewayId, null);
        } catch (TException e) {
            throw new AiravataException("Error! DataStaging sub task serialization failed", e);
        }
        String taskId = registryClient
                .addTask(archiveTask, processModel.getProcessId());
        archiveTask.setTaskId(taskId);
        dataStagingTaskIds.add(archiveTask.getTaskId());

    }

    private void createOutputDataSatagingTasks(RegistryService.Client registryClient, ProcessModel processModel,
                                               String gatewayId,
                                               List<String> dataStagingTaskIds,
                                               OutputDataObjectType processOutput) throws AiravataException, OrchestratorException {
        try {
            TaskModel outputDataStagingTask = getOutputDataStagingTask(registryClient, processModel, processOutput, gatewayId, null);
            String taskId = registryClient
                    .addTask(outputDataStagingTask, processModel.getProcessId());
            outputDataStagingTask.setTaskId(taskId);
            dataStagingTaskIds.add(outputDataStagingTask.getTaskId());
        } catch (TException e) {
            throw new AiravataException("Error while serializing data staging sub task model", e);
        }
    }

    private void createIntermediateOutputDataStagingTasks(RegistryService.Client registryClient,
            ProcessModel processModel,
            String gatewayId,
            ProcessModel parentProcess,
            List<String> dataStagingTaskIds,
            OutputDataObjectType processOutput) throws AiravataException, OrchestratorException {
        try {
            TaskModel outputDataStagingTask = getOutputDataStagingTask(registryClient, processModel, processOutput,
                    gatewayId, parentProcess);
            outputDataStagingTask.setTaskType(TaskTypes.OUTPUT_FETCHING);
            String taskId = registryClient
                    .addTask(outputDataStagingTask, processModel.getProcessId());
            outputDataStagingTask.setTaskId(taskId);
            dataStagingTaskIds.add(outputDataStagingTask.getTaskId());
        } catch (TException e) {
            throw new AiravataException("Error while serializing data staging sub task model", e);
        }
    }

    private List<String> createAndSaveSubmissionTasks(RegistryService.Client registryClient, String gatewayId,
                                                      JobSubmissionInterface jobSubmissionInterface,
                                                      ProcessModel processModel,
                                                      int wallTime)
            throws TException, OrchestratorException {

        JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();
        MonitorMode monitorMode = null;
        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH || jobSubmissionProtocol == JobSubmissionProtocol.SSH_FORK) {
            SSHJobSubmission sshJobSubmission = OrchestratorUtils.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
            monitorMode = sshJobSubmission.getMonitorMode();
        } else if (jobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
            monitorMode = MonitorMode.FORK;
        } else if(jobSubmissionProtocol == JobSubmissionProtocol.LOCAL){
            monitorMode = MonitorMode.LOCAL;
        } else if (jobSubmissionProtocol == JobSubmissionProtocol.CLOUD) {
            monitorMode = MonitorMode.CLOUD_JOB_MONITOR;
        }else {
            logger.error("expId : {}, processId : {} :- Unsupported Job submission protocol {}.",
                    processModel.getExperimentId(), processModel.getProcessId(), jobSubmissionProtocol.name());
            throw new OrchestratorException("Unsupported Job Submission Protocol " + jobSubmissionProtocol.name());
        }
        List<String> submissionTaskIds = new ArrayList<>();
        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processModel.getProcessId());
        taskModel.setCreationTime(System.currentTimeMillis());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatuses(Arrays.asList(taskStatus));
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        JobSubmissionTaskModel submissionSubTask = new JobSubmissionTaskModel();
        submissionSubTask.setMonitorMode(monitorMode);
        submissionSubTask.setJobSubmissionProtocol(jobSubmissionProtocol);
        submissionSubTask.setWallTime(wallTime);
        byte[] bytes = ThriftUtils.serializeThriftObject(submissionSubTask);
        taskModel.setSubTaskModel(bytes);
        taskModel.setMaxRetry(1);
        taskModel.setCurrentRetry(0);
        String taskId = registryClient.addTask(taskModel, processModel.getProcessId());
        taskModel.setTaskId(taskId);
        submissionTaskIds.add(taskModel.getTaskId());

        // create monitor task for this Email based monitor mode job
        if (monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR
                || monitorMode == MonitorMode.CLOUD_JOB_MONITOR) {
            TaskModel monitorTaskModel = new TaskModel();
            monitorTaskModel.setParentProcessId(processModel.getProcessId());
            monitorTaskModel.setCreationTime(System.currentTimeMillis());
            monitorTaskModel.setLastUpdateTime(monitorTaskModel.getCreationTime());
            TaskStatus monitorTaskStatus = new TaskStatus(TaskState.CREATED);
            monitorTaskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            monitorTaskModel.setTaskStatuses(Arrays.asList(monitorTaskStatus));
            monitorTaskModel.setTaskType(TaskTypes.MONITORING);
            MonitorTaskModel monitorSubTaskModel = new MonitorTaskModel();
            monitorSubTaskModel.setMonitorMode(monitorMode);
            monitorTaskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(monitorSubTaskModel));
            String mTaskId = (String) registryClient.addTask(monitorTaskModel, processModel.getProcessId());
            monitorTaskModel.setTaskId(mTaskId);
            submissionTaskIds.add(monitorTaskModel.getTaskId());
        }

        return submissionTaskIds;
    }

    private void sortByInputOrder(List<InputDataObjectType> processInputs) {
        Collections.sort(processInputs, new Comparator<InputDataObjectType>() {
            @Override
            public int compare(InputDataObjectType inputDT_1, InputDataObjectType inputDT_2) {
                return inputDT_1.getInputOrder() - inputDT_2.getInputOrder();
            }
        });
    }

    private TaskModel getInputDataStagingTask(RegistryService.Client registryClient, ProcessModel processModel, InputDataObjectType processInput, String gatewayId) throws TException, AiravataException, OrchestratorException {
        // create new task model for this task
        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processModel.getProcessId());
        taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatuses(Arrays.asList(taskStatus));
        taskModel.setTaskType(TaskTypes.DATA_STAGING);
        // create data staging sub task model
        DataStagingTaskModel submodel = new DataStagingTaskModel();
        ComputeResourceDescription computeResource = registryClient.
                getComputeResource(processModel.getComputeResourceId());
        String scratchLocation = OrchestratorUtils.getScratchLocation(processModel, gatewayId);
        String workingDir = (scratchLocation.endsWith(File.separator) ? scratchLocation : scratchLocation + File.separator) +
                processModel.getProcessId() + File.separator;
        URI destination = null;
        try {
            DataMovementProtocol dataMovementProtocol =
                    OrchestratorUtils.getPreferredDataMovementProtocol(processModel, gatewayId);
            String loginUserName = OrchestratorUtils.getLoginUserName(processModel, gatewayId);
            StringBuilder destinationPath = new StringBuilder(workingDir);
            Optional.ofNullable(processInput.getOverrideFilename()).ifPresent(destinationPath::append); //If an override filename is provided

            destination = new URI(dataMovementProtocol.name(),
                    loginUserName,
                    computeResource.getHostName(),
                    OrchestratorUtils.getDataMovementPort(processModel, gatewayId),
                    destinationPath.toString(), null, null);
        } catch (URISyntaxException e) {
            throw new OrchestratorException("Error while constructing destination file URI", e);
        }
        submodel.setType(DataStageType.INPUT);
        submodel.setSource(processInput.getValue());
        submodel.setProcessInput(processInput);
        submodel.setDestination(destination.toString());
        taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
        taskModel.setMaxRetry(3);
        taskModel.setCurrentRetry(0);
        return taskModel;
    }

    private TaskModel getOutputDataStagingTask(RegistryService.Client registryClient, ProcessModel processModel,
                                               OutputDataObjectType processOutput, String gatewayId,
                                               ProcessModel parentProcess) throws TException, AiravataException, OrchestratorException {
        try {

            // create new task model for this task
            TaskModel taskModel = new TaskModel();
            taskModel.setParentProcessId(processModel.getProcessId());
            taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            taskModel.setLastUpdateTime(taskModel.getCreationTime());
            TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskModel.setTaskStatuses(Arrays.asList(taskStatus));
            taskModel.setTaskType(TaskTypes.DATA_STAGING);
            ComputeResourceDescription computeResource = registryClient.
                    getComputeResource(processModel.getComputeResourceId());

            String workingDir = OrchestratorUtils.getScratchLocation(processModel, gatewayId)
                    + File.separator + (parentProcess == null ? processModel.getProcessId() : parentProcess.getProcessId()) + File.separator;
            DataStagingTaskModel submodel = new DataStagingTaskModel();
            DataMovementProtocol dataMovementProtocol = OrchestratorUtils.getPreferredDataMovementProtocol(processModel, gatewayId);
            URI source = null;
            try {
                String loginUserName = OrchestratorUtils.getLoginUserName(processModel, gatewayId);
                if (processOutput != null) {
                    submodel.setType(DataStageType.OUPUT);
                    submodel.setProcessOutput(processOutput);
                    source = new URI(dataMovementProtocol.name(),
                            loginUserName,
                            computeResource.getHostName(),
                            OrchestratorUtils.getDataMovementPort(processModel, gatewayId),
                            workingDir + processOutput.getValue(), null, null);
                } else {
                    // archive
                    submodel.setType(DataStageType.ARCHIVE_OUTPUT);
                    source = new URI(dataMovementProtocol.name(),
                            loginUserName,
                            computeResource.getHostName(),
                            OrchestratorUtils.getDataMovementPort(processModel, gatewayId),
                            workingDir, null, null);
                }
            } catch (URISyntaxException e) {
                throw new OrchestratorException("Error while constructing source file URI", e);
            }
            // We don't know destination location at this time, data staging task will set this.
            // because destination is required field we set dummy destination
            submodel.setSource(source.toString());
            // We don't know destination location at this time, data staging task will set this.
            // because destination is required field we set dummy destination
            submodel.setDestination("dummy://temp/file/location");
            taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
            taskModel.setMaxRetry(3);
            taskModel.setCurrentRetry(0);
            return taskModel;
        } catch (OrchestratorException e) {
           throw new OrchestratorException("Error occurred while retrieving data movement from app catalog", e);
        }
    }

    private RegistryService.Client getRegistryServiceClient() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException|ApplicationSettingsException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }

}
