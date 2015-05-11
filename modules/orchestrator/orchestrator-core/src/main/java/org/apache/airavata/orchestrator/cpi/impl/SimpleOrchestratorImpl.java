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

import org.apache.airavata.model.error.LaunchValidationException;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.impl.GFACPassiveJobSubmitter;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
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

    public boolean launchExperiment(Experiment experiment, WorkflowNodeDetails workflowNode, TaskDetails task,String tokenId) throws OrchestratorException {
        // we give higher priority to userExperimentID
        String experimentId = experiment.getExperimentID();
        String taskId = task.getTaskID();
        // creating monitorID to register with monitoring queue
        // this is a special case because amqp has to be in place before submitting the job
        try {
            return jobSubmitter.submit(experimentId, taskId,tokenId);
        } catch (Exception e) {
            throw new OrchestratorException("Error launching the job", e);
        }
    }

    /**
     * This method will parse the ExperimentConfiguration and based on the configuration
     * we create a single or multiple tasks for the experiment.
     *
     * @param experimentId
     * @return
     * @throws OrchestratorException
     */
    public List<TaskDetails> createTasks(String experimentId) throws OrchestratorException {
        Experiment experiment = null;
        List<TaskDetails> tasks = new ArrayList<TaskDetails>();
        try {
            Registry newRegistry = orchestratorContext.getNewRegistry();
            experiment = (Experiment) newRegistry.get(RegistryModelType.EXPERIMENT, experimentId);
            List<WorkflowNodeDetails> workflowNodeDetailsList = experiment.getWorkflowNodeDetailsList();
            if (workflowNodeDetailsList != null && !workflowNodeDetailsList.isEmpty()){
                for (WorkflowNodeDetails wfn : workflowNodeDetailsList){
                    List<TaskDetails> taskDetailsList = wfn.getTaskDetailsList();
                    if (taskDetailsList != null && !taskDetailsList.isEmpty()){
                        return taskDetailsList;
                    }
                }
            }else {
                WorkflowNodeDetails iDontNeedaNode = ExperimentModelUtil.createWorkflowNode("tempNode", null);
                String nodeID = (String) newRegistry.add(ChildDataType.WORKFLOW_NODE_DETAIL, iDontNeedaNode, experimentId);

                TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromExperiment(experiment);
                taskDetails.setTaskID((String) newRegistry.add(ChildDataType.TASK_DETAIL, taskDetails, nodeID));
                tasks.add(taskDetails);
            }

        } catch (Exception e) {
            throw new OrchestratorException("Error during creating a task");
        }
        return tasks;
    }

    public ValidationResults validateExperiment(Experiment experiment, WorkflowNodeDetails workflowNodeDetail, TaskDetails taskID) throws OrchestratorException,LaunchValidationException {
        org.apache.airavata.model.error.ValidationResults validationResults = new org.apache.airavata.model.error.ValidationResults();
        validationResults.setValidationState(true); // initially making it to success, if atleast one failed them simply mark it failed.
        String errorMsg = "Validation Errors : ";
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClzzez = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClzzez) {
                try {
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                    JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                    validationResults = jobMetadataValidator.validate(experiment, workflowNodeDetail, taskID);
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
                        logger.error("Validation of " + validator + " for experiment Id " + experiment.getExperimentID() + " is FAILED:[error]. " + errorMsg);
                        validationResults.setValidationState(false);
                        try {
                            ErrorDetails details = new ErrorDetails();
                            details.setActualErrorMessage(errorMsg);
                            details.setCorrectiveAction(CorrectiveAction.RETRY_SUBMISSION);
                            details.setActionableGroup(ActionableGroup.GATEWAYS_ADMINS);
                            details.setCreationTime(Calendar.getInstance().getTimeInMillis());
                            details.setErrorCategory(ErrorCategory.APPLICATION_FAILURE);
                            orchestratorContext.getNewRegistry().add(ChildDataType.ERROR_DETAIL, details,
                                    taskID.getTaskID());
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

    public void cancelExperiment(Experiment experiment, WorkflowNodeDetails workflowNode, TaskDetails task, String tokenId)
            throws OrchestratorException {
        List<JobDetails> jobDetailsList = task.getJobDetailsList();
        for(JobDetails jobDetails:jobDetailsList) {
            JobState jobState = jobDetails.getJobStatus().getJobState();
            if (jobState.getValue() > 4){
                logger.error("Cannot cancel the job, because current job state is : " + jobState.toString() +
                "jobId: " + jobDetails.getJobID() + " Job Name: " + jobDetails.getJobName());
                return;
            }
        }
        jobSubmitter.terminate(experiment.getExperimentID(),task.getTaskID(),tokenId);
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

}
