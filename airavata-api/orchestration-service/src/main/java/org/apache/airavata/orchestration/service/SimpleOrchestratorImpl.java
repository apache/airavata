/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.service;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.proto.MonitorMode;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.data.movement.proto.DataMovementProtocol;
import org.apache.airavata.model.error.proto.LaunchValidationException;
import org.apache.airavata.model.error.proto.ValidationResults;
import org.apache.airavata.model.error.proto.ValidatorResult;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.TaskState;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.DataStageType;
import org.apache.airavata.model.task.proto.DataStagingTaskModel;
import org.apache.airavata.model.task.proto.EnvironmentSetupTaskModel;
import org.apache.airavata.model.task.proto.JobSubmissionTaskModel;
import org.apache.airavata.model.task.proto.MonitorTaskModel;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.orchestration.infrastructure.GFACPassiveJobSubmitter;
import org.apache.airavata.orchestration.infrastructure.JobSubmitter;
import org.apache.airavata.orchestration.util.ExperimentModelUtil;
import org.apache.airavata.orchestration.util.OrchestratorConstants;
import org.apache.airavata.orchestration.util.OrchestratorUtils;
import org.apache.airavata.orchestration.validation.JobMetadataValidator;
import org.apache.airavata.task.SchedulerUtils;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleOrchestratorImpl extends AbstractOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;

    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;

    public SimpleOrchestratorImpl() throws OrchestratorException, Exception {
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

    public ValidationResults validateExperiment(ExperimentModel experiment) throws OrchestratorException {
        ValidationResults.Builder validationResultsBuilder = ValidationResults.newBuilder()
                .setValidationState(
                        true); // initially making it to success, if atleast one failed them simply mark it failed.
        String errorMsg = "Validation Errors : ";
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClasses =
                    this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClasses) {
                try {
                    Class<? extends JobMetadataValidator> vClass =
                            Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                    JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                    ValidationResults validationResults = jobMetadataValidator.validate(experiment, null);
                    if (validationResults.getValidationState()) {
                        logger.info("Validation of " + validator + " is SUCCESSFUL");
                    } else {
                        List<ValidatorResult> validationResultList = validationResults.getValidationResultListList();
                        for (ValidatorResult result : validationResultList) {
                            if (!result.getResult()) {
                                String validationError = result.getErrorDetails();
                                if (!validationError.isEmpty()) {
                                    errorMsg += validationError + " ";
                                }
                            }
                        }
                        logger.error("Validation of " + validator + " for experiment Id " + experiment.getExperimentId()
                                + " is FAILED:[error]. " + errorMsg);
                        validationResultsBuilder
                                .setValidationState(false)
                                .addAllValidationResultList(validationResults.getValidationResultListList());
                        try {
                            ErrorModel details = ErrorModel.newBuilder()
                                    .setActualErrorMessage(errorMsg)
                                    .setCreationTime(Calendar.getInstance().getTimeInMillis())
                                    .build();
                            final RegistryHandler registryClient = getRegistryHandler();
                            registryClient.addErrors(
                                    OrchestratorConstants.EXPERIMENT_ERROR, details, experiment.getExperimentId());
                        } catch (Exception e) {
                            throw new RuntimeException("Error while saving error details to registry", e);
                        }
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResultsBuilder.setValidationState(false);
                } catch (InstantiationException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResultsBuilder.setValidationState(false);
                } catch (IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResultsBuilder.setValidationState(false);
                }
            }
        }
        ValidationResults validationResults = validationResultsBuilder.build();
        if (validationResults.getValidationState()) {
            return validationResults;
        } else {
            // atleast one validation has failed, so we throw an exception
            LaunchValidationException launchValidationException = LaunchValidationException.newBuilder()
                    .setValidationResult(validationResults)
                    .setErrorMessage("Validation failed refer the validationResults list for "
                            + "detail error. Validation errors : " + errorMsg)
                    .build();
            throw new OrchestratorException(launchValidationException.getErrorMessage());
        }
    }

    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel)
            throws OrchestratorException {

        ValidationResults.Builder validationResultsBuilder = ValidationResults.newBuilder()
                .setValidationState(
                        true); // initially making it to success, if atleast one failed them simply mark it failed.
        String errorMsg = "Validation Errors : ";
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClzzez =
                    this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClzzez) {
                try {
                    Class<? extends JobMetadataValidator> vClass =
                            Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                    JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                    ValidationResults validationResults = jobMetadataValidator.validate(experiment, processModel);
                    if (validationResults.getValidationState()) {
                        logger.info("Validation of " + validator + " is SUCCESSFUL");
                    } else {
                        List<ValidatorResult> validationResultList = validationResults.getValidationResultListList();
                        for (ValidatorResult result : validationResultList) {
                            if (!result.getResult()) {
                                String validationError = result.getErrorDetails();
                                if (!validationError.isEmpty()) {
                                    errorMsg += validationError + " ";
                                }
                            }
                        }
                        logger.error("Validation of " + validator + " for experiment Id " + experiment.getExperimentId()
                                + " is FAILED:[error]. " + errorMsg);
                        validationResultsBuilder
                                .setValidationState(false)
                                .addAllValidationResultList(validationResults.getValidationResultListList());
                        try {
                            ErrorModel details = ErrorModel.newBuilder()
                                    .setActualErrorMessage(errorMsg)
                                    .setCreationTime(Calendar.getInstance().getTimeInMillis())
                                    .build();
                            final RegistryHandler registryClient = getRegistryHandler();
                            registryClient.addErrors(
                                    OrchestratorConstants.PROCESS_ERROR, details, processModel.getProcessId());
                        } catch (Exception e) {
                            throw new RuntimeException("Error while saving error details to registry", e);
                        }
                        break;
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResultsBuilder.setValidationState(false);
                }
            }
        }
        ValidationResults validationResults = validationResultsBuilder.build();
        if (validationResults.getValidationState()) {
            return validationResults;
        } else {
            // atleast one validation has failed, so we throw an exception
            LaunchValidationException launchValidationException = LaunchValidationException.newBuilder()
                    .setValidationResult(validationResults)
                    .setErrorMessage("Validation failed refer the validationResults "
                            + "list for detail error. Validation errors : " + errorMsg)
                    .build();
            throw new OrchestratorException(launchValidationException.getErrorMessage());
        }
    }

    public void cancelExperiment(ExperimentModel experiment, String tokenId) throws OrchestratorException {
        logger.info("Terminating experiment " + experiment.getExperimentId());
        RegistryHandler registryServiceClient = getRegistryHandler();

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
        } catch (Exception e) {
            logger.error("Failed to fetch process ids for experiment " + experiment.getExperimentId(), e);
            throw new OrchestratorException(
                    "Failed to fetch process ids for experiment " + experiment.getExperimentId(), e);
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

    public void initialize() throws OrchestratorException {}

    public List<ProcessModel> createProcesses(String experimentId, String gatewayId) throws OrchestratorException {
        final RegistryHandler registryClient = getRegistryHandler();
        try {
            ExperimentModel experimentModel = registryClient.getExperiment(experimentId);
            List<ProcessModel> processModels = registryClient.getProcessList(experimentId);
            if (processModels == null || processModels.isEmpty()) {
                ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
                String processId = registryClient.addProcess(processModel, experimentId);
                processModel = processModel.toBuilder().setProcessId(processId).build();
                processModels = new ArrayList<>();
                processModels.add(processModel);
            }
            return processModels;
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
        }
    }

    public String createAndSaveTasks(String gatewayId, ProcessModel processModel) throws OrchestratorException {
        final RegistryHandler registryClient = getRegistryHandler();
        try {
            GroupComputeResourcePreference preference =
                    OrchestratorUtils.getGroupComputeResourcePreference(processModel);
            ResourceType resourceType = preference.getResourceType();
            logger.info("Determined resource type as {} for process {}", resourceType, processModel.getProcessId());

            ComputationalResourceSchedulingModel resourceSchedule = processModel.getProcessResourceSchedule();
            int userGivenWallTime = resourceSchedule.getWallTimeLimit();
            String resourceHostId = resourceSchedule.getResourceHostId();
            if (resourceHostId == null) {
                throw new OrchestratorException("Compute Resource Id cannot be null at this point");
            }

            // TODO - handle for different resource types
            JobSubmissionInterface preferredJobSubmissionInterface =
                    OrchestratorUtils.getPreferredJobSubmissionInterface(processModel, gatewayId);
            JobSubmissionProtocol preferredJobSubmissionProtocol =
                    OrchestratorUtils.getPreferredJobSubmissionProtocol(processModel, gatewayId);
            List<String> taskIdList = new ArrayList<>();

            if (preferredJobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
                // TODO - breakdown unicore all in one task to multiple tasks, then we don't need to handle UNICORE
                // here.
                taskIdList.addAll(createAndSaveSubmissionTasks(
                        registryClient, preferredJobSubmissionInterface, processModel, userGivenWallTime));
            } else {
                taskIdList.addAll(createAndSaveEnvSetupTask(registryClient, gatewayId, processModel, resourceType));
                taskIdList.addAll(createAndSaveInputDataStagingTasks(processModel, gatewayId, resourceType));
                taskIdList.addAll(createAndSaveSubmissionTasks(
                        registryClient, preferredJobSubmissionInterface, processModel, userGivenWallTime));
                taskIdList.addAll(createAndSaveOutputDataStagingTasks(processModel, gatewayId, resourceType));
            }
            // update process scheduling
            registryClient.updateProcess(processModel, processModel.getProcessId());
            return getTaskDag(taskIdList);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
        }
    }

    public String createAndSaveIntermediateOutputFetchingTasks(
            String gatewayId, ProcessModel processModel, ProcessModel parentProcess) throws OrchestratorException {
        final RegistryHandler registryClient = getRegistryHandler();
        try {
            GroupComputeResourcePreference preference =
                    OrchestratorUtils.getGroupComputeResourcePreference(processModel);
            ResourceType resourceType = preference.getResourceType();
            List<String> taskIdList = new ArrayList<>(createAndSaveIntermediateOutputDataStagingTasks(
                    processModel, gatewayId, parentProcess, resourceType));
            // update process scheduling
            registryClient.updateProcess(processModel, processModel.getProcessId());
            return getTaskDag(taskIdList);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process", e);
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

    private List<String> createAndSaveEnvSetupTask(
            RegistryHandler registryClient, String gatewayId, ProcessModel processModel, ResourceType resourceType)
            throws Exception, AiravataException, OrchestratorException {
        List<String> envTaskIds = new ArrayList<>();

        String scratchLocation = OrchestratorUtils.getScratchLocation(processModel, gatewayId);
        String workingDir = scratchLocation + File.separator + processModel.getProcessId();

        EnvironmentSetupTaskModel envSetupSubModel = EnvironmentSetupTaskModel.newBuilder()
                .setProtocol(
                        OrchestratorUtils.getSecurityProtocol(processModel, gatewayId)) // TODO support for CLOUD (AWS)
                .setLocation(workingDir)
                .build();

        TaskModel envSetupTask = TaskModel.newBuilder()
                .setTaskType(TaskTypes.ENV_SETUP)
                .addTaskStatuses(TaskStatus.newBuilder()
                        .setState(TaskState.TASK_STATE_CREATED)
                        .build())
                .setCreationTime(AiravataUtils.getCurrentTimestamp().getTime())
                .setLastUpdateTime(AiravataUtils.getCurrentTimestamp().getTime())
                .setParentProcessId(processModel.getProcessId())
                .setSubTaskModel(com.google.protobuf.ByteString.copyFrom(envSetupSubModel.toByteArray()))
                .setMaxRetry(3)
                .setCurrentRetry(0)
                .build();
        String envSetupTaskId = registryClient.addTask(envSetupTask, processModel.getProcessId());
        envSetupTask = envSetupTask.toBuilder().setTaskId(envSetupTaskId).build();
        envTaskIds.add(envSetupTaskId);

        return envTaskIds;
    }

    public List<String> createAndSaveInputDataStagingTasks(
            ProcessModel processModel, String gatewayId, ResourceType resourceType) throws AiravataException {

        List<String> dataStagingTaskIds = new ArrayList<>();
        List<InputDataObjectType> processInputs = processModel.getProcessInputsList();

        sortByInputOrder(processInputs);
        if (!processInputs.isEmpty()) {
            for (InputDataObjectType processInput : processInputs) {
                DataType type = processInput.getType();
                switch (type) {
                    case STDERR:
                        break;
                    case STDOUT:
                        break;
                    case URI:
                    case URI_COLLECTION:
                        if (processInput.getValue().isEmpty() && !processInput.getIsRequired()) {
                            logger.debug(
                                    "Skipping input data staging task for {} since value is empty and not required",
                                    processInput.getName());
                            break;
                        }
                        final RegistryHandler registryClient = getRegistryHandler();
                        try {
                            TaskModel inputDataStagingTask = getInputDataStagingTask(
                                    registryClient, processModel, processInput, gatewayId, resourceType);
                            String taskId = registryClient.addTask(inputDataStagingTask, processModel.getProcessId());
                            inputDataStagingTask = inputDataStagingTask.toBuilder()
                                    .setTaskId(taskId)
                                    .build();
                            dataStagingTaskIds.add(inputDataStagingTask.getTaskId());
                        } catch (Exception e) {
                            throw new AiravataException("Error while serializing data staging sub task model", e);
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

    public List<String> createAndSaveOutputDataStagingTasks(
            ProcessModel processModel, String gatewayId, ResourceType resourceType)
            throws AiravataException, Exception, OrchestratorException {

        final RegistryHandler registryClient = getRegistryHandler();
        List<String> dataStagingTaskIds = new ArrayList<>();
        List<OutputDataObjectType> processOutputs = processModel.getProcessOutputsList();
        String appName = OrchestratorUtils.getApplicationInterfaceName(processModel);
        if (!processOutputs.isEmpty()) {
            for (OutputDataObjectType processOutput : processOutputs) {
                DataType type = processOutput.getType();
                switch (type) {
                    case STDOUT:
                        if (processOutput.getValue().trim().isEmpty()) {
                            processOutput = processOutput.toBuilder()
                                    .setValue(appName + ".stdout")
                                    .build();
                        }
                        createOutputDataSatagingTasks(
                                registryClient,
                                processModel,
                                gatewayId,
                                dataStagingTaskIds,
                                processOutput,
                                resourceType);
                        break;
                    case STDERR:
                        if (processOutput.getValue().trim().isEmpty()) {
                            processOutput = processOutput.toBuilder()
                                    .setValue(appName + ".stderr")
                                    .build();
                        }
                        createOutputDataSatagingTasks(
                                registryClient,
                                processModel,
                                gatewayId,
                                dataStagingTaskIds,
                                processOutput,
                                resourceType);
                        break;
                    case URI:
                    case URI_COLLECTION:
                        createOutputDataSatagingTasks(
                                registryClient,
                                processModel,
                                gatewayId,
                                dataStagingTaskIds,
                                processOutput,
                                resourceType);
                        break;
                    default:
                        // nothing to do
                        break;
                }
            }
        }

        try {
            if (isArchive(registryClient, processModel)) {
                createArchiveDataStatgingTask(
                        registryClient, processModel, gatewayId, dataStagingTaskIds, resourceType);
            }
        } catch (Exception e) {
            throw new AiravataException("Error! Application interface retrieval failed", e);
        }
        return dataStagingTaskIds;
    }

    public List<String> createAndSaveIntermediateOutputDataStagingTasks(
            ProcessModel processModel, String gatewayId, ProcessModel parentProcess, ResourceType resourceType)
            throws AiravataException, Exception, OrchestratorException {

        final RegistryHandler registryClient = getRegistryHandler();
        List<String> dataStagingTaskIds = new ArrayList<>();
        List<OutputDataObjectType> processOutputs = processModel.getProcessOutputsList();
        String appName = OrchestratorUtils.getApplicationInterfaceName(processModel);
        if (!processOutputs.isEmpty()) {
            for (OutputDataObjectType processOutput : processOutputs) {
                DataType type = processOutput.getType();
                switch (type) {
                    case STDOUT:
                        if (processOutput.getValue().trim().isEmpty()) {
                            processOutput = processOutput.toBuilder()
                                    .setValue(appName + ".stdout")
                                    .build();
                        }
                        createIntermediateOutputDataStagingTasks(
                                registryClient,
                                processModel,
                                gatewayId,
                                parentProcess,
                                dataStagingTaskIds,
                                processOutput,
                                resourceType);
                        break;
                    case STDERR:
                        if (processOutput.getValue().trim().isEmpty()) {
                            processOutput = processOutput.toBuilder()
                                    .setValue(appName + ".stderr")
                                    .build();
                        }
                        createIntermediateOutputDataStagingTasks(
                                registryClient,
                                processModel,
                                gatewayId,
                                parentProcess,
                                dataStagingTaskIds,
                                processOutput,
                                resourceType);
                        break;
                    case URI:
                    case URI_COLLECTION:
                        createIntermediateOutputDataStagingTasks(
                                registryClient,
                                processModel,
                                gatewayId,
                                parentProcess,
                                dataStagingTaskIds,
                                processOutput,
                                resourceType);
                        break;
                    default:
                        // nothing to do
                        break;
                }
            }
        }
        return dataStagingTaskIds;
    }

    private boolean isArchive(RegistryHandler registryClient, ProcessModel processModel) throws Exception {
        ApplicationInterfaceDescription appInterface =
                registryClient.getApplicationInterface(processModel.getApplicationInterfaceId());
        return appInterface.getArchiveWorkingDirectory();
    }

    private void createArchiveDataStatgingTask(
            RegistryHandler registryClient,
            ProcessModel processModel,
            String gatewayId,
            List<String> dataStagingTaskIds,
            ResourceType resourceType)
            throws AiravataException, Exception, OrchestratorException {
        TaskModel archiveTask;
        try {
            archiveTask = getOutputDataStagingTask(registryClient, processModel, null, gatewayId, null, resourceType);
        } catch (Exception e) {
            throw new AiravataException("Error! DataStaging sub task serialization failed", e);
        }
        String taskId = registryClient.addTask(archiveTask, processModel.getProcessId());
        archiveTask = archiveTask.toBuilder().setTaskId(taskId).build();
        dataStagingTaskIds.add(archiveTask.getTaskId());
    }

    private void createOutputDataSatagingTasks(
            RegistryHandler registryClient,
            ProcessModel processModel,
            String gatewayId,
            List<String> dataStagingTaskIds,
            OutputDataObjectType processOutput,
            ResourceType resourceType)
            throws AiravataException, OrchestratorException {
        try {
            TaskModel outputDataStagingTask = getOutputDataStagingTask(
                    registryClient, processModel, processOutput, gatewayId, null, resourceType);
            String taskId = registryClient.addTask(outputDataStagingTask, processModel.getProcessId());
            outputDataStagingTask =
                    outputDataStagingTask.toBuilder().setTaskId(taskId).build();
            dataStagingTaskIds.add(outputDataStagingTask.getTaskId());
        } catch (Exception e) {
            throw new AiravataException("Error while serializing data staging sub task model", e);
        }
    }

    private void createIntermediateOutputDataStagingTasks(
            RegistryHandler registryClient,
            ProcessModel processModel,
            String gatewayId,
            ProcessModel parentProcess,
            List<String> dataStagingTaskIds,
            OutputDataObjectType processOutput,
            ResourceType resourceType)
            throws AiravataException, OrchestratorException {
        try {
            TaskModel outputDataStagingTask = getOutputDataStagingTask(
                    registryClient, processModel, processOutput, gatewayId, parentProcess, resourceType);
            outputDataStagingTask = outputDataStagingTask.toBuilder()
                    .setTaskType(TaskTypes.OUTPUT_FETCHING)
                    .build();
            String taskId = registryClient.addTask(outputDataStagingTask, processModel.getProcessId());
            outputDataStagingTask =
                    outputDataStagingTask.toBuilder().setTaskId(taskId).build();
            dataStagingTaskIds.add(outputDataStagingTask.getTaskId());
        } catch (Exception e) {
            throw new AiravataException("Error while serializing data staging sub task model", e);
        }
    }

    private List<String> createAndSaveSubmissionTasks(
            RegistryHandler registryClient,
            JobSubmissionInterface jobSubmissionInterface,
            ProcessModel processModel,
            int wallTime)
            throws Exception, OrchestratorException {

        JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();
        MonitorMode monitorMode;

        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH
                || jobSubmissionProtocol == JobSubmissionProtocol.SSH_FORK) {
            SSHJobSubmission sshJobSubmission =
                    OrchestratorUtils.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
            monitorMode = sshJobSubmission.getMonitorMode();
        } else if (jobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
            monitorMode = MonitorMode.MONITOR_FORK;
        } else if (jobSubmissionProtocol == JobSubmissionProtocol.LOCAL) {
            monitorMode = MonitorMode.MONITOR_LOCAL;
        } else if (jobSubmissionProtocol == JobSubmissionProtocol.JSP_CLOUD) {
            monitorMode = MonitorMode.CLOUD_JOB_MONITOR;
        } else {
            logger.error(
                    "expId : {}, processId : {} :- Unsupported Job submission protocol {}.",
                    processModel.getExperimentId(),
                    processModel.getProcessId(),
                    jobSubmissionProtocol.name());
            throw new OrchestratorException("Unsupported Job Submission Protocol " + jobSubmissionProtocol.name());
        }

        List<String> submissionTaskIds = new ArrayList<>();
        TaskStatus taskStatus = TaskStatus.newBuilder()
                .setState(TaskState.TASK_STATE_CREATED)
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();

        JobSubmissionTaskModel submissionSubTask = JobSubmissionTaskModel.newBuilder()
                .setMonitorMode(monitorMode)
                .setJobSubmissionProtocol(jobSubmissionProtocol)
                .setWallTime(wallTime)
                .build();

        long creationTime = System.currentTimeMillis();
        TaskModel taskModel = TaskModel.newBuilder()
                .setParentProcessId(processModel.getProcessId())
                .setCreationTime(creationTime)
                .setLastUpdateTime(creationTime)
                .addTaskStatuses(taskStatus)
                .setTaskType(TaskTypes.JOB_SUBMISSION)
                .setSubTaskModel(com.google.protobuf.ByteString.copyFrom(submissionSubTask.toByteArray()))
                .setMaxRetry(1)
                .setCurrentRetry(0)
                .build();

        String taskId = registryClient.addTask(taskModel, processModel.getProcessId());
        taskModel = taskModel.toBuilder().setTaskId(taskId).build();
        submissionTaskIds.add(taskModel.getTaskId());

        // create monitor task for this Email based monitor mode job
        if (monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR || monitorMode == MonitorMode.CLOUD_JOB_MONITOR) {

            TaskStatus monitorTaskStatus = TaskStatus.newBuilder()
                    .setState(TaskState.TASK_STATE_CREATED)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();

            MonitorTaskModel monitorSubTaskModel =
                    MonitorTaskModel.newBuilder().setMonitorMode(monitorMode).build();

            long monitorCreationTime = System.currentTimeMillis();
            TaskModel monitorTaskModel = TaskModel.newBuilder()
                    .setParentProcessId(processModel.getProcessId())
                    .setCreationTime(monitorCreationTime)
                    .setLastUpdateTime(monitorCreationTime)
                    .addTaskStatuses(monitorTaskStatus)
                    .setTaskType(TaskTypes.MONITORING)
                    .setSubTaskModel(com.google.protobuf.ByteString.copyFrom(monitorSubTaskModel.toByteArray()))
                    .build();

            String mTaskId = registryClient.addTask(monitorTaskModel, processModel.getProcessId());
            monitorTaskModel = monitorTaskModel.toBuilder().setTaskId(mTaskId).build();
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

    private TaskModel getInputDataStagingTask(
            RegistryHandler registryClient,
            ProcessModel processModel,
            InputDataObjectType processInput,
            String gatewayId,
            ResourceType resourceType)
            throws Exception, AiravataException, OrchestratorException {
        ComputeResourceDescription computeResource =
                registryClient.getComputeResource(processModel.getComputeResourceId());

        String scratchLocation = OrchestratorUtils.getScratchLocation(processModel, gatewayId);
        String workingDir =
                (scratchLocation.endsWith(File.separator) ? scratchLocation : scratchLocation + File.separator)
                        + processModel.getProcessId()
                        + File.separator;

        URI destination;
        try {
            DataMovementProtocol dataMovementProtocol =
                    OrchestratorUtils.getPreferredDataMovementProtocol(processModel, gatewayId);
            String loginUserName = OrchestratorUtils.getLoginUserName(processModel, gatewayId);
            StringBuilder destinationPath = new StringBuilder(workingDir);
            String overrideFilename = processInput.getOverrideFilename();
            if (!overrideFilename.isEmpty()) {
                destinationPath.append(overrideFilename);
            }

            destination = new URI(
                    dataMovementProtocol.name(),
                    loginUserName,
                    computeResource.getHostName(),
                    OrchestratorUtils.getDataMovementPort(processModel, gatewayId),
                    destinationPath.toString(),
                    null,
                    null);

        } catch (URISyntaxException e) {
            logger.error("Error while constructing destination file URI", e);
            throw new OrchestratorException("Error while constructing destination file URI", e);
        }

        // create data staging sub task model
        DataStagingTaskModel submodel = DataStagingTaskModel.newBuilder()
                .setDestination(destination.toString())
                .setType(DataStageType.INPUT)
                .setSource(processInput.getValue())
                .setProcessInput(processInput)
                .build();

        long now = AiravataUtils.getCurrentTimestamp().getTime();
        TaskStatus taskStatus = TaskStatus.newBuilder()
                .setState(TaskState.TASK_STATE_CREATED)
                .setTimeOfStateChange(now)
                .build();

        // create new task model for this task
        return TaskModel.newBuilder()
                .setParentProcessId(processModel.getProcessId())
                .setCreationTime(now)
                .setLastUpdateTime(now)
                .addTaskStatuses(taskStatus)
                .setTaskType(TaskTypes.DATA_STAGING)
                .setSubTaskModel(com.google.protobuf.ByteString.copyFrom(submodel.toByteArray()))
                .setMaxRetry(3)
                .setCurrentRetry(0)
                .build();
    }

    private TaskModel getOutputDataStagingTask(
            RegistryHandler registryClient,
            ProcessModel processModel,
            OutputDataObjectType processOutput,
            String gatewayId,
            ProcessModel parentProcess,
            ResourceType resourceType)
            throws Exception, AiravataException, OrchestratorException {
        try {
            ComputeResourceDescription computeResource =
                    registryClient.getComputeResource(processModel.getComputeResourceId());

            String workingDir = OrchestratorUtils.getScratchLocation(processModel, gatewayId)
                    + File.separator
                    + (parentProcess == null ? processModel.getProcessId() : parentProcess.getProcessId())
                    + File.separator;
            DataMovementProtocol dataMovementProtocol =
                    OrchestratorUtils.getPreferredDataMovementProtocol(processModel, gatewayId);

            DataStagingTaskModel.Builder submodelBuilder = DataStagingTaskModel.newBuilder();
            URI source;
            try {
                String loginUserName = OrchestratorUtils.getLoginUserName(processModel, gatewayId);
                if (processOutput != null) {
                    submodelBuilder.setType(DataStageType.OUPUT).setProcessOutput(processOutput);
                    source = new URI(
                            dataMovementProtocol.name(),
                            loginUserName,
                            computeResource.getHostName(),
                            OrchestratorUtils.getDataMovementPort(processModel, gatewayId),
                            workingDir + processOutput.getValue(),
                            null,
                            null);
                } else {
                    // archive
                    submodelBuilder.setType(DataStageType.ARCHIVE_OUTPUT);
                    source = new URI(
                            dataMovementProtocol.name(),
                            loginUserName,
                            computeResource.getHostName(),
                            OrchestratorUtils.getDataMovementPort(processModel, gatewayId),
                            workingDir,
                            null,
                            null);
                }

            } catch (URISyntaxException e) {
                throw new OrchestratorException("Error while constructing source file URI", e);
            }

            DataStagingTaskModel submodel = submodelBuilder
                    .setSource(source.toString())
                    // We don't know destination location at this time, data staging task will set this.
                    // because destination is required field we set dummy destination
                    .setDestination("dummy://temp/file/location")
                    .build();

            long now = AiravataUtils.getCurrentTimestamp().getTime();
            TaskStatus taskStatus = TaskStatus.newBuilder()
                    .setState(TaskState.TASK_STATE_CREATED)
                    .setTimeOfStateChange(now)
                    .build();

            // create new task model for this task
            return TaskModel.newBuilder()
                    .setParentProcessId(processModel.getProcessId())
                    .setCreationTime(now)
                    .setLastUpdateTime(now)
                    .addTaskStatuses(taskStatus)
                    .setTaskType(TaskTypes.DATA_STAGING)
                    .setSubTaskModel(com.google.protobuf.ByteString.copyFrom(submodel.toByteArray()))
                    .setMaxRetry(3)
                    .setCurrentRetry(0)
                    .build();

        } catch (OrchestratorException e) {
            throw new OrchestratorException("Error occurred while retrieving data movement from app catalog", e);
        }
    }

    private RegistryHandler getRegistryHandler() {
        return SchedulerUtils.getRegistryHandler();
    }
}
