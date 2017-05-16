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
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.commons.user.entities.UserProfileEntity;
import org.apache.airavata.service.profile.user.core.repositories.UserProfileRepository;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.service.profile.utils.ProfileServiceUtils;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
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

    @Override
    @SecurityCheck
    public String addUserProfile(AuthzToken authzToken, UserProfile userProfile) throws UserProfileServiceException, AuthorizationException, TException {
        try{
            userProfile = userProfileRepository.create(userProfile);
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
                // replicate userProfile at end-places
                ProfileServiceUtils.getDbEventPublisher().publish(
                        ProfileServiceUtils.getDBEventMessageContext(EntityType.USER_PROFILE, CrudType.CREATE, userProfile),
                        DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString())
                );
                // return userId
                return userProfile.getUserId();
            } else {
                throw new Exception("User creation failed. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Error while creating user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while creating user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserProfile(AuthzToken authzToken, UserProfile userProfile) throws UserProfileServiceException, AuthorizationException, TException {
        try {
            if(userProfileRepository.update(userProfile) != null) {
                logger.info("Updated UserProfile with userId: " + userProfile.getUserId());
                // replicate userProfile at end-places
                ProfileServiceUtils.getDbEventPublisher().publish(
                        ProfileServiceUtils.getDBEventMessageContext(EntityType.USER_PROFILE, CrudType.UPDATE, userProfile),
                        DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString())
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error while Updating user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while Updating user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UserProfile getUserProfileById(AuthzToken authzToken, String userId, String gatewayId) throws UserProfileServiceException, AuthorizationException, TException {
        try{
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);
            if(userProfile != null)
                return userProfile;
            else
                throw new Exception("User with userId: " + userId + ", in Gateway: " + gatewayId + ", does not exist.");
        } catch (Exception e) {
            logger.error("Error retrieving user profile by ID", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error retrieving user profile by ID. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserProfile(AuthzToken authzToken, String userId, String gatewayId) throws UserProfileServiceException, AuthorizationException, TException {
        try{
            // find user-profile
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);

            // delete user
            boolean deleteSuccess = userProfileRepository.delete(userId);
            logger.info("Delete UserProfile with userId: " + userId + ", " + (deleteSuccess? "Success!" : "Failed!"));

            if (deleteSuccess) {
                // delete userProfile at end-places
                ProfileServiceUtils.getDbEventPublisher().publish(
                        ProfileServiceUtils.getDBEventMessageContext(EntityType.USER_PROFILE, CrudType.DELETE, userProfile),
                        DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString())
                );
            }
            return deleteSuccess;
        } catch (Exception e) {
            logger.error("Error while deleting user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while deleting user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> getAllUserProfilesInGateway(AuthzToken authzToken, String gatewayId, int offset, int limit) throws UserProfileServiceException, AuthorizationException, TException {
        try{
            List<UserProfile> usersInGateway = userProfileRepository.getAllUserProfilesInGateway(gatewayId, offset, limit);
            if(usersInGateway != null)
                return usersInGateway;
            else
                throw new Exception("There are no users for the requested gatewayId: " + gatewayId);
        } catch (Exception e) {
            logger.error("Error while retrieving user profile List", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while retrieving user profile List. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean doesUserExist(AuthzToken authzToken, String userId, String gatewayId) throws UserProfileServiceException, AuthorizationException, TException {
        try{
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);
            return null != userProfile;
        } catch (Exception e) {
            logger.error("Error while finding user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while finding user profile. More info : " + e.getMessage());
            throw exception;
        }
    }
}
