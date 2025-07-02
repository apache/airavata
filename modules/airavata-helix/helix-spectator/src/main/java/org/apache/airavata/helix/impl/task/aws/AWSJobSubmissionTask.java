/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.aws;

import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.adaptor.SSHJAgentAdaptor;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.submission.config.GroovyMapBuilder;
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
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@TaskDef(name = "AWS_JOB_SUBMISSION_TASK")
public class AWSJobSubmissionTask extends AiravataTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSJobSubmissionTask.class);

    private static final int POLL_INTERVAL_SECONDS = 10;
    private static final int TIMEOUT_MINUTES = 5;

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {
        LOGGER.info("Starting AWS Job Submission Task for process {}", getProcessId());

        try {
            AWSProcessContextManager awsContext = new AWSProcessContextManager(getTaskContext());
            AwsComputeResourcePreference awsPrefs = getTaskContext().getGroupComputeResourcePreference().getSpecificPreferences().getAws();
            String instanceId = awsContext.getInstanceId();
            String sshCredentialToken = awsContext.getSSHCredentialToken();
            String awsCredentialToken = getTaskContext().getGroupComputeResourcePreference().getResourceSpecificCredentialStoreToken();

            if (instanceId == null || sshCredentialToken == null) {
                LOGGER.error("Could not find instanceId: {} or sshCredentialToken: {} in the AWS process context {}", instanceId, sshCredentialToken, getProcessId());
                throw new Exception("Could not find instanceId: " + instanceId + "or sshCredentialToken: " + sshCredentialToken + "in the AWS process context: " + getProcessId());
            }

            String publicIpAddress = verifyInstanceIsRunning(awsCredentialToken, instanceId, awsPrefs.getRegion());
            LOGGER.info("Instance {} is verified and running at IP: {}", instanceId, publicIpAddress);
            awsContext.savePublicIp(publicIpAddress);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(sshCredentialToken, getGatewayId());

            SSHJAgentAdaptor adaptor = new SSHJAgentAdaptor();
            adaptor.init(
                    getTaskContext().getComputeResourceLoginUserName(),
                    publicIpAddress,
                    22,
                    sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(),
                    sshCredential.getPassphrase()
            );

            saveAndPublishProcessStatus(ProcessState.EXECUTING);

            JobManagerConfiguration jobManagerConfig = JobFactory.getJobManagerConfiguration(getTaskContext().getResourceJobManager());
            GroovyMapData mapData = new GroovyMapBuilder(getTaskContext()).build();
            String scriptContent = mapData.loadFromFile(jobManagerConfig.getJobDescriptionTemplateName());
            LOGGER.info("Generated job submission script for AWS:\n{}", scriptContent);

            File localScriptFile = new File(getLocalDataDir(), "aws-job-" + new SecureRandom().nextInt() + jobManagerConfig.getScriptExtension());
            FileUtils.writeStringToFile(localScriptFile, scriptContent, StandardCharsets.UTF_8);

            String remoteWorkingDir = getTaskContext().getWorkingDir();

            JobModel jobModel = createJobModel(mapData, "DEFAULT_JOB_ID", scriptContent);
            saveJobModel(jobModel);
            saveAndPublishJobStatus(jobModel);

            jobModel.setJobStatuses(Collections.singletonList(new JobStatus(JobState.QUEUED)));
            saveJobModel(jobModel);
            saveAndPublishJobStatus(jobModel);
            // TODO refine the logic
            long startTime = System.currentTimeMillis();
            long timeoutTime = startTime + TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES);

            while (System.currentTimeMillis() < timeoutTime) {
                try {
                    adaptor.createDirectory(remoteWorkingDir, false);
                    break;
                } catch (Exception e) {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(POLL_INTERVAL_SECONDS));
                }
            }

            if (System.currentTimeMillis() >= timeoutTime) {
                String reason = "Failed to create remote working directory " + remoteWorkingDir + " for the process: " + getProcessId();
                return handleJobSubmissionFailure(mapData, reason);
            }

            jobModel.setJobStatuses(Collections.singletonList(new JobStatus(JobState.ACTIVE)));
            saveJobModel(jobModel);
            saveAndPublishJobStatus(jobModel);

            adaptor.uploadFile(localScriptFile.getAbsolutePath(), remoteWorkingDir);
            LOGGER.info("Successfully uploaded script {} to {}", localScriptFile.getName(), remoteWorkingDir);

            RawCommandInfo submitCommandInfo = jobManagerConfig.getSubmitCommand(remoteWorkingDir, localScriptFile.getName());
            String remoteScriptPath = remoteWorkingDir + File.separator + localScriptFile.getName();
            String command = "chmod +x " + remoteScriptPath + " && nohup " + submitCommandInfo.getCommand() + " > " + remoteWorkingDir + "/AiravataAgent.stdout 2> " + remoteWorkingDir + "/AiravataAgent.stderr & echo $!";
            LOGGER.info("Executing command on EC2 instance: {} for the process: {}", command, getProcessId());

            CommandOutput commandOutput = adaptor.executeCommand(command, remoteWorkingDir);
            String jobId = commandOutput.getStdOut().trim();

            if (commandOutput.getExitCode() != 0) {
                String reason = "Failed to execute job submission command. STDERR: " + commandOutput.getStdError();
                return handleJobSubmissionFailure(mapData, reason);
            }

            if (jobId.isEmpty() || !jobId.matches("\\d+")) {
                String reason = "Job submission command did not return a valid process ID (PID). Output: " + commandOutput.getStdOut();
                return handleJobSubmissionFailure(mapData, reason);
            }

            LOGGER.info("Successfully launched job on EC2 instance. Remote process ID (jobId): {}", jobId);
            return onSuccess("Launched job " + jobId + " in instance " + instanceId);

        } catch (Exception e) {
            LOGGER.error("Fatal error during AWS job submission for process {}", getProcessId(), e);
            return onFail("Task failed due to unexpected issue", false, e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }

    private String verifyInstanceIsRunning(String token, String instanceId, String region) throws Exception {
        try (Ec2Client ec2Client = AWSTaskUtil.buildEc2Client(token, getGatewayId(), region)) {
            long startTime = System.currentTimeMillis();
            long timeoutTime = startTime + TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES);

            while (System.currentTimeMillis() < timeoutTime) {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(instanceId).build();
                DescribeInstancesResponse response = ec2Client.describeInstances(request);

                if (response.reservations().isEmpty() || response.reservations().get(0).instances().isEmpty()) {
                    LOGGER.error("No instance found with ID during verification: {} for the process: {}", instanceId, getProcessId());
                    throw new Exception("No instance found with ID during verification: " + instanceId + " for the process: " + getProcessId());
                }

                Instance instance = response.reservations().get(0).instances().get(0);
                InstanceStateName state = instance.state().name();
                LOGGER.info("Current state of instance {}: {} for the process: {}", instanceId, state, getProcessId());

                if (state == InstanceStateName.RUNNING) {
                    if (instance.publicIpAddress() == null || instance.publicIpAddress().isEmpty()) {
                        LOGGER.error("Instance is running but has no public IP address found during verification: {} for the process: {}", instanceId, getProcessId());
                        throw new Exception("Instance is running but has no public IP address found during verification: " + instanceId + " for the process: " + getProcessId());
                    }
                    return instance.publicIpAddress();
                }

                if (state == InstanceStateName.SHUTTING_DOWN || state == InstanceStateName.TERMINATED || state == InstanceStateName.STOPPED) {
                    LOGGER.error("Instance entered a failure state during verification: {} for the process: {}", state, getProcessId());
                    throw new Exception("Instance entered a failure state during verification: " + state + " for the process: " + getProcessId());
                }
                Thread.sleep(TimeUnit.SECONDS.toMillis(POLL_INTERVAL_SECONDS));
            }
        }
        throw new Exception("Instance did not become available within " + TIMEOUT_MINUTES + " minutes.");
    }

    private TaskResult handleJobSubmissionFailure(GroovyMapData mapData, String reason) throws TException {
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

    private JobModel createJobModel(GroovyMapData mapData, String jobId, String jobScript) {
        JobModel jobModel = new JobModel();
        jobModel.setJobId(jobId);
        jobModel.setProcessId(getProcessId());
        jobModel.setWorkingDir(mapData.getWorkingDirectory());
        jobModel.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
        jobModel.setTaskId(getTaskId());
        jobModel.setJobName(mapData.getJobName());
        jobModel.setJobDescription(jobScript);

        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobStatus.setReason("Job submitted to EC2 instance with PID: " + jobId);
        jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

        jobModel.setJobStatuses(Collections.singletonList(jobStatus));

        return jobModel;
    }

    private File getLocalDataDir() {
        String outputPath = ServerSettings.getLocalDataLocation();
        outputPath = (outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator);
        return new File(outputPath + getProcessId());
    }

    private void saveJobModel(JobModel jobModel) throws TException {
        getRegistryServiceClient().addJob(jobModel, getProcessId());
    }

    private void saveAndPublishJobStatus(JobModel jobModel) throws Exception {
        try {
            JobStatus jobStatus;
            if (jobModel.getJobStatuses() != null && !jobModel.getJobStatuses().isEmpty()) {
                jobStatus = jobModel.getJobStatuses().get(0);
            } else {
                LOGGER.error("Job statuses can not be empty for job model: {} for process: {}", jobModel.getJobId(), getProcessId());
                return;
            }

            jobModel.setJobStatuses(List.of(jobStatus));

            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0) {
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            } else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }

            getRegistryServiceClient().addJobStatus(jobStatus, jobModel.getTaskId(), jobModel.getJobId());

        } catch (Exception e) {
            LOGGER.error("Error persisting job status for process {}: {}", getProcessId(), e.getLocalizedMessage(), e);
            throw new Exception("Error persisting job status for process: " + getProcessId() + ". " + e.getLocalizedMessage(), e);
        }
    }
}