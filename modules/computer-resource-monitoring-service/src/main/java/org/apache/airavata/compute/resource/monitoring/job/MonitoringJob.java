package org.apache.airavata.compute.resource.monitoring.job;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.compute.resource.monitoring.job.output.OutputParser;
import org.apache.airavata.compute.resource.monitoring.job.output.OutputParserImpl;
import org.apache.airavata.compute.resource.monitoring.utils.Constants;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.helix.impl.task.submission.config.JobFactory;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is responsible to execute CR monitoring code
 */
public class MonitoringJob extends ComputeResourceMonitor implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RegistryService.Client client = null;
        try {
            LOGGER.info("Executing ComputeResources ....... ");

            client = this.registryClientPool.getResource();

            AdaptorSupportImpl adaptorSupport = AdaptorSupportImpl.getInstance();

            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String metaSchedulerGateway = jobDataMap.getString(Constants.METASCHEDULER_GATEWAY);
            String metaSchedulerGRP = jobDataMap.getString(Constants.METASCHEDULER_GRP_ID);
            String username = jobDataMap.getString(Constants.METASCHEDULER_USERNAME);
            int jobId = jobDataMap.getInt(Constants.METASCHEDULER_SCANNING_JOB_ID);
            int parallelJobs = jobDataMap.getInt(Constants.METASCHEDULER_SCANNING_JOBS);


            GroupResourceProfile groupResourceProfile = getGroupResourceProfile(metaSchedulerGRP);
            List<GroupComputeResourcePreference> computeResourcePreferenceList = groupResourceProfile.getComputePreferences();

            int size = computeResourcePreferenceList.size();

            int chunkSize = size / parallelJobs;

            int startIndex = jobId * chunkSize;

            int endIndex = (jobId + 1) * chunkSize;

            if (jobId == parallelJobs - 1) {
                endIndex = size;
            }

            List<GroupComputeResourcePreference> computeResourcePreferences = computeResourcePreferenceList
                    .subList(startIndex, endIndex);

            for (GroupComputeResourcePreference computeResourcePreference : computeResourcePreferences) {
                LOGGER.info("updating GRP########### PRID:"+computeResourcePreference.getComputeResourceId());
                updateComputeResource(client, adaptorSupport, metaSchedulerGateway, username, metaSchedulerGRP, computeResourcePreference);
            }
        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
        } finally {
            if (client != null) {
                registryClientPool.returnResource(client);
            }
        }


    }


    private void updateComputeResource(RegistryService.Client client, AdaptorSupport adaptorSupport,
                                       String gatewayId,
                                       String username,
                                       String groupResourceProfileId,
                                       GroupComputeResourcePreference computeResourcePreference) throws Exception {
        String computeResourceId = computeResourcePreference.getComputeResourceId();
        ComputeResourceDescription comResourceDes = client.getComputeResource(computeResourceId);
        List<JobSubmissionInterface> jobSubmissionInterfaces = comResourceDes.getJobSubmissionInterfaces();
        Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
        JobSubmissionInterface jobSubmissionInterface = jobSubmissionInterfaces.get(0);
        JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();

        ResourceJobManager resourceJobManager = JobFactory.getResourceJobManager(client, jobSubmissionProtocol, jobSubmissionInterface);

        LOGGER.info(" type "+ resourceJobManager.getResourceJobManagerType()+" jobSubmissionProtocol "+jobSubmissionProtocol);
        //TODO: intial phase we are only supporting SLURM
        if (resourceJobManager.getResourceJobManagerType().equals("SLURM")) {
            String baseCommand = "sinfo";

            if (resourceJobManager.getJobManagerCommands().containsKey(JobManagerCommand.SHOW_CLUSTER_INFO)) {
                baseCommand = resourceJobManager.getJobManagerCommands().get(JobManagerCommand.SHOW_CLUSTER_INFO);
            }



            String finalCommand = baseCommand + "-p" + computeResourcePreference.getPreferredBatchQueue();

            String computeResourceToken = getComputeResourceCredentialToken(
                    gatewayId,
                    username,
                    computeResourceId,
                    false,
                    true,
                    groupResourceProfileId);

            String loginUsername = getComputeResourceLoginUserName(gatewayId,
                    username,
                    computeResourceId,
                    false,
                    true,
                    groupResourceProfileId,
                    null);

            AgentAdaptor adaptor = adaptorSupport.fetchAdaptor(gatewayId,
                    computeResourceId,
                    jobSubmissionProtocol,
                    computeResourceToken,
                    loginUsername);

            CommandOutput commandOutput = adaptor.executeCommand(finalCommand, null);

            OutputParser outputParser = new OutputParserImpl();
            boolean queueStatus = false;
            int runningJobs = 0;
            int pendingJobs = 0;
            LOGGER.info("command output"+commandOutput.getStdOut()+" error "+commandOutput.getStdError()+" exist code "+commandOutput.getExitCode());
            if (outputParser.isComputeResourceAvailable(commandOutput)) {
                queueStatus = true;

                String runningJobCommand = "squeue";
                String pendingJobCommand = "squeue";
                if (resourceJobManager.getJobManagerCommands().containsKey(JobManagerCommand.SHOW_NO_OF_RUNNING_JOBS)) {
                    runningJobCommand = resourceJobManager.getJobManagerCommands().get(JobManagerCommand.SHOW_NO_OF_RUNNING_JOBS);
                }

                if (resourceJobManager.getJobManagerCommands().containsKey(JobManagerCommand.SHOW_NO_OF_PENDING_JOBS)) {
                    pendingJobCommand = resourceJobManager.getJobManagerCommands().get(JobManagerCommand.SHOW_NO_OF_PENDING_JOBS);
                }

                String runningJobsCommand = runningJobCommand + "-h -t running -r | wc -l";
                String pendingJobsCommand = pendingJobCommand + "-h -t pending -r | wc -l";

                CommandOutput runningJobsCommandOutput = adaptor.executeCommand(runningJobsCommand, null);

                CommandOutput pendingJobsCommandOutput = adaptor.executeCommand(pendingJobsCommand, null);

                runningJobs = outputParser.getNumberofJobs(runningJobsCommandOutput);
                pendingJobs = outputParser.getNumberofJobs(pendingJobsCommandOutput);

            }

            QueueStatusModel queueStatusModel = new QueueStatusModel();
            queueStatusModel.setHostName(comResourceDes.getHostName());
            queueStatusModel.setQueueName(computeResourcePreference.getPreferredBatchQueue());
            LOGGER.info("Storing hostname "+comResourceDes.getHostName()+" batch queue "+computeResourcePreference.getPreferredBatchQueue());
            queueStatusModel.setQueueUp(queueStatus);
            queueStatusModel.setRunningJobs(runningJobs);
            queueStatusModel.setQueuedJobs(pendingJobs);
            List<QueueStatusModel> queueStatusModels = new ArrayList<>();
            queueStatusModels.add(queueStatusModel);

            client.registerQueueStatuses(queueStatusModels);

        }
    }
}
