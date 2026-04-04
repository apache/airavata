/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.interfaces.CommandOutput;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.task.AdaptorSupport;
import org.apache.airavata.task.AdaptorSupportImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible to execute CR monitoring code
 */
public class MonitoringJob extends ComputeResourceMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringJob.class);

    private final String metaSchedulerGateway;
    private final String metaSchedulerGRP;
    private final String username;
    private final int jobId;
    private final int parallelJobs;

    public MonitoringJob(
            String metaSchedulerGateway, String metaSchedulerGRP, String username, int parallelJobs, int jobId) {
        this.metaSchedulerGateway = metaSchedulerGateway;
        this.metaSchedulerGRP = metaSchedulerGRP;
        this.username = username;
        this.parallelJobs = parallelJobs;
        this.jobId = jobId;
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("Executing ComputeResources Monitoring Job....... ");

            RegistryHandler client = this.registryHandler;

            LOGGER.debug("Main Gateway:" + metaSchedulerGateway + " Group Resource Profile: " + metaSchedulerGRP
                    + " username: " + username + " jobId: " + jobId + " parallellJobs: " + parallelJobs);

            executeComputeResourceMonitoring(
                    client, metaSchedulerGateway, username, metaSchedulerGRP, parallelJobs, jobId);

        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
        }
    }

    private void executeComputeResourceMonitoring(
            RegistryHandler client,
            String metaSchedulerGateway,
            String username,
            String metaSchedulerGRP,
            int parallelJobs,
            int jobId)
            throws Exception {
        AdaptorSupportImpl adaptorSupport = AdaptorSupportImpl.getInstance();
        GroupResourceProfile groupResourceProfile = getGroupResourceProfile(metaSchedulerGRP);
        //        List<GroupComputeResourcePreference> computeResourcePreferenceList =
        // groupResourceProfile.getComputePreferences();

        int size = groupResourceProfile.getComputeResourcePoliciesCount();

        int chunkSize = size / parallelJobs;

        int startIndex = jobId * chunkSize;

        int endIndex = (jobId + 1) * chunkSize;

        if (jobId == parallelJobs - 1) {
            endIndex = size;
        }

        List<ComputeResourcePolicy> computeResourcePolicyList =
                groupResourceProfile.getComputeResourcePoliciesList().subList(startIndex, endIndex);

        for (ComputeResourcePolicy computeResourcePolicy : computeResourcePolicyList) {
            updateComputeResource(client, adaptorSupport, metaSchedulerGateway, username, computeResourcePolicy);
        }
    }

    private void updateComputeResource(
            RegistryHandler client,
            AdaptorSupport adaptorSupport,
            String gatewayId,
            String username,
            ComputeResourcePolicy computeResourcePolicy)
            throws Exception {
        String computeResourceId = computeResourcePolicy.getComputeResourceId();
        ComputeResourceDescription comResourceDes = client.getComputeResource(computeResourceId);
        List<JobSubmissionInterface> jobSubmissionInterfaces = comResourceDes.getJobSubmissionInterfacesList();
        Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
        JobSubmissionInterface jobSubmissionInterface = jobSubmissionInterfaces.get(0);
        JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();

        ResourceJobManager resourceJobManager =
                JobFactoryHelper.getResourceJobManager(client, jobSubmissionProtocol, jobSubmissionInterface);

        // TODO: intial phase we are only supporting SLURM
        if (resourceJobManager.getResourceJobManagerType().name().equals("SLURM")) {
            String baseCommand = "sinfo";

            if (resourceJobManager.getJobManagerCommandsMap().containsKey(JobManagerCommand.SHOW_CLUSTER_INFO)) {
                baseCommand = resourceJobManager.getJobManagerCommandsMap().get(JobManagerCommand.SHOW_CLUSTER_INFO);
            }

            List<String> allowedBatchQueues = computeResourcePolicy.getAllowedBatchQueuesList();
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

                String loginUsername = getComputeResourceLoginUserName(
                        gatewayId,
                        username,
                        computeResourceId,
                        false,
                        true,
                        computeResourcePolicy.getGroupResourceProfileId(),
                        null);

                AgentAdaptor adaptor = adaptorSupport.fetchAdaptor(
                        gatewayId, computeResourceId, jobSubmissionProtocol, computeResourceToken, loginUsername);

                CommandOutput commandOutput = adaptor.executeCommand(finalCommand, null);

                OutputParser outputParser = new OutputParserImpl();
                boolean queueStatus = false;
                int runningJobs = 0;
                int pendingJobs = 0;

                if (outputParser.isComputeResourceAvailable(
                        commandOutput, MonitorConstants.JOB_SUBMISSION_PROTOCOL_SLURM)) {
                    queueStatus = true;

                    String runningJobCommand = "squeue";
                    String pendingJobCommand = "squeue";
                    if (resourceJobManager
                            .getJobManagerCommandsMap()
                            .containsKey(JobManagerCommand.SHOW_NO_OF_RUNNING_JOBS)) {
                        runningJobCommand = resourceJobManager
                                .getJobManagerCommandsMap()
                                .get(JobManagerCommand.SHOW_NO_OF_RUNNING_JOBS);
                    }

                    if (resourceJobManager
                            .getJobManagerCommandsMap()
                            .containsKey(JobManagerCommand.SHOW_NO_OF_PENDING_JOBS)) {
                        pendingJobCommand = resourceJobManager
                                .getJobManagerCommandsMap()
                                .get(JobManagerCommand.SHOW_NO_OF_PENDING_JOBS);
                    }

                    String runningJobsCommand = runningJobCommand + "-h -t running -r | wc -l";
                    String pendingJobsCommand = pendingJobCommand + "-h -t pending -r | wc -l";

                    CommandOutput runningJobsCommandOutput = adaptor.executeCommand(runningJobsCommand, null);

                    CommandOutput pendingJobsCommandOutput = adaptor.executeCommand(pendingJobsCommand, null);

                    runningJobs = outputParser.getNumberofJobs(
                            runningJobsCommandOutput, MonitorConstants.JOB_SUBMISSION_PROTOCOL_SLURM);
                    pendingJobs = outputParser.getNumberofJobs(
                            pendingJobsCommandOutput, MonitorConstants.JOB_SUBMISSION_PROTOCOL_SLURM);
                }

                QueueStatusModel queueStatusModel = QueueStatusModel.newBuilder()
                        .setHostName(comResourceDes.getHostName())
                        .setQueueName(queue)
                        .setQueueUp(queueStatus)
                        .setRunningJobs(runningJobs)
                        .setQueuedJobs(pendingJobs)
                        .setTime(System.currentTimeMillis())
                        .build();
                queueStatusModels.add(queueStatusModel);
            }
            client.registerQueueStatuses(queueStatusModels);
        }
    }
}
