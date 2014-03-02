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

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.gfac.cpi.GFacImpl;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleOrchestratorImpl extends AbstractOrchestrator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;


    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;

    private JobMetadataValidator jobMetadataValidator = null;


    public SimpleOrchestratorImpl() throws OrchestratorException {
        try {
            try {
                String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getNewJobSubmitterClass();
                Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
                jobSubmitter = aClass.newInstance();
                jobSubmitter.initialize(this.orchestratorContext);

                String validatorClzz = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClass();
                if (this.orchestratorConfiguration.isEnableValidation()) {
                    if (validatorClzz == null) {
                        logger.error("Job validation class is not properly set, so Validation will be turned off !");
                    }
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validatorClzz.trim()).asSubclass(JobMetadataValidator.class);
                    jobMetadataValidator = vClass.newInstance();
                }
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

    public String launchExperiment(String experimentID, String taskID) throws OrchestratorException {
        // we give higher priority to userExperimentID
        //todo support multiple validators
        String jobID = null;
        if (this.orchestratorConfiguration.isEnableValidation()) {
            if (jobMetadataValidator.validate(experimentID)) {
                logger.info("validation Successful for the experiment: " + experimentID);
            } else {
                throw new OrchestratorException("Validation Failed, so Job will not be submitted to GFAC");
            }
        }

        jobID = jobSubmitter.submit(experimentID, taskID);
        return jobID;
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
            experiment = (Experiment) newRegistry.get(DataType.EXPERIMENT, experimentId);


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

    @Subscribe
    public void handlePostExperimentTask(JobStatus status) throws OrchestratorException {
        if(status.getState() == JobState.COMPLETE){
            MonitorID monitorID = status.getMonitorID();
            jobSubmitter.runAfterJobTask(monitorID.getExperimentID(), monitorID.getTaskID());
        }
    }
    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public JobMetadataValidator getJobMetadataValidator() {
        return jobMetadataValidator;
    }

    public void setJobMetadataValidator(JobMetadataValidator jobMetadataValidator) {
        this.jobMetadataValidator = jobMetadataValidator;
    }

    public JobSubmitter getJobSubmitter() {
        return jobSubmitter;
    }

    public void setJobSubmitter(JobSubmitter jobSubmitter) {
        this.jobSubmitter = jobSubmitter;
    }
}
