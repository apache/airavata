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
package org.apache.airavata.userprofile.core;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.userprofile.cpi.UserProfileCPI;
import org.apache.airavata.userprofile.cpi.UserProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class UserProfileCPIImpl implements UserProfileCPI {
    private final static Logger logger = LoggerFactory.getLogger(UserProfileCPIImpl.class);

    private UserProfileDao userProfileDao;

    public UserProfileCPIImpl() throws UserProfileException {
        userProfileDao = new UserProfileDao();
    }

    @Override
    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId) throws UserProfileException {
        return userProfileDao.getAllUserProfilesInGateway(gatewayId);
    }

    @Override
    public String createUserProfile(UserProfile userProfile) throws UserProfileException {
        userProfile.setUserId(UUID.randomUUID().toString());
        // Setting user id to airavataInternalUserId. We don't distinguish these two at the moment.
        userProfile.setAiravataInternalUserId(userProfile.getUserId());
        userProfileDao.createUserProfile(userProfile);
        return userProfile.getUserId();
    }

    @Override
    public boolean updateUserProfile(UserProfile userProfile) throws UserProfileException {
        return userProfileDao.updateUserProfile(userProfile);
    }

    @Override
    public boolean deleteUserProfile(String userId) throws UserProfileException {
        return userProfileDao.deleteUserProfile(userId);
    }

    @Override
    public UserProfile getUserProfileFromUserId(String userId) throws UserProfileException {
        return userProfileDao.getUserProfileFromUserId(userId);
    }

    @Override
    public UserProfile getUserProfileFromUserName(String userName, String gatewayId) throws UserProfileException {
        return userProfileDao.getUserProfileFromUserName(userName, gatewayId);
    }

    @Override
    public boolean userProfileExists(String userName, String gatewayId) throws UserProfileException {
        return userProfileDao.userProfileExists(userName, gatewayId);
    }
}