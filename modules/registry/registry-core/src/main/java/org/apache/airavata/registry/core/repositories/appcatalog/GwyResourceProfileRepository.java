/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.GwyResourceProfile;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwyResourceProfileRepository extends AppCatAbstractRepository<GatewayResourceProfile, GatewayProfileEntity, String> implements GwyResourceProfile {

    private final static Logger logger = LoggerFactory.getLogger(GwyResourceProfileRepository.class);

    public GwyResourceProfileRepository() {
        super(GatewayResourceProfile.class, GatewayProfileEntity.class);
    }

    @Override
    public String addGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {

        return updateGatewayResourceProfile(gatewayResourceProfile);
    }

    @Override
    public void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile) throws AppCatalogException {
        updateGatewayResourceProfile(updatedProfile);
    }

    public String updateGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        String gatewayId = gatewayResourceProfile.getGatewayID();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GatewayProfileEntity gatewayProfileEntity = mapper.map(gatewayResourceProfile, GatewayProfileEntity.class);
        if (get(gatewayId) != null) {
            gatewayProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        }
        else {
            gatewayProfileEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }

        if (gatewayProfileEntity.getComputeResourcePreferences() != null)
            gatewayProfileEntity.getComputeResourcePreferences().forEach(pref->pref.setGatewayId(gatewayId));

        if (gatewayProfileEntity.getStoragePreferences() != null)
            gatewayProfileEntity.getStoragePreferences().forEach(pref->pref.setGatewayId(gatewayId));

        GatewayProfileEntity persistedCopy = execute(entityManager -> entityManager.merge(gatewayProfileEntity));

        List<ComputeResourcePreference> computeResourcePreferences = gatewayResourceProfile.getComputeResourcePreferences();
        if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
            for (ComputeResourcePreference preference : computeResourcePreferences ) {
                if (preference.getSshAccountProvisionerConfig() != null && !preference.getSshAccountProvisionerConfig().isEmpty()){
                    ComputeResourcePreferenceEntity computeResourcePreferenceEntity = mapper.map(preference, ComputeResourcePreferenceEntity.class);
                    computeResourcePreferenceEntity.setGatewayId(gatewayId);
                    List<SSHAccountProvisionerConfiguration> configurations = new ArrayList<>();
                    for (String sshAccountProvisionerConfigName : preference.getSshAccountProvisionerConfig().keySet()) {
                        String value = preference.getSshAccountProvisionerConfig().get(sshAccountProvisionerConfigName);
                        configurations.add(new SSHAccountProvisionerConfiguration(sshAccountProvisionerConfigName, value, computeResourcePreferenceEntity));
                    }
                    computeResourcePreferenceEntity.setSshAccountProvisionerConfigurations(configurations);
                    execute(entityManager -> entityManager.merge(computeResourcePreferenceEntity));
                }
            }
        }
        return persistedCopy.getGatewayId();
    }

    @Override
    public GatewayResourceProfile getGatewayProfile(String gatewayId) {
        GatewayResourceProfile gatewayResourceProfile = get(gatewayId);
        if (gatewayResourceProfile.getComputeResourcePreferences() != null && !gatewayResourceProfile.getComputeResourcePreferences().isEmpty()){
            for (ComputeResourcePreference preference: gatewayResourceProfile.getComputeResourcePreferences()){
                ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
                preference.setSshAccountProvisionerConfig(computeResourcePrefRepository.getsshAccountProvisionerConfig(gatewayResourceProfile.getGatewayID(), preference.getComputeResourceId()));
            }
        }
        return gatewayResourceProfile;
    }

    @Override
    public boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException {
        return delete(gatewayId);
    }

    @Override
    public List<GatewayResourceProfile> getAllGatewayProfiles() {

        List<GatewayResourceProfile> gwyResourceProfileList = new ArrayList<GatewayResourceProfile>();
        List<GatewayResourceProfile> gatewayResourceProfileList = select(QueryConstants.FIND_ALL_GATEWAY_PROFILES, 0);
        if (gatewayResourceProfileList != null && !gatewayResourceProfileList.isEmpty()) {
            for (GatewayResourceProfile gatewayResourceProfile: gatewayResourceProfileList) {
                if (gatewayResourceProfile.getComputeResourcePreferences() != null && !gatewayResourceProfile.getComputeResourcePreferences().isEmpty()){
                    for (ComputeResourcePreference preference: gatewayResourceProfile.getComputeResourcePreferences()){
                        ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
                        preference.setSshAccountProvisionerConfig(computeResourcePrefRepository.getsshAccountProvisionerConfig(gatewayResourceProfile.getGatewayID(), preference.getComputeResourceId()));
                    }
                }
            }
        }
        return gatewayResourceProfileList;
    }

    @Override
    public boolean removeComputeResourcePreferenceFromGateway(String gatewayId, String preferenceId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(preferenceId);
        (new ComputeResourcePrefRepository()).delete(computeResourcePreferencePK);
        return true;
    }

    @Override
    public boolean removeDataStoragePreferenceFromGateway(String gatewayId, String preferenceId) {
        StoragePreferencePK storagePreferencePK = new StoragePreferencePK();
        storagePreferencePK.setGatewayId(gatewayId);
        storagePreferencePK.setStorageResourceId(preferenceId);
        (new StoragePrefRepository()).delete(storagePreferencePK);
        return true;
    }

    @Override
    public boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException {
        return isExists(gatewayId);
    }

    @Override
    public ComputeResourcePreference getComputeResourcePreference(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
        ComputeResourcePreference computeResourcePreference = computeResourcePrefRepository.get(computeResourcePreferencePK);
        computeResourcePreference.setSshAccountProvisionerConfig(computeResourcePrefRepository.getsshAccountProvisionerConfig(gatewayId, hostId));
        return computeResourcePreference;
    }

    @Override
    public StoragePreference getStoragePreference(String gatewayId, String storageId){
        StoragePreferencePK storagePreferencePK = new StoragePreferencePK();
        storagePreferencePK.setStorageResourceId(storageId);
        storagePreferencePK.setGatewayId(gatewayId);
        return (new StoragePrefRepository()).get(storagePreferencePK);
    }

    @Override
    public List<ComputeResourcePreference> getAllComputeResourcePreferences(String gatewayId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ComputeResourcePreference.GATEWAY_ID, gatewayId);
        ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
        List<ComputeResourcePreference> preferences = computeResourcePrefRepository.select(QueryConstants.FIND_ALL_COMPUTE_RESOURCE_PREFERENCES, -1, 0, queryParameters);
        if (preferences != null && !preferences.isEmpty()) {
            for (ComputeResourcePreference preference: preferences){
                preference.setSshAccountProvisionerConfig(computeResourcePrefRepository.getsshAccountProvisionerConfig(gatewayId, preference.getComputeResourceId()));
            }
        }
        return preferences;
    }

    @Override
    public List<StoragePreference> getAllStoragePreferences(String gatewayId) {
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.StorageResourcePreference.GATEWAY_ID, gatewayId);
        return (new StoragePrefRepository()).select(QueryConstants.FIND_ALL_STORAGE_RESOURCE_PREFERENCES, -1, 0, queryParameters);
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        //not used anywhere. Skipping the implementation
        return null;
    }
}
