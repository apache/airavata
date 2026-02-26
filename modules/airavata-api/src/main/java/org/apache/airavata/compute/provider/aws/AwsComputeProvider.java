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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.compute.provider.ComputeProvider;
import org.apache.airavata.compute.resource.adapter.ComputeResourceAdapter;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.compute.resource.entity.ResourceBindingEntity;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.compute.resource.submission.JobManagerSpec;
import org.apache.airavata.compute.resource.submission.JobSubmissionData;
import org.apache.airavata.compute.resource.submission.JobSubmissionDataBuilder;
import org.apache.airavata.compute.resource.submission.JobSubmissionSupport;
import org.apache.airavata.compute.resource.submission.RawCommandInfo;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.execution.scheduling.ComputeSubmissionTracker;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.AgentAdapter;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // AWS provider context keys
    private static final String AWS_INSTANCE_ID = "AWS_INSTANCE_ID";
    private static final String AWS_SECURITY_GROUP_ID = "AWS_SECURITY_GROUP_ID";
    private static final String AWS_KEY_PAIR_NAME = "AWS_KEY_PAIR_NAME";
    private static final String AWS_SSH_CREDENTIAL_TOKEN = "AWS_SSH_CREDENTIAL_TOKEN";
    private static final String AWS_PUBLIC_IP = "AWS_PUBLIC_IP";
    private static final String AWS_JOB_ID = "AWS_JOB_ID";

    private final AwsTaskUtil awsTaskUtil;
    private final ProcessService processService;
    private final CredentialStoreService credentialStoreService;
    private final JobSubmissionSupport jobSubmissionSupport;
    private final JobSubmissionDataBuilder jobSubmissionDataBuilder;
    private final ComputeSubmissionTracker computeSubmissionTracker;
    private final JobService jobService;
    private final ComputeResourceAdapter computeResourceAdapter;
    private final ResourceProfileAdapter resourceProfileAdapter;

    public AwsComputeProvider(
            AwsTaskUtil awsTaskUtil,
            ProcessService processService,
            CredentialStoreService credentialStoreService,
            JobSubmissionSupport jobSubmissionSupport,
            JobSubmissionDataBuilder jobSubmissionDataBuilder,
            ComputeSubmissionTracker computeSubmissionTracker,
            JobService jobService,
            ComputeResourceAdapter computeResourceAdapter,
            ResourceProfileAdapter resourceProfileAdapter) {
        this.awsTaskUtil = awsTaskUtil;
        this.processService = processService;
        this.credentialStoreService = credentialStoreService;
        this.jobSubmissionSupport = jobSubmissionSupport;
        this.jobSubmissionDataBuilder = jobSubmissionDataBuilder;
        this.computeSubmissionTracker = computeSubmissionTracker;
        this.jobService = jobService;
        this.computeResourceAdapter = computeResourceAdapter;
        this.resourceProfileAdapter = resourceProfileAdapter;
    }

    // =========================================================================
    // Provision — create EC2 infrastructure
    // =========================================================================

    @Override
    public DagTaskResult provision(TaskContext context) {
        logger.info("Starting AWS provisioning for process {}", context.getProcessId());

        Ec2Client ec2Client = null;

        try {
            String credentialToken = null;

            // Resolve AWS configuration: binding metadata takes priority over resourceSchedule.
            // Operators configure awsRegion/awsAmiId/awsInstanceType in the ResourceBinding
            // metadata for this compute resource; resourceSchedule values serve as per-experiment
            // overrides when the binding does not carry a value.
            ResourceBindingEntity binding =
                    resourceProfileAdapter.getBinding(context.getComputeResourceId(), context.getGatewayId());
            String region = resolveAwsConfig(binding, context, "awsRegion", "us-east-1");

            ec2Client = awsTaskUtil.buildEc2Client(credentialToken, context.getGatewayId(), region);

            String securityGroupId = createSecurityGroup(ec2Client, context);
            saveProviderState(context, AWS_SECURITY_GROUP_ID, securityGroupId);
            logger.info("Created security group: {}", securityGroupId);

            String keyPairName = "airavata-key-" + context.getProcessId();
            CreateKeyPairResponse kpRes = ec2Client.createKeyPair(req -> req.keyName(keyPairName));
            saveProviderState(context, AWS_KEY_PAIR_NAME, keyPairName);

            String privateKeyPEM = kpRes.keyMaterial();
            String publicKey = SSHUtil.generatePublicKey(privateKeyPEM);

            String sshCredentialToken = saveSSHCredential(privateKeyPEM, publicKey, context.getGatewayId());
            saveProviderState(context, AWS_SSH_CREDENTIAL_TOKEN, sshCredentialToken);
            logger.info("Created key pair {} with credential token {}", keyPairName, sshCredentialToken);

            String amiId = resolveAwsConfig(binding, context, "awsAmiId", null);
            String instanceType = resolveAwsConfig(binding, context, "awsInstanceType", "t2.micro");

            RunInstancesRequest runRequest = RunInstancesRequest.builder()
                    .imageId(amiId)
                    .instanceType(InstanceType.fromValue(instanceType))
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
            saveProviderState(context, AWS_INSTANCE_ID, instanceId);
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
            String instanceId = getProviderState(context, AWS_INSTANCE_ID);
            String sshCredentialToken = getProviderState(context, AWS_SSH_CREDENTIAL_TOKEN);
            String awsCredentialToken = null;

            if (instanceId == null || sshCredentialToken == null) {
                logger.error(
                        "Could not find instanceId: {} or sshCredentialToken: {} in the AWS process context {}",
                        instanceId,
                        sshCredentialToken,
                        context.getProcessId());
                return new DagTaskResult.Failure(
                        "Could not find instanceId: " + instanceId + " or sshCredentialToken: " + sshCredentialToken
                                + " in the AWS process context: " + context.getProcessId(),
                        true);
            }

            ResourceBindingEntity binding =
                    resourceProfileAdapter.getBinding(context.getComputeResourceId(), context.getGatewayId());
            String region = resolveAwsConfig(binding, context, "awsRegion", "us-east-1");

            String publicIpAddress = verifyInstanceIsRunning(awsCredentialToken, instanceId, region, context);
            logger.info("Instance {} is verified and running at IP: {}", instanceId, publicIpAddress);
            saveProviderState(context, AWS_PUBLIC_IP, publicIpAddress);

            SSHJAgentAdapter adapter = initSSHJAgentAdapter(sshCredentialToken, publicIpAddress, context);

            JobManagerSpec jobManagerConfig =
                    jobSubmissionSupport.getJobManagerConfiguration(context.getComputeResource());
            JobSubmissionData mapData = jobSubmissionDataBuilder.build(context);
            jobSubmissionSupport.addMonitoringCommands(mapData);
            String scriptContent = mapData.loadFromFile(jobManagerConfig.getJobDescriptionTemplateName());
            logger.info("Generated job submission script for AWS:\n{}", scriptContent);

            JobModel jobModel =
                    jobSubmissionSupport.createJobModel(context.getProcessId(), context.getTaskId(), mapData);
            jobModel.setJobId("DEFAULT_JOB_ID");
            jobModel.setJobDescription(scriptContent);
            jobSubmissionSupport.publishJobStatus(
                    jobModel, JobState.SUBMITTED, "Job submitted to EC2 instance with PID: DEFAULT_JOB_ID");
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
            try {
                adapter.createDirectory(remoteWorkingDir, true);
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

            saveProviderState(context, AWS_JOB_ID, jobId);
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
            clearProviderState(context, AWS_INSTANCE_ID, AWS_SECURITY_GROUP_ID, AWS_KEY_PAIR_NAME);
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

            String publicIp = getProviderState(context, AWS_PUBLIC_IP);
            String sshCredentialToken = getProviderState(context, AWS_SSH_CREDENTIAL_TOKEN);

            if (publicIp == null || sshCredentialToken == null) {
                return new DagTaskResult.Success(
                        "No AWS instance info for monitoring process " + context.getProcessId());
            }

            SSHJAgentAdapter adapter = initSSHJAgentAdapter(sshCredentialToken, publicIp, context);
            JobManagerSpec config = jobSubmissionSupport.getJobManagerConfiguration(context.getComputeResource());

            for (JobModel job : jobs) {
                pollJobUntilSaturated(adapter, config, job);
            }

            return new DagTaskResult.Success("Job monitoring completed for process " + context.getProcessId());

        } catch (Exception e) {
            logger.error(
                    "Error during AWS job monitoring for process {} — continuing (non-critical)",
                    context.getProcessId(),
                    e);
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
    // Provider state helpers — read/write AWS context from providerContext JSON
    // -------------------------------------------------------------------------

    private void saveProviderState(TaskContext context, String key, String value) {
        try {
            context.getDagState().put(key, value);
            Map<String, String> contextMap = loadProviderContext(context);
            contextMap.put(key, value);
            context.getProcessModel().setProviderContext(MAPPER.writeValueAsString(contextMap));
            processService.updateProcess(context.getProcessModel(), context.getProcessId());
        } catch (Exception e) {
            logger.warn("Failed to persist provider state key '{}' for process {}", key, context.getProcessId(), e);
        }
    }

    private String getProviderState(TaskContext context, String key) {
        String value = context.getDagState().get(key);
        if (value != null) return value;
        try {
            return loadProviderContext(context).get(key);
        } catch (Exception e) {
            logger.warn("Failed to load provider context for process {}", context.getProcessId(), e);
            return null;
        }
    }

    private Map<String, String> loadProviderContext(TaskContext context) throws Exception {
        String json = context.getProcessModel().getProviderContext();
        if (json == null || json.isEmpty()) return new HashMap<>();
        return MAPPER.readValue(json, new TypeReference<>() {});
    }

    private void clearProviderState(TaskContext context, String... keys) {
        try {
            Map<String, String> contextMap = loadProviderContext(context);
            for (String key : keys) {
                contextMap.put(key, null);
                context.getDagState().remove(key);
            }
            context.getProcessModel().setProviderContext(MAPPER.writeValueAsString(contextMap));
            processService.updateProcess(context.getProcessModel(), context.getProcessId());
        } catch (Exception e) {
            logger.warn("Failed to clear provider state for process {}", context.getProcessId(), e);
        }
    }

    /**
     * Resolves an AWS configuration value for the given key using a two-tier lookup:
     * <ol>
     *   <li>ResourceBinding metadata — operator-level config stored on the binding for this
     *       compute resource and gateway. Takes priority so that gateway admins can configure
     *       defaults without requiring per-experiment scheduling overrides.</li>
     *   <li>Experiment resourceSchedule — per-experiment override supplied at launch time.
     *       Falls through to {@code defaultValue} when neither source carries the key.</li>
     * </ol>
     *
     * @param binding      the binding for this compute resource/gateway pair (may be {@code null})
     * @param context      the current task context
     * @param key          the configuration key (e.g. {@code "awsRegion"})
     * @param defaultValue fallback value when neither binding metadata nor schedule carries the key
     * @return the resolved configuration value, or {@code defaultValue} if not found
     */
    private String resolveAwsConfig(
            ResourceBindingEntity binding, TaskContext context, String key, String defaultValue) {
        // 1. Try binding metadata
        if (binding != null) {
            String fromBinding = ResourceProfileAdapter.getMetadataString(binding.getMetadata(), key);
            if (fromBinding != null && !fromBinding.isBlank()) {
                return fromBinding;
            }
        }
        // 2. Try per-experiment resourceSchedule
        try {
            var schedule = context.getProcessModel().getResourceSchedule();
            if (schedule != null && schedule.get(key) != null) {
                return schedule.get(key).toString();
            }
        } catch (Exception e) {
            logger.warn(
                    "Could not read resource schedule key '{}' for process {}; using default '{}'",
                    key,
                    context.getProcessId(),
                    defaultValue,
                    e);
        }
        return defaultValue;
    }

    // -------------------------------------------------------------------------
    // Monitor helpers
    // -------------------------------------------------------------------------

    private void pollJobUntilSaturated(AgentAdapter adapter, JobManagerSpec config, JobModel job) {
        try {
            var monitorCommand = config.getMonitorCommand(job.getJobId());
            if (monitorCommand.isEmpty()) {
                logger.info("No monitor command for job {} — skipping", job.getJobId());
                return;
            }

            CommandOutput output = adapter.executeCommand(monitorCommand.get().getRawCommand(), null);
            if (output.getExitCode() != 0) {
                logger.warn(
                        "Monitor command failed for job {}: stdout={}, stderr={}",
                        job.getJobId(),
                        output.getStdOut(),
                        output.getStdError());
                return;
            }

            var jobStatus = config.getParser().parseJobStatus(job.getJobId(), output.getStdOut());
            if (jobStatus != null) {
                logger.info("Job {} status: {}", job.getJobId(), jobStatus.getState());
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
        var vpcs = ec2.describeVpcs(req -> req.filters(f -> f.name("is-default").values("true")))
                .vpcs();
        if (vpcs.isEmpty()) {
            throw new Exception("No default VPC found in the AWS account");
        }
        String vpcId = vpcs.get(0).vpcId();
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
        try (Ec2Client ec2Client = awsTaskUtil.buildEc2Client(token, context.getGatewayId(), region)) {
            DescribeInstancesRequest request =
                    DescribeInstancesRequest.builder().instanceIds(instanceId).build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            if (response.reservations().isEmpty()
                    || response.reservations().get(0).instances().isEmpty()) {
                throw new Exception(
                        "No instance found with ID: " + instanceId + " for process: " + context.getProcessId());
            }

            Instance instance = response.reservations().get(0).instances().get(0);
            InstanceStateName state = instance.state().name();
            logger.info("Instance {} state: {} for process {}", instanceId, state, context.getProcessId());

            if (state == InstanceStateName.RUNNING) {
                String publicIp = instance.publicIpAddress();
                if (publicIp == null || publicIp.isEmpty()) {
                    throw new Exception("Instance " + instanceId + " is running but has no public IP yet");
                }
                return publicIp;
            }

            if (state == InstanceStateName.SHUTTING_DOWN
                    || state == InstanceStateName.TERMINATED
                    || state == InstanceStateName.STOPPED) {
                throw new Exception(
                        "Instance entered failure state: " + state + " for process: " + context.getProcessId());
            }

            // Still pending — throw so Temporal retries
            throw new Exception("Instance " + instanceId + " not yet running (state: " + state + ")");
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

    private SSHJAgentAdapter initSSHJAgentAdapter(
            String sshCredentialToken, String publicIpAddress, TaskContext context) throws Exception {
        SSHJAgentAdapter adapter = new SSHJAgentAdapter(computeResourceAdapter, credentialStoreService);
        SSHCredential sshCredential =
                credentialStoreService.getSSHCredential(sshCredentialToken, context.getGatewayId());
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
