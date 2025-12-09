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
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;

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
        gateway.setGatewayURL("https://test-gateway.example.com");
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
        userProfile.addToEmails(userId + "@example.com");
        userProfile.setState(Status.ACTIVE);
        userProfile.setCreationTime(System.currentTimeMillis());
        userProfile.setLastAccessTime(System.currentTimeMillis());
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
        sshJobSubmission.setSecurityProtocol(org.apache.airavata.model.data.movement.SecurityProtocol.SSH_KEYS);
        sshJobSubmission.setSshPort(22);

        // Create SCP Data Movement
        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setDataMovementInterfaceId(UUID.randomUUID().toString());
        scpDataMovement.setSecurityProtocol(org.apache.airavata.model.data.movement.SecurityProtocol.SSH_KEYS);
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
        cloudJobSubmission.setSecurityProtocol(org.apache.airavata.model.data.movement.SecurityProtocol.SSH_KEYS);

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
        preference.setResourceType(ResourceType.SLURM);
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
        preference.setResourceType(ResourceType.AWS);
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
}

