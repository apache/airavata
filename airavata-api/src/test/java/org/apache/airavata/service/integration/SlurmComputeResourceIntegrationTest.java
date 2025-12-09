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
import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.GroupResourceProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for SLURM compute resources.
 */
@DisplayName("SLURM Compute Resource Integration Tests")
public class SlurmComputeResourceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private ComputeResourceService computeResourceService;

    @Autowired
    private GroupResourceProfileService groupResourceProfileService;

    @Nested
    @DisplayName("SLURM Compute Resource Registration")
    class SlurmResourceRegistrationTests {

        @Test
        @DisplayName("Should register SLURM compute resource with batch queues")
        void shouldRegisterSlurmResourceWithBatchQueues() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getBatchQueues()).isNotEmpty();
            assertThat(retrieved.getBatchQueues().get(0).getQueueName()).isEqualTo("normal");
        }

        @Test
        @DisplayName("Should register SLURM compute resource with job manager commands")
        void shouldRegisterSlurmResourceWithJobManagerCommands() throws AppCatalogException {
            // Arrange
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
            computeResource.getJobSubmissionInterfaces().get(0).setJobSubmissionInterfaceId(submissionId);

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
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
            // Arrange
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
            assertThat(retrieved.getBatchQueues()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should set default queue")
        void shouldSetDefaultQueue() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();
            computeResource.getBatchQueues().get(0).setIsDefaultQueue(true);

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
            assertThat(retrieved.getBatchQueues()).isNotEmpty();
            assertThat(retrieved.getBatchQueues().get(0).isIsDefaultQueue()).isTrue();
        }
    }

    @Nested
    @DisplayName("SLURM Group Resource Profile Configuration")
    class SlurmGroupResourceProfileTests {

        @Test
        @DisplayName("Should create group resource profile with SLURM preference")
        void shouldCreateGroupResourceProfileWithSlurmPreference() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription computeResource = createSlurmComputeResourceWithQueues();
            String computeResourceId = computeResourceService.addComputeResource(computeResource);

            GroupResourceProfile groupProfile = TestDataFactory.createGroupResourceProfile(TEST_GATEWAY_ID);
            GroupComputeResourcePreference preference =
                    TestDataFactory.createSlurmGroupComputeResourcePreference(computeResourceId, groupProfile.getGroupResourceProfileId());
            groupProfile.addToComputePreferences(preference);

            // Act
            String groupProfileId = groupResourceProfileService.addGroupResourceProfile(groupProfile);

            // Assert
            assertThat(groupProfileId).isNotNull();
            GroupComputeResourcePreference retrieved =
                    groupResourceProfileService.getGroupComputeResourcePreference(computeResourceId, groupProfileId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getResourceType()).isEqualTo(ResourceType.SLURM);
        }
    }

    private ComputeResourceDescription createSlurmComputeResourceWithQueues() {
        ComputeResourceDescription computeResource = TestDataFactory.createSlurmComputeResource("slurm-host");
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
        return computeResource;
    }
}

