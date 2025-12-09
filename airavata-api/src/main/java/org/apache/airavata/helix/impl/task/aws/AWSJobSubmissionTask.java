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
package org.apache.airavata.helix.impl.task.aws;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.adaptor.SSHJAgentAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.aws.utils.AWSTaskUtil;
import org.apache.airavata.helix.impl.task.aws.utils.ExponentialBackoffWaiter;
import org.apache.airavata.helix.impl.task.submission.JobSubmissionTask;
import org.apache.airavata.helix.impl.task.submission.config.GroovyMapData;
import org.apache.airavata.helix.impl.task.submission.config.JobFactory;
import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.appcatalog.groupresourceprofile.AwsComputeResourcePreference;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;

@TaskDef(name = "AWS_JOB_SUBMISSION_TASK")
public class AWSJobSubmissionTask extends JobSubmissionTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSJobSubmissionTask.class);
    private static ApplicationContext applicationContext;

    public AWSJobSubmissionTask(
            ApplicationContext applicationContext,
            org.apache.airavata.service.RegistryService registryService,
            org.apache.airavata.service.UserProfileService userProfileService,
            CredentialStoreService credentialStoreService,
            org.apache.airavata.helix.impl.task.submission.config.GroovyMapBuilder groovyMapBuilder) {
        super(applicationContext, registryService, userProfileService, credentialStoreService, groovyMapBuilder);
        AWSJobSubmissionTask.applicationContext = applicationContext;
    }

    private static final int WAIT_MAX_RETRIES = 10;
    private static final long INITIAL_DELAY_SECONDS = 5;
    private static final long MAX_DELAY_SECONDS = 20;

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        LOGGER.info("Starting AWS Job Submission Task for process {}", getProcessId());

        try {
            AWSProcessContextManager awsContext = new AWSProcessContextManager(registryService, getTaskContext());
            AwsComputeResourcePreference awsPrefs = getTaskContext()
                    .getGroupComputeResourcePreference()
                    .getSpecificPreferences()
                    .getAws();
            String instanceId = awsContext.getInstanceId();
            String sshCredentialToken = awsContext.getSSHCredentialToken();
            String awsCredentialToken =
                    getTaskContext().getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken();

            if (instanceId == null || sshCredentialToken == null) {
                LOGGER.error(
                        "Could not find instanceId: {} or sshCredentialToken: {} in the AWS process context {}",
                        instanceId,
                        sshCredentialToken,
                        getProcessId());
                onFail(
                        "Could not find instanceId: " + instanceId + "or sshCredentialToken: " + sshCredentialToken
                                + "in the AWS process context: " + getProcessId(),
                        true);
            }

            String publicIpAddress = verifyInstanceIsRunning(awsCredentialToken, instanceId, awsPrefs.getRegion());
            LOGGER.info("Instance {} is verified and running at IP: {}", instanceId, publicIpAddress);
            awsContext.savePublicIp(publicIpAddress);

            saveAndPublishProcessStatus(ProcessState.EXECUTING);

            SSHJAgentAdaptor adaptor = initSSHJAgentAdaptor(sshCredentialToken, publicIpAddress);

            JobManagerConfiguration jobManagerConfig =
                    JobFactory.getJobManagerConfiguration(getTaskContext().getResourceJobManager());
            GroovyMapData mapData = groovyMapBuilder.build(getTaskContext());
            addMonitoringCommands(mapData);
            String scriptContent = mapData.loadFromFile(jobManagerConfig.getJobDescriptionTemplateName());
            LOGGER.info("Generated job submission script for AWS:\n{}", scriptContent);

            JobModel jobModel = createJobModel(mapData, scriptContent);

            File localScriptFile = new File(
                    getLocalDataDir(),
                    "aws-job-" + new SecureRandom().nextInt() + jobManagerConfig.getScriptExtension());
            FileUtils.writeStringToFile(localScriptFile, scriptContent, StandardCharsets.UTF_8);

            jobModel.setJobStatuses(Collections.singletonList(new JobStatus(JobState.QUEUED)));
            saveJobModel(jobModel);
            saveAndPublishJobStatus(jobModel);

            String remoteWorkingDir = getTaskContext().getWorkingDir();
            ExponentialBackoffWaiter sshWaiter = new ExponentialBackoffWaiter(
                    "SSH daemon readiness on EC2 instance " + instanceId,
                    WAIT_MAX_RETRIES,
                    INITIAL_DELAY_SECONDS,
                    MAX_DELAY_SECONDS,
                    TimeUnit.SECONDS);

            try {
                sshWaiter.waitUntil(() -> {
                    adaptor.createDirectory(remoteWorkingDir, true);
                    return true;
                });
            } catch (Exception e) {
                String reason = "Failed to connect to SSH daemon or create remote directory " + remoteWorkingDir + ". "
                        + e.getMessage();
                LOGGER.error(reason, e);
                return onFail(reason, false, e);
            }

            jobModel.setJobStatuses(Collections.singletonList(new JobStatus(JobState.ACTIVE)));
            saveJobModel(jobModel);
            saveAndPublishJobStatus(jobModel);

            adaptor.uploadFile(localScriptFile.getAbsolutePath(), remoteWorkingDir);
            LOGGER.info("Successfully uploaded script {} to {}", localScriptFile.getName(), remoteWorkingDir);

            RawCommandInfo submitCommandInfo =
                    jobManagerConfig.getSubmitCommand(remoteWorkingDir, localScriptFile.getName());
            String remoteScriptPath = remoteWorkingDir + File.separator + localScriptFile.getName();
            String command = "chmod +x " + remoteScriptPath + " && nohup " + submitCommandInfo.getCommand() + " > "
                    + remoteWorkingDir + "/AiravataAgent.stdout 2> " + remoteWorkingDir
                    + "/AiravataAgent.stderr & echo $!";
            LOGGER.info("Executing command on EC2 instance: {} for the process: {}", command, getProcessId());

            CommandOutput commandOutput = adaptor.executeCommand(command, remoteWorkingDir);
            String jobId = commandOutput.getStdOut().trim();

            if (commandOutput.getExitCode() != 0) {
                String reason = "Failed to execute job submission command. STDERR: " + commandOutput.getStdError();
                return handleJobSubmissionFailure(mapData, reason);
            }

            if (jobId.isEmpty() || !jobId.matches("\\d+")) {
                String reason = "Job submission command did not return a valid process ID (PID). Output: "
                        + commandOutput.getStdOut();
                return handleJobSubmissionFailure(mapData, reason);
            }

            awsContext.saveJobId(jobId);
            jobModel.setJobId(jobId);
            saveJobModel(jobModel);
            saveAndPublishJobStatus(jobModel);

            LOGGER.info("Successfully launched job on EC2 instance. Remote process ID (jobId): {}", jobId);
            return onSuccess("Launched job " + jobId + " in instance " + instanceId);

        } catch (Exception e) {
            LOGGER.error("Fatal error during AWS job submission for process {}", getProcessId(), e);
            return onFail("Task failed due to unexpected issue", false, e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {
        LOGGER.warn("Full cleanup triggered for process {}. Terminating all AWS resources.", getProcessId());

        try {
            AWSProcessContextManager awsContext = new AWSProcessContextManager(registryService, taskContext);
            String publicIpAddress = awsContext.getPublicIp();
            String sshCredentialToken = awsContext.getSSHCredentialToken();
            String jobId = awsContext.getJobId();
            SSHJAgentAdaptor adaptor = initSSHJAgentAdaptor(sshCredentialToken, publicIpAddress);

            JobManagerConfiguration jobManagerConfig =
                    JobFactory.getJobManagerConfiguration(getTaskContext().getResourceJobManager());
            CommandOutput commandOutput = adaptor.executeCommand(
                    jobManagerConfig.getCancelCommand(jobId).getRawCommand(), null);

            if (commandOutput.getExitCode() != 0) {
                LOGGER.warn("Failed to execute job cancellation command. STDERR: {}", commandOutput.getStdError());
            } else {
                LOGGER.info("Successfully executed job cancellation command. Output: {}", commandOutput.getStdOut());
            }
            LOGGER.info(
                    "Terminating AWS resources, instance {}, IP {}, for process {}",
                    awsContext.getInstanceId(),
                    publicIpAddress,
                    getProcessId());
            AWSTaskUtil.terminateEC2Instance(getTaskContext(), getGatewayId());

        } catch (Exception e) {
            LOGGER.error("Failed to execute full cleanup during onCancel for process {}", getProcessId(), e);
            onFail("Task failed during full cleanup due to unexpected issue", false, e);
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        LOGGER.info("AWS Job Submission Task cleanup for process {}", getProcessId());
        AWSTaskUtil.terminateEC2Instance(getTaskContext(), getGatewayId());
    }

    private String verifyInstanceIsRunning(String token, String instanceId, String region) throws Exception {
        ExponentialBackoffWaiter waiter = new ExponentialBackoffWaiter(
                "EC2 instance " + instanceId + " to enter 'running' state",
                WAIT_MAX_RETRIES,
                INITIAL_DELAY_SECONDS,
                MAX_DELAY_SECONDS,
                TimeUnit.SECONDS);

        try (Ec2Client ec2Client = AWSTaskUtil.buildEc2Client(token, getGatewayId(), region)) {
            return waiter.waitUntil(() -> {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                        .instanceIds(instanceId)
                        .build();
                DescribeInstancesResponse response = ec2Client.describeInstances(request);

                if (response.reservations().isEmpty()
                        || response.reservations().get(0).instances().isEmpty()) {
                    LOGGER.error(
                            "No instance found with ID during verification: {} for the process: {}",
                            instanceId,
                            getProcessId());
                    onFail(
                            "No instance found with ID during verification: " + instanceId + " for the process: "
                                    + getProcessId(),
                            true);
                }

                Instance instance = response.reservations().get(0).instances().get(0);
                InstanceStateName state = instance.state().name();
                LOGGER.info("Current state of instance {}: {} for the process: {}", instanceId, state, getProcessId());

                if (state == InstanceStateName.RUNNING) {
                    if (instance.publicIpAddress() == null
                            || instance.publicIpAddress().isEmpty()) {
                        // IP not assigned yet, treat as a retryable condition
                        return null;
                    }
                    return instance.publicIpAddress();
                }

                if (state == InstanceStateName.SHUTTING_DOWN
                        || state == InstanceStateName.TERMINATED
                        || state == InstanceStateName.STOPPED) {
                    LOGGER.error(
                            "Instance entered a failure state during verification: {} for the process: {}",
                            state,
                            getProcessId());
                    onFail(
                            "Instance entered a failure state during verification: " + state + " for the process: "
                                    + getProcessId(),
                            true);
                    throw new Exception("Instance entered a failure state during verification: " + state
                            + " for the process: " + getProcessId());
                }

                return null;
            });
        }
    }

    private TaskResult handleJobSubmissionFailure(GroovyMapData mapData, String reason)
            throws RegistryServiceException {
        LOGGER.error(reason);
        JobModel jobModel = new JobModel();
        jobModel.setProcessId(getProcessId());
        jobModel.setWorkingDir(mapData.getWorkingDirectory());
        jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        jobModel.setTaskId(getTaskId());
        jobModel.setJobName(mapData.getJobName());

        JobStatus jobStatus = new JobStatus(JobState.FAILED);
        jobStatus.setReason(reason);
        jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        jobModel.setJobStatuses(Collections.singletonList(jobStatus));

        saveJobModel(jobModel);
        return onFail(reason, false, null);
    }

    private JobModel createJobModel(GroovyMapData mapData, String jobScript) throws Exception {
        JobModel jobModel = new JobModel();
        jobModel.setJobId("DEFAULT_JOB_ID");
        jobModel.setProcessId(getProcessId());
        jobModel.setWorkingDir(mapData.getWorkingDirectory());
        jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        jobModel.setTaskId(getTaskId());
        jobModel.setJobName(mapData.getJobName());
        jobModel.setJobDescription(jobScript);

        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobStatus.setReason("Job submitted to EC2 instance with PID: " + "DEFAULT_JOB_ID");
        jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

        jobModel.setJobStatuses(Collections.singletonList(jobStatus));

        saveJobModel(jobModel);
        saveAndPublishJobStatus(jobModel);

        return jobModel;
    }

    private SSHJAgentAdaptor initSSHJAgentAdaptor(String sshCredentialToken, String publicIpAddress) throws Exception {
        org.apache.airavata.service.RegistryService registryService = getRegistryService();
        CredentialStoreService credentialStoreService = getCredentialStoreService();
        SSHJAgentAdaptor adaptor = new SSHJAgentAdaptor(registryService, credentialStoreService);
        SSHCredential sshCredential = credentialStoreService.getSSHCredential(sshCredentialToken, getGatewayId());
        adaptor.init(
                getTaskContext().getComputeResourceLoginUserName(),
                publicIpAddress,
                22,
                sshCredential.getPublicKey(),
                sshCredential.getPrivateKey(),
                sshCredential.getPassphrase());

        return adaptor;
    }
}
