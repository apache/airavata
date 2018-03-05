package org.apache.airavata.helix.impl.task.submission.task;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.JobSubmissionOutput;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.impl.task.submission.GroovyMapBuilder;
import org.apache.airavata.helix.impl.task.submission.GroovyMapData;
import org.apache.airavata.helix.impl.task.submission.SubmissionUtil;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;

import java.io.File;
import java.util.Arrays;

@TaskDef(name = "Fork Job Submission")
public class ForkJobSubmissionTask extends JobSubmissionTask {

    @Override
    public TaskResult onRun(TaskHelper taskHelper) {

        try {
            GroovyMapData mapData = new GroovyMapBuilder(getTaskContext()).build();

            JobModel jobModel = new JobModel();
            jobModel.setProcessId(getProcessId());
            jobModel.setWorkingDir(mapData.getWorkingDirectory());
            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            jobModel.setTaskId(getTaskId());
            jobModel.setJobName(mapData.getJobName());

            if (mapData != null) {
                //jobModel.setJobDescription(FileUtils.readFileToString(jobFile));
                AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                        getTaskContext().getGatewayId(),
                        getTaskContext().getComputeResourceId(),
                        getTaskContext().getJobSubmissionProtocol().name(),
                        getTaskContext().getComputeResourceCredentialToken(),
                        getTaskContext().getComputeResourceLoginUserName());

                JobSubmissionOutput submissionOutput = submitBatchJob(adaptor, mapData, mapData.getWorkingDirectory());

                jobModel.setExitCode(submissionOutput.getExitCode());
                jobModel.setStdErr(submissionOutput.getStdErr());
                jobModel.setStdOut(submissionOutput.getStdOut());

                String jobId = submissionOutput.getJobId();

                if (jobId != null && !jobId.isEmpty()) {
                    jobModel.setJobId(jobId);
                    saveJobModel(jobModel);
                    JobStatus jobStatus = new JobStatus();
                    jobStatus.setJobState(JobState.SUBMITTED);
                    jobStatus.setReason("Successfully Submitted to " + getComputeResourceDescription().getHostName());
                    jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    jobModel.setJobStatuses(Arrays.asList(jobStatus));
                    saveAndPublishJobStatus(jobModel);

                    return null;
                } else {
                    String msg = "expId:" + getExperimentId() + " Couldn't find remote jobId for JobName:" +
                            jobModel.getJobName() + ", both submit and verify steps doesn't return a valid JobId. " +
                            "Hence changing experiment state to Failed";
                }

            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCancel() {

    }
}
