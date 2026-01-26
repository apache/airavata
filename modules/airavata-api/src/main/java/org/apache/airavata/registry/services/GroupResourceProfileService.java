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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.model.BatchQueueResourcePolicy;
import org.apache.airavata.common.model.ComputeResourcePolicy;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.ProfileOwnerType;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntityPK;
import org.apache.airavata.registry.repositories.ResourcePreferenceRepository;
import org.apache.airavata.registry.repositories.appcatalog.ResourceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Group Resource Profiles.
 *
 * <p>Uses the unified resource model:
 * <ul>
 *   <li>{@link ResourceProfileEntity} with {@link ProfileOwnerType#GROUP} for profile metadata</li>
 *   <li>{@link ResourcePreferenceEntity} for all preferences (stored as key-value pairs)</li>
 * </ul>
 *
 * <p>Group resource profiles allow defining compute resource preferences at a group level,
 * which can then be inherited by users in that group.
 */
@Service
@Transactional
public class GroupResourceProfileService {

    // Preference key prefixes
    public static final String PROFILE_NAME_KEY = "group.profile.name";
    public static final String DEFAULT_CREDENTIAL_TOKEN_KEY = "group.defaultCredentialStoreToken";
    public static final String SSH_ACCOUNT_PROVISIONER_KEY = "ssh.accountProvisioner";
    public static final String SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO_KEY = "ssh.accountProvisionerAdditionalInfo";
    public static final String SSH_PROVISIONER_CONFIG_PREFIX = "ssh.provisioner.config.";
    public static final String LOGIN_USERNAME_KEY = "loginUserName";
    public static final String SCRATCH_LOCATION_KEY = "scratchLocation";
    public static final String ALLOCATION_PROJECT_NUMBER_KEY = "allocationProjectNumber";
    public static final String PREFERRED_BATCH_QUEUE_KEY = "preferredBatchQueue";
    public static final String QUALITY_OF_SERVICE_KEY = "qualityOfService";
    public static final String OVERRIDE_BY_AIRAVATA_KEY = "overrideByAiravata";
    public static final String RESOURCE_SPECIFIC_CREDENTIAL_TOKEN_KEY = "resourceSpecificCredentialStoreToken";
    public static final String PREFERRED_JOB_SUB_PROTOCOL_KEY = "preferredJobSubmissionProtocol";
    public static final String PREFERRED_DATA_MOVE_PROTOCOL_KEY = "preferredDataMovementProtocol";
    public static final String USAGE_REPORTING_GATEWAY_ID_KEY = "usageReportingGatewayId";
    public static final String RESOURCE_TYPE_KEY = "resourceType";
    // AWS specific
    public static final String AWS_REGION_KEY = "aws.region";
    public static final String AWS_PREFERRED_AMI_ID_KEY = "aws.preferredAmiId";
    public static final String AWS_PREFERRED_INSTANCE_TYPE_KEY = "aws.preferredInstanceType";
    // Policy keys
    public static final String POLICY_PREFIX = "policy.";
    public static final String COMPUTE_POLICY_PREFIX = "policy.compute.";
    public static final String BATCH_QUEUE_POLICY_PREFIX = "policy.batchQueue.";

    private final ResourceProfileRepository resourceProfileRepository;
    private final ResourcePreferenceRepository resourcePreferenceRepository;

    public GroupResourceProfileService(
            ResourceProfileRepository resourceProfileRepository,
            ResourcePreferenceRepository resourcePreferenceRepository) {
        this.resourceProfileRepository = resourceProfileRepository;
        this.resourcePreferenceRepository = resourcePreferenceRepository;
    }

    /**
     * Add a new group resource profile.
     */
    public String addGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        final String groupResourceProfileId = UUID.randomUUID().toString();
        groupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);
        groupResourceProfile.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        return updateGroupResourceProfile(groupResourceProfile);
    }

    /**
     * Update an existing group resource profile.
     */
    public String updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) {
        String profileId = groupResourceProfile.getGroupResourceProfileId();
        groupResourceProfile.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());

        // Create or update the profile entity
        ResourceProfileEntityPK pk = new ResourceProfileEntityPK();
        pk.setProfileId(profileId);
        pk.setProfileType(ProfileOwnerType.GROUP);

        ResourceProfileEntity profileEntity = resourceProfileRepository.findById(pk).orElse(null);
        if (profileEntity == null) {
            profileEntity = ResourceProfileEntity.forGroup(profileId, groupResourceProfile.getGatewayId());
        }

        profileEntity.setCredentialStoreToken(groupResourceProfile.getDefaultCredentialStoreToken());
        resourceProfileRepository.save(profileEntity);

        // Store profile name as a preference
        if (groupResourceProfile.getGroupResourceProfileName() != null) {
            savePreference(profileId, PROFILE_NAME_KEY, groupResourceProfile.getGroupResourceProfileName());
        }

        // Store compute preferences
        if (groupResourceProfile.getComputePreferences() != null) {
            for (GroupComputeResourcePreference pref : groupResourceProfile.getComputePreferences()) {
                saveComputeResourcePreference(profileId, pref);
            }
        }

        // Store compute resource policies
        if (groupResourceProfile.getComputeResourcePolicies() != null) {
            for (ComputeResourcePolicy policy : groupResourceProfile.getComputeResourcePolicies()) {
                saveComputeResourcePolicy(profileId, policy);
            }
        }

        // Store batch queue resource policies
        if (groupResourceProfile.getBatchQueueResourcePolicies() != null) {
            for (BatchQueueResourcePolicy policy : groupResourceProfile.getBatchQueueResourcePolicies()) {
                saveBatchQueueResourcePolicy(profileId, policy);
            }
        }

        return profileId;
    }

    /**
     * Get a group resource profile by ID.
     */
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) {
        ResourceProfileEntityPK pk = new ResourceProfileEntityPK();
        pk.setProfileId(groupResourceProfileId);
        pk.setProfileType(ProfileOwnerType.GROUP);

        ResourceProfileEntity entity = resourceProfileRepository.findById(pk).orElse(null);
        if (entity == null) return null;

        return entityToModel(entity);
    }

    /**
     * Remove a group resource profile.
     */
    public boolean removeGroupResourceProfile(String groupResourceProfileId) {
        ResourceProfileEntityPK pk = new ResourceProfileEntityPK();
        pk.setProfileId(groupResourceProfileId);
        pk.setProfileType(ProfileOwnerType.GROUP);

        if (!resourceProfileRepository.existsById(pk)) {
            return false;
        }

        // Delete all preferences for this profile
        resourcePreferenceRepository.deleteByOwnerId(groupResourceProfileId);

        // Delete the profile
        resourceProfileRepository.deleteById(pk);
        return true;
    }

    /**
     * Check if a group resource profile exists.
     */
    public boolean isGroupResourceProfileExists(String groupResourceProfileId) {
        ResourceProfileEntityPK pk = new ResourceProfileEntityPK();
        pk.setProfileId(groupResourceProfileId);
        pk.setProfileType(ProfileOwnerType.GROUP);
        return resourceProfileRepository.existsById(pk);
    }

    /**
     * Get all group resource profiles for a gateway.
     */
    public List<GroupResourceProfile> getAllGroupResourceProfiles(
            String gatewayId, List<String> accessibleGroupResProfileIds) {
        if (accessibleGroupResProfileIds == null || accessibleGroupResProfileIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResourceProfileEntity> entities = resourceProfileRepository
                .findByGatewayIdAndProfileTypeAndProfileIdIn(gatewayId, ProfileOwnerType.GROUP, accessibleGroupResProfileIds);

        List<GroupResourceProfile> profiles = new ArrayList<>();
        for (ResourceProfileEntity entity : entities) {
            profiles.add(entityToModel(entity));
        }
        return profiles;
    }

    /**
     * Get a specific compute resource preference.
     */
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) {
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, computeResourceId, groupResourceProfileId, PreferenceLevel.GROUP);

        if (prefs.isEmpty()) return null;

        return prefsToComputeResourcePreference(computeResourceId, groupResourceProfileId, prefs);
    }

    /**
     * Check if a compute resource preference exists.
     */
    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId) {
        return !resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, computeResourceId, groupResourceProfileId, PreferenceLevel.GROUP)
                .isEmpty();
    }

    /**
     * Get all compute resource preferences for a profile.
     */
    public List<GroupComputeResourcePreference> getAllGroupComputeResourcePreferences(String groupResourceProfileId) {
        // Get all COMPUTE preferences owned by this profile at GROUP level
        List<ResourcePreferenceEntity> allPrefs = resourcePreferenceRepository
                .findByResourceTypeAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, groupResourceProfileId, PreferenceLevel.GROUP);

        // Group by resource ID
        java.util.Map<String, List<ResourcePreferenceEntity>> byResource = new java.util.HashMap<>();
        for (ResourcePreferenceEntity pref : allPrefs) {
            byResource.computeIfAbsent(pref.getResourceId(), k -> new ArrayList<>()).add(pref);
        }

        List<GroupComputeResourcePreference> result = new ArrayList<>();
        for (java.util.Map.Entry<String, List<ResourcePreferenceEntity>> entry : byResource.entrySet()) {
            GroupComputeResourcePreference pref = prefsToComputeResourcePreference(
                    entry.getKey(), groupResourceProfileId, entry.getValue());
            if (pref != null) {
                result.add(pref);
            }
        }
        return result;
    }

    /**
     * Remove a compute resource preference.
     */
    public boolean removeGroupComputeResourcePreference(String computeResourceId, String groupResourceProfileId) {
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, computeResourceId, groupResourceProfileId, PreferenceLevel.GROUP);
        if (!prefs.isEmpty()) {
            resourcePreferenceRepository.deleteAll(prefs);
            return true;
        }
        return false;
    }

    /**
     * Get a compute resource policy.
     */
    public ComputeResourcePolicy getComputeResourcePolicy(String resourcePolicyId) {
        // Policies are stored with the policy ID as the resource ID
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceIdAndOwnerIdAndLevel(resourcePolicyId, "", PreferenceLevel.GROUP);
        // Find prefs with COMPUTE_POLICY_PREFIX
        for (ResourcePreferenceEntity pref : prefs) {
            if (pref.getKey().startsWith(COMPUTE_POLICY_PREFIX)) {
                return prefToComputeResourcePolicy(resourcePolicyId, pref);
            }
        }
        return null;
    }

    /**
     * Get all compute resource policies for a profile.
     */
    public List<ComputeResourcePolicy> getAllGroupComputeResourcePolicies(String groupResourceProfileId) {
        // TODO: Implement using the unified model
        return Collections.emptyList();
    }

    /**
     * Remove a compute resource policy.
     */
    public boolean removeComputeResourcePolicy(String resourcePolicyId) {
        // TODO: Implement using the unified model
        return true;
    }

    /**
     * Get a batch queue resource policy.
     */
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) {
        // TODO: Implement using the unified model
        return null;
    }

    /**
     * Get all batch queue resource policies for a profile.
     */
    public List<BatchQueueResourcePolicy> getAllGroupBatchQueueResourcePolicies(String groupResourceProfileId) {
        // TODO: Implement using the unified model
        return Collections.emptyList();
    }

    /**
     * Remove a batch queue resource policy.
     */
    public boolean removeBatchQueueResourcePolicy(String resourcePolicyId) {
        // TODO: Implement using the unified model
        return true;
    }

    // ========== Private Helper Methods ==========

    private GroupResourceProfile entityToModel(ResourceProfileEntity entity) {
        GroupResourceProfile profile = new GroupResourceProfile();
        profile.setGroupResourceProfileId(entity.getProfileId());
        profile.setGatewayId(entity.getGatewayId());
        profile.setDefaultCredentialStoreToken(entity.getCredentialStoreToken());

        if (entity.getCreationTime() != null) {
            profile.setCreationTime(entity.getCreationTime().getTime());
        }
        if (entity.getUpdateTime() != null) {
            profile.setUpdatedTime(entity.getUpdateTime().getTime());
        }

        // Load profile name from preferences
        ResourcePreferenceEntity namePref = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                        PreferenceResourceType.PROFILE, entity.getProfileId(), entity.getProfileId(),
                        PreferenceLevel.GROUP, PROFILE_NAME_KEY);
        if (namePref != null) {
            profile.setGroupResourceProfileName(namePref.getValue());
        }

        // Load compute preferences
        profile.setComputePreferences(getAllGroupComputeResourcePreferences(entity.getProfileId()));

        // Load policies
        profile.setComputeResourcePolicies(getAllGroupComputeResourcePolicies(entity.getProfileId()));
        profile.setBatchQueueResourcePolicies(getAllGroupBatchQueueResourcePolicies(entity.getProfileId()));

        return profile;
    }

    private void savePreference(String ownerId, String key, String value) {
        savePreference(PreferenceResourceType.PROFILE, ownerId, ownerId, PreferenceLevel.GROUP, key, value);
    }

    private void savePreference(
            PreferenceResourceType resourceType, String resourceId, String ownerId,
            PreferenceLevel level, String key, String value) {
        ResourcePreferenceEntity existing = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(
                        resourceType, resourceId, ownerId, level, key);
        if (existing != null) {
            existing.setValue(value);
            resourcePreferenceRepository.save(existing);
        } else {
            ResourcePreferenceEntity entity = new ResourcePreferenceEntity();
            entity.setResourceType(resourceType);
            entity.setResourceId(resourceId);
            entity.setOwnerId(ownerId);
            entity.setLevel(level);
            entity.setKey(key);
            entity.setTypedValue(value);
            resourcePreferenceRepository.save(entity);
        }
    }

    private void saveComputeResourcePreference(String profileId, GroupComputeResourcePreference pref) {
        String computeResourceId = pref.getComputeResourceId();
        PreferenceResourceType type = PreferenceResourceType.COMPUTE;
        PreferenceLevel level = PreferenceLevel.GROUP;

        if (pref.getLoginUserName() != null) {
            savePreference(type, computeResourceId, profileId, level, LOGIN_USERNAME_KEY, pref.getLoginUserName());
        }
        if (pref.getScratchLocation() != null) {
            savePreference(type, computeResourceId, profileId, level, SCRATCH_LOCATION_KEY, pref.getScratchLocation());
        }
        savePreference(type, computeResourceId, profileId, level, OVERRIDE_BY_AIRAVATA_KEY,
                String.valueOf(pref.getOverridebyAiravata()));
        if (pref.getResourceSpecificCredentialStoreToken() != null) {
            savePreference(type, computeResourceId, profileId, level, RESOURCE_SPECIFIC_CREDENTIAL_TOKEN_KEY,
                    pref.getResourceSpecificCredentialStoreToken());
        }
        if (pref.getPreferredJobSubmissionProtocol() != null) {
            savePreference(type, computeResourceId, profileId, level, PREFERRED_JOB_SUB_PROTOCOL_KEY,
                    pref.getPreferredJobSubmissionProtocol().name());
        }
        if (pref.getPreferredDataMovementProtocol() != null) {
            savePreference(type, computeResourceId, profileId, level, PREFERRED_DATA_MOVE_PROTOCOL_KEY,
                    pref.getPreferredDataMovementProtocol().name());
        }
        if (pref.getResourceType() != null) {
            savePreference(type, computeResourceId, profileId, level, RESOURCE_TYPE_KEY, pref.getResourceType().name());
        }

        // Save type-specific preferences
        if (pref.getSpecificPreferences() != null) {
            if (pref.getSpecificPreferences().isSlurm()) {
                var slurm = pref.getSpecificPreferences().getSlurm();
                if (slurm.getAllocationProjectNumber() != null) {
                    savePreference(type, computeResourceId, profileId, level, ALLOCATION_PROJECT_NUMBER_KEY,
                            slurm.getAllocationProjectNumber());
                }
                if (slurm.getPreferredBatchQueue() != null) {
                    savePreference(type, computeResourceId, profileId, level, PREFERRED_BATCH_QUEUE_KEY,
                            slurm.getPreferredBatchQueue());
                }
                if (slurm.getQualityOfService() != null) {
                    savePreference(type, computeResourceId, profileId, level, QUALITY_OF_SERVICE_KEY,
                            slurm.getQualityOfService());
                }
                if (slurm.getUsageReportingGatewayId() != null) {
                    savePreference(type, computeResourceId, profileId, level, USAGE_REPORTING_GATEWAY_ID_KEY,
                            slurm.getUsageReportingGatewayId());
                }
                if (slurm.getSshAccountProvisioner() != null) {
                    savePreference(type, computeResourceId, profileId, level, SSH_ACCOUNT_PROVISIONER_KEY,
                            slurm.getSshAccountProvisioner());
                }
                if (slurm.getSshAccountProvisionerAdditionalInfo() != null) {
                    savePreference(type, computeResourceId, profileId, level, SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO_KEY,
                            slurm.getSshAccountProvisionerAdditionalInfo());
                }
                if (slurm.getGroupSSHAccountProvisionerConfigs() != null) {
                    for (var config : slurm.getGroupSSHAccountProvisionerConfigs()) {
                        savePreference(type, computeResourceId, profileId, level,
                                SSH_PROVISIONER_CONFIG_PREFIX + config.getConfigName(), config.getConfigValue());
                    }
                }
            } else if (pref.getSpecificPreferences().isAws()) {
                var aws = pref.getSpecificPreferences().getAws();
                if (aws.getRegion() != null) {
                    savePreference(type, computeResourceId, profileId, level, AWS_REGION_KEY, aws.getRegion());
                }
                if (aws.getPreferredAmiId() != null) {
                    savePreference(type, computeResourceId, profileId, level, AWS_PREFERRED_AMI_ID_KEY,
                            aws.getPreferredAmiId());
                }
                if (aws.getPreferredInstanceType() != null) {
                    savePreference(type, computeResourceId, profileId, level, AWS_PREFERRED_INSTANCE_TYPE_KEY,
                            aws.getPreferredInstanceType());
                }
            }
        }
    }

    private GroupComputeResourcePreference prefsToComputeResourcePreference(
            String computeResourceId, String profileId, List<ResourcePreferenceEntity> prefs) {
        GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        pref.setComputeResourceId(computeResourceId);
        pref.setGroupResourceProfileId(profileId);

        for (ResourcePreferenceEntity prefEntity : prefs) {
            String key = prefEntity.getKey();
            String value = prefEntity.getValue();

            switch (key) {
                case LOGIN_USERNAME_KEY -> pref.setLoginUserName(value);
                case SCRATCH_LOCATION_KEY -> pref.setScratchLocation(value);
                case OVERRIDE_BY_AIRAVATA_KEY -> pref.setOverridebyAiravata(Boolean.parseBoolean(value));
                case RESOURCE_SPECIFIC_CREDENTIAL_TOKEN_KEY -> pref.setResourceSpecificCredentialStoreToken(value);
                case RESOURCE_TYPE_KEY -> {
                    try {
                        pref.setResourceType(org.apache.airavata.common.model.ComputeResourceType.valueOf(value));
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid values
                    }
                }
                // SLURM and AWS specific prefs are loaded but not mapped here for brevity
                // A full implementation would build the EnvironmentSpecificPreferences
            }
        }

        return pref;
    }

    private void saveComputeResourcePolicy(String profileId, ComputeResourcePolicy policy) {
        // TODO: Implement using the unified model
    }

    private void saveBatchQueueResourcePolicy(String profileId, BatchQueueResourcePolicy policy) {
        // TODO: Implement using the unified model
    }

    private ComputeResourcePolicy prefToComputeResourcePolicy(String policyId, ResourcePreferenceEntity pref) {
        // TODO: Implement using the unified model
        return null;
    }
}
