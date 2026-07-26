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
package org.apache.airavata.compute.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.model.UserStoragePreferencePK;
import org.apache.airavata.compute.repository.*;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.iam.repository.GatewayRepository;
import org.apache.airavata.iam.repository.UserProfileRepository;
import org.apache.airavata.models.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.models.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.models.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.models.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.models.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.models.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.models.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.models.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.models.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.models.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.storage.repository.StoragePrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ResourceProfileRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceProfileRegistryService.class);

    private final GatewayRepository gatewayExistenceProvider;
    private final UserProfileRepository userProfileProvider;
    private final UserResourceProfileRepository userResourceProfileRepository;
    private final UserStoragePreferenceRepository userStoragePreferenceRepository;
    private final StoragePrefRepository gatewayStoragePreferenceProvider;
    private final GatewayUsageReportingCommandRepository usageReportingProvider;

    public ResourceProfileRegistryService(
            GatewayRepository gatewayExistenceProvider,
            UserProfileRepository userProfileProvider,
            GatewayUsageReportingCommandRepository usageReportingProvider,
            StoragePrefRepository gatewayStoragePreferenceProvider) {
        this.gatewayExistenceProvider = gatewayExistenceProvider;
        this.userProfileProvider = userProfileProvider;
        this.userResourceProfileRepository = new UserResourceProfileRepository();
        this.userStoragePreferenceRepository = new UserStoragePreferenceRepository();
        this.gatewayStoragePreferenceProvider = gatewayStoragePreferenceProvider;
        this.usageReportingProvider = usageReportingProvider;
    }

    // =========================================================================
    // ResourceProfileRegistry interface methods
    // =========================================================================

    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile gatewayResourceProfile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            logger.debug("Retrieved gateway profile for gateway {}", gatewayID);
            return gatewayResourceProfile;
        } catch (Exception e) {
            logger.error("Error retrieving gateway resource profile {}: {}", gatewayID, e.getMessage(), e);
            throw e;
        }
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)) {
                logger.error("Gateway profile {} does not exist", gatewayID);
                throw new RuntimeException("Gateway profile does not exist");
            }
            if (!computeResourceRepository.isComputeResourceExists(computeResourceId)) {
                logger.error("Compute resource {} does not exist", computeResourceId);
                throw new RuntimeException("Compute resource does not exist");
            }
            ComputeResourcePreference computeResourcePreference = gwyResourceProfileRepository
                    .getComputeResourcePreference(gatewayID, computeResourceId);
            logger.debug("Retrieved gateway compute resource preference for gateway {} and compute resource {}",
                    gatewayID, computeResourceId);
            return computeResourcePreference;
        } catch (Exception e) {
            logger.error("Error reading gateway compute resource preference for gateway {} and resource {}: {}",
                    gatewayID, computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getGatewayProfile(gatewayID).computeResourcePreferences();
        } catch (Exception e) {
            logger.error("Error reading gateway compute resource preferences for {}: {}", gatewayID, e.getMessage(), e);
            throw e;
        }
    }

    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            return gatewayStoragePreferenceProvider.getGatewayStoragePreference(gatewayID, storageId);
        } catch (Exception e) {
            logger.error("Error reading gateway storage preference for gateway {} storage {}: {}", gatewayID, storageId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            return gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(gatewayID);
        } catch (Exception e) {
            logger.error("Error reading gateway storage preferences for {}: {}", gatewayID, e.getMessage(), e);
            throw e;
        }
    }

    // --- Group resource profile operations ---

    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("Group resource profile {} not found", groupResourceProfileId);
                throw new RuntimeException("Group resource profile not found");
            }
            return groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error retrieving group resource profile {}: {}", groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error checking group resource profile {} existence: {}", groupResourceProfileId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            GroupComputeResourcePreference groupComputeResourcePreference = groupResourceProfileRepository
                    .getGroupComputeResourcePreference(
                            computeResourceId, groupResourceProfileId);
            if (groupComputeResourcePreference == null) {
                logger.error("Group compute resource preference not found for resource {} in profile {}",
                        computeResourceId, groupResourceProfileId);
                throw new RuntimeException("Group compute resource preference not found");
            }
            return groupComputeResourcePreference;
        } catch (Exception e) {
            logger.error("Error retrieving group compute resource preference for {} in {}: {}", computeResourceId,
                    groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.isGroupComputeResourcePreferenceExists(
                    computeResourceId, groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error checking group compute resource preference existence for {} in {}: {}",
                    computeResourceId, groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error retrieving batch queue resource policies for {}: {}", groupResourceProfileId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error retrieving compute resource policies for {}: {}", groupResourceProfileId,
                    e.getMessage(), e);
            throw e;
        }
    }

    // --- User resource profile operations ---

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayId) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayId);
                throw new RuntimeException("User does not exist");
            }
            UserResourceProfile userResourceProfile = userResourceProfileRepository.getUserResourceProfile(userId,
                    gatewayId);
            logger.debug("Retrieved user resource profile for user {} in gateway {}", userId, gatewayId);
            return userResourceProfile;
        } catch (Exception e) {
            logger.error("Error retrieving user resource profile for {} in {}: {}", userId, gatewayId, e.getMessage(),
                    e);
            throw e;
        }
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayId) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayId);
                throw new RuntimeException("User does not exist");
            }
            return userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error checking user resource profile existence for {} in {}: {}", userId, gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                logger.error("User resource profile does not exist for user {} in gateway {}", userId, gatewayID);
                throw new RuntimeException("User resource profile does not exist");
            }
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!computeResourceRepository.isComputeResourceExists(userComputeResourceId)) {
                logger.error("Compute resource {} does not exist", userComputeResourceId);
                throw new RuntimeException("Compute resource does not exist");
            }
            UserComputeResourcePreference userComputeResourcePreference = userResourceProfileRepository
                    .getUserComputeResourcePreference(
                            userId, gatewayID, userComputeResourceId);
            logger.debug("Retrieved user compute resource preference for user {} gateway {} resource {}", userId,
                    gatewayID, userComputeResourceId);
            return userComputeResourcePreference;
        } catch (Exception e) {
            logger.error("Error reading user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayID, userComputeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) != null
                    && userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                return userResourceProfileRepository.isUserComputeResourcePreferenceExists(
                        userId, gatewayID, computeResourceId);
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayID, computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    // --- Usage reporting ---

    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId) throws Exception {
        try {
            return usageReportingProvider.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId);
        } catch (Exception e) {
            logger.error("Error checking gateway usage reporting availability for gateway {} resource {}: {}",
                    gatewayId, computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws Exception {
        try {
            if (usageReportingProvider.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId)) {
                return usageReportingProvider.getGatewayUsageReportingCommand(gatewayId, computeResourceId);
            } else {
                logger.error("No usage reporting information for gateway {} resource {}", gatewayId, computeResourceId);
                throw new RuntimeException("No usage reporting information found");
            }
        } catch (Exception e) {
            logger.error("Error retrieving gateway reporting command for gateway {} resource {}: {}", gatewayId,
                    computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // Additional resource profile methods (not yet on the interface)
    // =========================================================================

    // --- Gateway resource profile CRUD ---

    public boolean deleteGatewayResourceProfile(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            new GwyResourceProfileRepository().delete(gatewayID);
            return true;
        } catch (Exception e) {
            logger.error("Error removing gateway resource profile {}: {}", gatewayID, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            return new GwyResourceProfileRepository()
                    .removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
        } catch (Exception e) {
            logger.error("Error removing gateway compute resource preference for {} resource {}: {}", gatewayID,
                    computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            return gatewayStoragePreferenceProvider.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (Exception e) {
            logger.error("Error deleting gateway storage preference for {} storage {}: {}", gatewayID, storageId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws Exception {
        try {
            return new GwyResourceProfileRepository().getAllGatewayProfiles();
        } catch (Exception e) {
            logger.error("Error retrieving all gateway profiles: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            return gatewayStoragePreferenceProvider.updateGatewayStoragePreference(
                    gatewayID, storageId, storagePreference);
        } catch (Exception e) {
            logger.error("Error updating gateway storage preference for {} storage {}: {}", gatewayID, storageId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            GwyResourceProfileRepository r = new GwyResourceProfileRepository();
            GatewayResourceProfile profile = r.getGatewayProfile(gatewayID);
            GatewayResourceProfile.Builder b = profile.toBuilder();
            java.util.List<ComputeResourcePreference> prefs = profile.computeResourcePreferences();
            for (int i = 0; i < prefs.size(); i++) {
                if (prefs.get(i).computeResourceId().equals(computeResourceId)) {
                    b.removeComputeResourcePreferences(i);
                    break;
                }
            }
            b.addComputeResourcePreferences(crp);
            r.updateGatewayResourceProfile(b.build());
            return true;
        } catch (Exception e) {
            logger.error("Error updating gateway compute resource preference for {} resource {}: {}", gatewayID,
                    computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean addGatewayStoragePreference(String gatewayID, String storageResourceId, StoragePreference dsp)
            throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            return gatewayStoragePreferenceProvider.addGatewayStoragePreference(gatewayID, storageResourceId, dsp);
        } catch (Exception e) {
            logger.error("Error adding gateway storage preference for {} storage {}: {}", gatewayID, storageResourceId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            GwyResourceProfileRepository r = new GwyResourceProfileRepository();
            if (!r.isExists(gatewayID)) {
                logger.error("Gateway resource profile {} does not exist", gatewayID);
                throw new RuntimeException("Gateway resource profile does not exist");
            }
            GatewayResourceProfile profile = r.getGatewayProfile(gatewayID);
            profile = profile.toBuilder().addComputeResourcePreferences(crp).build();
            r.updateGatewayResourceProfile(profile);
            return true;
        } catch (Exception e) {
            logger.error("Error registering gateway resource profile preference for {} resource {}: {}", gatewayID,
                    computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile grp) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway {} does not exist", gatewayID);
                throw new RuntimeException("Gateway does not exist");
            }
            new GwyResourceProfileRepository().updateGatewayResourceProfile(grp);
            return true;
        } catch (Exception e) {
            logger.error("Error updating gateway resource profile {}: {}", gatewayID, e.getMessage(), e);
            throw e;
        }
    }

    public String registerGatewayResourceProfile(GatewayResourceProfile grp) throws Exception {
        try {
            if (!validateString(grp.gatewayId())) {
                logger.error("Gateway ID is empty");
                throw new RuntimeException("Cannot create gateway profile with empty gateway id");
            }
            if (!isGatewayExistInternal(grp.gatewayId())) {
                logger.error("Gateway {} does not exist", grp.gatewayId());
                throw new RuntimeException("Gateway does not exist");
            }
            return new GwyResourceProfileRepository().addGatewayResourceProfile(grp);
        } catch (Exception e) {
            logger.error("Error registering gateway resource profile {}: {}", grp.gatewayId(), e.getMessage(), e);
            throw e;
        }
    }

    // --- Group resource profile CRUD ---

    public String createGroupResourceProfile(GroupResourceProfile grp) throws Exception {
        try {
            if (!isGatewayExistInternal(grp.gatewayId())) {
                logger.error("Gateway {} does not exist", grp.gatewayId());
                throw new RuntimeException("Gateway does not exist");
            }
            return new GroupResourceProfileRepository().addGroupResourceProfile(grp);
        } catch (Exception e) {
            logger.error("Error creating group resource profile for gateway {}: {}", grp.gatewayId(), e.getMessage(),
                    e);
            throw e;
        }
    }

    public void updateGroupResourceProfile(GroupResourceProfile grp) throws Exception {
        try {
            GroupResourceProfileRepository r = new GroupResourceProfileRepository();
            if (!r.isGroupResourceProfileExists(grp.groupResourceProfileId())) {
                logger.error("Group resource profile {} not found", grp.groupResourceProfileId());
                throw new RuntimeException("Group resource profile not found");
            }
            r.updateGroupResourceProfile(grp);
        } catch (Exception e) {
            logger.error("Error updating group resource profile {}: {}", grp.groupResourceProfileId(),
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository r = new GroupResourceProfileRepository();
            if (!r.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("Group resource profile {} not found", groupResourceProfileId);
                throw new RuntimeException("Group resource profile not found");
            }
            return r.removeGroupResourceProfile(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error removing group resource profile {}: {}", groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws Exception {
        try {
            return new GroupResourceProfileRepository()
                    .getAllGroupResourceProfiles(gatewayId, accessibleGroupResProfileIds);
        } catch (Exception e) {
            logger.error("Error retrieving group resource list for gateway {}: {}", gatewayId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId) throws Exception {
        try {
            new GroupResourceProfileRepository()
                    .removeGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
            return true;
        } catch (Exception e) {
            logger.error("Error removing group compute preference for resource {} profile {}: {}", computeResourceId,
                    groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            new GroupResourceProfileRepository().removeComputeResourcePolicy(resourcePolicyId);
            return true;
        } catch (Exception e) {
            logger.error("Error removing group compute resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            new GroupResourceProfileRepository().removeBatchQueueResourcePolicy(resourcePolicyId);
            return true;
        } catch (Exception e) {
            logger.error("Error removing group batch queue resource policy {}: {}", resourcePolicyId, e.getMessage(),
                    e);
            throw e;
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            ComputeResourcePolicy p = new GroupResourceProfileRepository().getComputeResourcePolicy(resourcePolicyId);
            if (p == null) {
                logger.error("Group compute resource policy {} not found", resourcePolicyId);
                throw new RuntimeException("Group compute resource policy not found");
            }
            return p;
        } catch (Exception e) {
            logger.error("Error retrieving group compute resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            BatchQueueResourcePolicy p = new GroupResourceProfileRepository()
                    .getBatchQueueResourcePolicy(resourcePolicyId);
            if (p == null) {
                logger.error("Batch queue resource policy {} not found", resourcePolicyId);
                throw new RuntimeException("Batch queue resource policy not found");
            }
            return p;
        } catch (Exception e) {
            logger.error("Error retrieving batch queue resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws Exception {
        try {
            return new GroupResourceProfileRepository().getAllGroupComputeResourcePreferences(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error retrieving group compute resource preferences for profile {}: {}",
                    groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    // --- User resource profile CRUD ---

    public String registerUserResourceProfile(UserResourceProfile urp) throws Exception {
        try {
            if (!validateString(urp.userId()) || !validateString(urp.gatewayId())) {
                logger.error("User ID or gateway ID is empty");
                throw new RuntimeException("Cannot create user resource profile with empty user/gateway id");
            }
            if (userProfileProvider.getUserProfileByIdAndGateWay(urp.userId(), urp.gatewayId()) == null) {
                logger.error("User {} does not exist in gateway {}", urp.userId(), urp.gatewayId());
                throw new RuntimeException("User does not exist");
            }
            return userResourceProfileRepository.addUserResourceProfile(urp);
        } catch (Exception e) {
            logger.error("Error registering user resource profile for user {} gateway {}: {}", urp.userId(),
                    urp.gatewayId(), e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile urp)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, urp);
            return true;
        } catch (Exception e) {
            logger.error("Error updating user resource profile for user {} gateway {}: {}", userId, gatewayID,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            userResourceProfileRepository.removeUserResourceProfile(userId, gatewayID);
            return true;
        } catch (Exception e) {
            logger.error("Error removing user resource profile for user {} gateway {}: {}", userId, gatewayID,
                    e.getMessage(), e);
            throw e;
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws Exception {
        try {
            return userResourceProfileRepository.getAllUserResourceProfiles();
        } catch (Exception e) {
            logger.error("Error retrieving all user resource profiles: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean addUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference pref)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                logger.error("User resource profile does not exist for user {} gateway {}", userId, gatewayID);
                throw new RuntimeException("User resource profile does not exist");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            profile = profile.toBuilder().addUserComputeResourcePreferences(pref).build();
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            return true;
        } catch (Exception e) {
            logger.error("Error adding user compute resource preference for user {} gateway {} resource {}: {}", userId,
                    gatewayID, computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dsp) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                logger.error("User resource profile does not exist for user {} gateway {}", userId, gatewayID);
                throw new RuntimeException("User resource profile does not exist");
            }
            UserStoragePreference pref = dsp.toBuilder().setStorageResourceId(storageResourceId).build();
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            profile = profile.toBuilder().addUserStoragePreferences(pref).build();
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            return true;
        } catch (Exception e) {
            logger.error("Error adding user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayID, storageResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            UserStoragePreferencePK pk = new UserStoragePreferencePK();
            pk.setUserId(userId);
            pk.setGatewayId(gatewayID);
            pk.setStorageResourceId(storageId);
            return userStoragePreferenceRepository.get(pk);
        } catch (Exception e) {
            logger.error("Error reading user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayID, storageId, e.getMessage(), e);
            throw e;
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws Exception {
        try {
            if (!isUserExists(gatewayID, userId)) {
                logger.error("User resource profile does not exist for user {} gateway {}", userId, gatewayID);
                throw new RuntimeException("User resource profile does not exist");
            }
            return userResourceProfileRepository
                    .getUserResourceProfile(userId, gatewayID)
                    .userComputeResourcePreferences();
        } catch (Exception e) {
            logger.error("Error reading user compute resource preferences for user {} gateway {}: {}", userId,
                    gatewayID, e.getMessage(), e);
            throw e;
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID) throws Exception {
        try {
            if (!isUserExists(gatewayID, userId)) {
                logger.error("User resource profile does not exist for user {} gateway {}", userId, gatewayID);
                throw new RuntimeException("User resource profile does not exist");
            }
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put(DBConstants.UserStoragePreference.USER_ID, userId);
            queryParams.put(DBConstants.UserStoragePreference.GATEWAY_ID, gatewayID);
            return userStoragePreferenceRepository.select(
                    QueryConstants.GET_ALL_USER_STORAGE_PREFERENCE, -1, 0, queryParams);
        } catch (Exception e) {
            logger.error("Error reading user storage preferences for user {} gateway {}: {}", userId, gatewayID,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference pref)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            java.util.List<UserComputeResourcePreference> prefs = profile.userComputeResourcePreferences();
            UserResourceProfile.Builder b = profile.toBuilder();
            for (int i = 0; i < prefs.size(); i++) {
                if (prefs.get(i).computeResourceId().equals(computeResourceId)) {
                    b.removeUserComputeResourcePreferences(i);
                    break;
                }
            }
            b.addUserComputeResourcePreferences(pref);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, b.build());
            return true;
        } catch (Exception e) {
            logger.error("Error updating user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayID, computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference pref) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            java.util.List<UserStoragePreference> prefs = profile.userStoragePreferences();
            UserResourceProfile.Builder b = profile.toBuilder();
            for (int i = 0; i < prefs.size(); i++) {
                if (prefs.get(i).storageResourceId().equals(storageId)) {
                    b.removeUserStoragePreferences(i);
                    break;
                }
            }
            b.addUserStoragePreferences(pref);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, b.build());
            return true;
        } catch (Exception e) {
            logger.error("Error updating user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayID, storageId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            return userResourceProfileRepository.removeUserComputeResourcePreferenceFromGateway(
                    userId, gatewayID, computeResourceId);
        } catch (Exception e) {
            logger.error("Error deleting user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayID, computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("User {} does not exist in gateway {}", userId, gatewayID);
                throw new RuntimeException("User does not exist");
            }
            UserStoragePreferencePK pk = new UserStoragePreferencePK();
            pk.setUserId(userId);
            pk.setGatewayId(gatewayID);
            pk.setStorageResourceId(storageId);
            return userStoragePreferenceRepository.delete(pk);
        } catch (Exception e) {
            logger.error("Error deleting user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayID, storageId, e.getMessage(), e);
            throw e;
        }
    }

    // --- Usage reporting CRUD ---

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws Exception {
        try {
            usageReportingProvider.addGatewayUsageReportingCommand(command);
        } catch (Exception e) {
            logger.error("Error adding reporting information for gateway {} resource {}: {}", command.gatewayId(),
                    command.computeResourceId(), e.getMessage(), e);
            throw e;
        }
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId) throws Exception {
        try {
            usageReportingProvider.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (Exception e) {
            logger.error("Error removing reporting information for gateway {} resource {}: {}", gatewayId,
                    computeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private boolean isGatewayExistInternal(String gatewayId) throws Exception {
        try {
            return gatewayExistenceProvider.isGatewayExist(gatewayId);
        } catch (Exception e) {
            logger.error("Error checking gateway {} existence: {}", gatewayId, e.getMessage(), e);
            throw e;
        }
    }

    private boolean isUserExists(String gatewayId, String userName) throws Exception {
        try {
            return userProfileProvider.getUserProfileByIdAndGateWay(userName, gatewayId) != null;
        } catch (Exception e) {
            logger.error("Error verifying user {} in gateway {}: {}", userName, gatewayId, e.getMessage(), e);
            throw e;
        }
    }

    private boolean validateString(String name) {
        return name != null && !name.equals("") && name.trim().length() != 0;
    }
}
