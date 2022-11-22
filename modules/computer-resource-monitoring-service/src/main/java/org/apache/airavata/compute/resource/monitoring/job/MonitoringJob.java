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
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
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
            LOGGER.debug("Executing ComputeResources Monitoring Job....... ");

            client = this.registryClientPool.getResource();


            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String metaSchedulerGateway = jobDataMap.getString(Constants.METASCHEDULER_GATEWAY);
            String metaSchedulerGRP = jobDataMap.getString(Constants.METASCHEDULER_GRP_ID);
            String username = jobDataMap.getString(Constants.METASCHEDULER_USERNAME);
            int jobId = jobDataMap.getInt(Constants.METASCHEDULER_SCANNING_JOB_ID);
            int parallelJobs = jobDataMap.getInt(Constants.METASCHEDULER_SCANNING_JOBS);

            LOGGER.debug("Main Gateway:"+metaSchedulerGateway+" Group Resource Profile: "
                    +metaSchedulerGRP+" username: "+username+" jobId: "+jobId+" parallellJobs: "+parallelJobs);

            executeComputeResourceMonitoring(client, metaSchedulerGateway, username, metaSchedulerGRP, parallelJobs, jobId);


        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
        } finally {
            if (client != null) {
                registryClientPool.returnResource(client);
            }
        }


    }

    private void executeComputeResourceMonitoring(RegistryService.Client client, String metaSchedulerGateway, String username,
                                                  String metaSchedulerGRP, int parallelJobs, int jobId) throws Exception {
        AdaptorSupportImpl adaptorSupport = AdaptorSupportImpl.getInstance();
        GroupResourceProfile groupResourceProfile = getGroupResourceProfile(metaSchedulerGRP);
//        List<GroupComputeResourcePreference> computeResourcePreferenceList = groupResourceProfile.getComputePreferences();


        int size = groupResourceProfile.getComputeResourcePoliciesSize();

        int chunkSize = size / parallelJobs;

        int startIndex = jobId * chunkSize;

        int endIndex = (jobId + 1) * chunkSize;

        if (jobId == parallelJobs - 1) {
            endIndex = size;
        }

        List<ComputeResourcePolicy> computeResourcePolicyList = groupResourceProfile.getComputeResourcePolicies().
                subList(startIndex, endIndex);

        for (ComputeResourcePolicy computeResourcePolicy : computeResourcePolicyList) {
            updateComputeResource(client, adaptorSupport, metaSchedulerGateway, username, computeResourcePolicy);
        }
    }


    private void updateComputeResource(RegistryService.Client client, AdaptorSupport adaptorSupport,
                                       String gatewayId,
                                       String username,
                                       ComputeResourcePolicy computeResourcePolicy) throws Exception {
        String computeResourceId = computeResourcePolicy.getComputeResourceId();
        ComputeResourceDescription comResourceDes = client.getComputeResource(computeResourceId);
        List<JobSubmissionInterface> jobSubmissionInterfaces = comResourceDes.getJobSubmissionInterfaces();
        Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
        JobSubmissionInterface jobSubmissionInterface = jobSubmissionInterfaces.get(0);
        JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();

        ResourceJobManager resourceJobManager = JobFactory.getResourceJobManager(client, jobSubmissionProtocol, jobSubmissionInterface);

        //TODO: intial phase we are only supporting SLURM
        if (resourceJobManager.getResourceJobManagerType().name().equals("SLURM")) {
            String baseCommand = "sinfo";

            if (resourceJobManager.getJobManagerCommands().containsKey(JobManagerCommand.SHOW_CLUSTER_INFO)) {
                baseCommand = resourceJobManager.getJobManagerCommands().get(JobManagerCommand.SHOW_CLUSTER_INFO);
            }


            List<String> allowedBatchQueues = computeResourcePolicy.getAllowedBatchQueues();
            List<QueueStatusModel> queueStatusModels = new ArrayList<>();
            for (String queue : allowedBatchQueues) {

                String finalCommand = baseCommand + " -p " + queue;

                String computeResourceToken = getComputeResourceCredentialToken(
                        gatewayId,
                        username,
                        computeResourceId,
                        false,
                        true,
                        computeResourcePolicy.getGroupResourceProfileId());

                String loginUsername = getComputeResourceLoginUserName(gatewayId,
                        username,
                        computeResourceId,
                        false,
                        true,
                        computeResourcePolicy.getGroupResourceProfileId(),
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

                if (outputParser.isComputeResourceAvailable(commandOutput,Constants.JOB_SUBMISSION_PROTOCOL_SLURM)) {
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

                    runningJobs = outputParser.getNumberofJobs(runningJobsCommandOutput,Constants.JOB_SUBMISSION_PROTOCOL_SLURM);
                    pendingJobs = outputParser.getNumberofJobs(pendingJobsCommandOutput,Constants.JOB_SUBMISSION_PROTOCOL_SLURM);

                }

                QueueStatusModel queueStatusModel = new QueueStatusModel();
                queueStatusModel.setHostName(comResourceDes.getHostName());
                queueStatusModel.setQueueName(queue);
                queueStatusModel.setQueueUp(queueStatus);
                queueStatusModel.setRunningJobs(runningJobs);
                queueStatusModel.setQueuedJobs(pendingJobs);
                queueStatusModels.add(queueStatusModel);
                queueStatusModel.setTime(System.currentTimeMillis());
            }
            client.registerQueueStatuses(queueStatusModels);
        }
    }

}


