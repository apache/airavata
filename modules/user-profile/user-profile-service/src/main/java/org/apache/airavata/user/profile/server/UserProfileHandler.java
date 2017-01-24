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
package org.apache.airavata.user.profile.server;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.apache.airavata.userprofile.crude.cpi.UserProfileCrudeService;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileHandler implements UserProfileCrudeService.Iface {

    private UserProfileRepository userProfileRepository;

    public UserProfileHandler() {

        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
    }

    public String addUserProfile(UserProfile userProfile) throws RegistryServiceException, TException {

        userProfileRepository.create(userProfile);

        if (null != userProfile)
            return userProfile.getUserId();

        return null;
    }

    public boolean updateUserProfile(UserProfile userProfile) throws RegistryServiceException, TException {

        try {
            userProfileRepository.update(userProfile);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    public UserProfile getUserProfileById(String userId, String gatewayId) throws RegistryServiceException, TException {


        UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);

        return userProfile;
    }

    public boolean deleteUserProfile(String userId) throws RegistryServiceException, TException {

        boolean deleteResult = userProfileRepository.delete(userId);

        return deleteResult;
    }



    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit) throws RegistryServiceException, TException {
        List<UserProfile> usersInGateway = userProfileRepository.getAllUserProfilesInGateway(gatewayId, offset, limit);
        return usersInGateway;
    }


    public UserProfile getUserProfileByName(String userName, String gatewayId) throws RegistryServiceException, TException {

        UserProfile userProfile = userProfileRepository.getUserProfileByNameAndGateWay(userName, gatewayId);
        return userProfile;
    }

    public boolean doesUserExist(String userName, String gatewayId) throws RegistryServiceException, TException {

        UserProfile userProfile = userProfileRepository.getUserProfileByNameAndGateWay(userName, gatewayId);

        if (null != userProfile)
            return true;
        return false;
    }
}
