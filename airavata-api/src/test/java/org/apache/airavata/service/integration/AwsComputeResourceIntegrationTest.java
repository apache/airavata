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

import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.GroupResourceProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for AWS compute resources.
 */
@DisplayName("AWS Compute Resource Integration Tests")
public class AwsComputeResourceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private ComputeResourceService computeResourceService;

    @Autowired
    private GroupResourceProfileService groupResourceProfileService;

    @Nested
    @DisplayName("AWS Compute Resource Registration")
    class AwsResourceRegistrationTests {

        @Test
        @DisplayName("Should register AWS compute resource")
        void shouldRegisterAwsResource() throws AppCatalogException {
            // Arrange
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
            assertThat(resourceId).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getComputeResourceId()).isEqualTo(resourceId);
            assertThat(retrieved.getHostName()).contains("amazonaws.com");
        }

        @Test
        @DisplayName("Should register AWS compute resource with region")
        void shouldRegisterAwsResourceWithRegion() throws AppCatalogException {
            // Arrange
            String region = "us-west-2";
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource(region);

            // Act
            String resourceId = computeResourceService.addComputeResource(computeResource);
            ComputeResourceDescription retrieved = computeResourceService.getComputeResource(resourceId);

            // Assert
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
            // Arrange
            ComputeResourceDescription computeResource = TestDataFactory.createAwsComputeResource("us-east-1");
            String computeResourceId = computeResourceService.addComputeResource(computeResource);

            GroupResourceProfile groupProfile = TestDataFactory.createGroupResourceProfile(TEST_GATEWAY_ID);
            GroupComputeResourcePreference preference =
                    TestDataFactory.createAwsGroupComputeResourcePreference(computeResourceId, groupProfile.getGroupResourceProfileId());
            groupProfile.addToComputePreferences(preference);

            // Act
            String groupProfileId = groupResourceProfileService.addGroupResourceProfile(groupProfile);

            // Assert
            assertThat(groupProfileId).isNotNull();
            GroupComputeResourcePreference retrieved =
                    groupResourceProfileService.getGroupComputeResourcePreference(computeResourceId, groupProfileId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getResourceType()).isEqualTo(ResourceType.AWS);
        }
    }
}

