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
package org.apache.airavata.iam.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.iam.mapper.ProfileMapper;
import org.apache.airavata.iam.model.UserProfileEntity;
import org.apache.airavata.iam.util.QueryConstants;
import org.apache.airavata.interfaces.UserProfileProvider;
import org.apache.airavata.model.user.proto.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserProfileRepository extends AbstractRepository<UserProfile, UserProfileEntity, String>
        implements UserProfileProvider {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileRepository.class);

    public UserProfileRepository() {
        super(UserProfile.class, UserProfileEntity.class);
    }

    @Override
    protected UserProfile toModel(UserProfileEntity entity) {
        return ProfileMapper.INSTANCE.userProfileToModel(entity);
    }

    @Override
    protected UserProfileEntity toEntity(UserProfile model) {
        return ProfileMapper.INSTANCE.userProfileToEntity(model);
    }

    public UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId) {
        UserProfile userProfile = null;

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(QueryConstants.USER_ID, userId);
        queryParam.put(QueryConstants.GATEWAY_ID, gatewayId);
        List<UserProfile> resultList = select(QueryConstants.FIND_USER_PROFILE_BY_USER_ID, 1, 0, queryParam);

        if (resultList != null && resultList.size() > 0) userProfile = resultList.get(0);

        return userProfile;
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit) {

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put(QueryConstants.GATEWAY_ID, gatewayId);

        List<UserProfile> resultList = null;
        if (limit > 0) {
            resultList = select(QueryConstants.FIND_ALL_USER_PROFILES_BY_GATEWAY_ID, limit, offset, queryParams);
        } else {
            resultList = select(QueryConstants.FIND_ALL_USER_PROFILES_BY_GATEWAY_ID, queryParams);
        }

        return resultList;
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        return updateUserProfile(userProfile, null);
    }

    public UserProfile createUserProfile(UserProfile userProfile, Runnable postUpdateAction) {
        return updateUserProfile(userProfile, postUpdateAction);
    }

    public UserProfile updateUserProfile(UserProfile userProfile, Runnable postUpdateAction) {
        UserProfileEntity entity = ProfileMapper.INSTANCE.userProfileToEntity(userProfile);
        UserProfileEntity persistedCopy = execute(entityManager -> {
            UserProfileEntity result = entityManager.merge(entity);
            if (postUpdateAction != null) {
                postUpdateAction.run();
            }
            return result;
        });
        return ProfileMapper.INSTANCE.userProfileToModel(persistedCopy);
    }

    //    public static void main(String args[]) {
    //
    //        UserProfile up = new UserProfile();
    //        up.setAiravataInternalUserId("asd");
    //        up.setComments("asd");
    //        up.setCountry("sd");
    //        up.setCreationTime("ad");
    //        up.setGatewayId("asd");
    //
    //        UserProfileEntity upe = new UserProfileEntity();
    //        upe.setGatewayId("bl");
    //        upe.setCreationTime(new Date());
    //
    //        Class t = UserProfile.class;
    //        Class e = UserProfileEntity.class;
    //        Object o = mapper.map(upe, t);
    //        System.out.println(o);
    //    }
}
