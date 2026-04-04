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

import java.sql.Timestamp;
import java.util.*;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.UserComputeResourcePreferencePK;
import org.apache.airavata.compute.model.UserResourceProfileEntity;
import org.apache.airavata.compute.model.UserResourceProfilePK;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.UsrResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserResourceProfileRepository
        extends AbstractRepository<UserResourceProfile, UserResourceProfileEntity, UserResourceProfilePK>
        implements UsrResourceProfile {
    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileRepository.class);

    public UserResourceProfileRepository() {
        super(UserResourceProfile.class, UserResourceProfileEntity.class);
    }

    @Override
    protected UserResourceProfile toModel(UserResourceProfileEntity entity) {
        return ComputeMapper.INSTANCE.userResourceProfileToModel(entity);
    }

    @Override
    protected UserResourceProfileEntity toEntity(UserResourceProfile model) {
        return ComputeMapper.INSTANCE.userResourceProfileToEntity(model);
    }

    protected String saveUserResourceProfileData(UserResourceProfile userResourceProfile) throws AppCatalogException {
        UserResourceProfileEntity userResourceProfileEntity = saveUserResourceProfile(userResourceProfile);
        return userResourceProfileEntity.getUserId();
    }

    protected UserResourceProfileEntity saveUserResourceProfile(UserResourceProfile userResourceProfile)
            throws AppCatalogException {
        String userId = userResourceProfile.getUserId();
        String gatewayId = userResourceProfile.getGatewayId();
        UserResourceProfileEntity userResourceProfileEntity =
                ComputeMapper.INSTANCE.userResourceProfileToEntity(userResourceProfile);

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
    public void updateUserResourceProfile(String userId, String gatewayId, UserResourceProfile updatedProfile)
            throws AppCatalogException {
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
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayId, String hostId) throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository =
                new UserComputeResourcePreferenceRepository();
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(hostId);
        UserComputeResourcePreference userComputeResourcePreference =
                userComputeResourcePreferenceRepository.get(userComputeResourcePreferencePK);
        return userComputeResourcePreference;
    }

    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        List<UserResourceProfile> userResourceProfileList = select(QueryConstants.GET_ALL_USER_RESOURCE_PROFILE, 0);
        return userResourceProfileList;
    }

    @Override
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayId)
            throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository =
                new UserComputeResourcePreferenceRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserComputeResourcePreference.USER_ID, userId);
        queryParameters.put(DBConstants.UserComputeResourcePreference.GATEWAY_ID, gatewayId);
        List<UserComputeResourcePreference> userComputeResourcePreferenceList =
                userComputeResourcePreferenceRepository.select(
                        QueryConstants.GET_ALL_USER_COMPUTE_RESOURCE_PREFERENCE, -1, 0, queryParameters);
        return userComputeResourcePreferenceList;
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.UserResourceProfile.GATEWAY_ID, gatewayName);
        List<UserResourceProfile> userResourceProfileList =
                select(QueryConstants.GET_ALL_GATEWAY_ID, -1, 0, queryParameters);
        List<String> gatewayIdList = new ArrayList<>();
        for (UserResourceProfile userResourceProfile : userResourceProfileList) {
            gatewayIdList.add(userResourceProfile.getGatewayId());
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
    public boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository =
                new UserComputeResourcePreferenceRepository();
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        return userComputeResourcePreferenceRepository.delete(userComputeResourcePreferencePK);
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        UserResourceProfilePK userResourceProfilePK = new UserResourceProfilePK();
        userResourceProfilePK.setUserId(userId);
        userResourceProfilePK.setGatewayId(gatewayId);
        return isExists(userResourceProfilePK);
    }

    @Override
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayId, String preferenceId)
            throws AppCatalogException {
        UserComputeResourcePreferenceRepository userComputeResourcePreferenceRepository =
                new UserComputeResourcePreferenceRepository();
        UserComputeResourcePreferencePK userComputeResourcePreferencePK = new UserComputeResourcePreferencePK();
        userComputeResourcePreferencePK.setUserId(userId);
        userComputeResourcePreferencePK.setGatewayId(gatewayId);
        userComputeResourcePreferencePK.setComputeResourceId(preferenceId);
        return userComputeResourcePreferenceRepository.isExists(userComputeResourcePreferencePK);
    }

    public static Logger getLogger() {
        return logger;
    }
}
