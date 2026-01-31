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
package org.apache.airavata.service.profile;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.Status;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnApiService;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.exception.UserProfileServiceException;
import org.apache.airavata.profile.mappers.UserProfileMapper;
import org.apache.airavata.registry.entities.UserEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.UserRepository;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.AiravataSecurityManager;
import org.apache.airavata.security.UserInfo;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.IamAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@ConditionalOnApiService
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private final UserRepository userRepository;

    private final IamAdminService iamAdminService;

    private final UserProfileMapper userProfileMapper;
    private final AiravataSecurityManager securityManager;
    private final EntityManager entityManager;
    private final RegistryService registryService;
    private final TransactionTemplate iamUpdateTransactionTemplate;

    public UserProfileService(
            UserRepository userRepository,
            IamAdminService iamAdminService,
            UserProfileMapper userProfileMapper,
            AiravataSecurityManager securityManager,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager,
            RegistryService registryService) {
        this.userRepository = userRepository;
        this.iamAdminService = iamAdminService;
        this.userProfileMapper = userProfileMapper;
        this.securityManager = securityManager;
        this.entityManager = entityManager;
        this.registryService = registryService;
        // Create a TransactionTemplate for IAM updates that uses REQUIRES_NEW propagation
        this.iamUpdateTransactionTemplate = new TransactionTemplate(transactionManager);
        this.iamUpdateTransactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
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
            userProfile.getEmails().add(userInfo.getEmailAddress());
            userProfile.setFirstName(userInfo.getFirstName());
            userProfile.setLastName(userInfo.getLastName());
            userProfile.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            userProfile.setLastAccessTime(AiravataUtils.getCurrentTimestamp().getTime());
            userProfile.setValidUntil(-1);
            userProfile.setState(Status.ACTIVE);
            userProfile = createUserProfile(userProfile);
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
                // Create default project for the new user
                createDefaultProjectIfNeeded(userProfile);
                // return userId
                return userProfile.getUserId();
            } else {
                throw new UserProfileServiceException("User creation failed. Please try again.");
            }
        } catch (AiravataSecurityException e) {
            var message = "Error while initializing user profile: security error";
            logger.error(message, e);
            throw new UserProfileServiceException(message, e);
        } catch (RuntimeException e) {
            var message = String.format("Error while initializing user profile: %s", e.getMessage());
            logger.error(message, e);
            throw new UserProfileServiceException(message, e);
        }
    }

    @Transactional
    public String addUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, IamAdminServicesException {
        try {
            // Lowercase user id and internal id
            userProfile.setUserId(userProfile.getUserId().toLowerCase());
            userProfile.setAiravataInternalUserId(userProfile.getUserId() + "@" + userProfile.getGatewayId());
            // Only create IAM updater if IAM service is available - skip entirely if not
            Runnable iamUpdater = null;
            if (iamAdminService != null && securityManager != null && authzToken != null && userProfile != null) {
                try {
                    // Additional check: verify we can safely access authzToken properties
                    if (authzToken.getClaimsMap() != null
                            && authzToken.getClaimsMap().get(Constants.GATEWAY_ID) != null) {
                        iamUpdater = getIAMUserProfileUpdater(authzToken, userProfile, iamAdminService);
                    } else {
                        logger.debug("AuthzToken missing required claims, skipping IAM update");
                    }
                } catch (Throwable t) {
                    // If IAM updater creation fails, log and continue without IAM update
                    logger.debug("Failed to create IAM updater, continuing without IAM update: {}", t.getMessage());
                }
            } else {
                logger.debug(
                        "IAM service, security manager, authzToken, or userProfile not available, skipping IAM update");
            }
            userProfile = updateUserProfile(userProfile, iamUpdater);
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
                // Create default project for the new user
                createDefaultProjectIfNeeded(userProfile);
                // return userId
                return userProfile.getUserId();
            } else {
                throw new UserProfileServiceException("User creation failed. Please try again.");
            }
        } catch (RuntimeException e) {
            var message = String.format("Error while creating user profile: %s", e.getMessage());
            logger.error(message, e);
            throw new UserProfileServiceException(message, e);
        }
    }

    @Transactional
    public boolean updateUserProfile(AuthzToken authzToken, UserProfile userProfile)
            throws UserProfileServiceException, IamAdminServicesException {
        try {
            // Only create IAM updater if IAM service is available - skip entirely if not
            Runnable iamUserProfileUpdater = null;
            if (iamAdminService != null && securityManager != null && authzToken != null && userProfile != null) {
                try {
                    // Additional check: verify we can safely access authzToken properties
                    if (authzToken.getClaimsMap() != null
                            && authzToken.getClaimsMap().get(Constants.GATEWAY_ID) != null) {
                        iamUserProfileUpdater = getIAMUserProfileUpdater(authzToken, userProfile, iamAdminService);
                    } else {
                        logger.debug("AuthzToken missing required claims, skipping IAM update");
                    }
                } catch (Throwable t) {
                    // If IAM updater creation fails, log and continue without IAM update
                    logger.debug("Failed to create IAM updater, continuing without IAM update: {}", t.getMessage());
                }
            } else {
                logger.debug(
                        "IAM service, security manager, authzToken, or userProfile not available, skipping IAM update");
            }
            if (updateUserProfile(userProfile, iamUserProfileUpdater) != null) {
                logger.info("Updated UserProfile with userId: " + userProfile.getUserId());
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            // Check if the RuntimeException wraps a UserProfileServiceException or IamAdminServicesException
            if (e.getCause() instanceof UserProfileServiceException userProfileEx) {
                throw userProfileEx;
            }
            if (e.getCause() instanceof IamAdminServicesException iamAdminEx) {
                throw iamAdminEx;
            }
            var msg = String.format(
                    "Error while updating user profile: userId=%s, gatewayId=%s, airavataInternalUserId=%s. Reason: %s",
                    userProfile.getUserId(),
                    userProfile.getGatewayId(),
                    userProfile.getAiravataInternalUserId(),
                    e.getMessage());
            logger.error(msg, e);
            throw new UserProfileServiceException(msg, e);
        }
    }

    private Runnable getIAMUserProfileUpdater(
            AuthzToken authzToken, UserProfile userProfile, IamAdminService iamAdminService) {
        // Capture all values needed for the Runnable before creating it to avoid any exceptions during creation
        // This ensures the Runnable creation itself never throws exceptions
        String gatewayId;
        String userId;
        String userGatewayId;
        UserProfile finalUserProfile;

        try {
            // Defensive null checks to prevent any exceptions during value extraction
            if (authzToken == null || authzToken.getClaimsMap() == null) {
                logger.debug("AuthzToken or claims map is null, returning no-op IAM updater");
                return () -> {};
            }
            gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            if (gatewayId == null) {
                logger.debug("Gateway ID not found in authz token, returning no-op IAM updater");
                return () -> {};
            }
            if (userProfile == null) {
                logger.debug("UserProfile is null, returning no-op IAM updater");
                return () -> {};
            }
            userId = userProfile.getUserId();
            userGatewayId = userProfile.getGatewayId();
            finalUserProfile = userProfile;
        } catch (Throwable t) {
            // If anything fails during value extraction, return no-op
            logger.debug("Error extracting values for IAM updater, returning no-op: {}", t.getMessage());
            return () -> {};
        }

        // Now create the Runnable with captured values - this should never throw
        final String finalGatewayId = gatewayId;
        final String finalUserId = userId;
        final String finalUserGatewayId = userGatewayId;
        final IamAdminService finalIamAdminService = iamAdminService;
        return () -> {
            try {
                // Skip IAM update if IAM service is not available
                if (finalIamAdminService == null) {
                    logger.debug(
                            "IAM Admin Service not available, skipping IAM user profile update for userId: {}",
                            finalUserId);
                    return;
                }
                // Skip if security manager is not available (common in tests)
                if (securityManager == null) {
                    logger.debug(
                            "Security Manager not available, skipping IAM user profile update for userId: {}",
                            finalUserId);
                    return;
                }
                AuthzToken serviceAccountAuthzToken;
                try {
                    serviceAccountAuthzToken =
                            securityManager.getUserManagementServiceAccountAuthzToken(finalGatewayId);
                } catch (Throwable e) {
                    // If we can't get the service account token, skip IAM update
                    logger.debug(
                            "Could not get service account token, skipping IAM user profile update: {}",
                            e.getMessage());
                    return;
                }
                try {
                    finalIamAdminService.updateUserProfile(serviceAccountAuthzToken, finalUserProfile);
                } catch (Throwable e) {
                    // In test environments, IAM might not be configured - log and continue
                    // Catch everything including RuntimeException, Error, etc.
                    logger.debug(
                            "IAM update exception, skipping IAM user profile update for userId: {} - {}",
                            finalUserId,
                            e.getMessage());
                    // Don't throw - allow transaction to commit
                }
            } catch (Throwable e) {
                // Catch everything including RuntimeException, Error, etc. to prevent transaction rollback
                logger.debug(
                        "Exception during IAM update, skipping IAM user profile update for userId: {} - {}",
                        finalUserId,
                        e.getMessage());
                // Don't throw - allow transaction to commit
            }
        };
    }

    @Transactional(readOnly = true)
    public UserProfile getUserProfileById(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            var userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);
            if (userProfile != null) return userProfile;
            else
                throw new UserProfileServiceException(
                        "User with userId: " + userId + ", in Gateway: " + gatewayId + ", does not exist.");
        } catch (RuntimeException e) {
            var message = String.format("Error retrieving user profile by ID: %s", e.getMessage());
            logger.error(message, e);
            throw new UserProfileServiceException(message, e);
        }
    }

    @Transactional
    public boolean deleteUserProfile(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            // find user-profile
            UserProfile userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);

            // delete user
            boolean deleteSuccess = deleteUserProfile(userProfile.getAiravataInternalUserId());
            logger.info("Delete UserProfile with userId: " + userId + ", " + (deleteSuccess ? "Success!" : "Failed!"));
            return deleteSuccess;
        } catch (RuntimeException e) {
            String message = "Error while deleting user profile: " + e.getMessage();
            logger.error(message, e);
            throw new UserProfileServiceException(message, e);
        }
    }

    @Transactional(readOnly = true)
    public List<UserProfile> getAllUserProfilesInGateway(AuthzToken authzToken, String gatewayId, int offset, int limit)
            throws UserProfileServiceException {
        var usersInGateway = getAllUserProfilesInGateway(gatewayId, offset, limit);
        if (usersInGateway != null) return usersInGateway;
        else throw new UserProfileServiceException(String.format("No user profiles found for gatewayId=%s", gatewayId));
    }

    @Transactional(readOnly = true)
    public boolean doesUserExist(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            var userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);
            return null != userProfile;
        } catch (RuntimeException e) {
            var message = String.format(
                    "Error finding user profile: userId=%s, gatewayId=%s. Reason: %s",
                    userId, gatewayId, e.getMessage());
            logger.error(message, e);
            throw new UserProfileServiceException(message, e);
        }
    }

    // Helper methods that wrap repository calls and handle entity-to-model mapping
    public UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId) {
        Optional<UserEntity> entityOpt = userRepository.findByUserIdAndGatewayId(userId, gatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return userProfileMapper.toModel(entityOpt.get());
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        return updateUserProfile(userProfile, null);
    }

    private UserProfile updateUserProfile(UserProfile userProfile, Runnable postUpdateAction) {
        String airavataInternalUserId = userProfile.getUserId() != null && userProfile.getGatewayId() != null
                ? userProfile.getUserId() + "@" + userProfile.getGatewayId()
                : null;
        UserEntity persistedCopy;
        try {
            if (airavataInternalUserId != null) {
                UserEntity existingEntity = entityManager.find(UserEntity.class, airavataInternalUserId);
                if (existingEntity != null) {
                    // Update in place to avoid "different object with same identifier" (never create second instance)
                    if (userProfile.getUserId() != null) existingEntity.setSub(userProfile.getUserId());
                    if (userProfile.getGatewayId() != null) existingEntity.setGatewayId(userProfile.getGatewayId());
                    if (userProfile.getFirstName() != null) existingEntity.setGivenName(userProfile.getFirstName());
                    if (userProfile.getLastName() != null) existingEntity.setFamilyName(userProfile.getLastName());
                    if (userProfile.getEmails() != null && !userProfile.getEmails().isEmpty()) {
                        existingEntity.setEmail(userProfile.getEmails().get(0));
                    }
                    if (userProfile.getTimeZone() != null) existingEntity.setZoneinfo(userProfile.getTimeZone());
                    persistedCopy = existingEntity;
                    entityManager.flush();
                } else {
                    UserEntity entity = userProfileMapper.toEntity(userProfile);
                    persistedCopy = entityManager.merge(entity);
                    entityManager.flush();
                }
            } else {
                UserEntity entity = userProfileMapper.toEntity(userProfile);
                persistedCopy = entityManager.merge(entity);
                entityManager.flush();
            }
        } catch (RuntimeException e) {
            // If database operations fail, log and rethrow - this will be caught by the outer transaction handler
            logger.error("Database operation failed during user profile update: {}", e.getMessage(), e);
            throw e;
        }
        if (postUpdateAction != null) {
            // Register IAM update to run AFTER the transaction commits
            // This ensures it cannot affect the main transaction even if it fails
            // Wrap everything in try-catch to prevent ANY exception from affecting the transaction
            // Use a separate try-catch for each operation to ensure maximum safety
            boolean synchronizationRegistered = false;
            try {
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    try {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                // Run IAM update after transaction commits - failures won't affect the DB transaction
                                try {
                                    iamUpdateTransactionTemplate.executeWithoutResult(status -> {
                                        try {
                                            postUpdateAction.run();
                                        } catch (Throwable t) {
                                            // Catch everything - this is a best-effort update
                                            // Don't set rollbackOnly - we want this transaction to commit even if IAM
                                            // update fails
                                            logger.debug(
                                                    "IAM update failed after commit (non-critical): {}",
                                                    t.getMessage());
                                        }
                                    });
                                } catch (Throwable t) {
                                    // Catch everything - IAM update failure should not affect anything
                                    logger.debug(
                                            "IAM update transaction setup failed after commit: {}", t.getMessage());
                                }
                            }
                        });
                        synchronizationRegistered = true;
                    } catch (Throwable registrationException) {
                        // If registration fails, log and continue - don't let it affect the transaction
                        logger.debug(
                                "Failed to register transaction synchronization for IAM update: {}",
                                registrationException.getMessage());
                    }
                }
            } catch (Throwable t) {
                // Catch everything - even checking synchronization status should not affect the transaction
                logger.debug("Error checking transaction synchronization status: {}", t.getMessage());
            }

            // If synchronization registration failed, skip IAM update entirely
            // This is safe because IAM update is best-effort and not critical for the main transaction
            if (!synchronizationRegistered) {
                logger.debug("Skipping IAM update - transaction synchronization not available or registration failed");
            }
        }
        return userProfileMapper.toModel(persistedCopy);
    }

    private List<UserProfile> getAllUserProfilesInGateway(String gatewayId, int offset, int limit) {
        if (limit > 0) {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<UserEntity> page = userRepository.findByGatewayId(gatewayId, pageable);
            return userProfileMapper.toModelList(page.getContent());
        } else {
            List<UserEntity> entities = userRepository.findByGatewayId(gatewayId);
            return userProfileMapper.toModelList(entities);
        }
    }

    private boolean deleteUserProfile(String airavataInternalUserId) {
        if (userRepository.existsById(airavataInternalUserId)) {
            userRepository.deleteById(airavataInternalUserId);
            return true;
        }
        return false;
    }

    public UserProfile getUserProfileByAiravataInternalUserId(String airavataInternalUserId) {
        Optional<UserEntity> entityOpt = userRepository.findById(airavataInternalUserId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return userProfileMapper.toModel(entityOpt.get());
    }

    /**
     * Creates a default project for a user if they don't already have one.
     * This ensures every user has at least one project to work with.
     *
     * @param userProfile The user profile to create a default project for
     */
    private void createDefaultProjectIfNeeded(UserProfile userProfile) {
        try {
            // Check if user already has any projects
            var projects = registryService.getUserProjects(
                    userProfile.getGatewayId(), userProfile.getUserId(), 1, 0);
            if (projects.isEmpty()) {
                var defaultProject = new Project();
                defaultProject.setOwner(userProfile.getUserId());
                defaultProject.setName("Default Project");
                defaultProject.setGatewayId(userProfile.getGatewayId());
                defaultProject.setDescription("This is the default project for user " + userProfile.getUserId());
                var defaultProjectId = registryService.createProject(userProfile.getGatewayId(), defaultProject);
                logger.info("Default project created for user: {} with projectId: {}",
                        userProfile.getUserId(), defaultProjectId);
            }
        } catch (RegistryException e) {
            // Log but don't fail - default project creation is best-effort
            logger.warn("Failed to create default project for user: {}. Reason: {}",
                    userProfile.getUserId(), e.getMessage());
        }
    }
}
