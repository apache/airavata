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
import org.apache.airavata.common.model.UserComputeResourcePreference;
import org.apache.airavata.common.model.UserResourceProfile;
import org.apache.airavata.common.model.UserStoragePreference;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.appcatalog.UserComputeResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.UserComputeResourcePreferencePK;
import org.apache.airavata.registry.entities.appcatalog.UserResourceProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.UserResourceProfilePK;
import org.apache.airavata.registry.entities.appcatalog.UserStoragePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.UserStoragePreferencePK;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.UserComputeResourcePreferenceMapper;
import org.apache.airavata.registry.mappers.UserResourceProfileMapper;
import org.apache.airavata.registry.mappers.UserStoragePreferenceMapper;
import org.apache.airavata.registry.repositories.appcatalog.UserComputeResourcePreferenceRepository;
import org.apache.airavata.registry.repositories.appcatalog.UserResourceProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.UserStoragePreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserResourceProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileService.class);

    private final UserResourceProfileRepository userResourceProfileRepository;
    private final UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository;
    private final UserStoragePreferenceRepository userStoragePreferenceRepository;
    private final UserResourceProfileMapper userResourceProfileMapper;
    private final UserComputeResourcePreferenceMapper userComputeResourcePreferenceMapper;
    private final UserStoragePreferenceMapper userStoragePreferenceMapper;

    public UserResourceProfileService(
            UserResourceProfileRepository userResourceProfileRepository,
            UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository,
            UserStoragePreferenceRepository userStoragePreferenceRepository,
            UserResourceProfileMapper userResourceProfileMapper,
            UserComputeResourcePreferenceMapper userComputeResourcePreferenceMapper,
            UserStoragePreferenceMapper userStoragePreferenceMapper) {
        this.userResourceProfileRepository = userResourceProfileRepository;
        this.userComputeResourcePreferenceRepository = userComputeResourcePreferenceRepository;
        this.userStoragePreferenceRepository = userStoragePreferenceRepository;
        this.userResourceProfileMapper = userResourceProfileMapper;
        this.userComputeResourcePreferenceMapper = userComputeResourcePreferenceMapper;
        this.userStoragePreferenceMapper = userStoragePreferenceMapper;
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
        UserResourceProfileEntity userResourceProfileEntity = saveUserResourceProfile(userResourceProfile);
        return userResourceProfileEntity.getUserId();
    }

    private UserResourceProfileEntity saveUserResourceProfile(UserResourceProfile userResourceProfile)
            throws AppCatalogException {
        String userId = userResourceProfile.getUserId();
        String gatewayId = userResourceProfile.getGatewayID();
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            throw new AppCatalogException("GatewayID is required for UserResourceProfile");
        }
        UserResourceProfileEntity userResourceProfileEntity = userResourceProfileMapper.toEntity(userResourceProfile);

        // Ensure gatewayId is set on the entity (mapper might not handle it correctly)
        userResourceProfileEntity.setGatewayId(gatewayId);

        // Manually map compute resource preferences (mapper ignores them)
        if (userResourceProfile.getUserComputeResourcePreferences() != null
                && !userResourceProfile.getUserComputeResourcePreferences().isEmpty()) {
            logger.debug("Mapping UserComputeResourcePreferences for the User Resource Profile");
            List<UserComputeResourcePreferenceEntity> computePrefs = userComputeResourcePreferenceMapper.toEntityList(
                    userResourceProfile.getUserComputeResourcePreferences());
            computePrefs.forEach(pref -> {
                pref.setUserId(userId);
                pref.setGatewayId(gatewayId);
                // Note: Don't set userResourceProfile - the JoinColumns have insertable=false, updatable=false
                // The foreign keys are managed by the explicit userId and gatewayId fields
            });
            userResourceProfileEntity.setUserComputeResourcePreferences(computePrefs);
        }

        // Manually map storage preferences (mapper ignores them)
        if (userResourceProfile.getUserStoragePreferences() != null
                && !userResourceProfile.getUserStoragePreferences().isEmpty()) {
            logger.debug("Mapping UserStoragePreferences for the User Resource Profile");
            List<UserStoragePreferenceEntity> storagePrefs =
                    userStoragePreferenceMapper.toEntityList(userResourceProfile.getUserStoragePreferences());
            storagePrefs.forEach(pref -> {
                pref.setUserId(userId);
                pref.setGatewayId(gatewayId);
                // Note: Don't set userResourceProfile - the JoinColumns have insertable=false, updatable=false
                // The foreign keys are managed by the explicit userId and gatewayId fields
            });
            userResourceProfileEntity.setUserStoragePreferences(storagePrefs);
        }

        if (!isUserResourceProfileExists(userId, gatewayId)) {
            logger.debug("Checking if the User Resource Profile already exists");
            userResourceProfileEntity.setCreationTime(AiravataUtils.getUniqueTimestamp());
        }

        userResourceProfileEntity.setUpdateTime(AiravataUtils.getUniqueTimestamp());
        return userResourceProfileRepository.save(userResourceProfileEntity);
    }

    @Transactional(readOnly = true)
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        UserResourceProfileEntity entity =
                userResourceProfileRepository.findById(userResourceProfilePK).orElse(null);
        if (entity == null) return null;

        UserResourceProfile model = userResourceProfileMapper.toModel(entity);

        // Manually load preferences from repositories to ensure they're loaded
        // The entity's collections might not be initialized due to lazy loading
        List<UserComputeResourcePreferenceEntity> computePrefs =
                userComputeResourcePreferenceRepository.findByUserIdAndGatewayId(userId, gatewayId);
        if (computePrefs != null && !computePrefs.isEmpty()) {
            model.setUserComputeResourcePreferences(userComputeResourcePreferenceMapper.toModelList(computePrefs));
        } else {
            model.setUserComputeResourcePreferences(new ArrayList<>());
        }

        List<UserStoragePreferenceEntity> storagePrefs =
                userStoragePreferenceRepository.findByUserIdAndGatewayId(userId, gatewayId);
        if (storagePrefs != null && !storagePrefs.isEmpty()) {
            model.setUserStoragePreferences(userStoragePreferenceMapper.toModelList(storagePrefs));
        } else {
            model.setUserStoragePreferences(new ArrayList<>());
        }

        return model;
    }

    @Transactional(readOnly = true)
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayId, String hostId) throws AppCatalogException {
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(hostId);
        return userComputeResourcePreferenceRepository
                .findById(userComputeResourcePreferencePK)
                .map(entity -> userComputeResourcePreferenceMapper.toModel(entity))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId)
            throws AppCatalogException {
        UserStoragePreferencePK userStoragePreferencePK = new UserStoragePreferencePK();
        userStoragePreferencePK.setUserId(userId);
        userStoragePreferencePK.setGatewayId(gatewayId);
        userStoragePreferencePK.setStorageResourceId(storageId);
        return userStoragePreferenceRepository
                .findById(userStoragePreferencePK)
                .map(entity -> userStoragePreferenceMapper.toModel(entity))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        List<UserResourceProfileEntity> entities = userResourceProfileRepository.findAll();
        return userResourceProfileMapper.toModelList(entities);
    }

    @Transactional(readOnly = true)
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        List<UserComputeResourcePreferenceEntity> entities =
                userComputeResourcePreferenceRepository.findByUserIdAndGatewayId(userId, gatewayId);
        return userComputeResourcePreferenceMapper.toModelList(entities);
    }

    @Transactional(readOnly = true)
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        List<UserStoragePreferenceEntity> entities =
                userStoragePreferenceRepository.findByUserIdAndGatewayId(userId, gatewayId);
        return userStoragePreferenceMapper.toModelList(entities);
    }

    @Transactional(readOnly = true)
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        List<UserResourceProfileEntity> entities = userResourceProfileRepository.findAll();
        List<String> gatewayIdList = new ArrayList<>();
        for (UserResourceProfileEntity entity : entities) {
            UserResourceProfile profile = userResourceProfileMapper.toModel(entity);
            if (gatewayName == null || profile.getGatewayID().equals(gatewayName)) {
                gatewayIdList.add(profile.getGatewayID());
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
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        if (!userResourceProfileRepository.existsById(userResourceProfilePK)) {
            return false;
        }
        userResourceProfileRepository.deleteById(userResourceProfilePK);
        return true;
    }

    @Transactional
    public boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        userComputeResourcePreferenceRepository.deleteById(userComputeResourcePreferencePK);
        return true;
    }

    @Transactional
    public boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserStoragePreferencePK userStoragePreferencePK = new UserStoragePreferencePK();
        userStoragePreferencePK.setUserId(userId);
        userStoragePreferencePK.setGatewayId(gatewayId);
        userStoragePreferencePK.setStorageResourceId(preferenceId);
        userStoragePreferenceRepository.deleteById(userStoragePreferencePK);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        return userResourceProfileRepository.existsById(userResourceProfilePK);
    }

    @Transactional(readOnly = true)
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        return userComputeResourcePreferenceRepository.existsById(userComputeResourcePreferencePK);
    }
}
