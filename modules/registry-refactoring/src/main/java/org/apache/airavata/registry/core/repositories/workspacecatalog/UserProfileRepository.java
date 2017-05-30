/*
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
 *
*/
package org.apache.airavata.registry.core.repositories.workspacecatalog;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileRepository extends AbstractRepository<UserProfile, UserProfileEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileRepository.class);

    public UserProfileRepository(Class<UserProfile> thriftGenericClass, Class<UserProfileEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
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
        List<UserProfile> resultList = select(QueryConstants.FIND_USER_PROFILE_BY_USER_ID, 0, 1, queryParam);

        if (resultList != null && resultList.size() > 0)
            userProfile =  resultList.get(0);


        return userProfile;
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit)  {

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(UserProfile._Fields.GATEWAY_ID.getFieldName(), gatewayId);

        List<UserProfile> resultList = select(QueryConstants.FIND_ALL_USER_PROFILES_BY_GATEWAY_ID, limit, offset, queryParam);

        return  resultList;
    }
}