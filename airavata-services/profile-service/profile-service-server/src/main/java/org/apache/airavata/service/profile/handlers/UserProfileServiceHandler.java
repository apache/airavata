/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.CustosToAiravataDataModelMapper;
import org.apache.airavata.common.utils.CustosUtils;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.user.core.repositories.UserProfileRepository;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.custos.iam.service.FindUsersResponse;
import org.apache.custos.iam.service.OperationStatus;
import org.apache.custos.iam.service.UserRepresentation;
import org.apache.custos.user.management.client.UserManagementClient;
import org.apache.custos.user.profile.service.GetAllUserProfilesResponse;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserProfileServiceHandler implements UserProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(UserProfileServiceHandler.class);

    private UserProfileRepository userProfileRepository;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.USER_PROFILE);

    private UserManagementClient userManagementClient;

    public UserProfileServiceHandler() {
        try {
            userProfileRepository = new UserProfileRepository();
            userManagementClient = CustosUtils.getCustosClientProvider().getUserManagementClient();
        } catch (Exception ex) {
            logger.error("Error occurred while initializing Custos client");
        }

    }

    @Override
    public String getAPIVersion() throws TException {
        return profile_user_cpiConstants.USER_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String initializeUserProfile(AuthzToken authzToken) throws UserProfileServiceException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            org.apache.custos.iam.service.UserRepresentation userRepresentation =
                    userManagementClient.getUser(username, custosId);

            userManagementClient.updateUserProfile(userRepresentation.getUsername(),
                    userRepresentation.getFirstName(),
                    userRepresentation.getLastName(),
                    userRepresentation.getEmail(),
                    custosId);
            return userRepresentation.getUsername().toLowerCase();
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
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
            // Lowercase user id and internal id
            org.apache.custos.user.profile.service.UserProfile profile = userManagementClient.
                    updateUserProfile(userProfile.getUserId().toLowerCase(),
                            userProfile.getFirstName(),
                            userProfile.getLastName(),
                            userProfile.getEmails().get(0),
                            custosId);

            return profile.getUsername();
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
            // After updating the user profile in the database but before committing the transaction, the
            // following will update the user profile in the IAM service also. If the update in the IAM service
            // fails then the transaction will be rolled back.
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
            // Lowercase user id and internal id
            userManagementClient.
                    updateUserProfile(userProfile.getUserId().toLowerCase(),
                            userProfile.getFirstName(),
                            userProfile.getLastName(),
                            userProfile.getEmails().get(0),
                            custosId);

            return true;

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
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            UserRepresentation userRepresentation = userManagementClient.getUser(userId, custosId);

            return CustosToAiravataDataModelMapper.transform(userRepresentation, gatewayId);

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
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            OperationStatus status = userManagementClient
                    .deleteUser(userId, custosId, authzToken.getAccessToken());

            return status.getStatus();
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
        try {

            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            GetAllUserProfilesResponse response = userManagementClient.getAllUserProfiles(custosId);

            List<org.apache.custos.user.profile.service.UserProfile> userProfiles = response.getProfilesList();

            List<UserProfile> profiles = new ArrayList<>();
            if (userProfiles != null && !userProfiles.isEmpty()) {
                for (org.apache.custos.user.profile.service.UserProfile userProfile : userProfiles) {
                    UserProfile profile = CustosToAiravataDataModelMapper.transform(userProfile, custosId);
                    profiles.add(profile);
                }
            }

            return profiles;

        } catch (Exception e) {
            logger.error("Error while retrieving user profile List", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while retrieving user profile List. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean doesUserExist(AuthzToken authzToken, String userId, String gatewayId) throws UserProfileServiceException, AuthorizationException, TException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            FindUsersResponse response = userManagementClient.findUsers(null, userId, null, null,
                    null, 0, -1, custosId);

            return !response.getUsersList().isEmpty();

        } catch (Exception e) {
            logger.error("Error while finding user profile", e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage("Error while finding user profile. More info : " + e.getMessage());
            throw exception;
        }
    }


}
