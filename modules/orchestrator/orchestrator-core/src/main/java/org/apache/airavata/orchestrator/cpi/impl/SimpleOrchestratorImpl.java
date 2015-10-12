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

import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.impl.GFACPassiveJobSubmitter;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    public List<ProcessModel> createProcesses (String experimentId) throws OrchestratorException {
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

    public List<TaskModel> createTasks (String experimentId, String processId) throws OrchestratorException {
        List<TaskModel> taskModels = new ArrayList<TaskModel>();
        try {
            ExperimentCatalog experimentCatalog = orchestratorContext.getRegistry().getExperimentCatalog();
            AppCatalog appCatalog = orchestratorContext.getRegistry().getAppCatalog();
            ExperimentModel experimentModel = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            ProcessModel processModel = (ProcessModel)experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
            boolean autoSchedule = experimentModel.getUserConfigurationData().isAiravataAutoSchedule();
            ComputationalResourceSchedulingModel resourceSchedule = processModel.getResourceSchedule();
            String userGivenQueueName = resourceSchedule.getQueueName();
            int userGivenWallTime = resourceSchedule.getWallTimeLimit();
            String resourceHostId = resourceSchedule.getResourceHostId();
            if (resourceHostId == null){
                throw new OrchestratorException("Compute Resource Id cannot be null at this point");
            }
            ComputeResourceDescription computeResource = appCatalog.getComputeResource().getComputeResource(resourceHostId);
            List<BatchQueue> definedBatchQueues = computeResource.getBatchQueues();
            for (BatchQueue batchQueue : definedBatchQueues){
                if (batchQueue.getQueueName().equals(userGivenQueueName)){
                    int maxRunTime = batchQueue.getMaxRunTime();
                    if (maxRunTime < userGivenWallTime){
                        // need to create more job submissions
                    }
                }
            }


        } catch (Exception e) {
            throw new OrchestratorException("Error during creating process");
        }
        return taskModels;
    }

}
