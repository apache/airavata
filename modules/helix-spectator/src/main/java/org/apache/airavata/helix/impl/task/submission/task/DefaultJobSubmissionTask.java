package org.apache.airavata.helix.impl.task.submission.task;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.JobSubmissionOutput;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.impl.task.submission.GroovyMapBuilder;
import org.apache.airavata.helix.impl.task.submission.GroovyMapData;
import org.apache.airavata.helix.impl.task.submission.SubmissionUtil;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TaskDef(name = "Default Job Submission")
public class DefaultJobSubmissionTask extends JobSubmissionTask {

    private static final Logger logger = LogManager.getLogger(DefaultJobSubmissionTask.class);

    public static final String DEFAULT_JOB_ID = "DEFAULT_JOB_ID";

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
            jobModel.setJobDescription("Sample description");

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

                if (submissionOutput.getExitCode() != 0 || submissionOutput.isJobSubmissionFailed()) {
                    jobModel.setJobId(DEFAULT_JOB_ID);
                    if (submissionOutput.isJobSubmissionFailed()) {
                        List<JobStatus> statusList = new ArrayList<>();
                        statusList.add(new JobStatus(JobState.FAILED));
                        statusList.get(0).setReason(submissionOutput.getFailureReason());
                        jobModel.setJobStatuses(statusList);
                        saveJobModel(jobModel);
                        logger.error("expId: " + getExperimentId() + ", processid: " + getProcessId()+ ", taskId: " +
                                getTaskId() + " :- Job submission failed for job name " + jobModel.getJobName()
                                + ". Exit code : " + submissionOutput.getExitCode() + ", Submission failed : "
                                + submissionOutput.isJobSubmissionFailed());

                        ErrorModel errorModel = new ErrorModel();
                        errorModel.setUserFriendlyMessage(submissionOutput.getFailureReason());
                        errorModel.setActualErrorMessage(submissionOutput.getFailureReason());
                        saveExperimentError(errorModel);
                        saveProcessError(errorModel);
                        saveTaskError(errorModel);
                        //taskStatus.setState(TaskState.FAILED);
                        //taskStatus.setReason("Job submission command didn't return a jobId");
                        //taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                        //taskContext.setTaskStatus(taskStatus);
                        logger.error("Standard error message : " + submissionOutput.getStdErr());
                        logger.error("Standard out message : " + submissionOutput.getStdOut());
                        return onFail("Job submission command didn't return a jobId", false, null);

                    } else {
                        String msg;
                        saveJobModel(jobModel);
                        ErrorModel errorModel = new ErrorModel();
                        if (submissionOutput.getExitCode() != Integer.MIN_VALUE) {
                            msg = "expId:" + getExperimentId() + ", processId:" + getProcessId() + ", taskId: " + getTaskId() +
                                    " return non zero exit code:" + submissionOutput.getExitCode() + "  for JobName:" + jobModel.getJobName() +
                                    ", with failure reason : " + submissionOutput.getFailureReason()
                                    + " Hence changing job state to Failed." ;
                            errorModel.setActualErrorMessage(submissionOutput.getFailureReason());
                        } else {
                            msg = "expId:" + getExperimentId() + ", processId:" + getProcessId() + ", taskId: " + getTaskId() +
                                    " doesn't  return valid job submission exit code for JobName:" + jobModel.getJobName() +
                                    ", with failure reason : stdout ->" + submissionOutput.getStdOut() +
                                    " stderr -> " + submissionOutput.getStdErr() + " Hence changing job state to Failed." ;
                            errorModel.setActualErrorMessage(msg);
                        }
                        logger.error(msg);
                        errorModel.setUserFriendlyMessage(msg);
                        saveExperimentError(errorModel);
                        saveProcessError(errorModel);
                        saveTaskError(errorModel);
                        //taskStatus.setState(TaskState.FAILED);
                        //taskStatus.setReason(msg);
                        //taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                        //taskContext.setTaskStatus(taskStatus);
                        return onFail(msg, false, null);
                    }

                    //TODO save task status??
                } else if (jobId != null && !jobId.isEmpty()) {
                    logger.info("Received job id " + jobId + " from compute resource");
                    jobModel.setJobId(jobId);
                    saveJobModel(jobModel);

                    JobStatus jobStatus = new JobStatus();
                    jobStatus.setJobState(JobState.SUBMITTED);
                    jobStatus.setReason("Successfully Submitted to " + getComputeResourceDescription().getHostName());
                    jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    jobModel.setJobStatuses(Arrays.asList(jobStatus));
                    saveJobStatus(jobModel);

                    if (verifyJobSubmissionByJobId(adaptor, jobId)) {
                        jobStatus.setJobState(JobState.QUEUED);
                        jobStatus.setReason("Verification step succeeded");
                        jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                        jobModel.setJobStatuses(Arrays.asList(jobStatus));
                        saveJobStatus(jobModel);
                        createMonitoringNode(jobId);
                    }

                    if (getComputeResourceDescription().isGatewayUsageReporting()){
                        String loadCommand = getComputeResourceDescription().getGatewayUsageModuleLoadCommand();
                        String usageExecutable = getComputeResourceDescription().getGatewayUsageExecutable();
                        ExperimentModel experiment = (ExperimentModel)getExperimentCatalog().get(ExperimentCatalogModelType.EXPERIMENT, getExperimentId());
                        String username = experiment.getUserName() + "@" + getTaskContext().getGatewayComputeResourcePreference().getUsageReportingGatewayId();
                        RawCommandInfo rawCommandInfo = new RawCommandInfo(loadCommand + " && " + usageExecutable + " -gateway_user " +  username  +
                                " -submit_time \"`date '+%F %T %:z'`\"  -jobid " + jobId );
                        adaptor.executeCommand(rawCommandInfo.getRawCommand(), null);
                    }
                    //taskStatus = new TaskStatus(TaskState.COMPLETED);
                    //taskStatus.setReason("Submitted job to compute resource");
                    //taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

                    return onSuccess("Submitted job to compute resource");
                } else {
                    int verificationTryCount = 0;
                    while (verificationTryCount++ < 3) {
                        String verifyJobId = verifyJobSubmission(adaptor, jobModel.getJobName(), getTaskContext().getComputeResourceLoginUserName());
                        if (verifyJobId != null && !verifyJobId.isEmpty()) {
                            // JobStatus either changed from SUBMITTED to QUEUED or directly to QUEUED
                            jobId = verifyJobId;
                            jobModel.setJobId(jobId);
                            saveJobModel(jobModel);
                            JobStatus jobStatus = new JobStatus();
                            jobStatus.setJobState(JobState.QUEUED);
                            jobStatus.setReason("Verification step succeeded");
                            jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                            jobModel.setJobStatuses(Arrays.asList(jobStatus));
                            saveJobStatus(jobModel);
                            //taskStatus.setState(TaskState.COMPLETED);
                            //taskStatus.setReason("Submitted job to compute resource");
                            //taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                            break;
                        }
                        logger.info("Verify step return invalid jobId, retry verification step in " + (verificationTryCount * 10) + " secs");
                        Thread.sleep(verificationTryCount * 10000);
                    }
                }

                if (jobId == null || jobId.isEmpty()) {
                    jobModel.setJobId(DEFAULT_JOB_ID);
                    saveJobModel(jobModel);
                    String msg = "expId:" + getExperimentId() + " Couldn't find " +
                            "remote jobId for JobName:" + jobModel.getJobName() + ", both submit and verify steps " +
                            "doesn't return a valid JobId. " + "Hence changing experiment state to Failed";
                    logger.error(msg);
                    ErrorModel errorModel = new ErrorModel();
                    errorModel.setUserFriendlyMessage(msg);
                    errorModel.setActualErrorMessage(msg);
                    saveExperimentError(errorModel);
                    saveProcessError(errorModel);
                    saveTaskError(errorModel);
                    //taskStatus.setState(TaskState.FAILED);
                    //taskStatus.setReason("Couldn't find job id in both submitted and verified steps");
                    //taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    return onFail("Couldn't find job id in both submitted and verified steps", false, null);
                } else {
                    //GFacUtils.saveJobModel(processContext, jobModel);
                }

            }  else {
                return onFail("Job data is null", true, null);
                //  taskStatus.setReason("JobFile is null");
                //taskStatus.setState(TaskState.FAILED);
            }
        } catch (Exception e) {
            return onFail("Task failed due to unexpected issue", false, e);
        }
        // TODO get rid of this
        return onFail("Task moved to an unknown state", false, null);
    }

    private boolean verifyJobSubmissionByJobId(AgentAdaptor agentAdaptor, String jobID) throws Exception {
        JobStatus status = getJobStatus(agentAdaptor, jobID);
        return status != null &&  status.getJobState() != JobState.UNKNOWN;
    }

    private String verifyJobSubmission(AgentAdaptor agentAdaptor, String jobName, String userName) {
        String jobId = null;
        try {
            jobId  = getJobIdByJobName(agentAdaptor, jobName, userName);
        } catch (Exception e) {
            logger.error("Error while verifying JobId from JobName " + jobName);
        }
        return jobId;
    }

    @Override
    public void onCancel() {

    }
}
