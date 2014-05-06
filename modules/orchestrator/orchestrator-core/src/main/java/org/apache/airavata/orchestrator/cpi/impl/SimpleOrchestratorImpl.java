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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.naming.OperationNotSupportedException;

public class SimpleOrchestratorImpl extends AbstractOrchestrator{
    private final static Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;
    
    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;


    public SimpleOrchestratorImpl() throws OrchestratorException {
        try {
            try {
                String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getNewJobSubmitterClass();
                Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
                jobSubmitter = aClass.newInstance();
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

    public String launchExperiment(Experiment experiment, WorkflowNodeDetails workflowNode, TaskDetails task) throws OrchestratorException {
        // we give higher priority to userExperimentID
        //todo support multiple validators
        String experimentId = experiment.getExperimentID();
        String taskId = task.getTaskID();
        // creating monitorID to register with monitoring queue
        // this is a special case because amqp has to be in place before submitting the job
        try {
            JobExecutionContext jobExecutionContext = jobSubmitter.submit(experimentId, taskId);
            return jobExecutionContext.getJobDetails().getJobID();
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


            WorkflowNodeDetails iDontNeedaNode = ExperimentModelUtil.createWorkflowNode("IDontNeedaNode", null);
            String nodeID = (String) newRegistry.add(ChildDataType.WORKFLOW_NODE_DETAIL, iDontNeedaNode, experimentId);

            TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromExperiment(experiment);
            taskDetails.setTaskID((String) newRegistry.add(ChildDataType.TASK_DETAIL, taskDetails, nodeID));
            tasks.add(taskDetails);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating a task");
        }
        return tasks;
    }

    public boolean validateExperiment(Experiment experiment, WorkflowNodeDetails workflowNodeDetail, TaskDetails taskID) throws OrchestratorException {
        if (this.orchestratorConfiguration.isEnableValidation()) {
            List<String> validatorClzzez = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClasses();
            for (String validator : validatorClzzez) {
                try {
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validator.trim()).asSubclass(JobMetadataValidator.class);
                    JobMetadataValidator jobMetadataValidator = vClass.newInstance();
                    if(jobMetadataValidator.validate(experiment, workflowNodeDetail, taskID)){
                        logger.info("Validation of " + validator + " is SUCCESSFUL");
                    }else {
                        logger.error("Validation of " + validator + " is FAILED");
                        return false;
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("Error loading the validation class: ", validator, e);
                    return false;
                } catch (InstantiationException e) {
                   logger.error("Error loading the validation class: ", validator, e);
                    return false;
                } catch (IllegalAccessException e) {
                   logger.error("Error loading the validation class: ", validator, e);
                    return false;
                }
            }
        }
        return true;
    }

    public void cancelExperiment(String experimentID)
			throws OrchestratorException {
        throw new OrchestratorException(new OperationNotSupportedException());
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
