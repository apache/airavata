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
package org.apache.airavata.activities.monitoring.compute;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.apache.airavata.activities.shared.ScheduledActivityInput;
import org.apache.airavata.agents.api.AdaptorSupport;
import org.apache.airavata.common.model.ComputeResourcePolicy;
import org.apache.airavata.common.model.JobManagerCommand;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.monitor.compute.ComputeMonitorConstants;
import org.apache.airavata.monitor.compute.ComputeResourceMonitor;
import org.apache.airavata.monitor.compute.OutputParserImpl;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.task.submission.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activity for compute resource monitoring.
 */
public class ComputeMonitorActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(ComputeMonitorActivity.class);

    @Override
    public Void run(WorkflowActivityContext ctx) {
        var input = ctx.getInput(ScheduledActivityInput.class);
        logger.debug("ComputeMonitorActivity for jobId {}", input.jobId());

        var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
        var adaptorSupport = WorkflowRuntimeHolder.getBean(AdaptorSupport.class);

        try {
            // Note: These properties are not in AiravataServerProperties yet, using defaults
            // Add to AiravataServerProperties if needed
            final String metaUsername = ""; // properties.getMetascheduler().getUsername() when added
            final String metaGatewayId = ""; // properties.getMetascheduler().getGateway() when added
            final String metaGroupResourceProfileId = ""; // properties.getMetascheduler().getGrpId() when added

            logger.debug(
                    "Main Gateway: {} Group Resource Profile: {} username: {} jobId: {} parallelJobs: {}",
                    metaGatewayId,
                    metaGroupResourceProfileId,
                    metaUsername,
                    input.jobId(),
                    input.parallelJobs());

            executeComputeResourceMonitoring(
                    registryService,
                    adaptorSupport,
                    metaGatewayId,
                    metaUsername,
                    metaGroupResourceProfileId,
                    input.parallelJobs(),
                    input.jobId());

            logger.debug("ComputeMonitorActivity completed for jobId {}", input.jobId());
            return null;
        } catch (Exception ex) {
            logger.error("Error in ComputeMonitorActivity for jobId {}: {}", input.jobId(), ex.getMessage(), ex);
            throw new RuntimeException("ComputeMonitorActivity failed", ex);
        }
    }

    private void executeComputeResourceMonitoring(
            RegistryService registryService,
            AdaptorSupport adaptorSupport,
            String metaSchedulerGateway,
            String username,
            String metaSchedulerGRP,
            int parallelJobs,
            int jobId)
            throws Exception {
        // Create a concrete implementation of ComputeResourceMonitor
        var monitor = new ComputeResourceMonitor(registryService) {};
        var groupResourceProfile = monitor.getGroupResourceProfile(metaSchedulerGRP);

        int size = groupResourceProfile.getComputeResourcePolicies().size();
        int chunkSize = size / parallelJobs;
        int startIndex = jobId * chunkSize;
        int endIndex = (jobId + 1) * chunkSize;

        if (jobId == parallelJobs - 1) {
            endIndex = size;
        }

        var computeResourcePolicyList =
                groupResourceProfile.getComputeResourcePolicies().subList(startIndex, endIndex);

        for (var computeResourcePolicy : computeResourcePolicyList) {
            updateComputeResource(
                    registryService, adaptorSupport, monitor, metaSchedulerGateway, username, computeResourcePolicy);
        }
    }

    private void updateComputeResource(
            RegistryService registryService,
            AdaptorSupport adaptorSupport,
            ComputeResourceMonitor monitor,
            String gatewayId,
            String username,
            ComputeResourcePolicy computeResourcePolicy)
            throws Exception {
        var computeResourceId = computeResourcePolicy.getComputeResourceId();
        var comResourceDes = registryService.getComputeResource(computeResourceId);
        var jobSubmissionInterfaces = comResourceDes.getJobSubmissionInterfaces();
        Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
        var jobSubmissionInterface = jobSubmissionInterfaces.get(0);
        var jobSubmissionProtocol = jobSubmissionInterface.getJobSubmissionProtocol();

        var resourceJobManager =
                JobFactory.getResourceJobManager(registryService, jobSubmissionProtocol, jobSubmissionInterface);

        // Currently supporting SLURM only
        if (resourceJobManager.getResourceJobManagerType().name().equals("SLURM")) {
            String baseCommand = "sinfo";

            if (resourceJobManager.getJobManagerCommands().containsKey(JobManagerCommand.SHOW_CLUSTER_INFO)) {
                baseCommand = resourceJobManager.getJobManagerCommands().get(JobManagerCommand.SHOW_CLUSTER_INFO);
            }

            var allowedBatchQueues = computeResourcePolicy.getAllowedBatchQueues();
            var queueStatusModels = new ArrayList<QueueStatusModel>();
            for (var queue : allowedBatchQueues) {

                var finalCommand = baseCommand + " -p " + queue;

                var computeResourceToken = monitor.getComputeResourceCredentialToken(
                        gatewayId,
                        username,
                        computeResourceId,
                        false,
                        true,
                        computeResourcePolicy.getGroupResourceProfileId());

                var loginUsername = monitor.getComputeResourceLoginUserName(
                        gatewayId,
                        username,
                        computeResourceId,
                        false,
                        true,
                        computeResourcePolicy.getGroupResourceProfileId(),
                        null);

                var adaptor = adaptorSupport.fetchAdaptor(
                        gatewayId, computeResourceId, jobSubmissionProtocol, computeResourceToken, loginUsername);

                var commandOutput = adaptor.executeCommand(finalCommand, null);

                var outputParser = new OutputParserImpl();
                boolean queueStatus = false;
                int runningJobs = 0;
                int pendingJobs = 0;

                if (outputParser.isComputeResourceAvailable(
                        commandOutput, ComputeMonitorConstants.JOB_SUBMISSION_PROTOCOL_SLURM)) {
                    queueStatus = true;

                    var runningJobCommand = "squeue";
                    var pendingJobCommand = "squeue";
                    if (resourceJobManager
                            .getJobManagerCommands()
                            .containsKey(JobManagerCommand.SHOW_NO_OF_RUNNING_JOBS)) {
                        runningJobCommand = resourceJobManager
                                .getJobManagerCommands()
                                .get(JobManagerCommand.SHOW_NO_OF_RUNNING_JOBS);
                    }

                    if (resourceJobManager
                            .getJobManagerCommands()
                            .containsKey(JobManagerCommand.SHOW_NO_OF_PENDING_JOBS)) {
                        pendingJobCommand = resourceJobManager
                                .getJobManagerCommands()
                                .get(JobManagerCommand.SHOW_NO_OF_PENDING_JOBS);
                    }

                    var runningJobsCommand = runningJobCommand + "-h -t running -r | wc -l";
                    var pendingJobsCommand = pendingJobCommand + "-h -t pending -r | wc -l";

                    var runningJobsCommandOutput = adaptor.executeCommand(runningJobsCommand, null);
                    var pendingJobsCommandOutput = adaptor.executeCommand(pendingJobsCommand, null);

                    runningJobs = outputParser.getNumberofJobs(
                            runningJobsCommandOutput, ComputeMonitorConstants.JOB_SUBMISSION_PROTOCOL_SLURM);
                    pendingJobs = outputParser.getNumberofJobs(
                            pendingJobsCommandOutput, ComputeMonitorConstants.JOB_SUBMISSION_PROTOCOL_SLURM);
                }

                var queueStatusModel = new QueueStatusModel();
                queueStatusModel.setHostName(comResourceDes.getHostName());
                queueStatusModel.setQueueName(queue);
                queueStatusModel.setQueueUp(queueStatus);
                queueStatusModel.setRunningJobs(runningJobs);
                queueStatusModel.setQueuedJobs(pendingJobs);
                queueStatusModel.setTime(org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp()
                        .getTime());
                queueStatusModels.add(queueStatusModel);
            }
            registryService.registerQueueStatuses(queueStatusModels);
        }
    }
}
