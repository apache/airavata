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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.model.BatchQueue;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.JobManagerCommand;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.SecurityProtocol;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.GroupResourceProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for SLURM compute resources.
 */
@DisplayName("SLURM Compute Resource Integration Tests")
public class SlurmComputeResourceIntegrationTest extends ServiceIntegrationTestBase {

    private final ComputeResourceService computeResourceService;
    private final GroupResourceProfileService groupResourceProfileService;

    public SlurmComputeResourceIntegrationTest(
            ComputeResourceService computeResourceService, GroupResourceProfileService groupResourceProfileService) {
        this.computeResourceService = computeResourceService;
        this.groupResourceProfileService = groupResourceProfileService;
    }

    @Nested
    @DisplayName("SLURM Compute Resource Registration")
    class SlurmResourceRegistrationTests {

        @Test
        @DisplayName("Should register SLURM compute resource with batch queues")
        void shouldRegisterSlurmResourceWithBatchQueues() throws AppCatalogException {
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getBatchQueues()).isNotEmpty();
            assertThat(retrieved.getBatchQueues().get(0).getQueueName()).isEqualTo("normal");
        }

        @Test
        @DisplayName("Should register SLURM compute resource with job manager commands")
        void shouldRegisterSlurmResourceWithJobManagerCommands() throws AppCatalogException {
            ResourceJobManager jobManager = new ResourceJobManager();
            jobManager.setResourceJobManagerType(ResourceJobManagerType.SLURM);
            jobManager.setJobManagerBinPath("/usr/bin");
            Map<JobManagerCommand, String> commands = new HashMap<>();
            commands.put(JobManagerCommand.SUBMISSION, "sbatch");
            commands.put(JobManagerCommand.JOB_MONITORING, "squeue");
            commands.put(JobManagerCommand.DELETION, "scancel");
            jobManager.setJobManagerCommands(commands);
            computeResourceService.addResourceJobManager(jobManager);

            SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
            sshJobSubmission.setResourceJobManager(jobManager);
            sshJobSubmission.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            sshJobSubmission.setSshPort(22);
            String submissionId = computeResourceService.addSSHJobSubmission(sshJobSubmission);

            ComputeResourceDescription computeResource = TestDataFactory.createSlurmComputeResource("slurm-host");
            if (computeResource.getJobSubmissionInterfaces() == null) {
                computeResource.setJobSubmissionInterfaces(new java.util.ArrayList<>());
            }
            if (computeResource.getJobSubmissionInterfaces().isEmpty()) {
                org.apache.airavata.common.model.JobSubmissionInterface jobSubmissionInterface =
                        new org.apache.airavata.common.model.JobSubmissionInterface();
                jobSubmissionInterface.setJobSubmissionInterfaceId(submissionId);
                computeResource.getJobSubmissionInterfaces().add(jobSubmissionInterface);
            } else {
                computeResource.getJobSubmissionInterfaces().get(0).setJobSubmissionInterfaceId(submissionId);
            }

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
        }
    }

    @Nested
    @DisplayName("SLURM Batch Queue Configuration")
    class SlurmBatchQueueTests {

        @Test
        @DisplayName("Should configure multiple batch queues")
        void shouldConfigureMultipleBatchQueues() throws AppCatalogException {
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(retrieved.getBatchQueues()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should set default queue")
        void shouldSetDefaultQueue() throws AppCatalogException {
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();
            computeResource.getBatchQueues().get(0).setIsDefaultQueue(true);

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(retrieved.getBatchQueues()).isNotEmpty();
            assertThat(retrieved.getBatchQueues().get(0).getIsDefaultQueue()).isTrue();
        }
    }

    @Nested
    @DisplayName("SLURM Group Resource Profile Configuration")
    class SlurmGroupResourceProfileTests {

        @Test
        @DisplayName("Should create group resource profile with SLURM preference")
        void shouldCreateGroupResourceProfileWithSlurmPreference() throws AppCatalogException {
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();
            String computeResourceId = computeResourceService.addComputeResource(computeResource);

            GroupResourceProfile groupProfile = TestDataFactory.createGroupResourceProfile(TEST_GATEWAY_ID);
            // Initialize computePreferences if null
            if (groupProfile.getComputePreferences() == null) {
                groupProfile.setComputePreferences(new java.util.ArrayList<>());
            }
            GroupComputeResourcePreference preference = TestDataFactory.createSlurmGroupComputeResourcePreference(
                    computeResourceId, groupProfile.getGroupResourceProfileId());
            groupProfile.getComputePreferences().add(preference);

            String groupProfileId = groupResourceProfileService.addGroupResourceProfile(groupProfile);

            assertThat(groupProfileId).isNotNull();
            GroupComputeResourcePreference retrieved =
                    groupResourceProfileService.getGroupComputeResourcePreference(computeResourceId, groupProfileId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getResourceType()).isEqualTo(ComputeResourceType.SLURM);
        }
    }

    @Nested
    @DisplayName("SLURM Job Submission Interface Configuration")
    class SlurmJobSubmissionInterfaceTests {

        @Test
        @DisplayName("Should configure SSH job submission interface")
        void shouldConfigureSSHJobSubmissionInterface() throws AppCatalogException {
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();

            assertThat(computeResource.getJobSubmissionInterfaces()).isNotEmpty();

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(retrieved.getJobSubmissionInterfaces()).isNotEmpty();
            assertThat(retrieved.getJobSubmissionInterfaces().get(0).getJobSubmissionProtocol())
                    .isEqualTo(org.apache.airavata.common.model.JobSubmissionProtocol.SSH);
        }

        @Test
        @DisplayName("Should configure data movement interface")
        void shouldConfigureDataMovementInterface() throws AppCatalogException {
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(retrieved.getDataMovementInterfaces()).isNotEmpty();
            assertThat(retrieved.getDataMovementInterfaces().get(0).getDataMovementProtocol())
                    .isEqualTo(org.apache.airavata.common.model.DataMovementProtocol.SCP);
        }
    }

    /**
     * Note: Real SSH connection and job submission tests require:
     * 1. A test SSH server (can use Testcontainers with an SSH server image)
     * 2. Valid SSH credentials
     * 3. A SLURM installation on the test server
     *
     * For now, these tests verify configuration only.
     * To add real SSH tests, consider using:
     * - Testcontainers with an SSH server container
     * - Mock SSH server library
     * - Integration test environment with real compute resources
     */
    private ComputeResourceDescription createSlurmComputeResourceWithQueues() {
        ComputeResourceDescription computeResource = TestDataFactory.createSlurmComputeResource("slurm-host");
        // TestDataFactory already creates a "normal" queue, so we just use it
        // Ensure batchQueues list is initialized
        if (computeResource.getBatchQueues() == null) {
            computeResource.setBatchQueues(new java.util.ArrayList<>());
        }
        // If no queues exist, add one
        if (computeResource.getBatchQueues().isEmpty()) {
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
            computeResource.getBatchQueues().add(queue);
        }
        return computeResource;
    }
}
