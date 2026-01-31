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
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.PreferenceKeys;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.StoragePreference;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.registry.entities.appcatalog.ResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.mappers.ResourceProfileMapper;
import org.apache.airavata.registry.repositories.ResourcePreferenceRepository;
import org.apache.airavata.registry.repositories.appcatalog.ResourceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Gateway Resource Profiles using the unified RESOURCE_PREFERENCE key-value store.
 */
@Service
public class GwyResourceProfileService {

    private final ResourceProfileRepository resourceProfileRepository;
    private final ResourcePreferenceRepository resourcePreferenceRepository;
    private final ResourceProfileMapper resourceProfileMapper;
    private final CredentialStoreService credentialStoreService;

    public GwyResourceProfileService(
            ResourceProfileRepository resourceProfileRepository,
            ResourcePreferenceRepository resourcePreferenceRepository,
            ResourceProfileMapper resourceProfileMapper,
            CredentialStoreService credentialStoreService) {
        this.resourceProfileRepository = resourceProfileRepository;
        this.resourcePreferenceRepository = resourcePreferenceRepository;
        this.resourceProfileMapper = resourceProfileMapper;
        this.credentialStoreService = credentialStoreService;
    }

    @Transactional
    public String addGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        return updateGatewayResourceProfile(gatewayResourceProfile);
    }

    @Transactional
    public void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile)
            throws AppCatalogException {
        updateGatewayResourceProfile(updatedProfile);
    }

    @Transactional
    public String updateGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        String gatewayId = gatewayResourceProfile.getGatewayID();
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            throw new IllegalArgumentException("GatewayID is required for GatewayResourceProfile");
        }
        ResourceProfileEntity resourceProfileEntity = resourceProfileMapper.gatewayToEntity(gatewayResourceProfile);
        if (resourceProfileRepository.gatewayProfileExists(gatewayId)) {
            resourceProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        } else {
            resourceProfileEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }

        // Save the resource profile first
        ResourceProfileEntity persistedCopy = resourceProfileRepository.save(resourceProfileEntity);

        // Save compute resource preferences
        if (gatewayResourceProfile.getComputeResourcePreferences() != null
                && !gatewayResourceProfile.getComputeResourcePreferences().isEmpty()) {
            for (ComputeResourcePreference pref : gatewayResourceProfile.getComputeResourcePreferences()) {
                saveComputePreference(pref, gatewayId);
            }
        }

        // Save storage preferences
        if (gatewayResourceProfile.getStoragePreferences() != null
                && !gatewayResourceProfile.getStoragePreferences().isEmpty()) {
            for (StoragePreference pref : gatewayResourceProfile.getStoragePreferences()) {
                saveStoragePreference(pref, gatewayId);
            }
        }

        return persistedCopy.getGatewayId();
    }

    @Transactional(readOnly = true)
    public GatewayResourceProfile getGatewayProfile(String gatewayId) {
        ResourceProfileEntity entity = resourceProfileRepository.findGatewayProfile(gatewayId).orElse(null);
        if (entity == null) return null;
        GatewayResourceProfile profile = resourceProfileMapper.toGatewayResourceProfile(entity);
        profile.setGatewayID(gatewayId);

        // Load compute resource preferences
        List<String> computeResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.COMPUTE, gatewayId);
        List<ComputeResourcePreference> computePrefs = new ArrayList<>();
        for (String resourceId : computeResourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.COMPUTE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
            if (!prefs.isEmpty()) {
                computePrefs.add(toComputePreferenceModel(resourceId, prefs));
            }
        }
        profile.setComputeResourcePreferences(computePrefs);

        // Load storage preferences
        List<String> storageResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.STORAGE, gatewayId);
        List<StoragePreference> storagePrefs = new ArrayList<>();
        for (String resourceId : storageResourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.STORAGE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
            if (!prefs.isEmpty()) {
                storagePrefs.add(toStoragePreferenceModel(resourceId, prefs));
            }
        }
        profile.setStoragePreferences(storagePrefs);

        return profile;
    }

    @Transactional
    public boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException {
        // Delete compute resource preferences
        List<String> computeResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.COMPUTE, gatewayId);
        for (String resourceId : computeResourceIds) {
            resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                    PreferenceResourceType.COMPUTE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
        }

        // Delete storage preferences
        List<String> storageResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.STORAGE, gatewayId);
        for (String resourceId : storageResourceIds) {
            resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                    PreferenceResourceType.STORAGE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
        }

        // Delete the resource profile
        if (resourceProfileRepository.gatewayProfileExists(gatewayId)) {
            resourceProfileRepository.deleteGatewayProfile(gatewayId);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<GatewayResourceProfile> getAllGatewayProfiles() {
        List<ResourceProfileEntity> entities = resourceProfileRepository.findAllGatewayProfiles();
        List<GatewayResourceProfile> profiles = new ArrayList<>();
        for (ResourceProfileEntity entity : entities) {
            GatewayResourceProfile profile = resourceProfileMapper.toGatewayResourceProfile(entity);
            String gatewayId = entity.getGatewayId();

            // Load compute resource preferences
            List<String> computeResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                    PreferenceResourceType.COMPUTE, gatewayId);
            List<ComputeResourcePreference> computePrefs = new ArrayList<>();
            for (String resourceId : computeResourceIds) {
                List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                        .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                                PreferenceResourceType.COMPUTE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
                if (!prefs.isEmpty()) {
                    computePrefs.add(toComputePreferenceModel(resourceId, prefs));
                }
            }
            profile.setComputeResourcePreferences(computePrefs);

            // Load storage preferences
            List<String> storageResourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                    PreferenceResourceType.STORAGE, gatewayId);
            List<StoragePreference> storagePrefs = new ArrayList<>();
            for (String resourceId : storageResourceIds) {
                List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                        .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                                PreferenceResourceType.STORAGE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
                if (!prefs.isEmpty()) {
                    storagePrefs.add(toStoragePreferenceModel(resourceId, prefs));
                }
            }
            profile.setStoragePreferences(storagePrefs);

            profiles.add(profile);
        }
        return profiles;
    }

    @Transactional
    public boolean removeComputeResourcePreferenceFromGateway(String gatewayId, String computeResourceId) {
        resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                PreferenceResourceType.COMPUTE, computeResourceId, gatewayId, PreferenceLevel.GATEWAY);
        return true;
    }

    @Transactional
    public boolean removeDataStoragePreferenceFromGateway(String gatewayId, String storageResourceId) {
        resourcePreferenceRepository.deleteByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                PreferenceResourceType.STORAGE, storageResourceId, gatewayId, PreferenceLevel.GATEWAY);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException {
        return resourceProfileRepository.gatewayProfileExists(gatewayId);
    }

    @Transactional(readOnly = true)
    public ComputeResourcePreference getComputeResourcePreference(String gatewayId, String computeResourceId) {
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.COMPUTE, computeResourceId, gatewayId, PreferenceLevel.GATEWAY);
        if (prefs.isEmpty()) {
            return null;
        }
        return toComputePreferenceModel(computeResourceId, prefs);
    }

    @Transactional(readOnly = true)
    public StoragePreference getStoragePreference(String gatewayId, String storageResourceId) {
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                        PreferenceResourceType.STORAGE, storageResourceId, gatewayId, PreferenceLevel.GATEWAY);
        if (prefs.isEmpty()) {
            return null;
        }
        return toStoragePreferenceModel(storageResourceId, prefs);
    }

    @Transactional(readOnly = true)
    public List<ComputeResourcePreference> getAllComputeResourcePreferences(String gatewayId) {
        List<String> resourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.COMPUTE, gatewayId);
        List<ComputeResourcePreference> preferences = new ArrayList<>();
        for (String resourceId : resourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.COMPUTE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
            if (!prefs.isEmpty()) {
                preferences.add(toComputePreferenceModel(resourceId, prefs));
            }
        }
        return preferences;
    }

    @Transactional(readOnly = true)
    public List<StoragePreference> getAllStoragePreferences(String gatewayId) {
        List<String> resourceIds = resourcePreferenceRepository.findDistinctResourceIdsByResourceTypeAndOwnerId(
                PreferenceResourceType.STORAGE, gatewayId);
        List<StoragePreference> preferences = new ArrayList<>();
        for (String resourceId : resourceIds) {
            List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                    .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(
                            PreferenceResourceType.STORAGE, resourceId, gatewayId, PreferenceLevel.GATEWAY);
            if (!prefs.isEmpty()) {
                preferences.add(toStoragePreferenceModel(resourceId, prefs));
            }
        }
        return preferences;
    }

    // ========== Save Methods ==========

    private void saveComputePreference(ComputeResourcePreference model, String gatewayId) {
        String resourceId = model.getComputeResourceId();
        PreferenceResourceType resourceType = PreferenceResourceType.COMPUTE;
        PreferenceLevel level = PreferenceLevel.GATEWAY;

        // Save each field as a key-value pair
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.LOGIN_USERNAME, model.getLoginUserName());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.PREFERRED_BATCH_QUEUE, model.getPreferredBatchQueue());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.SCRATCH_LOCATION, model.getScratchLocation());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.ALLOCATION_PROJECT_NUMBER, model.getAllocationProjectNumber());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN, model.getResourceSpecificCredentialStoreToken());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.QUALITY_OF_SERVICE, model.getQualityOfService());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.RESERVATION, model.getReservation());
        if (model.getReservationStartTime() > 0) {
            savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.RESERVATION_START_TIME,
                    String.valueOf(model.getReservationStartTime()));
        }
        if (model.getReservationEndTime() > 0) {
            savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.RESERVATION_END_TIME,
                    String.valueOf(model.getReservationEndTime()));
        }
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.OVERRIDE_BY_AIRAVATA,
                String.valueOf(model.getOverridebyAiravata()));
        
        // Protocol preferences
        if (model.getPreferredJobSubmissionProtocol() != null) {
            savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.PREFERRED_JOB_SUBMISSION_PROTOCOL,
                    model.getPreferredJobSubmissionProtocol().name());
        }
        if (model.getPreferredDataMovementProtocol() != null) {
            savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.PREFERRED_DATA_MOVEMENT_PROTOCOL,
                    model.getPreferredDataMovementProtocol().name());
        }
        
        // Gateway-specific fields
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.USAGE_REPORTING_GATEWAY_ID, model.getUsageReportingGatewayId());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.SSH_ACCOUNT_PROVISIONER, model.getSshAccountProvisioner());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO, model.getSshAccountProvisionerAdditionalInfo());
        
        // SSH provisioner config (stored as individual keys with prefix)
        if (model.getSshAccountProvisionerConfig() != null && !model.getSshAccountProvisionerConfig().isEmpty()) {
            // First delete old config keys
            deletePreferencesWithPrefix(resourceType, resourceId, gatewayId, level, PreferenceKeys.SSH_PROVISIONER_CONFIG_PREFIX);
            // Save new config keys
            for (Map.Entry<String, String> entry : model.getSshAccountProvisionerConfig().entrySet()) {
                savePreference(resourceType, resourceId, gatewayId, level,
                        PreferenceKeys.SSH_PROVISIONER_CONFIG_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }

    private void saveStoragePreference(StoragePreference model, String gatewayId) {
        String resourceId = model.getStorageResourceId();
        PreferenceResourceType resourceType = PreferenceResourceType.STORAGE;
        PreferenceLevel level = PreferenceLevel.GATEWAY;

        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.LOGIN_USERNAME, model.getLoginUserName());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.FILE_SYSTEM_ROOT_LOCATION, model.getFileSystemRootLocation());
        savePreference(resourceType, resourceId, gatewayId, level, PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN, model.getResourceSpecificCredentialStoreToken());
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

        // Validate credential exists when saving resource-specific credential token (GATEWAY: ownerId = gatewayId)
        if (PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN.equals(key)
                && !credentialStoreService.credentialExists(value, ownerId)) {
            throw new IllegalArgumentException("Credential does not exist for token: " + value);
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

    private void deletePreferencesWithPrefix(PreferenceResourceType resourceType, String resourceId, String ownerId,
            PreferenceLevel level, String prefix) {
        List<ResourcePreferenceEntity> prefs = resourcePreferenceRepository
                .findByResourceTypeAndResourceIdAndOwnerIdAndLevel(resourceType, resourceId, ownerId, level);
        List<ResourcePreferenceEntity> toDelete = prefs.stream()
                .filter(p -> p.getKey().startsWith(prefix))
                .toList();
        if (!toDelete.isEmpty()) {
            resourcePreferenceRepository.deleteAll(toDelete);
        }
    }

    // ========== Conversion Methods ==========

    private ComputeResourcePreference toComputePreferenceModel(String resourceId, List<ResourcePreferenceEntity> prefs) {
        Map<String, String> prefMap = new HashMap<>();
        for (ResourcePreferenceEntity p : prefs) {
            prefMap.put(p.getKey(), p.getValue());
        }

        ComputeResourcePreference model = new ComputeResourcePreference();
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
        
        model.setOverridebyAiravata(!"false".equals(prefMap.get(PreferenceKeys.OVERRIDE_BY_AIRAVATA)));
        
        // Protocol preferences
        if (prefMap.get(PreferenceKeys.PREFERRED_JOB_SUBMISSION_PROTOCOL) != null) {
            model.setPreferredJobSubmissionProtocol(
                    JobSubmissionProtocol.valueOf(prefMap.get(PreferenceKeys.PREFERRED_JOB_SUBMISSION_PROTOCOL)));
        }
        if (prefMap.get(PreferenceKeys.PREFERRED_DATA_MOVEMENT_PROTOCOL) != null) {
            model.setPreferredDataMovementProtocol(
                    DataMovementProtocol.valueOf(prefMap.get(PreferenceKeys.PREFERRED_DATA_MOVEMENT_PROTOCOL)));
        }
        
        // Gateway-specific fields
        model.setUsageReportingGatewayId(prefMap.get(PreferenceKeys.USAGE_REPORTING_GATEWAY_ID));
        model.setSshAccountProvisioner(prefMap.get(PreferenceKeys.SSH_ACCOUNT_PROVISIONER));
        model.setSshAccountProvisionerAdditionalInfo(prefMap.get(PreferenceKeys.SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO));
        
        // SSH provisioner config
        Map<String, String> sshConfig = new HashMap<>();
        for (ResourcePreferenceEntity p : prefs) {
            if (p.getKey().startsWith(PreferenceKeys.SSH_PROVISIONER_CONFIG_PREFIX)) {
                sshConfig.put(p.getKey().substring(PreferenceKeys.SSH_PROVISIONER_CONFIG_PREFIX.length()), p.getValue());
            }
        }
        if (!sshConfig.isEmpty()) {
            model.setSshAccountProvisionerConfig(sshConfig);
        }
        
        return model;
    }

    private StoragePreference toStoragePreferenceModel(String resourceId, List<ResourcePreferenceEntity> prefs) {
        Map<String, String> prefMap = new HashMap<>();
        for (ResourcePreferenceEntity p : prefs) {
            prefMap.put(p.getKey(), p.getValue());
        }

        StoragePreference model = new StoragePreference();
        model.setStorageResourceId(resourceId);
        model.setLoginUserName(prefMap.get(PreferenceKeys.LOGIN_USERNAME));
        model.setFileSystemRootLocation(prefMap.get(PreferenceKeys.FILE_SYSTEM_ROOT_LOCATION));
        model.setResourceSpecificCredentialStoreToken(prefMap.get(PreferenceKeys.RESOURCE_CREDENTIAL_TOKEN));
        return model;
    }
}
