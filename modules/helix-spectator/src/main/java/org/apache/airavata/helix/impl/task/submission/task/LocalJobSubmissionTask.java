package org.apache.airavata.helix.impl.task.submission.task;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.JobSubmissionOutput;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.impl.task.submission.GroovyMapBuilder;
import org.apache.airavata.helix.impl.task.submission.GroovyMapData;
import org.apache.airavata.helix.impl.task.submission.SubmissionUtil;
import org.apache.airavata.helix.impl.task.submission.task.JobSubmissionTask;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

@TaskDef(name = "Local Job Submission")
public class LocalJobSubmissionTask extends JobSubmissionTask {

    @Override
    public TaskResult onRun(TaskHelper taskHelper) {

        try {
            GroovyMapData groovyMapData = new GroovyMapData();
            String jobId = "JOB_ID_" + UUID.randomUUID().toString();

            JobModel jobModel = new JobModel();
            jobModel.setProcessId(getProcessId());
            jobModel.setWorkingDir(groovyMapData.getWorkingDirectory());
            jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            jobModel.setTaskId(getTaskId());
            jobModel.setJobId(jobId);

            File jobFile = SubmissionUtil.createJobFile(groovyMapData);

            if (jobFile != null && jobFile.exists()) {
                jobModel.setJobDescription(FileUtils.readFileToString(jobFile));
                saveJobModel(jobModel);

                AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                        getTaskContext().getComputeResourceId(),
                        getTaskContext().getJobSubmissionProtocol().name(),
                        getTaskContext().getComputeResourceCredentialToken());

                GroovyMapData mapData = new GroovyMapBuilder(getTaskContext()).build();
                JobSubmissionOutput submissionOutput = submitBatchJob(adaptor, mapData, groovyMapData.getWorkingDirectory());

                JobStatus jobStatus = new JobStatus();
                jobStatus.setJobState(JobState.SUBMITTED);
                jobStatus.setReason("Successfully Submitted to " + getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));

                saveJobStatus(jobModel);

                jobModel.setExitCode(submissionOutput.getExitCode());
                jobModel.setStdErr(submissionOutput.getStdErr());
                jobModel.setStdOut(submissionOutput.getStdOut());

                jobStatus.setJobState(JobState.COMPLETE);
                jobStatus.setReason("Successfully Completed " + getComputeResourceDescription().getHostName());
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                jobModel.setJobStatuses(Arrays.asList(jobStatus));

                saveJobStatus(jobModel);

                return null;
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
