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
package org.apache.airavata.compute.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.*;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.GwyResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GwyResourceProfileRepository
        extends AbstractRepository<GatewayResourceProfile, GatewayProfileEntity, String> implements GwyResourceProfile {

    private static final Logger logger = LoggerFactory.getLogger(GwyResourceProfileRepository.class);

    public GwyResourceProfileRepository() {
        super(GatewayResourceProfile.class, GatewayProfileEntity.class);
    }

    @Override
    protected GatewayResourceProfile toModel(GatewayProfileEntity entity) {
        return ComputeMapper.INSTANCE.gatewayProfileToModel(entity);
    }

    @Override
    protected GatewayProfileEntity toEntity(GatewayResourceProfile model) {
        return ComputeMapper.INSTANCE.gatewayProfileToEntity(model);
    }

    @Override
    public String addGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {

        return updateGatewayResourceProfile(gatewayResourceProfile);
    }

    @Override
    public void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile)
            throws AppCatalogException {
        updateGatewayResourceProfile(updatedProfile);
    }

    public String updateGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
        String gatewayId = gatewayResourceProfile.getGatewayId();
        GatewayProfileEntity gatewayProfileEntity =
                ComputeMapper.INSTANCE.gatewayProfileToEntity(gatewayResourceProfile);
        // Explicitly set gatewayId since Dozer mapping does not handle gatewayID -> gatewayId conversion
        gatewayProfileEntity.setGatewayId(gatewayId);
        if (get(gatewayId) != null) {
            gatewayProfileEntity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        } else {
            gatewayProfileEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        }

        if (gatewayProfileEntity.getComputeResourcePreferences() != null)
            gatewayProfileEntity.getComputeResourcePreferences().forEach(pref -> pref.setGatewayId(gatewayId));

        GatewayProfileEntity persistedCopy = execute(entityManager -> entityManager.merge(gatewayProfileEntity));

        List<ComputeResourcePreference> computeResourcePreferences =
                gatewayResourceProfile.getComputeResourcePreferencesList();
        if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
            for (ComputeResourcePreference preference : computeResourcePreferences) {
                if (!preference.getSshAccountProvisionerConfigMap().isEmpty()) {
                    ComputeResourcePreferenceEntity computeResourcePreferenceEntity =
                            ComputeMapper.INSTANCE.computeResourcePrefToEntity(preference);
                    computeResourcePreferenceEntity.setGatewayId(gatewayId);
                    List<SSHAccountProvisionerConfiguration> configurations = new ArrayList<>();
                    for (String sshAccountProvisionerConfigName :
                            preference.getSshAccountProvisionerConfigMap().keySet()) {
                        String value =
                                preference.getSshAccountProvisionerConfigMap().get(sshAccountProvisionerConfigName);
                        configurations.add(new SSHAccountProvisionerConfiguration(
                                sshAccountProvisionerConfigName, value, computeResourcePreferenceEntity));
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
        GatewayResourceProfile.Builder builder =
                gatewayResourceProfile.toBuilder().setGatewayId(gatewayId);
        if (!gatewayResourceProfile.getComputeResourcePreferencesList().isEmpty()) {
            builder.clearComputeResourcePreferences();
            for (ComputeResourcePreference preference : gatewayResourceProfile.getComputeResourcePreferencesList()) {
                ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
                Map<String, String> sshConfig = computeResourcePrefRepository.getsshAccountProvisionerConfig(
                        gatewayId, preference.getComputeResourceId());
                builder.addComputeResourcePreferences(preference.toBuilder()
                        .putAllSshAccountProvisionerConfig(sshConfig)
                        .build());
            }
        }
        return builder.build();
    }

    @Override
    public boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException {
        return delete(gatewayId);
    }

    @Override
    public List<GatewayResourceProfile> getAllGatewayProfiles() {

        List<GatewayResourceProfile> gatewayResourceProfileList = select(QueryConstants.FIND_ALL_GATEWAY_PROFILES, 0);
        if (gatewayResourceProfileList != null && !gatewayResourceProfileList.isEmpty()) {
            List<GatewayResourceProfile> result = new ArrayList<>();
            for (GatewayResourceProfile gatewayResourceProfile : gatewayResourceProfileList) {
                if (!gatewayResourceProfile.getComputeResourcePreferencesList().isEmpty()) {
                    GatewayResourceProfile.Builder builder = gatewayResourceProfile.toBuilder();
                    builder.clearComputeResourcePreferences();
                    for (ComputeResourcePreference preference :
                            gatewayResourceProfile.getComputeResourcePreferencesList()) {
                        ComputeResourcePrefRepository computeResourcePrefRepository =
                                new ComputeResourcePrefRepository();
                        Map<String, String> sshConfig = computeResourcePrefRepository.getsshAccountProvisionerConfig(
                                gatewayResourceProfile.getGatewayId(), preference.getComputeResourceId());
                        builder.addComputeResourcePreferences(preference.toBuilder()
                                .putAllSshAccountProvisionerConfig(sshConfig)
                                .build());
                    }
                    result.add(builder.build());
                } else {
                    result.add(gatewayResourceProfile);
                }
            }
            return result;
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
    public boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException {
        return isExists(gatewayId);
    }

    @Override
    public ComputeResourcePreference getComputeResourcePreference(String gatewayId, String hostId) {
        ComputeResourcePreferencePK computeResourcePreferencePK = new ComputeResourcePreferencePK();
        computeResourcePreferencePK.setGatewayId(gatewayId);
        computeResourcePreferencePK.setComputeResourceId(hostId);
        ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
        ComputeResourcePreference computeResourcePreference =
                computeResourcePrefRepository.get(computeResourcePreferencePK);
        Map<String, String> sshConfig = computeResourcePrefRepository.getsshAccountProvisionerConfig(gatewayId, hostId);
        return computeResourcePreference.toBuilder()
                .putAllSshAccountProvisionerConfig(sshConfig)
                .build();
    }

    @Override
    public List<ComputeResourcePreference> getAllComputeResourcePreferences(String gatewayId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ComputeResourcePreference.GATEWAY_ID, gatewayId);
        ComputeResourcePrefRepository computeResourcePrefRepository = new ComputeResourcePrefRepository();
        List<ComputeResourcePreference> preferences = computeResourcePrefRepository.select(
                QueryConstants.FIND_ALL_COMPUTE_RESOURCE_PREFERENCES, -1, 0, queryParameters);
        if (preferences != null && !preferences.isEmpty()) {
            List<ComputeResourcePreference> result = new ArrayList<>();
            for (ComputeResourcePreference preference : preferences) {
                Map<String, String> sshConfig = computeResourcePrefRepository.getsshAccountProvisionerConfig(
                        gatewayId, preference.getComputeResourceId());
                result.add(preference.toBuilder()
                        .putAllSshAccountProvisionerConfig(sshConfig)
                        .build());
            }
            return result;
        }
        return preferences;
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        // not used anywhere. Skipping the implementation
        return null;
    }
}
