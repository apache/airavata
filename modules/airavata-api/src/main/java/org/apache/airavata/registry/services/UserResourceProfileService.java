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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.PreferenceKeys;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.UserComputeResourcePreference;
import org.apache.airavata.common.model.UserResourceProfile;
import org.apache.airavata.common.model.UserStoragePreference;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntity;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.ResourceProfileMapper;
import org.apache.airavata.registry.repositories.ResourcePreferenceRepository;
import org.apache.airavata.registry.repositories.appcatalog.ResourceProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing User Resource Profiles using the unified RESOURCE_PREFERENCE key-value store.
 */
@Service
public class UserResourceProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileService.class);

    private final ResourceProfileRepository resourceProfileRepository;
    private final ResourcePreferenceRepository resourcePreferenceRepository;
    private final ResourceProfileMapper resourceProfileMapper;

    public UserResourceProfileService(
            ResourceProfileRepository resourceProfileRepository,
            ResourcePreferenceRepository resourcePreferenceRepository,
            ResourceProfileMapper resourceProfileMapper) {
        this.resourceProfileRepository = resourceProfileRepository;
        this.resourcePreferenceRepository = resourcePreferenceRepository;
        this.resourceProfileMapper = resourceProfileMapper;
    }

    @Transactional
    public String addUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException {
        return saveUserResourceProfileData(userResourceProfile);
    }

    @Transactional
    public void updateUserResourceProfile(String userId, String gatewayId, UserResourceProfile updatedProfile)
            throws AppCatalogException {
        saveUserResourceProfileData(updatedProfile);
    }

    private String saveUserResourceProfileData(UserResourceProfile userResourceProfile) throws AppCatalogException {
        String userId = userResourceProfile.getUserId();
        String gatewayId = userResourceProfile.getGatewayID();
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            throw new AppCatalogException("GatewayID is required for UserResourceProfile");
        }

        ResourceProfileEntity resourceProfileEntity = resourceProfileMapper.userToEntity(userResourceProfile);
        if (!isUserResourceProfileExists(userId, gatewayId)) {
            logger.debug("Creating new User Resource Profile");
            resourceProfileEntity.setCreationTime(AiravataUtils.getUniqueTimestamp());
        }
        resourceProfileEntity.setUpdateTime(AiravataUtils.getUniqueTimestamp());
        resourceProfileRepository.save(resourceProfileEntity);

        // Owner ID for user-level preferences is constructed as "userId@gatewayId"
        String ownerId = buildOwnerId(userId, gatewayId);

        // Save compute resource preferences
        if (userResourceProfile.getUserComputeResourcePreferences() != null
                && !userResourceProfile.getUserComputeResourcePreferences().isEmpty()) {
            logger.debug("Saving UserComputeResourcePreferences for the User Resource Profile");
            for (UserComputeResourcePreference pref : userResourceProfile.getUserComputeResourcePreferences()) {
                saveComputePreference(pref, ownerId);
            }
        }

        // Save storage preferences
        if (userResourceProfile.getUserStoragePreferences() != null
                && !userResourceProfile.getUserStoragePreferences().isEmpty()) {
            logger.debug("Saving UserStoragePreferences for the User Resource Profile");
            for (UserStoragePreference pref : userResourceProfile.getUserStoragePreferences()) {
                saveStoragePreference(pref, ownerId);
            }
        }

        return userId;
    }

    @Transactional(readOnly = true)
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        ResourceProfileEntity entity = resourceProfileRepository.findUserProfile(userId, gatewayId).orElse(null);
        if (entity == null) return null;

        UserResourceProfile model = resourceProfileMapper.toUserResourceProfile(entity);
        String ownerId = buildOwnerId(userId, gatewayId);

        // Load compute resource preferences
        List<String> computeResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.COMPUTE, ownerId);
        List<UserComputeResourcePreference> computePrefs = new ArrayList<>();
        for (String resourceId : computeResourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.COMPUTE, resourceId, ownerId, PreferenceLevel.USER);
            if (!prefs.isEmpty()) {
                computePrefs.add(toUserComputePreferenceModel(resourceId, prefs));
            }
        }
        model.setUserComputeResourcePreferences(computePrefs);

        // Load storage preferences
        List<String> storageResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.STORAGE, ownerId);
        List<UserStoragePreference> storagePrefs = new ArrayList<>();
        for (String resourceId : storageResourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.STORAGE, resourceId, ownerId, PreferenceLevel.USER);
            if (!prefs.isEmpty()) {
                storagePrefs.add(toUserStoragePreferenceModel(resourceId, prefs));
            }
        }
        model.setUserStoragePreferences(storagePrefs);

        return model;
    }

    @Transactional(readOnly = true)
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayId, String computeResourceId) throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, computeResourceId, ownerId, PreferenceLevel.USER);
        if (prefs.isEmpty()) {
            return null;
        }
        return toUserComputePreferenceModel(computeResourceId, prefs);
    }

    @Transactional(readOnly = true)
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageResourceId)
            throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.STORAGE, storageResourceId, ownerId, PreferenceLevel.USER);
        if (prefs.isEmpty()) {
            return null;
        }
        return toUserStoragePreferenceModel(storageResourceId, prefs);
    }

    @Transactional(readOnly = true)
    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        List<ResourceProfileEntity> entities = resourceProfileRepository.findAllUserProfiles();
        return resourceProfileMapper.toUserResourceProfileList(entities);
    }

    @Transactional(readOnly = true)
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        List<String> resourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.COMPUTE, ownerId);
        List<UserComputeResourcePreference> preferences = new ArrayList<>();
        for (String resourceId : resourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.COMPUTE, resourceId, ownerId, PreferenceLevel.USER);
            if (!prefs.isEmpty()) {
                preferences.add(toUserComputePreferenceModel(resourceId, prefs));
            }
        }
        return preferences;
    }

    @Transactional(readOnly = true)
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        List<String> resourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.STORAGE, ownerId);
        List<UserStoragePreference> preferences = new ArrayList<>();
        for (String resourceId : resourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.STORAGE, resourceId, ownerId, PreferenceLevel.USER);
            if (!prefs.isEmpty()) {
                preferences.add(toUserStoragePreferenceModel(resourceId, prefs));
            }
        }
        return preferences;
    }

    @Transactional(readOnly = true)
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        List<ResourceProfileEntity> entities = resourceProfileRepository.findAllUserProfiles();
        List<String> gatewayIdList = new ArrayList<>();
        for (ResourceProfileEntity entity : entities) {
            if (gatewayName == null || entity.getGatewayId().equals(gatewayName)) {
                gatewayIdList.add(entity.getGatewayId());
            }
        }
        return gatewayIdList;
    }

    @Transactional(readOnly = true)
    public String getUserNamefromID(String userId, String gatewayID) throws AppCatalogException {
        return userId;
    }

    @Transactional
    public boolean removeUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        if (!resourceProfileRepository.userProfileExists(userId, gatewayId)) {
            return false;
        }

        String ownerId = buildOwnerId(userId, gatewayId);

        // Delete all compute resource preferences for this user
        List<String> computeResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.COMPUTE, ownerId);
        for (String resourceId : computeResourceIds) {
            resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                    PreferenceResourceType.COMPUTE, resourceId, ownerId, PreferenceLevel.USER);
        }

        // Delete all storage preferences for this user
        List<String> storageResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.STORAGE, ownerId);
        for (String resourceId : storageResourceIds) {
            resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                    PreferenceResourceType.STORAGE, resourceId, ownerId, PreferenceLevel.USER);
        }

        // Delete the resource profile
        resourceProfileRepository.deleteUserProfile(userId, gatewayId);
        return true;
    }

    @Transactional
    public boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String computeResourceId)
            throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                PreferenceResourceType.COMPUTE, computeResourceId, ownerId, PreferenceLevel.USER);
        return true;
    }

    @Transactional
    public boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String storageResourceId)
            throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                PreferenceResourceType.STORAGE, storageResourceId, ownerId, PreferenceLevel.USER);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        return resourceProfileRepository.userProfileExists(userId, gatewayId);
    }

    @Transactional(readOnly = true)
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayId, String computeResourceId)
            throws AppCatalogException {
        String ownerId = buildOwnerId(userId, gatewayId);
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, computeResourceId, ownerId, PreferenceLevel.USER);
        return !prefs.isEmpty();
    }

    // ========== Helper Methods ==========

    /**
     * Builds the owner ID for user-level preferences.
     * Format: "userId@gatewayId"
     */
    private String buildOwnerId(String userId, String gatewayId) {
        return userId + "@" + gatewayId;
    }

    private void saveComputePreference(UserComputeResourcePreference model, String ownerId) {
        String resourceId = model.getComputeResourceId();
        PreferenceResourceType resourceType = PreferenceResourceType.COMPUTE;
        PreferenceLevel level = PreferenceLevel.USER;

        // Save each field as a key-value pair
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.LOGIN_USERNAME, model.getLoginUserName());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.PREFERRED_BATCH_QUEUE, model.getPreferredBatchQueue());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.SCRATCH_LOCATION, model.getScratchLocation());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.ALLOCATION_PROJECT_NUMBER, model.getAllocationProjectNumber());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN, model.getResourceSpecificCredentialStoreToken());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.QUALITY_OF_SERVICE, model.getQualityOfService());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.RESERVATION, model.getReservation());
        if (model.getReservationStartTime() > 0) {
            savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.RESERVATION_START_TIME, 
                    String.valueOf(model.getReservationStartTime()));
        }
        if (model.getReservationEndTime() > 0) {
            savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.RESERVATION_END_TIME, 
                    String.valueOf(model.getReservationEndTime()));
        }
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.VALIDATED, 
                String.valueOf(model.getValidated()));
    }

    private void saveStoragePreference(UserStoragePreference model, String ownerId) {
        String resourceId = model.getStorageResourceId();
        PreferenceResourceType resourceType = PreferenceResourceType.STORAGE;
        PreferenceLevel level = PreferenceLevel.USER;

        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.LOGIN_USERNAME, model.getLoginUserName());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.FILE_SYSTEM_ROOT_LOCATION, model.getFileSystemRootLocation());
        savePreference(resourceType, resourceId, ownerId, level, PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN, model.getResourceSpecificCredentialStoreToken());
    }

    private void savePreference(PreferenceResourceType resourceType, String resourceId, String ownerId,
            PreferenceLevel level, String key, String value) {
        if (value == null) {
            // Delete the preference if value is null
            ResourcePreferenceEntity existing = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(resourceType, resourceId, ownerId, level, key);
            if (existing != null) {
                resourcePreferenceRepository.delete(existing);
            }
            return;
        }
        
        ResourcePreferenceEntity existing = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevelAndKey(resourceType, resourceId, ownerId, level, key);
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

    private UserComputeResourcePreference toUserComputePreferenceModel(String resourceId, List<ResourcePreferenceEntity> prefs) {
        Map<String, String> prefMap = new HashMap<>();
        for (ResourcePreferenceEntity p : prefs) {
            prefMap.put(p.getKey(), p.getValue());
        }

        UserComputeResourcePreference model = new UserComputeResourcePreference();
        model.setComputeResourceId(resourceId);
        model.setLoginUserName(prefMap.get(PreferenceKeys.LOGIN_USERNAME));
        model.setPreferredBatchQueue(prefMap.get(PreferenceKeys.PREFERRED_BATCH_QUEUE));
        model.setScratchLocation(prefMap.get(PreferenceKeys.SCRATCH_LOCATION));
        model.setAllocationProjectNumber(prefMap.get(PreferenceKeys.ALLOCATION_PROJECT_NUMBER));
        model.setResourceSpecificCredentialStoreToken(prefMap.get(PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN));
        model.setQualityOfService(prefMap.get(PreferenceKeys.QUALITY_OF_SERVICE));
        model.setReservation(prefMap.get(PreferenceKeys.RESERVATION));
        if (prefMap.get(PreferenceKeys.RESERVATION_START_TIME) != null) {
            model.setReservationStartTime(Long.parseLong(prefMap.get(PreferenceKeys.RESERVATION_START_TIME)));
        }
        if (prefMap.get(PreferenceKeys.RESERVATION_END_TIME) != null) {
            model.setReservationEndTime(Long.parseLong(prefMap.get(PreferenceKeys.RESERVATION_END_TIME)));
        }
        model.setValidated("true".equals(prefMap.get(PreferenceKeys.VALIDATED)));
        return model;
    }

    private UserStoragePreference toUserStoragePreferenceModel(String resourceId, List<ResourcePreferenceEntity> prefs) {
        Map<String, String> prefMap = new HashMap<>();
        for (ResourcePreferenceEntity p : prefs) {
            prefMap.put(p.getKey(), p.getValue());
        }

        UserStoragePreference model = new UserStoragePreference();
        model.setStorageResourceId(resourceId);
        model.setLoginUserName(prefMap.get(PreferenceKeys.LOGIN_USERNAME));
        model.setFileSystemRootLocation(prefMap.get(PreferenceKeys.FILE_SYSTEM_ROOT_LOCATION));
        model.setResourceSpecificCredentialStoreToken(prefMap.get(PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN));
        return model;
    }
}
