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
package org.apache.airavata.interfaces;

import java.util.List;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;

/**
 * Registry operations for gateway, user, and group resource profiles, plus usage reporting.
 */
public interface ResourceProfileRegistry {

    // --- Gateway resource profile operations ---
    GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws Exception;

    List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws Exception;

    String registerGatewayResourceProfile(GatewayResourceProfile grp) throws Exception;

    boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile grp) throws Exception;

    boolean deleteGatewayResourceProfile(String gatewayID) throws Exception;

    ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayId, String computeResourceId)
            throws Exception;

    List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws Exception;

    boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception;

    boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception;

    boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws Exception;

    StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws Exception;

    List<StoragePreference> getAllGatewayStoragePreferences(String gatewayId) throws Exception;

    boolean addGatewayStoragePreference(String gatewayID, String storageResourceId, StoragePreference storagePreference)
            throws Exception;

    boolean updateGatewayStoragePreference(String gatewayID, String storageId, StoragePreference storagePreference)
            throws Exception;

    boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws Exception;

    // --- Group resource profile operations ---
    GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws Exception;

    boolean isGroupResourceProfileExists(String groupResourceProfileId) throws Exception;

    GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws Exception;

    boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws Exception;

    List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId) throws Exception;

    List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId) throws Exception;

    // --- User resource profile operations ---
    UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws Exception;

    boolean isUserResourceProfileExists(String userId, String gatewayId) throws Exception;

    UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId) throws Exception;

    boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws Exception;

    // --- User storage preference operations ---
    UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId) throws Exception;

    boolean addUserStoragePreference(
            String userId, String gatewayId, String storageResourceId, UserStoragePreference storagePreference)
            throws Exception;

    boolean updateUserStoragePreference(
            String userId, String gatewayId, String storageId, UserStoragePreference storagePreference)
            throws Exception;

    boolean deleteUserStoragePreference(String userId, String gatewayId, String storageId) throws Exception;

    List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayId)
            throws Exception;

    List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId) throws Exception;

    List<UserResourceProfile> getAllUserResourceProfiles() throws Exception;

    // --- Group resource profile mutators ---
    String createGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws Exception;

    void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws Exception;

    boolean removeGroupResourceProfile(String groupResourceProfileId) throws Exception;

    List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws Exception;

    boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId) throws Exception;

    boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws Exception;

    boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws Exception;

    ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws Exception;

    BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws Exception;

    List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws Exception;

    // --- User resource profile mutators ---
    String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws Exception;

    boolean updateUserResourceProfile(String userId, String gatewayId, UserResourceProfile userResourceProfile)
            throws Exception;

    boolean deleteUserResourceProfile(String userId, String gatewayId) throws Exception;

    boolean addUserComputeResourcePreference(
            String userId, String gatewayId, String computeResourceId, UserComputeResourcePreference preference)
            throws Exception;

    boolean updateUserComputeResourcePreference(
            String userId, String gatewayId, String computeResourceId, UserComputeResourcePreference preference)
            throws Exception;

    boolean deleteUserComputeResourcePreference(String userId, String gatewayId, String computeResourceId)
            throws Exception;

    // --- Usage reporting ---
    boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId) throws Exception;

    GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws Exception;
}
