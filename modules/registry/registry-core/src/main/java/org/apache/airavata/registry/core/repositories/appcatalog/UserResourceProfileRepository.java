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

import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.registry.core.entities.appcatalog.UserComputeResourcePreferencePK;
import org.apache.airavata.registry.core.entities.appcatalog.UserResourceProfileEntity;
import org.apache.airavata.registry.core.entities.appcatalog.UserResourceProfilePK;
import org.apache.airavata.registry.core.entities.appcatalog.UserStoragePreferencePK;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.UsrResourceProfile;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class UserResourceProfileRepository extends AppCatAbstractRepository<UserResourceProfile, UserResourceProfileEntity, UserResourceProfilePK> implements UsrResourceProfile {
    private final static Logger logger = LoggerFactory.getLogger(UserResourceProfileRepository.class);

    public UserResourceProfileRepository() {
        super(UserResourceProfile.class, UserResourceProfileEntity.class);
    }

    protected String saveUserResourceProfileData(UserResourceProfile userResourceProfile) throws AppCatalogException {
        UserResourceProfileEntity userResourceProfileEntity = saveUserResourceProfile(userResourceProfile);
        return userResourceProfileEntity.getUserId();
    }

    protected UserResourceProfileEntity saveUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException {
        String userId = userResourceProfile.getUserId();
        String gatewayId = userResourceProfile.getGatewayID();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UserResourceProfileEntity userResourceProfileEntity = mapper.map(userResourceProfile, UserResourceProfileEntity.class);

        if (userResourceProfileEntity.getUserComputeResourcePreferences() != null) {
            logger.debug("Populating the Primary Key UserComputeResourcePreferences objects for the User Resource Profile");
            userResourceProfileEntity.getUserComputeResourcePreferences().forEach(userComputeResourcePreferenceEntity -> { userComputeResourcePreferenceEntity.setUserId(userId);
                userComputeResourcePreferenceEntity.setGatewayId(gatewayId); });
        }

        if (userResourceProfileEntity.getUserStoragePreferences() != null) {
            logger.debug("Populating the Primary Key UserStoragePreferences objects for the User Resource Profile");
            userResourceProfileEntity.getUserStoragePreferences().forEach(userStoragePreferenceEntity -> { userStoragePreferenceEntity.setUserId(userId);
                userStoragePreferenceEntity.setGatewayId(gatewayId); });
        }

        if (!isUserResourceProfileExists(userId, gatewayId)) {
            logger.debug("Checking if the User Resource Profile already exists");
            userResourceProfileEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        userResourceProfileEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return execute(entityManager -> entityManager.merge(userResourceProfileEntity));
    }

    @Override
    public String addUserResourceProfile(UserResourceProfile userResourceProfile) throws AppCatalogException {
        return saveUserResourceProfileData(userResourceProfile);
    }

    @Override
    public void updateUserResourceProfile(String userId, String gatewayId, UserResourceProfile updatedProfile) throws AppCatalogException {
        saveUserResourceProfileData(updatedProfile);
    }

    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        UserResourceProfile userResourceProfile = get(userResourceProfilePK);
        return userResourceProfile;
    }

    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(String userId, String gatewayId, String hostId) throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository = new UserComputeResourcePreferenceRepository();
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(hostId);
        UserComputeResourcePreference userComputeResourcePreference = userComputeResourcePreferenceRepository.get(userComputeResourcePreferencePK);
        return userComputeResourcePreference;
    }

    @Override
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId) throws AppCatalogException {
        UserStoragePreferenceRepository userStoragePreferenceRepository = new UserStoragePreferenceRepository();
        UserStoragePreferencePK userStoragePreferencePK = new UserStoragePreferencePK();
        userStoragePreferencePK.setUserId(userId);
        userStoragePreferencePK.setGatewayId(gatewayId);
        userStoragePreferencePK.setStorageResourceId(storageId);
        UserStoragePreference userStoragePreference = userStoragePreferenceRepository.get(userStoragePreferencePK);
        return userStoragePreference;
    }

    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        List<UserResourceProfile> userResourceProfileList = select(QueryConstants.GET_ALL_USER_RESOURCE_PROFILE, 0);
        return userResourceProfileList;
    }

    @Override
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayId) throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository = new UserComputeResourcePreferenceRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserComputeResourcePreference.USER_ID, userId);
        queryParameters.put(DBConstants.UserComputeResourcePreference.GATEWAY_ID, gatewayId);
        List<UserComputeResourcePreference> userComputeResourcePreferenceList =
                userComputeResourcePreferenceRepository.select(QueryConstants.GET_ALL_USER_COMPUTE_RESOURCE_PREFERENCE, -1, 0, queryParameters);
        return userComputeResourcePreferenceList;
    }

    @Override
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayId) throws AppCatalogException {
        UserStoragePreferenceRepository userStoragePreferenceRepository = new UserStoragePreferenceRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserStoragePreference.USER_ID, userId);
        queryParameters.put(DBConstants.UserStoragePreference.GATEWAY_ID, gatewayId);
        List<UserStoragePreference> userStoragePreferenceList =
                userStoragePreferenceRepository.select(QueryConstants.GET_ALL_USER_STORAGE_PREFERENCE, -1, 0, queryParameters);
        return userStoragePreferenceList;
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserResourceProfile.GATEWAY_ID, gatewayName);
        List<UserResourceProfile> userResourceProfileList = select(QueryConstants.GET_ALL_GATEWAY_ID, -1, 0, queryParameters);
        List<String> gatewayIdList = new ArrayList<>();
        for (UserResourceProfile userResourceProfile : userResourceProfileList) {
            gatewayIdList.add(userResourceProfile.getGatewayID());
        }
        return gatewayIdList;
    }

    @Override
    public String getUserNamefromID(String userId, String gatewayID) throws AppCatalogException {
        return userId;
    }

    @Override
    public boolean removeUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        return delete(userResourceProfilePK);
    }

    @Override
    public boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository = new UserComputeResourcePreferenceRepository();
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        return userComputeResourcePreferenceRepository.delete(userComputeResourcePreferencePK);
    }

    @Override
    public boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException {
        UserStoragePreferenceRepository userStoragePreferenceRepository = new UserStoragePreferenceRepository();
        UserStoragePreferencePK userStoragePreferencePK = new UserStoragePreferencePK();
        userStoragePreferencePK.setUserId(userId);
        userStoragePreferencePK.setGatewayId(gatewayId);
        userStoragePreferencePK.setStorageResourceId(preferenceId);
        return userStoragePreferenceRepository.delete(userStoragePreferencePK);
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        return isExists(userResourceProfilePK);
    }

    public static Logger getLogger() {
        return logger;
    }

}
