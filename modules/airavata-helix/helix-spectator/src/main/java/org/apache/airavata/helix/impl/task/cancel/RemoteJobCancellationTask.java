package org.apache.airavata.helix.impl.task.cancel;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.submission.config.JobFactory;
import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@TaskDef(name = "Remote Job Cancellation Task")
public class RemoteJobCancellationTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(RemoteJobCancellationTask.class);

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
    }

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        try {

            List<String> jobs = getJobsOfProcess(getProcessId());

            logger.info("Fetching jobs for process " + getProcessId());

            if (jobs == null || jobs.size() == 0) {
                return onSuccess("Can not find running jobs for process " + getProcessId());
            }

            logger.info("Found " + jobs.size() + " jobs for process");

            logger.info("Fetching job manager configuration for process " + getProcessId());

            JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                    getAppCatalog(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface()));

            for (String jobId : jobs) {

                try {
                    logger.info("Cancelling job " + jobId + " of process " + getProcessId());
                    RawCommandInfo cancelCommand = jobManagerConfiguration.getCancelCommand(jobId);

                    logger.info("Command to cancel the job " + jobId + " : " + cancelCommand.getRawCommand());

                    AgentAdaptor adaptor = taskHelper.getAdaptorSupport().fetchAdaptor(
                            getTaskContext().getGatewayId(),
                            getTaskContext().getComputeResourceId(),
                            getTaskContext().getJobSubmissionProtocol().name(),
                            getTaskContext().getComputeResourceCredentialToken(),
                            getTaskContext().getComputeResourceLoginUserName());

                    logger.info("Running cancel command on compute host");
                    CommandOutput commandOutput = adaptor.executeCommand(cancelCommand.getRawCommand(), null);

                    if (commandOutput.getExitCode() != 0) {
                        logger.error("Failed to execute job cancellation command for job " + jobId + " Sout : " +
                                commandOutput.getStdOut() + ", Serr : " + commandOutput.getStdError());
                        return onFail("Failed to execute job cancellation command for job " + jobId + " Sout : " +
                                commandOutput.getStdOut() + ", Serr : " + commandOutput.getStdError(), true, null);
                    }
                } catch (Exception ex) {
                    logger.error("Unknown error while canceling job " + jobId + " of process " + getProcessId());
                    return onFail("Unknown error while canceling job " + jobId + " of process " + getProcessId(), true, ex);
                }
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

    private List<String> getJobsOfProcess(String processId) throws Exception {
        String path = "/registry/" + processId + "/jobs";
        if (getCuratorClient().checkExists().forPath(path) != null) {
            return getCuratorClient().getChildren().forPath(path);
        } else {
            return null;
        }
    }
}
