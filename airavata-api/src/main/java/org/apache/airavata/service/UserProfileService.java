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

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.apache.airavata.profile.entities.UserProfileEntity;
import org.apache.airavata.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.profile.repositories.UserProfileRepository;
import org.apache.airavata.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.AiravataSecurityManager;
import org.apache.airavata.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository userProfileRepository;

    @Lazy
    private final IamAdminService iamAdminService;

    private final Mapper mapper;
    private final AiravataSecurityManager securityManager;
    private final EntityManager entityManager;

    public UserProfileService(
            UserProfileRepository userProfileRepository,
            @Lazy IamAdminService iamAdminService,
            Mapper mapper,
            AiravataSecurityManager securityManager,
            @Qualifier("profileServiceEntityManager") EntityManager entityManager) {
        this.userProfileRepository = userProfileRepository;
        this.iamAdminService = iamAdminService;
        this.mapper = mapper;
        this.securityManager = securityManager;
        this.entityManager = entityManager;
    }

    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.USER_PROFILE);

    public String initializeUserProfile(AuthzToken authzToken) throws UserProfileServiceException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            // Load UserInfo for the access token and create an initial UserProfile from it
            UserInfo userInfo = securityManager.getUserInfoFromAuthzToken(authzToken);
            final UserProfile existingProfile = getUserProfileByIdAndGateWay(userInfo.getUsername(), gatewayId);
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
            userProfile = createUserProfile(userProfile);
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
            userProfile = updateUserProfile(userProfile, getIAMUserProfileUpdater(authzToken, userProfile));
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
            if (updateUserProfile(userProfile, iamUserProfileUpdater) != null) {
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
            UserProfile userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);
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
            UserProfile userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);

            // delete user
            boolean deleteSuccess = deleteUserProfile(userProfile.getAiravataInternalUserId());
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
            List<UserProfile> usersInGateway = getAllUserProfilesInGateway(gatewayId, offset, limit);
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
            UserProfile userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);
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

    private IamAdminService getIamAdminService() throws UserProfileServiceException {
        if (iamAdminService == null) {
            String message = "IAM Admin Service not available";
            logger.error(message);
            throw new UserProfileServiceException(message);
        }
        return iamAdminService;
    }

    // Helper methods that wrap repository calls and handle entity-to-model mapping
    public UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId) {
        Optional<UserProfileEntity> entityOpt = userProfileRepository.findByUserIdAndGatewayId(userId, gatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return mapper.map(entityOpt.get(), UserProfile.class);
    }

    @Transactional
    public UserProfile createUserProfile(UserProfile userProfile) {
        return updateUserProfile(userProfile, null);
    }

    @Transactional
    private UserProfile updateUserProfile(UserProfile userProfile, Runnable postUpdateAction) {
        UserProfileEntity entity = mapper.map(userProfile, UserProfileEntity.class);
        UserProfileEntity persistedCopy = entityManager.merge(entity);
        if (postUpdateAction != null) {
            postUpdateAction.run();
        }
        return mapper.map(persistedCopy, UserProfile.class);
    }

    private List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit) {
        List<UserProfile> result = new ArrayList<>();
        if (limit > 0) {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<UserProfileEntity> page = userProfileRepository.findByGatewayId(gatewayId, pageable);
            page.getContent().forEach(entity -> result.add(mapper.map(entity, UserProfile.class)));
        } else {
            List<UserProfileEntity> entities = userProfileRepository.findByGatewayId(gatewayId);
            entities.forEach(entity -> result.add(mapper.map(entity, UserProfile.class)));
        }
        return result;
    }

    @Transactional
    private boolean deleteUserProfile(String airavataInternalUserId) {
        if (userProfileRepository.existsById(airavataInternalUserId)) {
            userProfileRepository.deleteById(airavataInternalUserId);
            return true;
        }
        return false;
    }

    public UserProfile getUserProfileByAiravataInternalUserId(String airavataInternalUserId) {
        Optional<UserProfileEntity> entityOpt = userProfileRepository.findById(airavataInternalUserId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return mapper.map(entityOpt.get(), UserProfile.class);
    }
}
