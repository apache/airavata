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
package org.apache.airavata.compute.provider.aws;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.compute.provider.ComputeProvider;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.compute.resource.submission.JobManagerSpec;
import org.apache.airavata.compute.resource.submission.JobSubmissionData;
import org.apache.airavata.compute.resource.submission.JobSubmissionDataBuilder;
import org.apache.airavata.compute.resource.submission.JobSubmissionSupport;
import org.apache.airavata.compute.resource.submission.RawCommandInfo;
import org.apache.airavata.compute.resource.adapter.ComputeResourceAdapter;
import org.apache.airavata.compute.resource.model.AwsComputeResourcePreference;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.scheduling.ComputeSubmissionTracker;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.CommandOutput;
import org.apache.airavata.protocol.ssh.SSHJAgentAdapter;
import org.apache.airavata.protocol.ssh.SSHUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;

/**
 * AWS compute provider — covers the full lifecycle of EC2-backed job execution.
 *
 * <p>Resource lifecycle:
 * <ul>
 *   <li>{@link #provision} — creates security group, key pair, and launches EC2 instance
 *   <li>{@link #deprovision} — terminates EC2 instance and cleans up context
 * </ul>
 *
 * <p>Job lifecycle:
 * <ul>
 *   <li>{@link #submit} — verifies instance readiness, uploads job script, executes via SSH
 *   <li>{@link #monitor} — polls PID status on EC2 instance until process exits
 *   <li>{@link #cancel} — terminates EC2 instance (killing the running job)
 * </ul>
 */
@Component
@ConditionalOnParticipant
public class AwsComputeProvider implements ComputeProvider {

    private static final Logger logger = LoggerFactory.getLogger(AwsComputeProvider.class);

    private static final int WAIT_MAX_RETRIES = 10;
    private static final long INITIAL_DELAY_SECONDS = 5;
    private static final long MAX_DELAY_SECONDS = 20;

    private final AwsTaskUtil awsTaskUtil;
    private final ProcessService processService;
    private final CredentialStoreService credentialStoreService;
    private final JobSubmissionSupport jobSubmissionSupport;
    private final JobSubmissionDataBuilder jobSubmissionDataBuilder;
    private final ComputeSubmissionTracker computeSubmissionTracker;
    private final JobService jobService;
    private final ComputeResourceAdapter computeResourceAdapter;

    public AwsComputeProvider(
            AwsTaskUtil awsTaskUtil,
            ProcessService processService,
            CredentialStoreService credentialStoreService,
            JobSubmissionSupport jobSubmissionSupport,
            JobSubmissionDataBuilder jobSubmissionDataBuilder,
            ComputeSubmissionTracker computeSubmissionTracker,
            JobService jobService,
            ComputeResourceAdapter computeResourceAdapter) {
        this.awsTaskUtil = awsTaskUtil;
        this.processService = processService;
        this.credentialStoreService = credentialStoreService;
        this.jobSubmissionSupport = jobSubmissionSupport;
        this.jobSubmissionDataBuilder = jobSubmissionDataBuilder;
        this.computeSubmissionTracker = computeSubmissionTracker;
        this.jobService = jobService;
        this.computeResourceAdapter = computeResourceAdapter;
    }

    // =========================================================================
    // Provision — create EC2 infrastructure
    // =========================================================================

    @Override
    public DagTaskResult provision(TaskContext context) {
        logger.info("Starting AWS provisioning for process {}", context.getProcessId());

        AwsProcessContext awsContext = new AwsProcessContext(processService, context);
        Ec2Client ec2Client = null;

        try {
            AwsComputeResourcePreference awsPrefs = awsContext.getAwsComputeResourcePreference();
            String credentialToken = null;

            ec2Client = awsTaskUtil.buildEc2Client(credentialToken, context.getGatewayId(), awsPrefs.getRegion());

            String securityGroupId = createSecurityGroup(ec2Client, context);
            awsContext.saveSecurityGroupId(securityGroupId);
            logger.info("Created security group: {}", securityGroupId);

            String keyPairName = "airavata-key-" + context.getProcessId();
            CreateKeyPairResponse kpRes = ec2Client.createKeyPair(req -> req.keyName(keyPairName));
            awsContext.saveKeyPairName(keyPairName);

            String privateKeyPEM = kpRes.keyMaterial();
            String publicKey = SSHUtil.generatePublicKey(privateKeyPEM);

            String sshCredentialToken = saveSSHCredential(privateKeyPEM, publicKey, context.getGatewayId());
            awsContext.saveSSHCredentialToken(sshCredentialToken);
            logger.info("Created key pair {} with credential token {}", keyPairName, sshCredentialToken);

            RunInstancesRequest runRequest = RunInstancesRequest.builder()
                    .imageId(awsPrefs.getPreferredAmiId())
                    .instanceType(InstanceType.fromValue(awsPrefs.getPreferredInstanceType()))
                    .keyName(keyPairName)
                    .securityGroupIds(securityGroupId)
                    .minCount(1)
                    .maxCount(1)
                    .build();
            RunInstancesResponse runResponse = ec2Client.runInstances(runRequest);

            if (runResponse.instances() == null || runResponse.instances().isEmpty()) {
                awsTaskUtil.terminateEC2Instance(context, context.getGatewayId());
                return new DagTaskResult.Failure("No instances were launched by AWS", false);
            }

            String instanceId = runResponse.instances().get(0).instanceId();
            awsContext.saveInstanceId(instanceId);
            logger.info("Launched EC2 instance {}", instanceId);

            return new DagTaskResult.Success("AWS provisioning completed for " + context.getTaskId());

        } catch (Ec2Exception e) {
            String errorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN";
            logger.error("AWS EC2 error for process {}: {}", context.getProcessId(), errorCode, e);
            awsTaskUtil.terminateEC2Instance(context, context.getGatewayId());
            boolean fatal = errorCode.contains("InvalidAMIID")
                    || errorCode.contains("UnauthorizedOperation")
                    || errorCode.contains("AuthFailure");
            return new DagTaskResult.Failure("AWS EC2 error: " + errorCode + " - " + e.getMessage(), fatal, e);

        } catch (SdkException e) {
            logger.error("AWS SDK error for process {}", context.getProcessId(), e);
            awsTaskUtil.terminateEC2Instance(context, context.getGatewayId());
            return new DagTaskResult.Failure("AWS SDK error: " + e.getMessage(), false, e);

        } catch (Exception e) {
            logger.error("Unexpected error during AWS provisioning for process {}", context.getProcessId(), e);
            awsTaskUtil.terminateEC2Instance(context, context.getGatewayId());
            return new DagTaskResult.Failure("Unexpected error during AWS provisioning", false, e);

        } finally {
            if (ec2Client != null) {
                ec2Client.close();
            }
        }
    }

    // =========================================================================
    // Submit — verify instance readiness and execute job via SSH
    // =========================================================================

    @Override
    public DagTaskResult submit(TaskContext context) {
        logger.info("Starting AWS job submission for process {}", context.getProcessId());

        try {
            AwsProcessContext awsContext = new AwsProcessContext(processService, context);
            AwsComputeResourcePreference awsPrefs = awsContext.getAwsComputeResourcePreference();
            String instanceId = awsContext.getInstanceId();
            String sshCredentialToken = awsContext.getSSHCredentialToken();
            String awsCredentialToken = null;

            if (instanceId == null || sshCredentialToken == null) {
                logger.error(
                        "Could not find instanceId: {} or sshCredentialToken: {} in the AWS process context {}",
                        instanceId, sshCredentialToken, context.getProcessId());
                return new DagTaskResult.Failure(
                        "Could not find instanceId: " + instanceId + " or sshCredentialToken: " + sshCredentialToken
                                + " in the AWS process context: " + context.getProcessId(),
                        true);
            }

            String publicIpAddress = verifyInstanceIsRunning(awsCredentialToken, instanceId, awsPrefs.getRegion(), context);
            logger.info("Instance {} is verified and running at IP: {}", instanceId, publicIpAddress);
            awsContext.savePublicIp(publicIpAddress);

            SSHJAgentAdapter adapter = initSSHJAgentAdapter(sshCredentialToken, publicIpAddress, context);

            JobManagerSpec jobManagerConfig =
                    jobSubmissionSupport.getJobManagerConfiguration(context.getComputeResource());
            JobSubmissionData mapData = jobSubmissionDataBuilder.build(context);
            jobSubmissionSupport.addMonitoringCommands(mapData);
            String scriptContent = mapData.loadFromFile(jobManagerConfig.getJobDescriptionTemplateName());
            logger.info("Generated job submission script for AWS:\n{}", scriptContent);

            JobModel jobModel = jobSubmissionSupport.createJobModel(context.getProcessId(), context.getTaskId(), mapData);
            jobModel.setJobId("DEFAULT_JOB_ID");
            jobModel.setJobDescription(scriptContent);
            jobSubmissionSupport.publishJobStatus(jobModel, JobState.SUBMITTED,
                    "Job submitted to EC2 instance with PID: DEFAULT_JOB_ID");
            jobService.saveJob(jobModel);

            File localScriptFile = new File(
                    jobSubmissionSupport.getLocalDataDir(context.getProcessId()),
                    "aws-job-" + new SecureRandom().nextInt() + jobManagerConfig.getScriptExtension());
            Files.writeString(
                    localScriptFile.toPath(),
                    scriptContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            jobSubmissionSupport.publishJobStatus(jobModel, JobState.QUEUED, "Job queued on EC2 instance");
            jobService.saveJob(jobModel);

            String remoteWorkingDir = context.getWorkingDir();
            ExponentialBackoffWaiter sshWaiter = new ExponentialBackoffWaiter(
                    "SSH daemon readiness on EC2 instance " + instanceId,
                    WAIT_MAX_RETRIES,
                    INITIAL_DELAY_SECONDS,
                    MAX_DELAY_SECONDS,
                    TimeUnit.SECONDS);

            try {
                sshWaiter.waitUntil(() -> {
                    adapter.createDirectory(remoteWorkingDir, true);
                    return true;
                });
            } catch (Exception e) {
                String reason = "Failed to connect to SSH daemon or create remote directory " + remoteWorkingDir + ". "
                        + e.getMessage();
                logger.error(reason, e);
                return new DagTaskResult.Failure(reason, false, e);
            }
            jobSubmissionSupport.publishJobStatus(jobModel, JobState.ACTIVE, "SSH connected to EC2 instance");
            jobService.saveJob(jobModel);

            adapter.uploadFile(localScriptFile.getAbsolutePath(), remoteWorkingDir);
            logger.info("Successfully uploaded script {} to {}", localScriptFile.getName(), remoteWorkingDir);

            RawCommandInfo submitCommandInfo =
                    jobManagerConfig.getSubmitCommand(remoteWorkingDir, localScriptFile.getName());
            String remoteScriptPath = remoteWorkingDir + File.separator + localScriptFile.getName();
            String command = "chmod +x " + remoteScriptPath + " && nohup " + submitCommandInfo.getCommand() + " > "
                    + remoteWorkingDir + "/AiravataAgent.stdout 2> " + remoteWorkingDir
                    + "/AiravataAgent.stderr & echo $!";
            logger.info("Executing command on EC2 instance: {} for the process: {}", command, context.getProcessId());

            CommandOutput commandOutput = adapter.executeCommand(command, remoteWorkingDir);
            String jobId = commandOutput.getStdOut().trim();

            if (commandOutput.getExitCode() != 0) {
                String reason = "Failed to execute job submission command. STDERR: " + commandOutput.getStdError();
                return handleJobSubmissionFailure(mapData, reason, context);
            }

            if (jobId.isEmpty() || !jobId.matches("\\d+")) {
                String reason = "Job submission command did not return a valid process ID (PID). Output: "
                        + commandOutput.getStdOut();
                return handleJobSubmissionFailure(mapData, reason, context);
            }

            awsContext.saveJobId(jobId);
            jobModel.setJobId(jobId);
            jobService.saveJob(jobModel);
            jobSubmissionSupport.saveAndPublishJobStatus(jobModel);

            logger.info("Successfully launched job on EC2 instance. Remote process ID (jobId): {}", jobId);
            return new DagTaskResult.Success("Launched job " + jobId + " in instance " + instanceId);

        } catch (Exception e) {
            logger.error("Fatal error during AWS job submission for process {}", context.getProcessId(), e);
            return new DagTaskResult.Failure("Task failed due to unexpected issue", false, e);
        }
    }

    // =========================================================================
    // Deprovision — terminate EC2 instance
    // =========================================================================

    @Override
    public DagTaskResult deprovision(TaskContext context) {
        logger.info("Deprovisioning AWS resources for process {}", context.getProcessId());

        try {
            AwsProcessContext awsContext = new AwsProcessContext(processService, context);
            awsContext.cleanup();
            awsTaskUtil.terminateEC2Instance(context, context.getGatewayId());
            return new DagTaskResult.Success("AWS resources deprovisioned for process " + context.getProcessId());
        } catch (Exception e) {
            logger.error("Error during AWS deprovisioning for process {}", context.getProcessId(), e);
            return new DagTaskResult.Failure(
                    "AWS deprovisioning failed for process " + context.getProcessId(), false, e);
        }
    }

    // =========================================================================
    // Monitor — poll PID status on EC2 instance
    // =========================================================================

    @Override
    public DagTaskResult monitor(TaskContext context) {
        try {
            java.util.List<JobModel> jobs = jobService.getJobs("processId", context.getProcessId());
            if (jobs == null || jobs.isEmpty()) {
                return new DagTaskResult.Success("No running jobs found for process " + context.getProcessId());
            }

            AwsProcessContext awsContext = new AwsProcessContext(processService, context);
            String publicIp = awsContext.getPublicIp();
            String sshCredentialToken = awsContext.getSSHCredentialToken();

            if (publicIp == null || sshCredentialToken == null) {
                return new DagTaskResult.Success("No AWS instance info for monitoring process " + context.getProcessId());
            }

            SSHJAgentAdapter adapter = initSSHJAgentAdapter(sshCredentialToken, publicIp, context);
            JobManagerSpec config = jobSubmissionSupport.getJobManagerConfiguration(context.getComputeResource());

            for (JobModel job : jobs) {
                pollJobUntilSaturated(adapter, config, job);
            }

            return new DagTaskResult.Success("Job monitoring completed for process " + context.getProcessId());

        } catch (Exception e) {
            logger.error("Error during AWS job monitoring for process {} — continuing (non-critical)",
                    context.getProcessId(), e);
            return new DagTaskResult.Success("Job monitoring encountered errors but continuing (non-critical)");
        }
    }

    // =========================================================================
    // Cancel — terminate EC2 instance (killing the running job)
    // =========================================================================

    @Override
    public DagTaskResult cancel(TaskContext context) {
        return deprovision(context);
    }

    // -------------------------------------------------------------------------
    // Monitor helpers
    // -------------------------------------------------------------------------

    private void pollJobUntilSaturated(SSHJAgentAdapter adapter, JobManagerSpec config, JobModel job) {
        try {
            var monitorCommand = config.getMonitorCommand(job.getJobId());
            if (monitorCommand.isEmpty()) {
                logger.info("No monitor command for job {} — skipping", job.getJobId());
                return;
            }

            int retryDelaySeconds = 30;
            for (int i = 1; i <= 4; i++) {
                CommandOutput output = adapter.executeCommand(monitorCommand.get().getRawCommand(), null);
                if (output.getExitCode() != 0) {
                    logger.warn("Monitor command failed for job {}: stdout={}, stderr={}",
                            job.getJobId(), output.getStdOut(), output.getStdError());
                    break;
                }

                var jobStatus = config.getParser().parseJobStatus(job.getJobId(), output.getStdOut());
                if (jobStatus == null) {
                    logger.info("Status unavailable for job {} — skipping", job.getJobId());
                    break;
                }

                logger.info("Job {} status: {}", job.getJobId(), jobStatus.getState());

                if (jobStatus.getState() != org.apache.airavata.compute.resource.model.JobState.ACTIVE
                        && jobStatus.getState() != org.apache.airavata.compute.resource.model.JobState.QUEUED
                        && jobStatus.getState() != org.apache.airavata.compute.resource.model.JobState.SUBMITTED) {
                    logger.info("Job {} reached saturated state", job.getJobId());
                    break;
                }

                int waitTime = retryDelaySeconds * i;
                logger.info("Waiting {} seconds before next poll for job {}", waitTime, job.getJobId());
                Thread.sleep(waitTime * 1000L);
            }
        } catch (Exception e) {
            logger.warn("Error polling job {} — continuing", job.getJobId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Provision helpers
    // -------------------------------------------------------------------------

    private String saveSSHCredential(String privateKey, String publicKey, String gatewayId) throws Exception {
        SSHCredential credential = new SSHCredential();
        credential.setGatewayId(gatewayId);
        credential.setToken(UUID.randomUUID().toString());
        credential.setPrivateKey(privateKey);
        credential.setPublicKey(publicKey);
        return credentialStoreService.addSSHCredential(credential);
    }

    private String createSecurityGroup(Ec2Client ec2, TaskContext context) throws Exception {
        String vpcId = ec2.describeVpcs(
                        req -> req.filters(f -> f.name("is-default").values("true")))
                .vpcs()
                .get(0)
                .vpcId();
        CreateSecurityGroupResponse sgRes =
                ec2.createSecurityGroup(req -> req.groupName("airavata-sg-" + context.getProcessId())
                        .description("Airavata temporary security group for " + context.getProcessId())
                        .vpcId(vpcId));

        // TODO: Restrict to specific IP range instead of 0.0.0.0/0 in production
        String allowedCidr = "0.0.0.0/0";
        logger.warn("Security group allows SSH from any IP ({}). Restrict in production.", allowedCidr);
        ec2.authorizeSecurityGroupIngress(req -> req.groupId(sgRes.groupId())
                .ipPermissions(p -> p.ipProtocol("tcp").fromPort(22).toPort(22).ipRanges(r -> r.cidrIp(allowedCidr))));

        return sgRes.groupId();
    }

    // -------------------------------------------------------------------------
    // Submit helpers
    // -------------------------------------------------------------------------

    private String verifyInstanceIsRunning(String token, String instanceId, String region, TaskContext context)
            throws Exception {
        ExponentialBackoffWaiter waiter = new ExponentialBackoffWaiter(
                "EC2 instance " + instanceId + " to enter 'running' state",
                WAIT_MAX_RETRIES,
                INITIAL_DELAY_SECONDS,
                MAX_DELAY_SECONDS,
                TimeUnit.SECONDS);

        try (Ec2Client ec2Client = awsTaskUtil.buildEc2Client(token, context.getGatewayId(), region)) {
            return waiter.waitUntil(() -> {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                        .instanceIds(instanceId)
                        .build();
                DescribeInstancesResponse response = ec2Client.describeInstances(request);

                if (response.reservations().isEmpty()
                        || response.reservations().get(0).instances().isEmpty()) {
                    logger.error(
                            "No instance found with ID during verification: {} for the process: {}",
                            instanceId, context.getProcessId());
                    throw new Exception("No instance found with ID during verification: " + instanceId
                            + " for the process: " + context.getProcessId());
                }

                Instance instance = response.reservations().get(0).instances().get(0);
                InstanceStateName state = instance.state().name();
                logger.info("Current state of instance {}: {} for the process: {}", instanceId, state,
                        context.getProcessId());

                if (state == InstanceStateName.RUNNING) {
                    if (instance.publicIpAddress() == null
                            || instance.publicIpAddress().isEmpty()) {
                        return null;
                    }
                    return instance.publicIpAddress();
                }

                if (state == InstanceStateName.SHUTTING_DOWN
                        || state == InstanceStateName.TERMINATED
                        || state == InstanceStateName.STOPPED) {
                    logger.error(
                            "Instance entered a failure state during verification: {} for the process: {}",
                            state, context.getProcessId());
                    throw new Exception("Instance entered a failure state during verification: " + state
                            + " for the process: " + context.getProcessId());
                }

                return null;
            });
        }
    }

    private DagTaskResult handleJobSubmissionFailure(JobSubmissionData mapData, String reason, TaskContext context)
            throws Exception {
        logger.error(reason);
        JobModel jobModel = jobSubmissionSupport.createJobModel(context.getProcessId(), context.getTaskId(), mapData);
        jobSubmissionSupport.publishJobStatus(jobModel, JobState.FAILED, reason);
        jobService.saveJob(jobModel);
        return new DagTaskResult.Failure(reason, false);
    }

    private SSHJAgentAdapter initSSHJAgentAdapter(String sshCredentialToken, String publicIpAddress,
            TaskContext context) throws Exception {
        SSHJAgentAdapter adapter = new SSHJAgentAdapter(computeResourceAdapter, credentialStoreService);
        SSHCredential sshCredential = credentialStoreService.getSSHCredential(sshCredentialToken, context.getGatewayId());
        adapter.init(
                context.getComputeResourceLoginUserName(),
                publicIpAddress,
                22,
                sshCredential.getPublicKey(),
                sshCredential.getPrivateKey(),
                sshCredential.getPassphrase());

        return adapter;
    }
}
