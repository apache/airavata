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
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
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
import java.util.Arrays;
import java.util.Map;

public class ForkJobSubmissionTask implements JobSubmissionTask {
    private static final Logger log = LoggerFactory.getLogger(ForkJobSubmissionTask.class);
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
            GroovyMap groovyMap = GFacUtils.createGroovyMap(processContext, taskContext);
            jobModel.setJobName(groovyMap.get(Script.JOB_NAME).toString());
            ResourceJobManager resourceJobManager = GFacUtils.getResourceJobManager(processContext);
            JobManagerConfiguration jConfig = null;
            if (resourceJobManager != null) {
                jConfig = Factory.getJobManagerConfiguration(resourceJobManager);
            }
            JobStatus jobStatus = new JobStatus();
	        File jobFile = GFacUtils.createJobFile(groovyMap, taskContext, jConfig);
	        if (jobFile != null && jobFile.exists()) {
                jobModel.setJobDescription(FileUtils.readFileToString(jobFile));
	            JobSubmissionOutput jobSubmissionOutput = remoteCluster.submitBatchJob(jobFile.getPath(),
			            processContext.getWorkingDir());
	            jobModel.setExitCode(jobSubmissionOutput.getExitCode());
	            jobModel.setStdErr(jobSubmissionOutput.getStdErr());
	            jobModel.setStdOut(jobSubmissionOutput.getStdOut());
	            String jobId = jobSubmissionOutput.getJobId();
	            if (jobId != null && !jobId.isEmpty()) {
                    jobModel.setJobId(jobId);
                    GFacUtils.saveJobModel(processContext, jobModel);
                    jobStatus.setJobState(JobState.SUBMITTED);
                    jobStatus.setReason("Successfully Submitted to " + taskContext.getParentProcessContext()
                            .getComputeResourceDescription().getHostName());
                    jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    jobModel.setJobStatuses(Arrays.asList(jobStatus));
                    GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), jobModel);
                    taskStatus = new TaskStatus(TaskState.COMPLETED);
                    taskStatus.setReason("Submitted job to compute resource");
                }
                if (jobId == null || jobId.isEmpty()) {
                    String msg = "expId:" + processContext.getProcessModel().getExperimentId() + " Couldn't find " +
                            "remote jobId for JobName:" + jobModel.getJobName() + ", both submit and verify steps " +
                            "doesn't return a valid JobId. " + "Hence changing experiment state to Failed";
                    log.error(msg);
                    ErrorModel errorModel = new ErrorModel();
                    errorModel.setActualErrorMessage(msg);
                    errorModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
                    GFacUtils.saveExperimentError(processContext, errorModel);
                    GFacUtils.saveProcessError(processContext, errorModel);
                    GFacUtils.saveTaskError(taskContext, errorModel);
                    taskStatus.setState(TaskState.FAILED);
                    taskStatus.setReason("Couldn't find job id in both submitted and verified steps");
                }else {
                    GFacUtils.saveJobModel(processContext, jobModel);
                }
            } else {
                taskStatus.setState(TaskState.FAILED);
                if (jobFile == null) {
                    taskStatus.setReason("JobFile is null");
                } else {
                    taskStatus.setReason("Job file doesn't exist");
                }
            }
        } catch (ApplicationSettingsException e) {
            String msg = "Error occurred while creating job descriptor";
            log.error(msg, e);
            taskStatus.setState(TaskState.FAILED);
            taskStatus.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (AppCatalogException e) {
            String msg = "Error while instantiating app catalog";
            log.error(msg, e);
            taskStatus.setState(TaskState.FAILED);
            taskStatus.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (GFacException e) {
            String msg = "Error occurred while submitting the job";
            log.error(msg, e);
            taskStatus.setState(TaskState.FAILED);
            taskStatus.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (IOException e) {
            String msg = "Error while reading the content of the job file";
            log.error(msg, e);
            taskStatus.setState(TaskState.FAILED);
            taskStatus.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        }
        return taskStatus;
    }

    @Override
    public TaskStatus recover(TaskContext taskContext) {
        //TODO implement recovery scenario instead of calling execute.
        return execute(taskContext);
    }

	@Override
	public TaskTypes getType() {
		return TaskTypes.JOB_SUBMISSION;
	}

	@Override
	public JobStatus cancel(TaskContext taskcontext) {
		// TODO - implement cancel with SSH Fork
		return null;
	}
}
