package org.apache.airavata.helix.impl.task.cancel;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.submission.config.JobFactory;
import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TaskDef(name = "Remote Job Cancellation Task")
public class RemoteJobCancellationTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(RemoteJobCancellationTask.class);

    public static final String JOB_ALREADY_CANCELLED_OR_NOT_AVAILABLE = "job-already-cancelled";

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
    }

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        try {

            List<JobModel> jobs = getRegistryServiceClient().getJobs("processId", getProcessId());

            logger.info("Fetching jobs for process " + getProcessId());

            if (jobs == null || jobs.size() == 0) {
                setContextVariable(JOB_ALREADY_CANCELLED_OR_NOT_AVAILABLE, "true");
                return onSuccess("Can not find running jobs for process " + getProcessId());
            }

            logger.info("Found " + jobs.size() + " jobs for process");

            logger.info("Fetching job manager configuration for process " + getProcessId());

            JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(
                    JobFactory.getResourceJobManager(
                            getRegistryServiceClient(),
                            getTaskContext().getJobSubmissionProtocol(),
                            getTaskContext().getPreferredJobSubmissionInterface()));

            AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                    getTaskContext().getGatewayId(),
                    getTaskContext().getComputeResourceId(),
                    getTaskContext().getJobSubmissionProtocol(),
                    getTaskContext().getComputeResourceCredentialToken(),
                    getTaskContext().getComputeResourceLoginUserName());

            for (JobModel job : jobs) {

                try {
                    logger.info("Fetching current job status for job id " + job.getJobId());
                    RawCommandInfo monitorCommand = jobManagerConfiguration.getMonitorCommand(job.getJobId());

                    CommandOutput jobMonitorOutput = adaptor.executeCommand(monitorCommand.getRawCommand(), null);

                    if (jobMonitorOutput.getExitCode() == 0) {
                        JobStatus jobStatus = jobManagerConfiguration.getParser().parseJobStatus(job.getJobId(), jobMonitorOutput.getStdOut());
                        if (jobStatus != null) {
                            logger.info("Job " + job.getJobId() + " state is " + jobStatus.getJobState().name());
                            switch (jobStatus.getJobState()) {
                                case COMPLETE:
                                case CANCELED:
                                case SUSPENDED:
                                case FAILED:
                                    // if the job already is in above states, there is no use of trying cancellation
                                    // setting context variable to be used in the Cancel Completing Task
                                    setContextVariable(JOB_ALREADY_CANCELLED_OR_NOT_AVAILABLE, "true");
                                    return onSuccess("Job already is in a saturated state");
                            }
                        } else {
                            logger.warn("Job status for job " + job.getJobId() + " is null. Std out " + jobMonitorOutput.getStdOut() +
                                    ". Std err " + jobMonitorOutput.getStdError() + ". Job monitor command " + monitorCommand.getRawCommand());
                        }
                    } else {
                        logger.warn("Error while fetching the job " + job.getJobId() + " status. Std out " + jobMonitorOutput.getStdOut() +
                                ". Std err " + jobMonitorOutput.getStdError() + ". Job monitor command " + monitorCommand.getRawCommand());
                    }
                } catch (Exception e) {
                    logger.error("Unknown error while fetching the job status but continuing..", e);
                }

                try {
                    logger.info("Cancelling job " + job.getJobId() + " of process " + getProcessId());
                    RawCommandInfo cancelCommand = jobManagerConfiguration.getCancelCommand(job.getJobId());

                    logger.info("Command to cancel the job " + job.getJobId() + " : " + cancelCommand.getRawCommand());

                    logger.info("Running cancel command on compute host");
                    CommandOutput jobCancelOutput = adaptor.executeCommand(cancelCommand.getRawCommand(), null);

                    if (jobCancelOutput.getExitCode() != 0) {
                        logger.warn("Failed to execute job cancellation command for job " + job.getJobId() + " Sout : " +
                                jobCancelOutput.getStdOut() + ", Serr : " + jobCancelOutput.getStdError());
                        //return onFail("Failed to execute job cancellation command for job " + jobId + " Sout : " +
                        //        jobCancelOutput.getStdOut() + ", Serr : " + jobCancelOutput.getStdError(), true, null);
                    }
                } catch (Exception ex) {
                    logger.error("Unknown error while canceling job " + job.getJobId() + " of process " + getProcessId());
                    return onFail("Unknown error while canceling job " + job.getJobId() + " of process " + getProcessId(), true, ex);
                }

                // TODO this is temporary fix. Remove this line when the schedulers are configured to notify when an job is externally cancelled
                // forcefully make the job state as cancelled as some schedulers do not notify when the job is cancelled.
                saveAndPublishJobStatus(job.getJobId(), job.getTaskId(), getProcessId(), getExperimentId(), getGatewayId(), JobState.CANCELED);
            }

            logger.info("Successfully completed job cancellation task");
            return onSuccess("Successfully completed job cancellation task");

        } catch (Exception e) {
            logger.error("Unknown error while canceling jobs of process " + getProcessId());
            return onFail("Unknown error while canceling jobs of process " + getProcessId(), true, e);
        }

    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
