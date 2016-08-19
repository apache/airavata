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
package org.apache.airavata.orchestrator.cpi.impl;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.*;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.impl.GFACPassiveJobSubmitter;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
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


    public SimpleOrchestratorImpl() throws OrchestratorException {
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

    public ValidationResults validateExperiment(ExperimentModel experiment) throws OrchestratorException,LaunchValidationException {
        org.apache.airavata.model.error.ValidationResults validationResults = new org.apache.airavata.model.error.ValidationResults();
        validationResults.setValidationState(true); // initially making it to success, if atleast one failed them simply mark it failed.
        String errorMsg = "Validation Errors : ";
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClasses = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClasses) {
                try {
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
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
                        logger.error("Validation of " + validator + " for experiment Id " + experiment.getExperimentId() + " is FAILED:[error]. " + errorMsg);
                        validationResults.setValidationState(false);
                        try {
                            ErrorModel details = new ErrorModel();
                            details.setActualErrorMessage(errorMsg);
                            details.setCreationTime(Calendar.getInstance().getTimeInMillis());
                            orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.EXPERIMENT_ERROR, details,
                                    experiment.getExperimentId());
                        } catch (RegistryException e) {
                            logger.error("Error while saving error details to registry", e);
                        }
                        break;
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                } /*catch (InstantiationException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                } catch (IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                }*/
            }
        }
        if(validationResults.isValidationState()){
            return validationResults;
        }else {
            //atleast one validation has failed, so we throw an exception
            LaunchValidationException launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults list for detail error. Validation errors : " + errorMsg);
            throw launchValidationException;
        }
    }

    public ValidationResults validateProcess(ExperimentModel experiment, ProcessModel processModel) throws OrchestratorException,LaunchValidationException {
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
                        for (ValidatorResult result : validationResultList){
                            if (!result.isResult()){
                                String validationError = result.getErrorDetails();
                                if (validationError != null){
                                    errorMsg += validationError + " ";
                                }
                            }
                        }
                        logger.error("Validation of " + validator + " for experiment Id " + experiment.getExperimentId() + " is FAILED:[error]. " + errorMsg);
                        validationResults.setValidationState(false);
                        try {
                            ErrorModel details = new ErrorModel();
                            details.setActualErrorMessage(errorMsg);
                            details.setCreationTime(Calendar.getInstance().getTimeInMillis());
                            orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.PROCESS_ERROR, details,
                                    processModel.getProcessId());
                        } catch (RegistryException e) {
                            logger.error("Error while saving error details to registry", e);
                        }
                        break;
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                } /*catch (InstantiationException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                } catch (IllegalAccessException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    validationResults.setValidationState(false);
                }*/
            }
        }
        if(validationResults.isValidationState()){
            return validationResults;
        }else {
            //atleast one validation has failed, so we throw an exception
            LaunchValidationException launchValidationException = new LaunchValidationException();
            launchValidationException.setValidationResult(validationResults);
            launchValidationException.setErrorMessage("Validation failed refer the validationResults list for detail error. Validation errors : " + errorMsg);
            throw launchValidationException;
        }
    }


    public void cancelExperiment(ExperimentModel experiment, ProcessModel processModel, String tokenId)
            throws OrchestratorException {
        // FIXME
//        List<JobDetails> jobDetailsList = task.getJobDetailsList();
//        for(JobDetails jobDetails:jobDetailsList) {
//            JobState jobState = jobDetails.getJobStatus().getJobState();
//            if (jobState.getValue() > 4){
//                logger.error("Cannot cancel the job, because current job state is : " + jobState.toString() +
//                "jobId: " + jobDetails.getJobID() + " Job Name: " + jobDetails.getJobName());
//                return;
//            }
//        }
//        jobSubmitter.terminate(experiment.getExperimentID(),task.getTaskID(),tokenId);
    }


    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executorIn) {
        this.executor = executorIn;
    }

    public JobSubmitter getJobSubmitter() {
        return jobSubmitter;
    }

    public void setJobSubmitter(JobSubmitter jobSubmitterIn) {
        this.jobSubmitter = jobSubmitterIn;
    }

    public void initialize() throws OrchestratorException {

    }

    public List<ProcessModel> createProcesses (String experimentId, String gatewayId) throws OrchestratorException {
        List<ProcessModel> processModels = new ArrayList<ProcessModel>();
        try {
            Registry registry = orchestratorContext.getRegistry();
            ExperimentModel experimentModel = (ExperimentModel)registry.getExperimentCatalog().get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            List<Object> processList = registry.getExperimentCatalog().get(ExperimentCatalogModelType.PROCESS, Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentId);
            if (processList != null && !processList.isEmpty()) {
                for (Object processObject : processList) {
                    ProcessModel processModel = (ProcessModel)processObject;
                    processModels.add(processModel);
                }
            }else {
                ProcessModel processModel = ExperimentModelUtil.cloneProcessFromExperiment(experimentModel);
                String processId = (String)registry.getExperimentCatalog().add(ExpCatChildDataType.PROCESS, processModel, experimentId);
                processModel.setProcessId(processId);
                processModels.add(processModel);
            }
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process");
        }
        return processModels;
    }

    public String createAndSaveTasks(String gatewayId, ProcessModel processModel, boolean autoSchedule) throws OrchestratorException {
        try {
            ExperimentCatalog experimentCatalog = orchestratorContext.getRegistry().getExperimentCatalog();
            AppCatalog appCatalog = orchestratorContext.getRegistry().getAppCatalog();
            ComputationalResourceSchedulingModel resourceSchedule = processModel.getResourceSchedule();
            String userGivenQueueName = resourceSchedule.getQueueName();
            int userGivenWallTime = resourceSchedule.getWallTimeLimit();
            String resourceHostId = resourceSchedule.getResourceHostId();
            if (resourceHostId == null){
                throw new OrchestratorException("Compute Resource Id cannot be null at this point");
            }
            ComputeResourceDescription computeResource = appCatalog.getComputeResource().getComputeResource(resourceHostId);
            JobSubmissionInterface preferredJobSubmissionInterface = OrchestratorUtils.getPreferredJobSubmissionInterface(orchestratorContext, processModel, gatewayId);
            ComputeResourcePreference resourcePreference = OrchestratorUtils.getComputeResourcePreference(orchestratorContext, processModel, gatewayId);
            List<String> taskIdList = new ArrayList<>();

            if (resourcePreference.getPreferredJobSubmissionProtocol() == JobSubmissionProtocol.UNICORE) {
                // TODO - breakdown unicore all in one task to multiple tasks, then we don't need to handle UNICORE here.
                taskIdList.addAll(createAndSaveSubmissionTasks(gatewayId, preferredJobSubmissionInterface, processModel, userGivenWallTime));
            } else {
                taskIdList.addAll(createAndSaveEnvSetupTask(gatewayId, processModel, experimentCatalog));
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
                                    taskIdList.addAll(createAndSaveSubmissionTasks(gatewayId,preferredJobSubmissionInterface, processModel, maxRunTime));
                                }
                                int leftWallTime = userGivenWallTime % maxRunTime;
                                if (leftWallTime != 0) {
                                    taskIdList.addAll(createAndSaveSubmissionTasks(gatewayId,preferredJobSubmissionInterface, processModel, leftWallTime));
                                }
                            } else {
                                taskIdList.addAll(createAndSaveSubmissionTasks(gatewayId,preferredJobSubmissionInterface, processModel, userGivenWallTime));
                            }
                        }
                    }
                } else {
                    taskIdList.addAll(createAndSaveSubmissionTasks(gatewayId,preferredJobSubmissionInterface, processModel, userGivenWallTime));
                }
                taskIdList.addAll(createAndSaveOutputDataStagingTasks(processModel, gatewayId));
            }
            // update process scheduling
            experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processModel.getProcessId());
            return getTaskDag(taskIdList);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process");
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

    private List<String> createAndSaveEnvSetupTask(String gatewayId,
                                                   ProcessModel processModel,
                                                   ExperimentCatalog experimentCatalog)
            throws RegistryException, TException {
        List<String> envTaskIds = new ArrayList<>();
        TaskModel envSetupTask = new TaskModel();
        envSetupTask.setTaskType(TaskTypes.ENV_SETUP);
        envSetupTask.setTaskStatus(new TaskStatus(TaskState.CREATED));
        envSetupTask.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        envSetupTask.setParentProcessId(processModel.getProcessId());
        EnvironmentSetupTaskModel envSetupSubModel = new EnvironmentSetupTaskModel();
        envSetupSubModel.setProtocol(OrchestratorUtils.getSecurityProtocol(orchestratorContext, processModel, gatewayId));
        ComputeResourcePreference computeResourcePreference = OrchestratorUtils.getComputeResourcePreference(orchestratorContext, processModel, gatewayId);
        String scratchLocation = OrchestratorUtils.getScratchLocation(orchestratorContext,processModel, gatewayId);
        String workingDir = scratchLocation + File.separator + processModel.getProcessId();
        envSetupSubModel.setLocation(workingDir);
        byte[] envSetupSub = ThriftUtils.serializeThriftObject(envSetupSubModel);
        envSetupTask.setSubTaskModel(envSetupSub);
        String envSetupTaskId = (String) experimentCatalog.add(ExpCatChildDataType.TASK, envSetupTask, processModel.getProcessId());
        envSetupTask.setTaskId(envSetupTaskId);
        envTaskIds.add(envSetupTaskId);
        return envTaskIds;
    }

    public List<String> createAndSaveInputDataStagingTasks(ProcessModel processModel, String gatewayId) throws RegistryException {
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
                        try {
                            TaskModel inputDataStagingTask = getInputDataStagingTask(processModel, processInput, gatewayId);
                            String taskId = (String) orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.TASK, inputDataStagingTask,
                                    processModel.getProcessId());
                            inputDataStagingTask.setTaskId(taskId);
                            dataStagingTaskIds.add(inputDataStagingTask.getTaskId());
                        } catch (TException | AppCatalogException | TaskException e) {
                            throw new RegistryException("Error while serializing data staging sub task model");
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

    public List<String> createAndSaveOutputDataStagingTasks(ProcessModel processModel, String gatewayId) throws RegistryException {
        List<String> dataStagingTaskIds = new ArrayList<>();
        List<OutputDataObjectType> processOutputs = processModel.getProcessOutputs();
        String appName = OrchestratorUtils.getApplicationInterfaceName(orchestratorContext, processModel);
        if (processOutputs != null) {
            for (OutputDataObjectType processOutput : processOutputs) {
                DataType type = processOutput.getType();
                switch (type) {
                    case STDOUT :
                        processOutput.setValue(appName + ".stdout");
                        createOutputDataSatagingTasks(processModel, gatewayId, dataStagingTaskIds, processOutput);
                        break;
                    case STDERR:
                        processOutput.setValue(appName + ".stderr");
                        createOutputDataSatagingTasks(processModel, gatewayId, dataStagingTaskIds, processOutput);
                        break;
                    case URI:
                        createOutputDataSatagingTasks(processModel, gatewayId, dataStagingTaskIds, processOutput);
                        break;
                    default:
                        // nothing to do
                        break;
                }
            }
        }

        try {
            if (isArchive(processModel, gatewayId)) {
                createArchiveDataStatgingTask(processModel, gatewayId, dataStagingTaskIds);
            }
        } catch (AppCatalogException e) {
            throw new RegistryException("Error! Application interface retrieval failed");
        }
        return dataStagingTaskIds;
    }

    private boolean isArchive(ProcessModel processModel, String gatewayId) throws AppCatalogException {
        AppCatalog appCatalog = RegistryFactory.getAppCatalog();
        ApplicationInterfaceDescription appInterface = appCatalog.getApplicationInterface().getApplicationInterface(processModel.getApplicationInterfaceId());
        return appInterface.isArchiveWorkingDirectory();
    }

    private void createArchiveDataStatgingTask(ProcessModel processModel, String gatewayId, List<String> dataStagingTaskIds) throws RegistryException {
        TaskModel archiveTask = null;
        try {
            archiveTask = getOutputDataStagingTask(processModel, null, gatewayId);
        } catch (TException e) {
            throw new RegistryException("Error! DataStaging sub task serialization failed");
        }
        String taskId = (String) orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.TASK, archiveTask,
                processModel.getProcessId());
        archiveTask.setTaskId(taskId);
        dataStagingTaskIds.add(archiveTask.getTaskId());

    }

    private void createOutputDataSatagingTasks(ProcessModel processModel, String gatewayId, List<String> dataStagingTaskIds, OutputDataObjectType processOutput) throws RegistryException {
        try {
            TaskModel outputDataStagingTask = getOutputDataStagingTask(processModel, processOutput, gatewayId);
            String taskId = (String) orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.TASK, outputDataStagingTask,
                    processModel.getProcessId());
            outputDataStagingTask.setTaskId(taskId);
            dataStagingTaskIds.add(outputDataStagingTask.getTaskId());
        } catch (TException e) {
            throw new RegistryException("Error while serializing data staging sub task model", e);
        }
    }

    private List<String> createAndSaveSubmissionTasks(String gatewayId, JobSubmissionInterface jobSubmissionInterface, ProcessModel processModel, int wallTime)
            throws TException, RegistryException, OrchestratorException {

        JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();
        MonitorMode monitorMode = null;
        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH || jobSubmissionProtocol == JobSubmissionProtocol.SSH_FORK) {
            SSHJobSubmission sshJobSubmission = OrchestratorUtils.getSSHJobSubmission(orchestratorContext, jobSubmissionInterface.getJobSubmissionInterfaceId());
            monitorMode = sshJobSubmission.getMonitorMode();
        } else if (jobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
            monitorMode = MonitorMode.FORK;
        } else {
            logger.error("expId : {}, processId : {} :- Unsupported Job submission protocol {}.",
                    processModel.getExperimentId(), processModel.getProcessId(), jobSubmissionProtocol.name());
            throw new OrchestratorException("Unsupported Job Submission Protocol " + jobSubmissionProtocol.name());
        }
        List<String> submissionTaskIds = new ArrayList<>();
        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processModel.getProcessId());
        taskModel.setCreationTime(new Date().getTime());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatus(taskStatus);
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        JobSubmissionTaskModel submissionSubTask = new JobSubmissionTaskModel();
        submissionSubTask.setMonitorMode(monitorMode);
        submissionSubTask.setJobSubmissionProtocol(jobSubmissionProtocol);
        submissionSubTask.setWallTime(wallTime);
        byte[] bytes = ThriftUtils.serializeThriftObject(submissionSubTask);
        taskModel.setSubTaskModel(bytes);
        String taskId = (String) orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.TASK, taskModel,
                processModel.getProcessId());
        taskModel.setTaskId(taskId);
        submissionTaskIds.add(taskModel.getTaskId());

        // create monitor task for this Email based monitor mode job
        if (monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR) {
            TaskModel monitorTaskModel = new TaskModel();
            monitorTaskModel.setParentProcessId(processModel.getProcessId());
            monitorTaskModel.setCreationTime(new Date().getTime());
            monitorTaskModel.setLastUpdateTime(monitorTaskModel.getCreationTime());
            TaskStatus monitorTaskStatus = new TaskStatus(TaskState.CREATED);
            monitorTaskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            monitorTaskModel.setTaskStatus(monitorTaskStatus);
            monitorTaskModel.setTaskType(TaskTypes.MONITORING);
            MonitorTaskModel monitorSubTaskModel = new MonitorTaskModel();
            monitorSubTaskModel.setMonitorMode(monitorMode);
            monitorTaskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(monitorSubTaskModel));
            String mTaskId = (String) orchestratorContext.getRegistry().getExperimentCatalog().add(ExpCatChildDataType.TASK, monitorTaskModel, processModel.getProcessId());
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

    private TaskModel getInputDataStagingTask(ProcessModel processModel, InputDataObjectType processInput, String gatewayId) throws RegistryException, TException, AppCatalogException, TaskException {
        // create new task model for this task
        TaskModel taskModel = new TaskModel();
        taskModel.setParentProcessId(processModel.getProcessId());
        taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setLastUpdateTime(taskModel.getCreationTime());
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        taskModel.setTaskStatus(taskStatus);
        taskModel.setTaskType(TaskTypes.DATA_STAGING);
        // create data staging sub task model
        DataStagingTaskModel submodel = new DataStagingTaskModel();
        ComputeResourcePreference computeResourcePreference = OrchestratorUtils.getComputeResourcePreference(orchestratorContext, processModel, gatewayId);
        ComputeResourceDescription computeResource = orchestratorContext.getRegistry().getAppCatalog().getComputeResource().getComputeResource(processModel.getComputeResourceId());
        String remoteOutputDir = OrchestratorUtils.getScratchLocation(orchestratorContext,processModel, gatewayId) + File.separator + processModel.getProcessId();
        remoteOutputDir = remoteOutputDir.endsWith("/") ? remoteOutputDir : remoteOutputDir + "/";
        URI destination = null;
        try {
            DataMovementProtocol dataMovementProtocol = OrchestratorUtils.getPreferredDataMovementProtocol(orchestratorContext, processModel, gatewayId);
            String loginUserName = OrchestratorUtils.getLoginUserName(orchestratorContext, processModel, gatewayId);
            destination = new URI(dataMovementProtocol.name(),
                    loginUserName,
                    computeResource.getHostName(),
                    OrchestratorUtils.getDataMovementPort(orchestratorContext, processModel, gatewayId),
                    remoteOutputDir , null, null);
        } catch (URISyntaxException e) {
            throw new TaskException("Error while constructing destination file URI");
        }
        submodel.setType(DataStageType.INPUT);
        submodel.setSource(processInput.getValue());
        submodel.setProcessInput(processInput);
        submodel.setDestination(destination.toString());
        taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
        return taskModel;
    }

    private TaskModel getOutputDataStagingTask(ProcessModel processModel, OutputDataObjectType processOutput, String gatewayId) throws RegistryException, TException {
        try {

            // create new task model for this task
            TaskModel taskModel = new TaskModel();
            taskModel.setParentProcessId(processModel.getProcessId());
            taskModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            taskModel.setLastUpdateTime(taskModel.getCreationTime());
            TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            taskModel.setTaskStatus(taskStatus);
            taskModel.setTaskType(TaskTypes.DATA_STAGING);
            ComputeResourcePreference computeResourcePreference = OrchestratorUtils.getComputeResourcePreference(orchestratorContext, processModel, gatewayId);
            ComputeResourceDescription computeResource = orchestratorContext.getRegistry().getAppCatalog().getComputeResource().getComputeResource(processModel.getComputeResourceId());

            String remoteOutputDir = OrchestratorUtils.getScratchLocation(orchestratorContext,processModel, gatewayId) + File.separator + processModel.getProcessId();
            remoteOutputDir = remoteOutputDir.endsWith("/") ? remoteOutputDir : remoteOutputDir + "/";
            DataStagingTaskModel submodel = new DataStagingTaskModel();
            DataMovementProtocol dataMovementProtocol = OrchestratorUtils.getPreferredDataMovementProtocol(orchestratorContext, processModel, gatewayId);
            URI source = null;
            try {
                String loginUserName = OrchestratorUtils.getLoginUserName(orchestratorContext, processModel, gatewayId);
                if (processOutput != null) {
                    submodel.setType(DataStageType.OUPUT);
                    submodel.setProcessOutput(processOutput);
                    source = new URI(dataMovementProtocol.name(),
                            loginUserName,
                            computeResource.getHostName(),
                            OrchestratorUtils.getDataMovementPort(orchestratorContext, processModel, gatewayId),
                            remoteOutputDir + processOutput.getValue(), null, null);
                } else {
                    // archive
                    submodel.setType(DataStageType.ARCHIVE_OUTPUT);
                    source = new URI(dataMovementProtocol.name(),
                            loginUserName,
                            computeResource.getHostName(),
                            OrchestratorUtils.getDataMovementPort(orchestratorContext, processModel, gatewayId),
                            remoteOutputDir, null, null);
                }
            } catch (URISyntaxException e) {
                throw new TaskException("Error while constructing source file URI");
            }
            // We don't know destination location at this time, data staging task will set this.
            // because destination is required field we set dummy destination
            submodel.setSource(source.toString());
            // We don't know destination location at this time, data staging task will set this.
            // because destination is required field we set dummy destination
            submodel.setDestination("dummy://temp/file/location");
            taskModel.setSubTaskModel(ThriftUtils.serializeThriftObject(submodel));
            return taskModel;
        } catch (AppCatalogException | TaskException e) {
           throw new RegistryException("Error occurred while retrieving data movement from app catalog", e);
        }
    }


}
