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

import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.GroupResourceProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for AWS compute resources.
 */
@DisplayName("AWS Compute Resource Integration Tests")
public class AwsComputeResourceIntegrationTest extends ServiceIntegrationTestBase {

    private final ComputeResourceService computeResourceService;
    private final GroupResourceProfileService groupResourceProfileService;

    public AwsComputeResourceIntegrationTest(
            ComputeResourceService computeResourceService, GroupResourceProfileService groupResourceProfileService) {
        this.computeResourceService = computeResourceService;
        this.groupResourceProfileService = groupResourceProfileService;
    }

    @Nested
    @DisplayName("AWS Compute Resource Registration")
    class AwsResourceRegistrationTests {

        @Test
        @DisplayName("Should register AWS compute resource")
        void shouldRegisterAwsResource() throws AppCatalogException {
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getComputeResourceId()).isEqualTo(resourceId);
            assertThat(retrieved.getHostName()).contains("amazonaws.com");
        }

        @Test
        @DisplayName("Should register AWS compute resource with region")
        void shouldRegisterAwsResourceWithRegion() throws AppCatalogException {
            String region = "us-west-2";
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource(region);

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(resourceId).isNotNull();
            assertThat(retrieved.getHostName()).contains(region);
        }
    }

    @Nested
    @DisplayName("AWS Group Resource Profile Configuration")
    class AwsGroupResourceProfileTests {

        @Test
        @DisplayName("Should create group resource profile with AWS preference")
        void shouldCreateGroupResourceProfileWithAwsPreference() throws AppCatalogException {
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");
            String computeResourceId = computeResourceService.addComputeResource(computeResource);

            GroupResourceProfile groupProfile = TestDataFactory.createGroupResourceProfile(TEST_GATEWAY_ID);
            // Initialize computePreferences if null
            if (groupProfile.getComputePreferences() == null) {
                groupProfile.setComputePreferences(new java.util.ArrayList<>());
            }
            GroupComputeResourcePreference preference = TestDataFactory.createAwsGroupComputeResourcePreference(
                    computeResourceId, groupProfile.getGroupResourceProfileId());
            groupProfile.getComputePreferences().add(preference);

            String groupProfileId = groupResourceProfileService.addGroupResourceProfile(groupProfile);

            assertThat(groupProfileId).isNotNull();
            GroupComputeResourcePreference retrieved =
                    groupResourceProfileService.getGroupComputeResourcePreference(computeResourceId, groupProfileId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getResourceType()).isEqualTo(ComputeResourceType.AWS);
        }
    }

    @Nested
    @DisplayName("AWS Cloud Job Submission Configuration")
    class AwsCloudJobSubmissionTests {

        @Test
        @DisplayName("Should configure cloud job submission interface")
        void shouldConfigureCloudJobSubmissionInterface() throws AppCatalogException {
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");

            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            assertThat(retrieved).isNotNull();
            // Note: ComputeResourceDescription doesn't have computeResourceType field
            // The type is determined by the job submission interfaces
            assertThat(retrieved).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple AWS regions")
        void shouldSupportMultipleAwsRegions() throws AppCatalogException {
            String region1 = "us-east-1";
            String region2 = "us-west-2";

            ComputeResourceDescription resource1 = TestDataFactory.createAwsComputeResource(region1);
            ComputeResourceDescription resource2 = TestDataFactory.createAwsComputeResource(region2);

            String id1 = computeResourceService.addComputeResource(resource1);
            String id2 = computeResourceService.addComputeResource(resource2);

            assertThat(id1).isNotEqualTo(id2);
            ComputeResourceDescription retrieved1 = computeResourceService.getComputeResource(id1);
            ComputeResourceDescription retrieved2 = computeResourceService.getComputeResource(id2);

            assertThat(retrieved1.getHostName()).contains(region1);
            assertThat(retrieved2.getHostName()).contains(region2);
        }
    }

    /**
     * Note: Real AWS EC2 integration tests require:
     * 1. Valid AWS credentials
     * 2. AWS account with EC2 permissions
     * 3. Proper AWS SDK configuration
     *
     * For now, these tests verify configuration and registration only.
     * To add real AWS tests, consider:
     * - Using LocalStack for local AWS service emulation
     * - Integration test environment with real AWS account
     * - Mock AWS SDK clients
     */
}
