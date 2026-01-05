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
        GatewayProfileEntity gatewayProfileEntity = gatewayResourceProfileMapper.toEntity(gatewayResourceProfile);
        if (gwyResourceProfileRepository.findById(gatewayId).isPresent()) {
            gatewayProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        } else {
            gatewayProfileEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }

        if (gatewayProfileEntity.getComputeResourcePreferences() != null)
            gatewayProfileEntity.getComputeResourcePreferences().forEach(pref -> pref.setGatewayId(gatewayId));

        if (gatewayProfileEntity.getStoragePreferences() != null)
            gatewayProfileEntity.getStoragePreferences().forEach(pref -> pref.setGatewayId(gatewayId));

        GatewayProfileEntity persistedCopy = gwyResourceProfileRepository.save(gatewayProfileEntity);

        List<ComputeResourcePreference> computeResourcePreferences =
                gatewayResourceProfile.getComputeResourcePreferences();
        if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
            for (ComputeResourcePreference preference : computeResourcePreferences) {
                if (preference.getSshAccountProvisionerConfig() != null
                        && !preference.getSshAccountProvisionerConfig().isEmpty()) {
                    ComputeResourcePreferenceEntity computeResourcePreferenceEntity =
                            computeResourcePreferenceMapper.toEntity(preference);
                    computeResourcePreferenceEntity.setGatewayId(gatewayId);
                    List<SSHAccountProvisionerConfiguration> configurations = new ArrayList<>();
                    for (String sshAccountProvisionerConfigName :
                            preference.getSshAccountProvisionerConfig().keySet()) {
                        String value =
                                preference.getSshAccountProvisionerConfig().get(sshAccountProvisionerConfigName);
                        configurations.add(new SSHAccountProvisionerConfiguration(
                                sshAccountProvisionerConfigName, value, computeResourcePreferenceEntity));
                    }
                    computeResourcePreferenceEntity.setSshAccountProvisionerConfigurations(configurations);
                    computeResourcePrefRepository.save(computeResourcePreferenceEntity);
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
        if (!gwyResourceProfileRepository.existsById(gatewayId)) {
            return false;
        }
        gwyResourceProfileRepository.deleteById(gatewayId);
        return true;
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
        computeResourcePrefRepository.deleteById(computeResourcePreferencePK);
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
