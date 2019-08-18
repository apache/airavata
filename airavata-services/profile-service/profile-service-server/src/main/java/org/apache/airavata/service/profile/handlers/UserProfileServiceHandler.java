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
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.user.core.repositories.CustomUserDashboardRepository;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.service.profile.utils.CustosClientFactory;
import org.apache.airavata.service.profile.utils.ThriftCustosDataModelConversion;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserProfileServiceHandler implements UserProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(UserProfileServiceHandler.class);

    private static CustomUserDashboardRepository customUserDashboardRepository = new CustomUserDashboardRepository();
    private static org.apache.custos.profile.user.cpi.UserProfileService.Client custosUserProfileClient;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.USER_PROFILE);

    public UserProfileServiceHandler() throws TException{
        custosUserProfileClient = CustosClientFactory.getCustosUserProfileClient();
    }

    @Override
    public String getAPIVersion() throws TException {
        return profile_user_cpiConstants.USER_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String initializeUserProfile(AuthzToken authzToken) throws UserProfileServiceException, AuthorizationException, TException {
        try {
            UserProfile userProfile= ThriftCustosDataModelConversion.getUserProfile(custosUserProfileClient.initializeUserProfile(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken)));

            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
                // replicate userProfile at end-places
                dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, userProfile);
                // return userId
                return userProfile.getUserId();
            } else {
                throw new Exception("User creation failed. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Error while initializing user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while initializing user profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addUserProfile(AuthzToken authzToken, UserProfile userProfile) throws UserProfileServiceException, AuthorizationException, TException {
        try{
            UserProfile createdUserProfile = ThriftCustosDataModelConversion.getUserProfile(custosUserProfileClient.addUserProfile(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), ThriftCustosDataModelConversion.getCustosUserProfile(userProfile)));

            if (null != createdUserProfile) {
                if(userProfile.isSetCustomDashboard()) {
                    createdUserProfile.setCustomDashboard(userProfile.getCustomDashboard());
                    customUserDashboardRepository.updateCustosDashboard(userProfile.getCustomDashboard(),null);
                }
                logger.info("Added UserProfile with userId: " + createdUserProfile.getUserId());
                // replicate userProfile at end-places
                dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, createdUserProfile);
                // return userId
                return createdUserProfile.getUserId();
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
            UserProfile updatedUserProfile = ThriftCustosDataModelConversion.getUserProfile(custosUserProfileClient.updateUserProfile(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), ThriftCustosDataModelConversion.getCustosUserProfile(userProfile)));
            if(userProfile != null) {
                if(userProfile.isSetCustomDashboard()) {
                    updatedUserProfile.setCustomDashboard(userProfile.getCustomDashboard());
                    customUserDashboardRepository.updateCustosDashboard(userProfile.getCustomDashboard(),null);
                }
                logger.info("Updated UserProfile with userId: " + updatedUserProfile.getUserId());
                // replicate userProfile at end-places
                dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.UPDATE, updatedUserProfile);
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
        try {
            UserProfile userProfile = ThriftCustosDataModelConversion.getUserProfile(custosUserProfileClient.getUserProfileById(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), userId, gatewayId));
            if (userProfile != null){
                userProfile.setCustomDashboard(customUserDashboardRepository.getDashboardDetailsUsingAiravataInternalUserId(userProfile.getAiravataInternalUserId()));
            return userProfile;
        }
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
            UserProfile userProfile = ThriftCustosDataModelConversion.getUserProfile(custosUserProfileClient.deleteUserProfile(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), userId, gatewayId));

            // delete user
            boolean deleteSuccess = (userProfile != null);
            logger.info("Delete UserProfile with userId: " + userId + ", " + (deleteSuccess? "Success!" : "Failed!"));

            if (deleteSuccess) {
                // delete userProfile at end-places
                customUserDashboardRepository.delete(userProfile.getAiravataInternalUserId());
                dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.DELETE, userProfile);
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
            List<UserProfile> usersInGateway = ThriftCustosDataModelConversion.getUserProfiles(custosUserProfileClient.getAllUserProfilesInGateway(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), gatewayId, offset, limit));
            for(UserProfile users: usersInGateway) {
                users.setCustomDashboard(customUserDashboardRepository.getDashboardDetailsUsingAiravataInternalUserId(users.getAiravataInternalUserId()));
            }
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
            return custosUserProfileClient.doesUserExist(ThriftCustosDataModelConversion.getCustosAuthzToken(authzToken), userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error while finding user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while finding user profile. More info : " + e.getMessage());
            throw exception;
        }
    }
}
