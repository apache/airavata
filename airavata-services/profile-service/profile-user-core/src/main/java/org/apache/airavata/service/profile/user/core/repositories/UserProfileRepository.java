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
package org.apache.airavata.service.profile.user.core.repositories;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.commons.repositories.AbstractRepository;
import org.apache.airavata.service.profile.commons.user.entities.UserProfileEntity;
import org.apache.airavata.service.profile.commons.utils.JPAUtils;
import org.apache.airavata.service.profile.commons.utils.ObjectMapperSingleton;
import org.apache.airavata.service.profile.commons.utils.QueryConstants;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileRepository extends AbstractRepository<UserProfile, UserProfileEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileRepository.class);

    public UserProfileRepository() {
        super(UserProfile.class, UserProfileEntity.class);
    }

    @Override
    public List<UserProfile> select(String query, int offset, int limit) {
        throw new UnsupportedOperationException("Due to performance overheads this method is not supported. Instead use" +
                " UserProfileSummaryRepository");
    }

    public UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId)   {
        UserProfile userProfile = null;

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(UserProfile._Fields.USER_ID.getFieldName(), userId);
        queryParam.put(UserProfile._Fields.GATEWAY_ID.getFieldName(), gatewayId);
        List<UserProfile> resultList = select(QueryConstants.FIND_USER_PROFILE_BY_USER_ID, 1, 0, queryParam);

        if (resultList != null && resultList.size() > 0)
            userProfile =  resultList.get(0);

        return userProfile;
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit)  {

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put(UserProfile._Fields.GATEWAY_ID.getFieldName(), gatewayId);

        List<UserProfile> resultList = null;
        if (limit > 0) {
            resultList = select(QueryConstants.FIND_ALL_USER_PROFILES_BY_GATEWAY_ID, limit, offset, queryParams);
        } else {
            resultList = select(QueryConstants.FIND_ALL_USER_PROFILES_BY_GATEWAY_ID, queryParams);
        }

        return  resultList;
    }

    @Override
    public UserProfile create(UserProfile userProfile) {
        throw new UnsupportedOperationException("Please use createUserProfile instead");
    }

    @Override
    public UserProfile update(UserProfile userProfile) {
        throw new UnsupportedOperationException("Please use updateUserProfile instead");
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        return updateUserProfile(userProfile, null);
    }

    public UserProfile createUserProfile(UserProfile userProfile, Runnable postUpdateAction) {
        return updateUserProfile(userProfile, postUpdateAction);
    }

    public UserProfile updateUserProfile(UserProfile userProfile, Runnable postUpdateAction) {

        Mapper mapper = ObjectMapperSingleton.getInstance();
        UserProfileEntity entity = mapper.map(userProfile, UserProfileEntity.class);
        UserProfileEntity persistedCopy = JPAUtils.execute(entityManager -> {
            UserProfileEntity result = entityManager.merge(entity);
            if (postUpdateAction != null) {
                postUpdateAction.run();
            }
            return result;
        });
        return mapper.map(persistedCopy, UserProfile.class);
    }

//    public static void main(String args[]) {
//        Mapper mapper = ObjectMapperSingleton.getInstance();
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