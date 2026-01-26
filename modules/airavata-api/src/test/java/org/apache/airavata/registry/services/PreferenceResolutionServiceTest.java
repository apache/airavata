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
package org.apache.airavata.registry.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for PreferenceResolutionService.
 * Tests the multi-level preference resolution with USER > GROUP > GATEWAY precedence.
 */
@DisplayName("PreferenceResolutionService - Multi-level preference resolution")
public class PreferenceResolutionServiceTest extends TestBase {

    @Autowired
    private PreferenceResolutionService preferenceResolutionService;

    private static final String TEST_GATEWAY_ID = "test-gateway";
    private static final String TEST_USER_ID = "test-user";
    private static final String TEST_GROUP_ID = "test-group";
    private static final String TEST_COMPUTE_RESOURCE_ID = "compute-resource-1";
    private static final String TEST_STORAGE_RESOURCE_ID = "storage-resource-1";

    @BeforeEach
    void setUp() {
        // Clean up any existing preferences
        preferenceResolutionService.deleteAllPreferences(
                PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, TEST_GATEWAY_ID, PreferenceLevel.GATEWAY);
        preferenceResolutionService.deleteAllPreferences(
                PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, TEST_GROUP_ID, PreferenceLevel.GROUP);
        preferenceResolutionService.deleteAllPreferences(
                PreferenceResourceType.COMPUTE,
                TEST_COMPUTE_RESOURCE_ID,
                TEST_USER_ID + "@" + TEST_GATEWAY_ID,
                PreferenceLevel.USER);
    }

    @Nested
    @DisplayName("Preference CRUD Operations")
    class PreferenceCRUDTests {

        @Test
        @DisplayName("Should set and get preference at gateway level")
        void shouldSetAndGetGatewayLevelPreference() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");

            Map<String, String> prefs = preferenceResolutionService.getPreferencesAtLevel(
                    PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, TEST_GATEWAY_ID, PreferenceLevel.GATEWAY);

            assertThat(prefs).containsEntry("scratchDir", "/scratch/gateway");
        }

        @Test
        @DisplayName("Should set and get preference at group level")
        void shouldSetAndGetGroupLevelPreference() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GROUP_ID,
                    PreferenceLevel.GROUP,
                    "scratchDir",
                    "/scratch/group");

            Map<String, String> prefs = preferenceResolutionService.getPreferencesAtLevel(
                    PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, TEST_GROUP_ID, PreferenceLevel.GROUP);

            assertThat(prefs).containsEntry("scratchDir", "/scratch/group");
        }

        @Test
        @DisplayName("Should set and get preference at user level")
        void shouldSetAndGetUserLevelPreference() {
            String airavataInternalUserId = TEST_USER_ID + "@" + TEST_GATEWAY_ID;

            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    airavataInternalUserId,
                    PreferenceLevel.USER,
                    "scratchDir",
                    "/scratch/user");

            Map<String, String> prefs = preferenceResolutionService.getPreferencesAtLevel(
                    PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, airavataInternalUserId, PreferenceLevel.USER);

            assertThat(prefs).containsEntry("scratchDir", "/scratch/user");
        }

        @Test
        @DisplayName("Should delete preference")
        void shouldDeletePreference() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");

            preferenceResolutionService.deletePreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir");

            Map<String, String> prefs = preferenceResolutionService.getPreferencesAtLevel(
                    PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, TEST_GATEWAY_ID, PreferenceLevel.GATEWAY);

            assertThat(prefs).doesNotContainKey("scratchDir");
        }

        @Test
        @DisplayName("Should update existing preference")
        void shouldUpdateExistingPreference() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/v1");

            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/v2");

            Map<String, String> prefs = preferenceResolutionService.getPreferencesAtLevel(
                    PreferenceResourceType.COMPUTE, TEST_COMPUTE_RESOURCE_ID, TEST_GATEWAY_ID, PreferenceLevel.GATEWAY);

            assertThat(prefs).containsEntry("scratchDir", "/scratch/v2");
        }
    }

    @Nested
    @DisplayName("Preference Resolution with Precedence")
    class PreferenceResolutionTests {

        @Test
        @DisplayName("Should resolve USER level over GROUP and GATEWAY")
        void shouldResolveUserOverGroupAndGateway() {
            // Set preferences at all levels
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");

            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GROUP_ID,
                    PreferenceLevel.GROUP,
                    "scratchDir",
                    "/scratch/group");

            String airavataInternalUserId = TEST_USER_ID + "@" + TEST_GATEWAY_ID;
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    airavataInternalUserId,
                    PreferenceLevel.USER,
                    "scratchDir",
                    "/scratch/user");

            // Resolve
            Map<String, String> resolved = preferenceResolutionService.resolveComputePreferences(
                    TEST_GATEWAY_ID, TEST_USER_ID, Collections.singletonList(TEST_GROUP_ID), TEST_COMPUTE_RESOURCE_ID);

            assertThat(resolved).containsEntry("scratchDir", "/scratch/user");
        }

        @Test
        @DisplayName("Should resolve GROUP level when USER is not set")
        void shouldResolveGroupWhenUserNotSet() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");

            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GROUP_ID,
                    PreferenceLevel.GROUP,
                    "scratchDir",
                    "/scratch/group");

            Map<String, String> resolved = preferenceResolutionService.resolveComputePreferences(
                    TEST_GATEWAY_ID, TEST_USER_ID, Collections.singletonList(TEST_GROUP_ID), TEST_COMPUTE_RESOURCE_ID);

            assertThat(resolved).containsEntry("scratchDir", "/scratch/group");
        }

        @Test
        @DisplayName("Should resolve GATEWAY level when GROUP and USER are not set")
        void shouldResolveGatewayWhenGroupAndUserNotSet() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");

            Map<String, String> resolved = preferenceResolutionService.resolveComputePreferences(
                    TEST_GATEWAY_ID, TEST_USER_ID, Collections.emptyList(), TEST_COMPUTE_RESOURCE_ID);

            assertThat(resolved).containsEntry("scratchDir", "/scratch/gateway");
        }

        @Test
        @DisplayName("Should resolve multiple preferences with different inheritance")
        void shouldResolveMultiplePreferencesWithDifferentInheritance() {
            // GATEWAY level: scratchDir and queueName
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "queueName",
                    "default");

            // GROUP level: only queueName (overrides)
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GROUP_ID,
                    PreferenceLevel.GROUP,
                    "queueName",
                    "priority");

            // USER level: only maxWallTime (new)
            String airavataInternalUserId = TEST_USER_ID + "@" + TEST_GATEWAY_ID;
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    airavataInternalUserId,
                    PreferenceLevel.USER,
                    "maxWallTime",
                    "3600");

            Map<String, String> resolved = preferenceResolutionService.resolveComputePreferences(
                    TEST_GATEWAY_ID, TEST_USER_ID, Collections.singletonList(TEST_GROUP_ID), TEST_COMPUTE_RESOURCE_ID);

            // scratchDir from GATEWAY (no override)
            assertThat(resolved).containsEntry("scratchDir", "/scratch/gateway");
            // queueName from GROUP (overrides GATEWAY)
            assertThat(resolved).containsEntry("queueName", "priority");
            // maxWallTime from USER (new)
            assertThat(resolved).containsEntry("maxWallTime", "3600");
        }

        @Test
        @DisplayName("Should handle user in multiple groups")
        void shouldHandleUserInMultipleGroups() {
            String group1 = "group-1";
            String group2 = "group-2";

            // GROUP1: scratchDir
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    group1,
                    PreferenceLevel.GROUP,
                    "scratchDir",
                    "/scratch/group1");

            // GROUP2: queueName
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    group2,
                    PreferenceLevel.GROUP,
                    "queueName",
                    "group2-queue");

            List<String> userGroups = Arrays.asList(group1, group2);
            Map<String, String> resolved = preferenceResolutionService.resolveComputePreferences(
                    TEST_GATEWAY_ID, TEST_USER_ID, userGroups, TEST_COMPUTE_RESOURCE_ID);

            // Both group preferences should be merged
            assertThat(resolved).containsEntry("scratchDir", "/scratch/group1");
            assertThat(resolved).containsEntry("queueName", "group2-queue");
        }
    }

    @Nested
    @DisplayName("Storage Preferences")
    class StoragePreferenceTests {

        @Test
        @DisplayName("Should resolve storage preferences")
        void shouldResolveStoragePreferences() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.STORAGE,
                    TEST_STORAGE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "fileSystemRoot",
                    "/data/gateway");

            preferenceResolutionService.setPreference(
                    PreferenceResourceType.STORAGE,
                    TEST_STORAGE_RESOURCE_ID,
                    TEST_GROUP_ID,
                    PreferenceLevel.GROUP,
                    "fileSystemRoot",
                    "/data/group");

            Map<String, String> resolved = preferenceResolutionService.resolveStoragePreferences(
                    TEST_GATEWAY_ID, TEST_USER_ID, Collections.singletonList(TEST_GROUP_ID), TEST_STORAGE_RESOURCE_ID);

            assertThat(resolved).containsEntry("fileSystemRoot", "/data/group");
        }
    }

    @Nested
    @DisplayName("Single Preference Resolution")
    class SinglePreferenceResolutionTests {

        @Test
        @DisplayName("Should resolve single preference key")
        void shouldResolveSinglePreference() {
            preferenceResolutionService.setPreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    PreferenceLevel.GATEWAY,
                    "scratchDir",
                    "/scratch/gateway");

            String value = preferenceResolutionService.resolvePreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    TEST_USER_ID,
                    Collections.emptyList(),
                    "scratchDir");

            assertThat(value).isEqualTo("/scratch/gateway");
        }

        @Test
        @DisplayName("Should return null for non-existent preference")
        void shouldReturnNullForNonExistentPreference() {
            String value = preferenceResolutionService.resolvePreference(
                    PreferenceResourceType.COMPUTE,
                    TEST_COMPUTE_RESOURCE_ID,
                    TEST_GATEWAY_ID,
                    TEST_USER_ID,
                    Collections.emptyList(),
                    "nonExistentKey");

            assertThat(value).isNull();
        }
    }
}
