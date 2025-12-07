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

import com.github.dozermapper.core.Mapper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.registry.entities.appcatalog.UserComputeResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.UserComputeResourcePreferencePK;
import org.apache.airavata.registry.entities.appcatalog.UserResourceProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.UserResourceProfilePK;
import org.apache.airavata.registry.entities.appcatalog.UserStoragePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.UserStoragePreferencePK;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.appcatalog.UserComputeResourcePreferenceRepository;
import org.apache.airavata.registry.repositories.appcatalog.UserResourceProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.UserStoragePreferenceRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserResourceProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileService.class);

    @Autowired
    private UserResourceProfileRepository userResourceProfileRepository;

    @Autowired
    private UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository;

    @Autowired
    private UserStoragePreferenceRepository userStoragePreferenceRepository;

    public String addUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException {
        return saveUserResourceProfileData(userResourceProfile);
    }

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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UserResourceProfileEntity userResourceProfileEntity =
                mapper.map(userResourceProfile, UserResourceProfileEntity.class);

        if (userResourceProfileEntity.getUserComputeResourcePreferences() != null) {
            logger.debug(
                    "Populating the Primary Key UserComputeResourcePreferences objects for the User Resource Profile");
            userResourceProfileEntity
                    .getUserComputeResourcePreferences()
                    .forEach(userComputeResourcePreferenceEntity -> {
                        userComputeResourcePreferenceEntity.setUserId(userId);
                        userComputeResourcePreferenceEntity.setGatewayId(gatewayId);
                    });
        }

        if (userResourceProfileEntity.getUserStoragePreferences() != null) {
            logger.debug("Populating the Primary Key UserStoragePreferences objects for the User Resource Profile");
            userResourceProfileEntity.getUserStoragePreferences().forEach(userStoragePreferenceEntity -> {
                userStoragePreferenceEntity.setUserId(userId);
                userStoragePreferenceEntity.setGatewayId(gatewayId);
            });
        }

        if (!isUserResourceProfileExists(userId, gatewayId)) {
            logger.debug("Checking if the User Resource Profile already exists");
            userResourceProfileEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        userResourceProfileEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return userResourceProfileRepository.save(userResourceProfileEntity);
    }

    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        UserResourceProfileEntity entity =
                userResourceProfileRepository.findById(userResourceProfilePK).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, UserResourceProfile.class);
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayId, String hostId) throws AppCatalogException {
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(hostId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return userComputeResourcePreferenceRepository
                .findById(userComputeResourcePreferencePK)
                .map(entity -> mapper.map(entity, UserComputeResourcePreference.class))
                .orElse(null);
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId)
            throws AppCatalogException {
        UserStoragePreferencePK userStoragePreferencePK = new UserStoragePreferencePK();
        userStoragePreferencePK.setUserId(userId);
        userStoragePreferencePK.setGatewayId(gatewayId);
        userStoragePreferencePK.setStorageResourceId(storageId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return userStoragePreferenceRepository
                .findById(userStoragePreferencePK)
                .map(entity -> mapper.map(entity, UserStoragePreference.class))
                .orElse(null);
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        List<UserResourceProfileEntity> entities = userResourceProfileRepository.findAll();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<UserResourceProfile> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, UserResourceProfile.class)));
        return result;
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<UserComputeResourcePreferenceEntity> entities =
                userComputeResourcePreferenceRepository.findByUserIdAndGatewayId(userId, gatewayId);
        return entities.stream()
                .map(entity -> mapper.map(entity, UserComputeResourcePreference.class))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<UserStoragePreferenceEntity> entities =
                userStoragePreferenceRepository.findByUserIdAndGatewayId(userId, gatewayId);
        return entities.stream()
                .map(entity -> mapper.map(entity, UserStoragePreference.class))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        List<UserResourceProfileEntity> entities = userResourceProfileRepository.findAll();
        List<String> gatewayIdList = new ArrayList<>();
        for (UserResourceProfileEntity entity : entities) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            UserResourceProfile profile = mapper.map(entity, UserResourceProfile.class);
            if (gatewayName == null || profile.getGatewayID().equals(gatewayName)) {
                gatewayIdList.add(profile.getGatewayID());
            }
        }
        return gatewayIdList;
    }

    public String getUserNamefromID(String userId, String gatewayID) throws AppCatalogException {
        return userId;
    }

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

    public boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        userComputeResourcePreferenceRepository.deleteById(userComputeResourcePreferencePK);
        return true;
    }

    public boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserStoragePreferencePK userStoragePreferencePK = new UserStoragePreferencePK();
        userStoragePreferencePK.setUserId(userId);
        userStoragePreferencePK.setGatewayId(gatewayId);
        userStoragePreferencePK.setStorageResourceId(preferenceId);
        userStoragePreferenceRepository.deleteById(userStoragePreferencePK);
        return true;
    }

    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        return userResourceProfileRepository.existsById(userResourceProfilePK);
    }

    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        return userComputeResourcePreferenceRepository.existsById(userComputeResourcePreferencePK);
    }
}
