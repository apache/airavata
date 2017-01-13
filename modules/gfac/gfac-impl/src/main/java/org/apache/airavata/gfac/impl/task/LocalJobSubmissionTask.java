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

package org.apache.airavata.gfac.impl.task;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.gfac.core.*;
import org.apache.airavata.gfac.core.cluster.JobSubmissionOutput;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.SetEnvPaths;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LocalJobSubmissionTask implements JobSubmissionTask{
    private static final Logger log = LoggerFactory.getLogger(LocalJobSubmissionTask.class);
    private ProcessBuilder builder;

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {
    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        try {
            ProcessContext processContext = taskContext.getParentProcessContext();
            JobModel jobModel = processContext.getJobModel();
            jobModel.setTaskId(taskContext.getTaskId());

            RemoteCluster remoteCluster = processContext.getJobSubmissionRemoteCluster();
            GroovyMap groovyMap = GFacUtils.createGroovyMap(processContext,taskContext);

            String jobId = AiravataUtils.getId("JOB_ID_");
            jobModel.setJobName(groovyMap.get(Script.JOB_NAME).toString());
            jobModel.setJobId(jobId);

            ResourceJobManager resourceJobManager = GFacUtils.getResourceJobManager(processContext);
            JobManagerConfiguration jConfig = null;

            if (resourceJobManager != null) {
                jConfig = Factory.getJobManagerConfiguration(resourceJobManager);
            }

            JobStatus jobStatus = new JobStatus();
            File jobFile = GFacUtils.createJobFile(groovyMap, taskContext, jConfig);
            if (jobFile != null && jobFile.exists()) {
                jobModel.setJobDescription(FileUtils.readFileToString(jobFile));

                GFacUtils.saveJobModel(processContext, jobModel);

                JobSubmissionOutput jobSubmissionOutput = remoteCluster.submitBatchJob(jobFile.getPath(),
                        processContext.getWorkingDir());

                jobStatus.setJobState(JobState.SUBMITTED);
                jobStatus.setReason("Successfully Submitted to " + taskContext.getParentProcessContext()
                        .getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));
                //log job submit status
                GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), jobModel);

                //for local, job gets completed synchronously
                //so changing job status to complete

                jobModel.setExitCode(jobSubmissionOutput.getExitCode());
                jobModel.setStdErr(jobSubmissionOutput.getStdErr());
                jobModel.setStdOut(jobSubmissionOutput.getStdOut());


                jobModel.setJobId(jobId);
                jobStatus.setJobState(JobState.COMPLETE);
                jobStatus.setReason("Successfully Completed " + taskContext.getParentProcessContext()
                        .getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));
                //log job complete status
                GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), jobModel);


                taskStatus = new TaskStatus(TaskState.COMPLETED);
                taskStatus.setReason("Submitted job to compute resource");

            } else {
                taskStatus.setState(TaskState.FAILED);
                if (jobFile == null) {
                    taskStatus.setReason("JobFile is null");
                } else {
                    taskStatus.setReason("Job file doesn't exist");
                }
            }

        } catch (GFacException | IOException | AppCatalogException | ApplicationSettingsException e) {
            String msg = "Error occurred while submitting a local job";
            log.error(msg, e);
            taskStatus.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
            taskStatus.setState(TaskState.FAILED);
        }
        return taskStatus;
    }

    @Override
    public TaskStatus recover(TaskContext taskContext) {
        return null;
    }

    private List<String> buildCommand(ProcessContext processContext) {
        List<String> cmdList = new ArrayList<>();
        cmdList.add(processContext.getApplicationDeploymentDescription().getExecutablePath());
        List<InputDataObjectType> processInputs = processContext.getProcessModel().getProcessInputs();

        // sort the inputs first and then build the command List
        Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
            @Override
            public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
                return inputDataObjectType.getInputOrder() - t1.getInputOrder();
            }
        };
        Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
        for (InputDataObjectType input : processInputs) {
                sortedInputSet.add(input);
        }
        for (InputDataObjectType inputDataObjectType : sortedInputSet) {
            if (inputDataObjectType.getApplicationArgument() != null
                    && !inputDataObjectType.getApplicationArgument().equals("")) {
                cmdList.add(inputDataObjectType.getApplicationArgument());
            }

            if (inputDataObjectType.getValue() != null
                    && !inputDataObjectType.getValue().equals("")) {
                cmdList.add(inputDataObjectType.getValue());
            }
        }
        return cmdList;
    }

    private void initProcessBuilder(ApplicationDeploymentDescription app, List<String> cmdList){
        builder = new ProcessBuilder(cmdList);

        List<SetEnvPaths> setEnvironment = app.getSetEnvironment();
        if (setEnvironment != null) {
            for (SetEnvPaths envPath : setEnvironment) {
                Map<String,String> builderEnv = builder.environment();
                builderEnv.put(envPath.getName(), envPath.getValue());
            }
        }
    }

	@Override
	public TaskTypes getType() {
		return TaskTypes.JOB_SUBMISSION;
	}

	@Override
	public JobStatus cancel(TaskContext taskcontext) {
		// TODO - implement Local Job cancel
		return null;
	}
}
