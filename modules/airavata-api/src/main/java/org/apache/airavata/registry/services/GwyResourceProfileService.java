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
import java.util.List;
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.model.StoragePreference;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourcePreferencePK;
import org.apache.airavata.registry.entities.appcatalog.GatewayProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.SSHAccountProvisionerConfiguration;
import org.apache.airavata.registry.entities.appcatalog.StoragePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.StoragePreferencePK;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.ComputeResourcePreferenceMapper;
import org.apache.airavata.registry.mappers.GatewayResourceProfileMapper;
import org.apache.airavata.registry.mappers.StoragePreferenceMapper;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourcePrefRepository;
import org.apache.airavata.registry.repositories.appcatalog.GwyResourceProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.SSHAccountProvisionerConfigurationRepository;
import org.apache.airavata.registry.repositories.appcatalog.StoragePrefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GwyResourceProfileService {
    private final GwyResourceProfileRepository gwyResourceProfileRepository;
    private final ComputeResourcePrefRepository computeResourcePrefRepository;
    private final StoragePrefRepository storagePrefRepository;
    private final GatewayResourceProfileMapper gatewayResourceProfileMapper;
    private final ComputeResourcePreferenceMapper computeResourcePreferenceMapper;
    private final StoragePreferenceMapper storagePreferenceMapper;
    private final SSHAccountProvisionerConfigurationRepository sshAccountProvisionerConfigurationRepository;

    public GwyResourceProfileService(
            GwyResourceProfileRepository gwyResourceProfileRepository,
            ComputeResourcePrefRepository computeResourcePrefRepository,
            StoragePrefRepository storagePrefRepository,
            GatewayResourceProfileMapper gatewayResourceProfileMapper,
            ComputeResourcePreferenceMapper computeResourcePreferenceMapper,
            StoragePreferenceMapper storagePreferenceMapper,
            SSHAccountProvisionerConfigurationRepository sshAccountProvisionerConfigurationRepository) {
        this.gwyResourceProfileRepository = gwyResourceProfileRepository;
        this.computeResourcePrefRepository = computeResourcePrefRepository;
        this.storagePrefRepository = storagePrefRepository;
        this.gatewayResourceProfileMapper = gatewayResourceProfileMapper;
        this.computeResourcePreferenceMapper = computeResourcePreferenceMapper;
        this.storagePreferenceMapper = storagePreferenceMapper;
        this.sshAccountProvisionerConfigurationRepository = sshAccountProvisionerConfigurationRepository;
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
        GatewayProfileEntity gatewayProfileEntity = gatewayResourceProfileMapper.toEntity(gatewayResourceProfile);
        // Ensure gatewayId is set on the entity (mapper field name mismatch: model uses gatewayID, entity uses
        // gatewayId)
        gatewayProfileEntity.setGatewayId(gatewayId);
        if (gwyResourceProfileRepository.findById(gatewayId).isPresent()) {
            gatewayProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        } else {
            gatewayProfileEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }

        // Manually map compute resource preferences (mapper ignores them)
        if (gatewayResourceProfile.getComputeResourcePreferences() != null
                && !gatewayResourceProfile.getComputeResourcePreferences().isEmpty()) {
            List<ComputeResourcePreferenceEntity> computePrefs = computeResourcePreferenceMapper.toEntityList(
                    gatewayResourceProfile.getComputeResourcePreferences());
            for (ComputeResourcePreferenceEntity pref : computePrefs) {
                pref.setGatewayId(gatewayId);
                // Set the back-reference for proper cascade behavior
                // Even though JoinColumn has insertable=false, updatable=false,
                // the back-reference is needed for Hibernate's cascade management
                pref.setGatewayProfileResource(gatewayProfileEntity);
            }
            gatewayProfileEntity.setComputeResourcePreferences(computePrefs);
        }

        // Manually map storage preferences (mapper ignores them)
        if (gatewayResourceProfile.getStoragePreferences() != null
                && !gatewayResourceProfile.getStoragePreferences().isEmpty()) {
            List<StoragePreferenceEntity> storagePrefs =
                    storagePreferenceMapper.toEntityList(gatewayResourceProfile.getStoragePreferences());
            for (StoragePreferenceEntity pref : storagePrefs) {
                pref.setGatewayId(gatewayId);
                // Set the back-reference for proper cascade behavior
                pref.setGatewayProfileResource(gatewayProfileEntity);
            }
            gatewayProfileEntity.setStoragePreferences(storagePrefs);
        }

        GatewayProfileEntity persistedCopy = gwyResourceProfileRepository.save(gatewayProfileEntity);

        // Handle SSH Account Provisioner Configurations for compute resource preferences
        List<ComputeResourcePreference> computeResourcePreferences =
                gatewayResourceProfile.getComputeResourcePreferences();
        if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
            for (ComputeResourcePreference preference : computeResourcePreferences) {
                if (preference.getSshAccountProvisionerConfig() != null
                        && !preference.getSshAccountProvisionerConfig().isEmpty()) {
                    // Look up the existing persisted compute resource preference
                    ComputeResourcePreferencePK prefPK = new ComputeResourcePreferencePK();
                    prefPK.setGatewayId(gatewayId);
                    prefPK.setComputeResourceId(preference.getComputeResourceId());
                    ComputeResourcePreferenceEntity existingPref =
                            computeResourcePrefRepository.findById(prefPK).orElse(null);

                    if (existingPref != null) {
                        // Delete existing SSH configs first
                        if (existingPref.getSshAccountProvisionerConfigurations() != null) {
                            sshAccountProvisionerConfigurationRepository.deleteAll(
                                    existingPref.getSshAccountProvisionerConfigurations());
                        }

                        // Create new SSH configs using the explicit fields, not the relationship
                        List<SSHAccountProvisionerConfiguration> configurations = new ArrayList<>();
                        for (String configName :
                                preference.getSshAccountProvisionerConfig().keySet()) {
                            String configValue =
                                    preference.getSshAccountProvisionerConfig().get(configName);
                            SSHAccountProvisionerConfiguration sshConfig = new SSHAccountProvisionerConfiguration();
                            sshConfig.setGatewayId(gatewayId);
                            sshConfig.setResourceId(preference.getComputeResourceId());
                            sshConfig.setConfigName(configName);
                            sshConfig.setConfigValue(configValue);
                            // Don't set computeResourcePreference - foreign key is managed by explicit fields
                            configurations.add(sshConfig);
                        }
                        sshAccountProvisionerConfigurationRepository.saveAll(configurations);
                    }
                }
            }
        }
        return persistedCopy.getGatewayId();
    }

    @Transactional(readOnly = true)
    public GatewayResourceProfile getGatewayProfile(String gatewayId) {
        GatewayProfileEntity entity =
                gwyResourceProfileRepository.findById(gatewayId).orElse(null);
        if (entity == null) return null;
        GatewayResourceProfile gatewayResourceProfile = gatewayResourceProfileMapper.toModel(entity);
        gatewayResourceProfile.setGatewayID(gatewayId);
        if (gatewayResourceProfile.getComputeResourcePreferences() != null
                && !gatewayResourceProfile.getComputeResourcePreferences().isEmpty()) {
            for (ComputeResourcePreference preference : gatewayResourceProfile.getComputeResourcePreferences()) {
                preference.setSshAccountProvisionerConfig(
                        sshAccountProvisionerConfigurationRepository.getSshAccountProvisionerConfig(
                                gatewayResourceProfile.getGatewayID(), preference.getComputeResourceId()));
            }
        }
        return gatewayResourceProfile;
    }

    @Transactional
    public boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException {
        // Load entity first to trigger JPA cascade deletes for compute resource preferences
        // and their SSH account provisioner configurations
        // deleteById() bypasses entity lifecycle and doesn't trigger cascade operations
        return gwyResourceProfileRepository
                .findById(gatewayId)
                .map(entity -> {
                    gwyResourceProfileRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<GatewayResourceProfile> getAllGatewayProfiles() {
        List<GatewayProfileEntity> entities = gwyResourceProfileRepository.findAll();
        List<GatewayResourceProfile> gatewayResourceProfileList = new ArrayList<>();
        for (GatewayProfileEntity entity : entities) {
            GatewayResourceProfile gatewayResourceProfile = gatewayResourceProfileMapper.toModel(entity);
            if (gatewayResourceProfile.getComputeResourcePreferences() != null
                    && !gatewayResourceProfile.getComputeResourcePreferences().isEmpty()) {
                for (ComputeResourcePreference preference : gatewayResourceProfile.getComputeResourcePreferences()) {
                    preference.setSshAccountProvisionerConfig(
                            sshAccountProvisionerConfigurationRepository.getSshAccountProvisionerConfig(
                                    gatewayResourceProfile.getGatewayID(), preference.getComputeResourceId()));
                }
            }
            gatewayResourceProfileList.add(gatewayResourceProfile);
        }
        return gatewayResourceProfileList;
    }

    @Transactional
    public boolean removeComputeResourcePreferenceFromGateway(String gatewayId, String preferenceId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(preferenceId);
        // Load entity first to trigger JPA cascade deletes for SSH_ACCOUNT_PROVISIONER_CONFIG
        // deleteById() bypasses entity lifecycle and doesn't trigger cascade operations
        computeResourcePrefRepository
                .findById(computeResourcePreferencePK)
                .ifPresent(computeResourcePrefRepository::delete);
        return true;
    }

    @Transactional
    public boolean removeDataStoragePreferenceFromGateway(String gatewayId, String preferenceId) {
        StoragePreferencePK storagePreferencePK = new StoragePreferencePK();
        storagePreferencePK.setGatewayId(gatewayId);
        storagePreferencePK.setStorageResourceId(preferenceId);
        storagePrefRepository.deleteById(storagePreferencePK);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException {
        return gwyResourceProfileRepository.existsById(gatewayId);
    }

    @Transactional(readOnly = true)
    public ComputeResourcePreference getComputeResourcePreference(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        ComputeResourcePreference computeResourcePreference = computeResourcePrefRepository
                .findById(computeResourcePreferencePK)
                .map(entity -> computeResourcePreferenceMapper.toModel(entity))
                .orElse(null);
        if (computeResourcePreference != null) {
            computeResourcePreference.setSshAccountProvisionerConfig(
                    sshAccountProvisionerConfigurationRepository.getSshAccountProvisionerConfig(gatewayId, hostId));
        }
        return computeResourcePreference;
    }

    @Transactional(readOnly = true)
    public StoragePreference getStoragePreference(String gatewayId, String storageId) {
        StoragePreferencePK storagePreferencePK = new StoragePreferencePK();
        storagePreferencePK.setStorageResourceId(storageId);
        storagePreferencePK.setGatewayId(gatewayId);
        return storagePrefRepository
                .findById(storagePreferencePK)
                .map(entity -> storagePreferenceMapper.toModel(entity))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ComputeResourcePreference> getAllComputeResourcePreferences(String gatewayId) {
        List<ComputeResourcePreferenceEntity> entities = computeResourcePrefRepository.findByGatewayId(gatewayId);
        List<ComputeResourcePreference> preferences = computeResourcePreferenceMapper.toModelList(entities);
        if (preferences != null && !preferences.isEmpty()) {
            for (ComputeResourcePreference preference : preferences) {
                preference.setSshAccountProvisionerConfig(
                        sshAccountProvisionerConfigurationRepository.getSshAccountProvisionerConfig(
                                gatewayId, preference.getComputeResourceId()));
            }
        }
        return preferences;
    }

    @Transactional(readOnly = true)
    public List<StoragePreference> getAllStoragePreferences(String gatewayId) {
        List<StoragePreferenceEntity> entities = storagePrefRepository.findByGatewayId(gatewayId);
        return storagePreferenceMapper.toModelList(entities);
    }
}
