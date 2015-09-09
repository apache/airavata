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
import java.util.Map;

public class SSHJobSubmissionTask implements JobSubmissionTask {
    private static final Logger log = LoggerFactory.getLogger(SSHJobSubmissionTask.class);
    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {

    }

    @Override
    public TaskStatus execute(TaskContext taskContext){
	    TaskStatus taskStatus = new TaskStatus(TaskState.COMPLETED); // set to completed.
	    try {
		    ProcessContext processContext = taskContext.getParentProcessContext();
		    JobModel jobModel = processContext.getJobModel();
		    jobModel.setTaskId(taskContext.getTaskId());
		    RemoteCluster remoteCluster = processContext.getRemoteCluster();
		    JobDescriptor jobDescriptor = GFacUtils.createJobDescriptor(processContext);
		    jobModel.setJobName(jobDescriptor.getJobName());
		    ResourceJobManager resourceJobManager = GFacUtils.getResourceJobManager(processContext);
		    JobManagerConfiguration jConfig = null;
		    if (resourceJobManager != null) {
			    jConfig = Factory.getJobManagerConfiguration(resourceJobManager);
		    }
		    JobStatus jobStatus = new JobStatus();
		    File jobFile = GFacUtils.createJobFile(jobDescriptor, jConfig);
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
				    jobModel.setJobStatus(jobStatus);
				    GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), jobModel);
				    if (verifyJobSubmissionByJobId(remoteCluster, jobId)) {
					    jobStatus.setJobState(JobState.QUEUED);
					    jobStatus.setReason("Verification step succeeded");
					    jobModel.setJobStatus(jobStatus);
					    GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), jobModel);
				    }
				    taskStatus = new TaskStatus(TaskState.COMPLETED);
				    taskStatus.setReason("Submitted job to compute resource");
			    } else {
				    int verificationTryCount = 0;
				    while (verificationTryCount++ < 3) {
					    String verifyJobId = verifyJobSubmission(remoteCluster, jobModel);
					    if (verifyJobId != null && !verifyJobId.isEmpty()) {
						    // JobStatus either changed from SUBMITTED to QUEUED or directly to QUEUED
						    jobId = verifyJobId;
						    jobModel.setJobId(jobId);
						    GFacUtils.saveJobModel(processContext,jobModel);
						    jobStatus.setJobState(JobState.QUEUED);
						    jobStatus.setReason("Verification step succeeded");
						    jobModel.setJobStatus(jobStatus);
						    GFacUtils.saveJobStatus(taskContext.getParentProcessContext(), jobModel);
						    taskStatus.setState(TaskState.COMPLETED);
						    taskStatus.setReason("Submitted job to compute resource");
						    break;
					    }
					    log.info("Verify step return invalid jobId, retry verification step in {} secs", verificationTryCount);
					    Thread.sleep(verificationTryCount * 1000);
				    }
			    }

			    if (jobId == null || jobId.isEmpty()) {
				    String msg = "expId:" + processContext.getProcessModel().getExperimentId() + " Couldn't find " +
						    "remote jobId for JobName:" + jobModel.getJobName() + ", both submit and verify steps " +
						    "doesn't return a valid JobId. " + "Hence changing experiment state to Failed";
				    log.error(msg);
                    ErrorModel errorModel = new ErrorModel();
                    errorModel.setUserFriendlyMessage(msg);
                    errorModel.setActualErrorMessage(msg);
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

	    } catch (AppCatalogException e) {
		    String msg = "Error while instantiating app catalog";
		    log.error(msg, e);
		    taskStatus.setState(TaskState.FAILED);
		    taskStatus.setReason(msg);
		    ErrorModel errorModel = new ErrorModel();
		    errorModel.setActualErrorMessage(e.getMessage());
		    errorModel.setUserFriendlyMessage(msg);
		    taskContext.getTaskModel().setTaskError(errorModel);
	    } catch (ApplicationSettingsException e) {
		    String msg = "Error occurred while creating job descriptor";
		    log.error(msg, e);
		    taskStatus.setState(TaskState.FAILED);
		    taskStatus.setReason(msg);
		    ErrorModel errorModel = new ErrorModel();
		    errorModel.setActualErrorMessage(e.getMessage());
		    errorModel.setUserFriendlyMessage(msg);
		    taskContext.getTaskModel().setTaskError(errorModel);
	    } catch (GFacException e) {
		    String msg = "Error occurred while creating job descriptor";
		    log.error(msg, e);
		    taskStatus.setState(TaskState.FAILED);
		    taskStatus.setReason(msg);
		    ErrorModel errorModel = new ErrorModel();
		    errorModel.setActualErrorMessage(e.getMessage());
		    errorModel.setUserFriendlyMessage(msg);
		    taskContext.getTaskModel().setTaskError(errorModel);
	    } catch (SSHApiException e) {
		    String msg = "Error occurred while submitting the job";
		    log.error(msg, e);
		    taskStatus.setState(TaskState.FAILED);
		    taskStatus.setReason(msg);
		    ErrorModel errorModel = new ErrorModel();
		    errorModel.setActualErrorMessage(e.getMessage());
		    errorModel.setUserFriendlyMessage(msg);
		    taskContext.getTaskModel().setTaskError(errorModel);
	    } catch (IOException e) {
		    String msg = "Error while reading the content of the job file";
		    log.error(msg, e);
		    taskStatus.setState(TaskState.FAILED);
		    taskStatus.setReason(msg);
		    ErrorModel errorModel = new ErrorModel();
		    errorModel.setActualErrorMessage(e.getMessage());
		    errorModel.setUserFriendlyMessage(msg);
		    taskContext.getTaskModel().setTaskError(errorModel);
	    } catch (InterruptedException e) {
		    String msg = "Error occurred while verifying the job submission";
		    log.error(msg, e);
		    taskStatus.setState(TaskState.FAILED);
		    taskStatus.setReason(msg);
		    ErrorModel errorModel = new ErrorModel();
		    errorModel.setActualErrorMessage(e.getMessage());
		    errorModel.setUserFriendlyMessage(msg);
		    taskContext.getTaskModel().setTaskError(errorModel);
	    }

	    taskContext.setTaskStatus(taskStatus);
	    try {
		    GFacUtils.saveAndPublishTaskStatus(taskContext);
	    } catch (GFacException e) {
		    log.error("Error while saving task status", e);
	    }
	    return taskStatus;
    }

    private boolean verifyJobSubmissionByJobId(RemoteCluster remoteCluster, String jobID) throws SSHApiException {
        JobStatus status = remoteCluster.getJobStatus(jobID);
        return status != null &&  status.getJobState() != JobState.UNKNOWN;
    }

    private String verifyJobSubmission(RemoteCluster remoteCluster, JobModel jobDetails) {
        String jobName = jobDetails.getJobName();
        String jobId = null;
        try {
            jobId  = remoteCluster.getJobIdByJobName(jobName, remoteCluster.getServerInfo().getUserName());
        } catch (SSHApiException e) {
            log.error("Error while verifying JobId from JobName");
        }
        return jobId;
    }


    @Override
    public TaskStatus recover(TaskContext taskContext) {
            ProcessContext processContext = taskContext.getParentProcessContext();
            JobModel jobModel = processContext.getJobModel();
            // original job failed before submitting
            if (jobModel == null || jobModel.getJobId() == null ){
                return execute(taskContext);
            }else {
	            // job is already submitted and monitor should handle the recovery
	            return new TaskStatus(TaskState.COMPLETED);
            }
    }

	@Override
	public TaskTypes getType() {
		return TaskTypes.JOB_SUBMISSION;
	}
}
