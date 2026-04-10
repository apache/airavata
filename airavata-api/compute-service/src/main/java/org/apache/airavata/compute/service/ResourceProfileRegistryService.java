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
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.GatewayExistenceProvider;
import org.apache.airavata.interfaces.GatewayStoragePreferenceProvider;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.interfaces.ResourceProfileRegistry;
import org.apache.airavata.interfaces.UsageReportingProvider;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.*;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ResourceProfileRegistryService implements ResourceProfileRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ResourceProfileRegistryService.class);

    private final GatewayExistenceProvider gatewayExistenceProvider;
    private final UserProfileProvider userProfileProvider;
    private final UserResourceProfileRepository userResourceProfileRepository;
    private final UserStoragePreferenceRepository userStoragePreferenceRepository;
    private final GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider;
    private final UsageReportingProvider usageReportingProvider;

    public ResourceProfileRegistryService(
            GatewayExistenceProvider gatewayExistenceProvider,
            UserProfileProvider userProfileProvider,
            UsageReportingProvider usageReportingProvider,
            GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider) {
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

    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Internal error");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            GatewayResourceProfile gatewayResourceProfile = gwyResourceProfileRepository.getGatewayProfile(gatewayID);
            logger.debug("Airavata retrieved gateway profile with gateway id : " + gatewayID);
            return gatewayResourceProfile;
        } catch (Exception e) {
            logger.error(gatewayID, "Error while retrieving gateway resource profile...", e);
            throw new RegistryException(
                    "Error while retrieving gateway resource profile. More info : " + e.getMessage());
        }
    }

    @Override
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayID)) {
                logger.error(
                        gatewayID,
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw new RegistryException(
                        "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
            }
            if (!computeResourceRepository.isComputeResourceExists(computeResourceId)) {
                logger.error(
                        computeResourceId,
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw new RegistryException(
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
            }
            ComputeResourcePreference computeResourcePreference =
                    gwyResourceProfileRepository.getComputeResourcePreference(gatewayID, computeResourceId);
            logger.debug("Airavata retrieved gateway compute resource preference with gateway id : " + gatewayID
                    + " and for compute resoruce id : " + computeResourceId);
            return computeResourcePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            throw new RegistryException(
                    "Error while reading gateway compute resource preference. More info : " + e.getMessage());
        }
    }

    @Override
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
            return gwyResourceProfileRepository.getGatewayProfile(gatewayID).getComputeResourcePreferencesList();
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            throw new RegistryException(
                    "Error while reading gateway compute resource preferences. More info : " + e.getMessage());
        }
    }

    @Override
    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gatewayStoragePreferenceProvider.getGatewayStoragePreference(gatewayID, storageId);
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway storage preference...", e);
            throw new RegistryException(
                    "Error while reading gateway storage preference. More info : " + e.getMessage());
        }
    }

    @Override
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(gatewayID);
        } catch (Exception e) {
            logger.error(gatewayID, "Error while reading gateway storage preferences...", e);
            throw new RegistryException(
                    "Error while reading gateway storage preferences. More info : " + e.getMessage());
        }
    }

    // --- Group resource profile operations ---

    @Override
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            if (!groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId)) {
                logger.error("No group resource profile found with matching gatewayId and groupResourceProfileId");
                throw new RegistryException(
                        "No group resource profile found with matching gatewayId and groupResourceProfileId");
            }
            return groupResourceProfileRepository.getGroupResourceProfile(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving group resource profile...", e);
            throw new RegistryException("Error while retrieving group resource profile. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.isGroupResourceProfileExists(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving group resource profile...", e);
            throw new RegistryException("Error while retrieving group resource profile. More info : " + e.getMessage());
        }
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            GroupComputeResourcePreference groupComputeResourcePreference =
                    groupResourceProfileRepository.getGroupComputeResourcePreference(
                            computeResourceId, groupResourceProfileId);
            if (!(groupComputeResourcePreference != null)) {
                logger.error("GroupComputeResourcePreference not found");
                throw new RegistryException("GroupComputeResourcePreference not found ");
            }
            return groupComputeResourcePreference;
        } catch (Exception e) {
            logger.error("Error while retrieving group compute resource preference", e);
            throw new RegistryException(
                    "Error while retrieving group compute resource preference. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.isGroupComputeResourcePreferenceExists(
                    computeResourceId, groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving group compute resource preference", e);
            throw new RegistryException(
                    "Error while retrieving group compute resource preference. More info : " + e.getMessage());
        }
    }

    @Override
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving retrieving Group Batch Queue Resource policy list", e);
            throw new RegistryException(
                    "Error while retrieving retrieving Group Batch Queue Resource policy list. More info : "
                            + e.getMessage());
        }
    }

    @Override
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws Exception {
        try {
            GroupResourceProfileRepository groupResourceProfileRepository = new GroupResourceProfileRepository();
            return groupResourceProfileRepository.getAllGroupComputeResourcePolicies(groupResourceProfileId);
        } catch (Exception e) {
            logger.error("Error while retrieving retrieving Group Compute Resource policy list", e);
            throw new RegistryException(
                    "Error while retrieving retrieving Group Compute Resource policy list. More info : "
                            + e.getMessage());
        }
    }

    // --- User resource profile operations ---

    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayId) == null) {
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Internal error");
            }
            UserResourceProfile userResourceProfile =
                    userResourceProfileRepository.getUserResourceProfile(userId, gatewayId);
            logger.debug("Airavata retrieved User resource profile with user id : " + userId);
            return userResourceProfile;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving user resource profile...", e);
            throw new RegistryException("Error while retrieving user resource profile. More info : " + e.getMessage());
        } catch (RegistryException e) {
            logger.error("Error while retrieving user resource profile...", e);
            throw new RegistryException("Error while retrieving user resource profile. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayId) == null) {
                logger.error("user does not exist.Please provide a valid gateway id...");
                throw new RegistryException("Internal error");
            }
            return userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while checking existence of user resource profile...", e);
            throw new RegistryException(
                    "Error while checking existence of user resource profile. More info : " + e.getMessage());
        } catch (RegistryException e) {
            logger.error("Error while checking existence of user resource profile...", e);
            throw new RegistryException(
                    "Error while checking existence of user resource profile. More info : " + e.getMessage());
        }
    }

    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                logger.error("user does not exist.Please provide a valid user id...");
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new RegistryException("User resource profile with user id'" + userId + " &  gateway Id"
                        + gatewayID + "' does not exist!!!");
            }
            ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();
            if (!computeResourceRepository.isComputeResourceExists(userComputeResourceId)) {
                logger.error(
                        userComputeResourceId,
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw new RegistryException(
                        "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
            }
            UserComputeResourcePreference userComputeResourcePreference =
                    userResourceProfileRepository.getUserComputeResourcePreference(
                            userId, gatewayID, userComputeResourceId);
            logger.debug("Airavata retrieved user compute resource preference with gateway id : " + gatewayID
                    + " and for compute resoruce id : " + userComputeResourceId);
            return userComputeResourcePreference;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading user compute resource preference...", e);
            throw new RegistryException(
                    "Error while reading user compute resource preference. More info : " + e.getMessage());
        } catch (RegistryException e) {
            logger.error(userId, "Error while retrieving user resource profile...", e);
            throw new RegistryException("Error while retrieving user resource profile. More info : " + e.getMessage());
        }
    }

    @Override
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) != null
                    && userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                return userResourceProfileRepository.isUserComputeResourcePreferenceExists(
                        userId, gatewayID, computeResourceId);
            }
            return false;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while fetching compute resource preference", e);
            throw new RegistryException(
                    "Error while fetching compute resource preference. More info : " + e.getMessage());
        }
    }

    // --- Usage reporting ---

    @Override
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId) throws Exception {
        try {
            return usageReportingProvider.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId);
        } catch (Exception e) {
            String message = "Failed to check the availability to find the reporting information for the gateway "
                    + gatewayId + " and compute resource " + computeResourceId;
            logger.error(message, e);
            throw new RegistryException(message + ". More info " + e.getMessage());
        }
    }

    @Override
    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws Exception {
        try {
            if (usageReportingProvider.isGatewayUsageReportingCommandExists(gatewayId, computeResourceId)) {
                return usageReportingProvider.getGatewayUsageReportingCommand(gatewayId, computeResourceId);
            } else {
                String message = "No usage reporting information for the gateway " + gatewayId
                        + " and compute resource " + computeResourceId;
                logger.error(message);
                throw new RegistryException(message);
            }
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            String message = "Failed to check the availability to find the reporting information for the gateway "
                    + gatewayId + " and compute resource " + computeResourceId;
            logger.error(message, e);
            throw new RegistryException(message + ". More info " + e.getMessage());
        }
    }

    // =========================================================================
    // Additional resource profile methods (not yet on the interface)
    // =========================================================================

    // --- Gateway resource profile CRUD ---

    public boolean deleteGatewayResourceProfile(String gatewayID) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            new GwyResourceProfileRepository().delete(gatewayID);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while removing gateway resource profile. More info : " + e.getMessage());
        }
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return new GwyResourceProfileRepository()
                    .removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while updating gateway compute resource preference. More info : " + e.getMessage());
        }
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gatewayStoragePreferenceProvider.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while deleting gateway storage preference. More info : " + e.getMessage());
        }
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws Exception {
        try {
            return new GwyResourceProfileRepository().getAllGatewayProfiles();
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
        }
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gatewayStoragePreferenceProvider.updateGatewayStoragePreference(
                    gatewayID, storageId, storagePreference);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while updating gateway storage preference. More info : " + e.getMessage());
        }
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository r = new GwyResourceProfileRepository();
            GatewayResourceProfile profile = r.getGatewayProfile(gatewayID);
            GatewayResourceProfile.Builder b = profile.toBuilder();
            java.util.List<ComputeResourcePreference> prefs = profile.getComputeResourcePreferencesList();
            for (int i = 0; i < prefs.size(); i++) {
                if (prefs.get(i).getComputeResourceId().equals(computeResourceId)) {
                    b.removeComputeResourcePreferences(i);
                    break;
                }
            }
            b.addComputeResourcePreferences(crp);
            r.updateGatewayResourceProfile(b.build());
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while updating gateway compute resource preference. More info : " + e.getMessage());
        }
    }

    public boolean addGatewayStoragePreference(String gatewayID, String storageResourceId, StoragePreference dsp)
            throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return gatewayStoragePreferenceProvider.addGatewayStoragePreference(gatewayID, storageResourceId, dsp);
        } catch (Exception e) {
            throw new RegistryException("Error while adding gateway storage preference. More info : " + e.getMessage());
        }
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            GwyResourceProfileRepository r = new GwyResourceProfileRepository();
            if (!(r.isExists(gatewayID))) {
                throw new RegistryException("Gateway resource profile '" + gatewayID + "' does not exist!!!");
            }
            GatewayResourceProfile profile = r.getGatewayProfile(gatewayID);
            profile = profile.toBuilder().addComputeResourcePreferences(crp).build();
            r.updateGatewayResourceProfile(profile);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while registering gateway resource profile preference. More info : " + e.getMessage());
        }
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile grp) throws Exception {
        try {
            if (!isGatewayExistInternal(gatewayID)) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            new GwyResourceProfileRepository().updateGatewayResourceProfile(grp);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while updating gateway resource profile. More info : " + e.getMessage());
        }
    }

    public String registerGatewayResourceProfile(GatewayResourceProfile grp) throws Exception {
        try {
            if (!validateString(grp.getGatewayId())) {
                throw new RegistryException("Cannot create gateway profile with empty gateway id");
            }
            if (!isGatewayExistInternal(grp.getGatewayId())) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return new GwyResourceProfileRepository().addGatewayResourceProfile(grp);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while registering gateway resource profile. More info : " + e.getMessage());
        }
    }

    // --- Group resource profile CRUD ---

    public String createGroupResourceProfile(GroupResourceProfile grp) throws Exception {
        try {
            if (!isGatewayExistInternal(grp.getGatewayId())) {
                throw new RegistryException("Gateway does not exist.Please provide a valid gateway id...");
            }
            return new GroupResourceProfileRepository().addGroupResourceProfile(grp);
        } catch (Exception e) {
            throw new RegistryException("Error while creating group resource profile. More info : " + e.getMessage());
        }
    }

    public void updateGroupResourceProfile(GroupResourceProfile grp) throws Exception {
        try {
            GroupResourceProfileRepository r = new GroupResourceProfileRepository();
            if (!r.isGroupResourceProfileExists(grp.getGroupResourceProfileId())) {
                throw new RegistryException("Cannot update. No group resource profile found");
            }
            r.updateGroupResourceProfile(grp);
        } catch (Exception e) {
            throw new RegistryException("Error while updating group resource profile. More info : " + e.getMessage());
        }
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws Exception {
        try {
            GroupResourceProfileRepository r = new GroupResourceProfileRepository();
            if (!r.isGroupResourceProfileExists(groupResourceProfileId)) {
                throw new RegistryException("Cannot Remove. No group resource profile found");
            }
            return r.removeGroupResourceProfile(groupResourceProfileId);
        } catch (Exception e) {
            throw new RegistryException("Error while removing group resource profile. More info : " + e.getMessage());
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws Exception {
        try {
            return new GroupResourceProfileRepository()
                    .getAllGroupResourceProfiles(gatewayId, accessibleGroupResProfileIds);
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving group resource list. More info : " + e.getMessage());
        }
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId) throws Exception {
        try {
            new GroupResourceProfileRepository()
                    .removeGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while removing group compute preference. More info : " + e.getMessage());
        }
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            new GroupResourceProfileRepository().removeComputeResourcePolicy(resourcePolicyId);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while removing group compute resource policy. More info : " + e.getMessage());
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            new GroupResourceProfileRepository().removeBatchQueueResourcePolicy(resourcePolicyId);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while removing group batch queue resource policy. More info : " + e.getMessage());
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            ComputeResourcePolicy p = new GroupResourceProfileRepository().getComputeResourcePolicy(resourcePolicyId);
            if (p == null) {
                throw new RegistryException("Group Compute Resource policy not found ");
            }
            return p;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while retrieving group compute resource policy. More info : " + e.getMessage());
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws Exception {
        try {
            BatchQueueResourcePolicy p =
                    new GroupResourceProfileRepository().getBatchQueueResourcePolicy(resourcePolicyId);
            if (p == null) {
                throw new RegistryException("Group Batch Queue Resource policy not found ");
            }
            return p;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while retrieving Batch Queue resource policy. More info : " + e.getMessage());
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws Exception {
        try {
            return new GroupResourceProfileRepository().getAllGroupComputeResourcePreferences(groupResourceProfileId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while retrieving retrieving Group Compute Resource Preference list. More info : "
                            + e.getMessage());
        }
    }

    // --- User resource profile CRUD ---

    public String registerUserResourceProfile(UserResourceProfile urp) throws Exception {
        try {
            if (!validateString(urp.getUserId()) || !validateString(urp.getGatewayId())) {
                throw new RegistryException("Cannot create user resource profile with empty user/gateway id");
            }
            if (userProfileProvider.getUserProfileByIdAndGateWay(urp.getUserId(), urp.getGatewayId()) == null) {
                throw new RegistryException("User does not exist.Please provide a valid user ID...");
            }
            return userResourceProfileRepository.addUserResourceProfile(urp);
        } catch (Exception e) {
            throw new RegistryException("Error while registering user resource profile. More info : " + e.getMessage());
        }
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile urp)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, urp);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while updating gateway resource profile. More info : " + e.getMessage());
        }
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            userResourceProfileRepository.removeUserResourceProfile(userId, gatewayID);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while removing User resource profile. More info : " + e.getMessage());
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws Exception {
        try {
            return userResourceProfileRepository.getAllUserResourceProfiles();
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
        }
    }

    public boolean addUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference pref)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new RegistryException("User resource profile does not exist!!!");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            profile =
                    profile.toBuilder().addUserComputeResourcePreferences(pref).build();
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while registering user resource profile preference. More info : " + e.getMessage());
        }
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dsp) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayID)) {
                throw new RegistryException("User resource profile does not exist!!!");
            }
            UserStoragePreference pref =
                    dsp.toBuilder().setStorageResourceId(storageResourceId).build();
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            profile = profile.toBuilder().addUserStoragePreferences(pref).build();
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, profile);
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while adding user storage preference. More info : " + e.getMessage());
        }
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            UserStoragePreferencePK pk = new UserStoragePreferencePK();
            pk.setUserId(userId);
            pk.setGatewayId(gatewayID);
            pk.setStorageResourceId(storageId);
            return userStoragePreferenceRepository.get(pk);
        } catch (Exception e) {
            logger.error(userId, "Error while reading user storage preference...", e);
            throw new RegistryException("Error while reading user storage preference. More info : " + e.getMessage());
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws Exception {
        try {
            if (!isUserExists(gatewayID, userId)) {
                throw new RegistryException(
                        "User Resource Profile does not exist.Please provide a valid gateway id...");
            }
            return userResourceProfileRepository
                    .getUserResourceProfile(userId, gatewayID)
                    .getUserComputeResourcePreferencesList();
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while reading User Resource Profile compute resource preferences. More info : "
                            + e.getMessage());
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID) throws Exception {
        try {
            if (!isUserExists(gatewayID, userId)) {
                throw new RegistryException(
                        "User Resource Profile does not exist.Please provide a valid gateway id...");
            }
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put(DBConstants.UserStoragePreference.USER_ID, userId);
            queryParams.put(DBConstants.UserStoragePreference.GATEWAY_ID, gatewayID);
            return userStoragePreferenceRepository.select(
                    QueryConstants.GET_ALL_USER_STORAGE_PREFERENCE, -1, 0, queryParams);
        } catch (Exception e) {
            throw new RegistryException("Error while reading user storage preferences. More info : " + e.getMessage());
        }
    }

    public boolean updateUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference pref)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            java.util.List<UserComputeResourcePreference> prefs = profile.getUserComputeResourcePreferencesList();
            UserResourceProfile.Builder b = profile.toBuilder();
            for (int i = 0; i < prefs.size(); i++) {
                if (prefs.get(i).getComputeResourceId().equals(computeResourceId)) {
                    b.removeUserComputeResourcePreferences(i);
                    break;
                }
            }
            b.addUserComputeResourcePreferences(pref);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, b.build());
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while updating user compute resource preference. More info : " + e.getMessage());
        }
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference pref) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            UserResourceProfile profile = userResourceProfileRepository.getUserResourceProfile(userId, gatewayID);
            java.util.List<UserStoragePreference> prefs = profile.getUserStoragePreferencesList();
            UserResourceProfile.Builder b = profile.toBuilder();
            for (int i = 0; i < prefs.size(); i++) {
                if (prefs.get(i).getStorageResourceId().equals(storageId)) {
                    b.removeUserStoragePreferences(i);
                    break;
                }
            }
            b.addUserStoragePreferences(pref);
            userResourceProfileRepository.updateUserResourceProfile(userId, gatewayID, b.build());
            return true;
        } catch (Exception e) {
            throw new RegistryException("Error while updating user storage preference. More info : " + e.getMessage());
        }
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            return userResourceProfileRepository.removeUserComputeResourcePreferenceFromGateway(
                    userId, gatewayID, computeResourceId);
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while updating user compute resource preference. More info : " + e.getMessage());
        }
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId) throws Exception {
        try {
            if (userProfileProvider.getUserProfileByIdAndGateWay(userId, gatewayID) == null) {
                throw new RegistryException("user does not exist.Please provide a valid user id...");
            }
            UserStoragePreferencePK pk = new UserStoragePreferencePK();
            pk.setUserId(userId);
            pk.setGatewayId(gatewayID);
            pk.setStorageResourceId(storageId);
            return userStoragePreferenceRepository.delete(pk);
        } catch (Exception e) {
            throw new RegistryException("Error while deleting user storage preference. More info : " + e.getMessage());
        }
    }

    // --- Usage reporting CRUD ---

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws Exception {
        try {
            usageReportingProvider.addGatewayUsageReportingCommand(command);
        } catch (Exception e) {
            throw new RegistryException("Failed to add the reporting information for the gateway "
                    + command.getGatewayId() + " and compute resource " + command.getComputeResourceId()
                    + ". More info " + e.getMessage());
        }
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId) throws Exception {
        try {
            usageReportingProvider.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (Exception e) {
            throw new RegistryException("Failed to add the reporting information for the gateway " + gatewayId
                    + " and compute resource " + computeResourceId + ". More info " + e.getMessage());
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private boolean isGatewayExistInternal(String gatewayId) throws Exception {
        try {
            return gatewayExistenceProvider.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            throw new RegistryException("Error while getting gateway. More info : " + e.getMessage());
        }
    }

    private boolean isUserExists(String gatewayId, String userName) throws Exception {
        try {
            return userProfileProvider.getUserProfileByIdAndGateWay(userName, gatewayId) != null;
        } catch (Exception e) {
            logger.error("Error while verifying user", e);
            throw new RegistryException("Error while verifying user. More info : " + e.getMessage());
        }
    }

    private boolean validateString(String name) {
        return name != null && !name.equals("") && name.trim().length() != 0;
    }
}
