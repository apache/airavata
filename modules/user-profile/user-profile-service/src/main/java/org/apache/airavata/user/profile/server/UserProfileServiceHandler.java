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
package org.apache.airavata.user.profile.server;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.apache.airavata.userprofile.cpi.UserProfileService;
import org.apache.airavata.userprofile.cpi.exception.UserProfileServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class UserProfileServiceHandler implements UserProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(UserProfileServiceHandler.class);

    private UserProfileRepository userProfileRepository;

    public UserProfileServiceHandler() {

        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
    }

    public String addUserProfile(UserProfile userProfile) throws UserProfileServiceException{
        try{
            userProfileRepository.create(userProfile);
            if (null != userProfile)
                return userProfile.getUserId();
            return null;
        } catch (Exception e){
            logger.error("Error while creating user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while creating user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    public boolean updateUserProfile(UserProfile userProfile) throws UserProfileServiceException, TException {
        try {
            if(userProfileRepository.update(userProfile) != null)
                return true;
            return false;
        } catch (Exception e) {
            logger.error("Error while Updating user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while Updating user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    public UserProfile getUserProfileById(String userId, String gatewayId) throws UserProfileServiceException {
        try{
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);
            if(userProfile != null)
                return userProfile;
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving user profile by ID", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error retrieving user profile by ID. More info : " + e.getMessage());
            throw exception;
        }
    }

    public boolean deleteUserProfile(String userId) throws UserProfileServiceException {
        try{
            boolean deleteResult = userProfileRepository.delete(userId);
            return deleteResult;
        } catch (Exception e) {
            logger.error("Error while deleting user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while deleting user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit) throws UserProfileServiceException {
        try{
            List<UserProfile> usersInGateway = userProfileRepository.getAllUserProfilesInGateway(gatewayId, offset, limit);
            if(usersInGateway != null)
                return usersInGateway;
            return null;
        } catch (Exception e) {
            logger.error("Error while retrieving user profile List", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while retrieving user profile List. More info : " + e.getMessage());
            throw exception;
        }
    }


    public UserProfile getUserProfileByName(String userName, String gatewayId) throws UserProfileServiceException {
        try{
            UserProfile userProfile = userProfileRepository.getUserProfileByNameAndGateWay(userName, gatewayId);
            if(userProfile != null)
                return userProfile;
            return null;
        } catch (Exception e) {
            logger.error("Error while retrieving user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while retrieving user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    public boolean doesUserExist(String userName, String gatewayId) throws UserProfileServiceException, TException {
        try{
            UserProfile userProfile = userProfileRepository.getUserProfileByNameAndGateWay(userName, gatewayId);
            if (null != userProfile)
                return true;
            return false;
        } catch (Exception e) {
            logger.error("Error while finding user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while finding user profile. More info : " + e.getMessage());
            throw exception;
        }
    }
}
