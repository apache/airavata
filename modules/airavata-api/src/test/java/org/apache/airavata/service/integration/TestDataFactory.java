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
package org.apache.airavata.service.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.common.model.BatchQueue;
import org.apache.airavata.common.model.CloudJobSubmission;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.DataMovementInterface;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.JobManagerCommand;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.common.model.SCPDataMovement;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.SecurityProtocol;
import org.apache.airavata.common.model.Status;
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.security.model.AuthzToken;

/**
 * Factory class for creating test data objects.
 * Provides convenient methods to create commonly used test entities.
 */
public class TestDataFactory {

    /**
     * Creates a test Gateway.
     */
    public static Gateway createTestGateway(String gatewayId) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId(gatewayId);
        gateway.setGatewayName("Test Gateway " + gatewayId);
        // Use unique URL based on gatewayId to prevent duplicate detection across tests
        gateway.setGatewayURL("https://" + gatewayId + ".example.com");
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
        gateway.setRequesterUsername("test-admin");
        return gateway;
    }

    /**
     * Creates a test UserProfile.
     */
    public static UserProfile createTestUserProfile(String userId, String gatewayId) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setGatewayId(gatewayId);
        userProfile.setAiravataInternalUserId(userId + "@" + gatewayId);
        userProfile.setFirstName("Test");
        userProfile.setLastName("User");
        if (userProfile.getEmails() == null) {
            userProfile.setEmails(new java.util.ArrayList<>());
        }
        userProfile.getEmails().add(userId + "@example.com");
        userProfile.setState(Status.ACTIVE);
        long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
        userProfile.setCreationTime(currentTime);
        userProfile.setLastAccessTime(currentTime);
        return userProfile;
    }

    /**
     * Creates a test AuthzToken.
     */
    public static AuthzToken createTestAuthzToken(String gatewayId, String username) {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken("test-access-token-" + UUID.randomUUID());
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(org.apache.airavata.common.utils.Constants.GATEWAY_ID, gatewayId);
        claimsMap.put(org.apache.airavata.common.utils.Constants.USER_NAME, username);
        authzToken.setClaimsMap(claimsMap);
        return authzToken;
    }

    /**
     * Creates a test Project.
     */
    public static Project createTestProject(String projectName, String gatewayId) {
        Project project = new Project();
        project.setProjectID(UUID.randomUUID().toString());
        project.setName(projectName);
        project.setDescription("Test project: " + projectName);
        project.setGatewayId(gatewayId);
        project.setOwner("test-user");
        return project;
    }

    /**
     * Creates a test ExperimentModel.
     */
    public static ExperimentModel createTestExperiment(String experimentName, String projectId, String gatewayId) {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId(UUID.randomUUID().toString());
        experiment.setProjectId(projectId);
        experiment.setGatewayId(gatewayId);
        experiment.setExperimentName(experimentName);
        experiment.setUserName("test-user");
        experiment.setDescription("Test experiment: " + experimentName);
        return experiment;
    }

    /**
     * Creates a test SLURM ComputeResourceDescription.
     */
    public static ComputeResourceDescription createSlurmComputeResource(String hostName) {
        ComputeResourceDescription computeResource = new ComputeResourceDescription();
        computeResource.setComputeResourceId(UUID.randomUUID().toString());
        computeResource.setHostName(hostName);
        computeResource.setResourceDescription("Test SLURM Compute Resource");
        computeResource.setEnabled(true);

        // Create SLURM ResourceJobManager
        ResourceJobManager resourceJobManager = new ResourceJobManager();
        resourceJobManager.setResourceJobManagerId(UUID.randomUUID().toString());
        resourceJobManager.setResourceJobManagerType(ResourceJobManagerType.SLURM);
        resourceJobManager.setJobManagerBinPath("/usr/bin");
        Map<JobManagerCommand, String> commands = new HashMap<>();
        commands.put(JobManagerCommand.SUBMISSION, "sbatch");
        commands.put(JobManagerCommand.JOB_MONITORING, "squeue");
        commands.put(JobManagerCommand.DELETION, "scancel");
        resourceJobManager.setJobManagerCommands(commands);

        // Create SSH Job Submission
        SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
        sshJobSubmission.setResourceJobManager(resourceJobManager);
        sshJobSubmission.setJobSubmissionInterfaceId(UUID.randomUUID().toString());
        sshJobSubmission.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        sshJobSubmission.setSshPort(22);

        // Create SCP Data Movement
        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setDataMovementInterfaceId(UUID.randomUUID().toString());
        scpDataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        scpDataMovement.setSshPort(22);

        DataMovementInterface dataMovementInterface = new DataMovementInterface();
        dataMovementInterface.setDataMovementInterfaceId(scpDataMovement.getDataMovementInterfaceId());
        dataMovementInterface.setDataMovementProtocol(DataMovementProtocol.SCP);
        dataMovementInterface.setPriorityOrder(0);

        // Create Batch Queues
        List<BatchQueue> batchQueues = new ArrayList<>();
        BatchQueue queue = new BatchQueue();
        queue.setQueueName("normal");
        queue.setQueueDescription("Normal queue");
        queue.setMaxRunTime(4320);
        queue.setMaxNodes(8);
        queue.setMaxProcessors(64);
        queue.setMaxJobsInQueue(100);
        queue.setMaxMemory(512000);
        queue.setCpuPerNode(8);
        queue.setDefaultNodeCount(1);
        queue.setDefaultCPUCount(1);
        queue.setDefaultWalltime(30);
        queue.setIsDefaultQueue(true);
        batchQueues.add(queue);

        computeResource.setBatchQueues(batchQueues);

        // Create JobSubmissionInterface and add to compute resource
        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId(sshJobSubmission.getJobSubmissionInterfaceId());
        jobSubmissionInterface.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);
        jobSubmissionInterface.setPriorityOrder(0);

        List<JobSubmissionInterface> jobSubmissionInterfaces = new ArrayList<>();
        jobSubmissionInterfaces.add(jobSubmissionInterface);
        computeResource.setJobSubmissionInterfaces(jobSubmissionInterfaces);

        // Add data movement interface to compute resource
        List<DataMovementInterface> dataMovementInterfaces = new ArrayList<>();
        dataMovementInterfaces.add(dataMovementInterface);
        computeResource.setDataMovementInterfaces(dataMovementInterfaces);

        return computeResource;
    }

    /**
     * Creates a test AWS ComputeResourceDescription.
     */
    public static ComputeResourceDescription createAwsComputeResource(String region) {
        ComputeResourceDescription computeResource = new ComputeResourceDescription();
        computeResource.setComputeResourceId(UUID.randomUUID().toString());
        computeResource.setHostName("ec2." + region + ".amazonaws.com");
        computeResource.setResourceDescription("Test AWS Compute Resource");
        computeResource.setEnabled(true);

        // Create Cloud Job Submission for AWS
        CloudJobSubmission cloudJobSubmission = new CloudJobSubmission();
        cloudJobSubmission.setJobSubmissionInterfaceId(UUID.randomUUID().toString());
        cloudJobSubmission.setSecurityProtocol(SecurityProtocol.SSH_KEYS);

        return computeResource;
    }

    /**
     * Creates a test StorageResourceDescription.
     */
    public static StorageResourceDescription createStorageResource(String hostName) {
        StorageResourceDescription storageResource = new StorageResourceDescription();
        storageResource.setStorageResourceId(UUID.randomUUID().toString());
        storageResource.setHostName(hostName);
        return storageResource;
    }

    /**
     * Creates a test GatewayResourceProfile.
     */
    public static GatewayResourceProfile createGatewayResourceProfile(String gatewayId) {
        GatewayResourceProfile profile = new GatewayResourceProfile();
        profile.setGatewayID(gatewayId);
        return profile;
    }

    /**
     * Creates a test GroupResourceProfile.
     */
    public static GroupResourceProfile createGroupResourceProfile(String gatewayId) {
        GroupResourceProfile profile = new GroupResourceProfile();
        profile.setGroupResourceProfileId(UUID.randomUUID().toString());
        profile.setGatewayId(gatewayId);
        profile.setGroupResourceProfileName("Test Group Resource Profile");
        return profile;
    }

    /**
     * Creates a test GroupComputeResourcePreference for SLURM.
     */
    public static GroupComputeResourcePreference createSlurmGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePreference preference = new GroupComputeResourcePreference();
        preference.setComputeResourceId(computeResourceId);
        preference.setGroupResourceProfileId(groupResourceProfileId);
        preference.setResourceType(ComputeResourceType.SLURM);
        preference.setOverridebyAiravata(true);
        preference.setLoginUserName("testuser");
        return preference;
    }

    /**
     * Creates a test GroupComputeResourcePreference for AWS.
     */
    public static GroupComputeResourcePreference createAwsGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        GroupComputeResourcePreference preference = new GroupComputeResourcePreference();
        preference.setComputeResourceId(computeResourceId);
        preference.setGroupResourceProfileId(groupResourceProfileId);
        preference.setResourceType(ComputeResourceType.AWS);
        preference.setOverridebyAiravata(true);
        return preference;
    }

    /**
     * Creates a test SSHCredential.
     */
    public static SSHCredential createSSHCredential(String gatewayId, String username) {
        SSHCredential sshCredential = new SSHCredential();
        sshCredential.setGatewayId(gatewayId);
        sshCredential.setUsername(username);
        sshCredential.setDescription("Test SSH Credential");
        // Note: In real tests, you would generate actual key pairs
        return sshCredential;
    }

    /**
     * Creates a test Gateway with all required fields.
     */
    public static Gateway createTestGatewayWithDefaults() {
        return createTestGateway("test-gateway-" + UUID.randomUUID().toString());
    }

    /**
     * Creates a test UserProfile with defaults.
     */
    public static UserProfile createTestUserProfileWithDefaults() {
        String userId = "test-user-" + UUID.randomUUID().toString();
        String gatewayId = "test-gateway";
        return createTestUserProfile(userId, gatewayId);
    }

    /**
     * Creates a test Project with defaults.
     */
    public static Project createTestProjectWithDefaults(String gatewayId) {
        return createTestProject("test-project-" + UUID.randomUUID().toString(), gatewayId);
    }

    /**
     * Creates a test ExperimentModel with defaults.
     */
    public static ExperimentModel createTestExperimentWithDefaults(String projectId, String gatewayId) {
        return createTestExperiment("test-experiment-" + UUID.randomUUID().toString(), projectId, gatewayId);
    }

    /**
     * Creates a minimal test ComputeResourceDescription (no job submission or data movement).
     */
    public static ComputeResourceDescription createMinimalComputeResource(String hostName) {
        ComputeResourceDescription computeResource = new ComputeResourceDescription();
        computeResource.setComputeResourceId(UUID.randomUUID().toString());
        computeResource.setHostName(hostName);
        computeResource.setResourceDescription("Test Compute Resource");
        computeResource.setEnabled(true);
        // Note: ComputeResourceType.LOCAL doesn't exist - using SLURM as default
        // The compute resource type is determined by job submission interfaces, not this field
        return computeResource;
    }
}
