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
package org.apache.airavata.service;

import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.profile.user.core.repositories.UserProfileRepository;
import org.apache.airavata.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.AiravataSecurityManager;
import org.apache.airavata.security.SecurityManagerFactory;
import org.apache.airavata.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private UserProfileRepository userProfileRepository;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.USER_PROFILE);

    public UserProfileService() {
        userProfileRepository = new UserProfileRepository();
    }

    public String initializeUserProfile(AuthzToken authzToken) throws UserProfileServiceException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            // Load UserInfo for the access token and create an initial UserProfile from it
            UserInfo userInfo = SecurityManagerFactory.getSecurityManager().getUserInfoFromAuthzToken(authzToken);
            final UserProfile existingProfile =
                    userProfileRepository.getUserProfileByIdAndGateWay(userInfo.getUsername(), gatewayId);
            // If a user profile already exists, just return the userId
            if (existingProfile != null) {
                return existingProfile.getUserId();
            }
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(userInfo.getUsername().toLowerCase());
            userProfile.setGatewayId(gatewayId);
            userProfile.setAiravataInternalUserId(userProfile.getUserId() + "@" + gatewayId);
            userProfile.addToEmails(userInfo.getEmailAddress());
            userProfile.setFirstName(userInfo.getFirstName());
            userProfile.setLastName(userInfo.getLastName());
            userProfile.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            userProfile.setLastAccessTime(AiravataUtils.getCurrentTimestamp().getTime());
            userProfile.setValidUntil(-1);
            userProfile.setState(Status.ACTIVE);
            userProfile = userProfileRepository.createUserProfile(userProfile);
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
                // replicate userProfile at end-places
                try {
                    dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, userProfile);
                } catch (AiravataException e) {
                    logger.error("Error publishing user profile creation event", e);
                }
                // return userId
                return userProfile.getUserId();
            } else {
                throw new UserProfileServiceException("User creation failed. Please try again.");
            }
        } catch (AiravataSecurityException e) {
            String message = "Error while initializing user profile: security error";
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        } catch (RuntimeException e) {
            String message = "Error while initializing user profile: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public String addUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, IamAdminServicesException {
        try {
            // Lowercase user id and internal id
            userProfile.setUserId(userProfile.getUserId().toLowerCase());
            userProfile.setAiravataInternalUserId(userProfile.getUserId() + "@" + userProfile.getGatewayId());
            userProfile = userProfileRepository.updateUserProfile(
                    userProfile, getIAMUserProfileUpdater(authzToken, userProfile));
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
                // replicate userProfile at end-places
                try {
                    dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, userProfile);
                } catch (AiravataException e) {
                    logger.error("Error publishing user profile creation event", e);
                }
                // return userId
                return userProfile.getUserId();
            } else {
                throw new UserProfileServiceException("User creation failed. Please try again.");
            }
        } catch (RuntimeException e) {
            String message = "Error while creating user profile: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean updateUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, IamAdminServicesException {
        try {
            // After updating the user profile in the database but before committing the transaction, the
            // following will update the user profile in the IAM service also. If the update in the IAM service
            // fails then the transaction will be rolled back.
            Runnable iamUserProfileUpdater = getIAMUserProfileUpdater(authzToken, userProfile);
            if (userProfileRepository.updateUserProfile(userProfile, iamUserProfileUpdater) != null) {
                logger.info("Updated UserProfile with userId: " + userProfile.getUserId());
                // replicate userProfile at end-places
                try {
                    dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.UPDATE, userProfile);
                } catch (AiravataException e) {
                    logger.error("Error publishing user profile update event", e);
                }
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            // Check if the RuntimeException wraps a UserProfileServiceException or IamAdminServicesException
            if (e.getCause() instanceof UserProfileServiceException) {
                throw (UserProfileServiceException) e.getCause();
            }
            if (e.getCause() instanceof IamAdminServicesException) {
                throw (IamAdminServicesException) e.getCause();
            }
            String msg = String.format(
                    "Error while updating user profile: userId=%s, gatewayId=%s, airavataInternalUserId=%s. Reason: %s",
                    userProfile.getUserId(),
                    userProfile.getGatewayId(),
                    userProfile.getAiravataInternalUserId(),
                    e.getMessage());
            logger.error(msg, e);
            UserProfileServiceException exception = new UserProfileServiceException(msg);
            exception.initCause(e);
            throw exception;
        }
    }

    private Runnable getIAMUserProfileUpdater(AuthzToken authzToken, UserProfile userProfile) {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        return () -> {
            try {
                AiravataSecurityManager securityManager = SecurityManagerFactory.getSecurityManager();
                AuthzToken serviceAccountAuthzToken =
                        securityManager.getUserManagementServiceAccountAuthzToken(gatewayId);
                IamAdminService iamAdminService = getIamAdminService();
                iamAdminService.updateUserProfile(serviceAccountAuthzToken, userProfile);
            } catch (AiravataSecurityException e) {
                String msg = String.format(
                        "Failed to update user profile in IAM service: gatewayId=%s, userId=%s, gatewayIdFromProfile=%s. Reason: %s",
                        gatewayId, userProfile.getUserId(), userProfile.getGatewayId(), e.getMessage());
                logger.error(msg, e);
                UserProfileServiceException exception = new UserProfileServiceException(msg);
                exception.initCause(e);
                throw new RuntimeException(exception);
            } catch (ServiceFactoryException e) {
                String msg = String.format(
                        "Failed to update user profile in IAM service: gatewayId=%s, userId=%s, gatewayIdFromProfile=%s. Reason: %s",
                        gatewayId, userProfile.getUserId(), userProfile.getGatewayId(), e.getMessage());
                logger.error(msg, e);
                UserProfileServiceException exception = new UserProfileServiceException(msg);
                exception.initCause(e);
                throw new RuntimeException(exception);
            } catch (IamAdminServicesException e) {
                String msg = String.format(
                        "Failed to update user profile in IAM service: gatewayId=%s, userId=%s, gatewayIdFromProfile=%s. Reason: %s",
                        gatewayId, userProfile.getUserId(), userProfile.getGatewayId(), e.getMessage());
                logger.error(msg, e);
                throw new RuntimeException(e);
            } catch (UserProfileServiceException e) {
                String msg = String.format(
                        "Failed to update user profile in IAM service: gatewayId=%s, userId=%s, gatewayIdFromProfile=%s. Reason: %s",
                        gatewayId, userProfile.getUserId(), userProfile.getGatewayId(), e.getMessage());
                logger.error(msg, e);
                throw new RuntimeException(e);
            }
        };
    }

    public UserProfile getUserProfileById(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);
            if (userProfile != null) return userProfile;
            else
                throw new UserProfileServiceException(
                        "User with userId: " + userId + ", in Gateway: " + gatewayId + ", does not exist.");
        } catch (RuntimeException e) {
            String message = "Error retrieving user profile by ID: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean deleteUserProfile(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            // find user-profile
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);

            // delete user
            boolean deleteSuccess = userProfileRepository.delete(userId);
            logger.info("Delete UserProfile with userId: " + userId + ", " + (deleteSuccess ? "Success!" : "Failed!"));

            if (deleteSuccess) {
                // delete userProfile at end-places
                try {
                    dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.DELETE, userProfile);
                } catch (AiravataException e) {
                    logger.error("Error publishing user profile deletion event", e);
                }
            }
            return deleteSuccess;
        } catch (RuntimeException e) {
            String message = "Error while deleting user profile: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<UserProfile> getAllUserProfilesInGateway(AuthzToken authzToken, String gatewayId, int offset, int limit)
            throws UserProfileServiceException {
        try {
            List<UserProfile> usersInGateway =
                    userProfileRepository.getAllUserProfilesInGateway(gatewayId, offset, limit);
            if (usersInGateway != null) return usersInGateway;
            else throw new UserProfileServiceException("There are no users for the requested gatewayId: " + gatewayId);
        } catch (RuntimeException e) {
            String message = "Error while retrieving user profile List: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean doesUserExist(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);
            return null != userProfile;
        } catch (RuntimeException e) {
            String message = "Error while finding user profile: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException exception = new UserProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    private IamAdminService getIamAdminService() throws UserProfileServiceException, ServiceFactoryException {
        try {
            return ServiceFactory.getInstance().getIamAdminService();
        } catch (ServiceFactoryException e) {
            logger.error("Failed to create IAM Admin Service", e);
            UserProfileServiceException ex = new UserProfileServiceException("Failed to create IAM Admin Service");
            ex.initCause(e);
            throw ex;
        } catch (RuntimeException e) {
            String message = "Failed to create IAM Admin Service: " + e.getMessage();
            logger.error(message, e);
            UserProfileServiceException ex = new UserProfileServiceException();
            ex.setMessage(message);
            ex.initCause(e);
            throw ex;
        }
    }
}
