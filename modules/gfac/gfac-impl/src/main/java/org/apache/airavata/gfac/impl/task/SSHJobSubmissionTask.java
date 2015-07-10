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
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.JobSubmissionTask;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskState;
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
    public TaskState execute(TaskContext taskContext) throws TaskException {
        try {
            ProcessContext processContext = taskContext.getParentProcessContext();
            JobModel jobModel = processContext.getJobModel();
            if (jobModel == null){
                jobModel = new JobModel();
	            jobModel.setWorkingDir(processContext.getWorkingDir());
	            jobModel.setTaskId(taskContext.getTaskId());
	            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            }
            RemoteCluster remoteCluster = processContext.getRemoteCluster();
            JobDescriptor jobDescriptor = GFacUtils.createJobDescriptor(processContext);
            jobModel.setJobName(jobDescriptor.getJobName());
            ResourceJobManager resourceJobManager = GFacUtils.getResourceJobManager(processContext);
            JobManagerConfiguration jConfig = null;
            if (resourceJobManager != null) {
                jConfig = Factory.getJobManagerConfiguration(resourceJobManager);
            }
            File jobFile = GFacUtils.createJobFile(jobDescriptor, jConfig);
            if (jobFile != null && jobFile.exists()){
                jobModel.setJobDescription(FileUtils.readFileToString(jobFile));
                String jobId = remoteCluster.submitBatchJob(jobFile.getPath(), processContext.getWorkingDir());
                if (jobId != null && !jobId.isEmpty()) {
                    jobModel.setJobId(jobId);
                    GFacUtils.saveJobStatus(taskContext, jobModel, JobState.SUBMITTED);
//                    publisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
//                            , GfacExperimentState.JOBSUBMITTED));
                    processContext.setJobModel(jobModel);
                    if (verifyJobSubmissionByJobId(remoteCluster, jobId)) {
//                        publisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
//                                , GfacExperimentState.JOBSUBMITTED));
                        GFacUtils.saveJobStatus(taskContext, jobModel, JobState.QUEUED);
                    }
                } else {
                    processContext.setJobModel(jobModel);
                    int verificationTryCount = 0;
                    while (verificationTryCount++ < 3) {
                        String verifyJobId = verifyJobSubmission(remoteCluster, jobModel);
                        if (verifyJobId != null && !verifyJobId.isEmpty()) {
                            // JobStatus either changed from SUBMITTED to QUEUED or directly to QUEUED
                            jobId = verifyJobId;
                            jobModel.setJobId(jobId);
//                            publisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
//                                    , GfacExperimentState.JOBSUBMITTED));
                            GFacUtils.saveJobStatus(taskContext, jobModel, JobState.QUEUED);
                            break;
                        }
                        Thread.sleep(verificationTryCount * 1000);
                    }
                }

                if (jobId == null || jobId.isEmpty()) {
                    String msg = "expId:" + processContext.getProcessModel().getExperimentId() + " Couldn't find remote jobId for JobName:"
                            + jobModel.getJobName() + ", both submit and verify steps doesn't return a valid JobId. Hence changing experiment state to Failed";
                    log.error(msg);
                    GFacUtils.saveErrorDetails(processContext, msg);
                    // FIXME : Need to handle according to status update chain
//                    GFacUtils.publishTaskStatus(jobExecutionContext, publisher, TaskState.FAILED);
                    return TaskState.FAILED;
                }
            }
            return TaskState.COMPLETED;
        } catch (AppCatalogException e) {
            log.error("Error while instatiating app catalog",e);
            throw new TaskException("Error while instatiating app catalog", e);
        } catch (ApplicationSettingsException e) {
            log.error("Error occurred while creating job descriptor", e);
            throw new TaskException("Error occurred while creating job descriptor", e);
        } catch (GFacException e) {
            log.error("Error occurred while creating job descriptor", e);
            throw new TaskException("Error occurred while creating job descriptor", e);
        } catch (SSHApiException e) {
            log.error("Error occurred while submitting the job", e);
            throw new TaskException("Error occurred while submitting the job", e);
        } catch (IOException e) {
            log.error("Error while reading the content of the job file", e);
            throw new TaskException("Error while reading the content of the job file", e);
        } catch (InterruptedException e) {
            log.error("Error occurred while verifying the job submission", e);
            throw new TaskException("Error occurred while verifying the job submission", e);
        }
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
    public TaskState recover(TaskContext taskContext) throws TaskException {
            ProcessContext processContext = taskContext.getParentProcessContext();
            JobModel jobModel = processContext.getJobModel();
            // original job failed before submitting
            if (jobModel == null || jobModel.getJobId() == null ){
                return execute(taskContext);
            }else {
                // job is already submitted and monitor should handle the recovery
                return TaskState.COMPLETED;
            }
    }

	@Override
	public TaskTypes getType() {
		return TaskTypes.JOB_SUBMISSION;
	}
}
