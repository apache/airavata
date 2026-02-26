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
package org.apache.airavata.iam.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.iam.entity.UserEntity;
import org.apache.airavata.iam.exception.AiravataSecurityException;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.exception.UserProfileServiceException;
import org.apache.airavata.iam.mapper.UserMapper;
import org.apache.airavata.iam.mapper.UserProfileMapper;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.Status;
import org.apache.airavata.iam.model.UserInfo;
import org.apache.airavata.iam.model.UserProfile;
import org.apache.airavata.iam.repository.UserRepository;
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

/**
 * Service for managing users in the IAM layer.
 */
@Service("iamUserService")
@Transactional
public class DefaultUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final IamAdminService iamAdminService;
    private final RequestAuthenticator securityManager;
    private final EntityManager entityManager;
    private final TransactionTemplate iamUpdateTransactionTemplate;

    public DefaultUserService(
            UserRepository userRepository,
            UserMapper userMapper,
            UserProfileMapper userProfileMapper,
            IamAdminService iamAdminService,
            RequestAuthenticator securityManager,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.iamAdminService = iamAdminService;
        this.securityManager = securityManager;
        this.entityManager = entityManager;
        // Create a TransactionTemplate for IAM updates that uses REQUIRES_NEW propagation
        this.iamUpdateTransactionTemplate = new TransactionTemplate(transactionManager);
        this.iamUpdateTransactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    // -----------------------------------------------------------------------
    // Core UserService methods
    // -----------------------------------------------------------------------

    /**
     * Check if a user exists by gatewayId and userName (sub).
     *
     * @param gatewayId the gateway identifier
     * @param userName the user name (maps to sub)
     * @return true if user exists
     */
    public boolean isUserExists(String gatewayId, String userName) throws RegistryException {
        return userRepository.existsByUserIdAndGatewayId(userName, gatewayId);
    }

    /**
     * Get all usernames (sub values) in a gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of usernames (sub values)
     */
    public List<String> getAllUsernamesInGateway(String gatewayName) throws RegistryException {
        List<UserEntity> entities = userRepository.findByGatewayName(gatewayName);
        return entities.stream().map(UserEntity::getSub).toList();
    }

    /**
     * Add a new user.
     *
     * @param userProfile the user profile to add
     * @return the created user profile
     */
    public UserProfile addUser(UserProfile userProfile) throws RegistryException {
        UserEntity entity = userMapper.toEntity(userProfile);
        if (entity.getUserId() == null && entity.getSub() != null && entity.getGatewayId() != null) {
            entity.setUserId(UserEntity.createUserId(entity.getSub(), entity.getGatewayId()));
        }
        UserEntity saved = userRepository.save(entity);
        return userMapper.toModel(saved);
    }

    /**
     * Get a user by userId (sub) and gatewayId.
     *
     * @param userId the user identifier (sub)
     * @param gatewayId the gateway identifier
     * @return the user profile, or null if not found
     */
    public UserProfile get(String userId, String gatewayId) throws RegistryException {
        UserEntity entity =
                userRepository.findByUserIdAndGatewayId(userId, gatewayId).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    /**
     * Get a user by airavataInternalUserId.
     *
     * @param airavataInternalUserId the internal user ID (sub@gatewayId format)
     * @return the user profile, or null if not found
     */
    public UserProfile getByInternalUserId(String airavataInternalUserId) throws RegistryException {
        UserEntity entity = userRepository.findById(airavataInternalUserId).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    /**
     * Delete a user by userId (sub) and gatewayId.
     *
     * @param userId the user identifier (sub)
     * @param gatewayId the gateway identifier
     */
    public void delete(String userId, String gatewayId) throws RegistryException {
        String internalUserId = UserEntity.createUserId(userId, gatewayId);
        userRepository.deleteById(internalUserId);
    }

    /**
     * Delete a user by airavataInternalUserId.
     *
     * @param airavataInternalUserId the internal user ID
     */
    public void deleteByInternalUserId(String airavataInternalUserId) throws RegistryException {
        userRepository.deleteById(airavataInternalUserId);
    }

    // -----------------------------------------------------------------------
    // UserProfile operations (merged from DefaultUserProfileService)
    // -----------------------------------------------------------------------

    @Transactional
    public String initializeUserProfile(AuthzToken authzToken) throws UserProfileServiceException {
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            UserInfo userInfo = securityManager.getUserInfoFromAuthzToken(authzToken);
            String sub = userInfo.getSub();
            final UserProfile existingProfile = getUserProfileByIdAndGateWay(sub, gatewayId);
            if (existingProfile != null) {
                return existingProfile.getUserId();
            }
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(sub);
            userProfile.setGatewayId(gatewayId);
            userProfile.setAiravataInternalUserId(sub + "@" + gatewayId);
            userProfile.setCreationTime(IdGenerator.getCurrentTimestamp().getTime());
            userProfile.setValidUntil(-1);
            userProfile.setState(Status.ACTIVE);
            if (userProfile.getEmails() == null) userProfile.setEmails(new java.util.ArrayList<>());
            userProfile.getEmails().add(userInfo.getEmailAddress());
            userProfile.setFirstName(userInfo.getFirstName());
            userProfile.setLastName(userInfo.getLastName());
            userProfile.setLastAccessTime(IdGenerator.getCurrentTimestamp().getTime());
            userProfile = createUserProfile(userProfile);
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
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
                    logger.debug("Failed to create IAM updater, continuing without IAM update: {}", t.getMessage());
                }
            } else {
                logger.debug(
                        "IAM service, security manager, authzToken, or userProfile not available, skipping IAM update");
            }
            userProfile = updateUserProfileInternal(userProfile, iamUpdater);
            if (null != userProfile) {
                logger.info("Added UserProfile with userId: " + userProfile.getUserId());
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
            Runnable iamUserProfileUpdater = null;
            if (iamAdminService != null && securityManager != null && authzToken != null && userProfile != null) {
                try {
                    if (authzToken.getClaimsMap() != null
                            && authzToken.getClaimsMap().get(Constants.GATEWAY_ID) != null) {
                        iamUserProfileUpdater = getIAMUserProfileUpdater(authzToken, userProfile, iamAdminService);
                    } else {
                        logger.debug("AuthzToken missing required claims, skipping IAM update");
                    }
                } catch (Throwable t) {
                    logger.debug("Failed to create IAM updater, continuing without IAM update: {}", t.getMessage());
                }
            } else {
                logger.debug(
                        "IAM service, security manager, authzToken, or userProfile not available, skipping IAM update");
            }
            if (updateUserProfileInternal(userProfile, iamUserProfileUpdater) != null) {
                logger.info("Updated UserProfile with userId: " + userProfile.getUserId());
                return true;
            }
            return false;
        } catch (RuntimeException e) {
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

    @Transactional(readOnly = true)
    public UserProfile getUserProfileById(AuthzToken authzToken, String userId, String gatewayId)
            throws UserProfileServiceException {
        try {
            var userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);
            if (userProfile == null) {
                throw new UserProfileServiceException(
                        "User with userId: " + userId + ", in Gateway: " + gatewayId + ", does not exist.");
            }
            enrichProfileFromIam(userProfile, authzToken, gatewayId);
            return userProfile;
        } catch (UserProfileServiceException e) {
            throw e;
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
            UserProfile userProfile = getUserProfileByIdAndGateWay(userId, gatewayId);
            boolean deleteSuccess = deleteUserProfileByInternalId(userProfile.getAiravataInternalUserId());
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
        var usersInGateway = getAllUserProfilesInGatewayInternal(gatewayId, offset, limit);
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

    public UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId) {
        Optional<UserEntity> entityOpt = userRepository.findByUserIdAndGatewayId(userId, gatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return userProfileMapper.toModel(entityOpt.get());
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        return updateUserProfileInternal(userProfile, null);
    }

    public UserProfile getUserProfileByAiravataInternalUserId(String userId) {
        Optional<UserEntity> entityOpt = userRepository.findById(userId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return userProfileMapper.toModel(entityOpt.get());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Runnable getIAMUserProfileUpdater(
            AuthzToken authzToken, UserProfile userProfile, IamAdminService iamAdminService) {
        String gatewayId;
        String userId;
        String userGatewayId;
        UserProfile finalUserProfile;

        try {
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
            logger.debug("Error extracting values for IAM updater, returning no-op: {}", t.getMessage());
            return () -> {};
        }

        final String finalGatewayId = gatewayId;
        final String finalUserId = userId;
        final String finalUserGatewayId = userGatewayId;
        final IamAdminService finalIamAdminService = iamAdminService;
        return () -> {
            try {
                if (finalIamAdminService == null) {
                    logger.debug(
                            "IAM Admin Service not available, skipping IAM user profile update for userId: {}",
                            finalUserId);
                    return;
                }
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
                    logger.debug(
                            "Could not get service account token, skipping IAM user profile update: {}",
                            e.getMessage());
                    return;
                }
                try {
                    finalIamAdminService.updateUserProfile(serviceAccountAuthzToken, finalUserProfile);
                } catch (Throwable e) {
                    logger.debug(
                            "IAM update exception, skipping IAM user profile update for userId: {} - {}",
                            finalUserId,
                            e.getMessage());
                }
            } catch (Throwable e) {
                logger.debug(
                        "Exception during IAM update, skipping IAM user profile update for userId: {} - {}",
                        finalUserId,
                        e.getMessage());
            }
        };
    }

    /**
     * Enriches UserProfile with data from IAM (Keycloak). Profile data (firstName, lastName, email, etc.)
     * is fetched on demand and merged into the profile. Best-effort: if IAM is unavailable, profile
     * remains with only DB-sourced fields.
     */
    private void enrichProfileFromIam(UserProfile profile, AuthzToken authzToken, String gatewayId) {
        if (iamAdminService == null || authzToken == null) return;
        try {
            var authz = new AuthzToken();
            authz.setClaimsMap(new java.util.HashMap<>(authzToken.getClaimsMap()));
            authz.getClaimsMap().put(Constants.GATEWAY_ID, gatewayId);
            UserProfile iamProfile = iamAdminService.getUser(authz, profile.getUserId());
            if (iamProfile != null) {
                if (iamProfile.getFirstName() != null) profile.setFirstName(iamProfile.getFirstName());
                if (iamProfile.getLastName() != null) profile.setLastName(iamProfile.getLastName());
                if (iamProfile.getEmails() != null && !iamProfile.getEmails().isEmpty())
                    profile.setEmails(iamProfile.getEmails());
                if (iamProfile.getTimeZone() != null) profile.setTimeZone(iamProfile.getTimeZone());
            }
        } catch (Exception e) {
            logger.debug(
                    "Could not enrich user profile from IAM for userId={}: {}", profile.getUserId(), e.getMessage());
        }
    }

    private UserProfile updateUserProfileInternal(UserProfile userProfile, Runnable postUpdateAction) {
        String userId = userProfile.getUserId() != null && userProfile.getGatewayId() != null
                ? userProfile.getUserId() + "@" + userProfile.getGatewayId()
                : null;
        UserEntity persistedCopy;
        try {
            if (userId != null) {
                UserEntity existingEntity = entityManager.find(UserEntity.class, userId);
                if (existingEntity != null) {
                    if (userProfile.getUserId() != null) existingEntity.setSub(userProfile.getUserId());
                    if (userProfile.getGatewayId() != null) existingEntity.setGatewayId(userProfile.getGatewayId());
                    persistedCopy = existingEntity;
                    entityManager.flush();
                } else {
                    UserEntity entity = userProfileMapper.toEntity(userProfile);
                    entity.setUserId(userId);
                    persistedCopy = entityManager.merge(entity);
                    entityManager.flush();
                }
            } else {
                UserEntity entity = userProfileMapper.toEntity(userProfile);
                if (userProfile.getUserId() != null && userProfile.getGatewayId() != null) {
                    entity.setUserId(userProfile.getUserId() + "@" + userProfile.getGatewayId());
                }
                persistedCopy = entityManager.merge(entity);
                entityManager.flush();
            }
        } catch (RuntimeException e) {
            logger.error("Database operation failed during user profile update: {}", e.getMessage(), e);
            throw e;
        }
        if (postUpdateAction != null) {
            boolean synchronizationRegistered = false;
            try {
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    try {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    iamUpdateTransactionTemplate.executeWithoutResult(status -> {
                                        try {
                                            postUpdateAction.run();
                                        } catch (Throwable t) {
                                            logger.debug(
                                                    "IAM update failed after commit (non-critical): {}",
                                                    t.getMessage());
                                        }
                                    });
                                } catch (Throwable t) {
                                    logger.debug(
                                            "IAM update transaction setup failed after commit: {}", t.getMessage());
                                }
                            }
                        });
                        synchronizationRegistered = true;
                    } catch (Throwable registrationException) {
                        logger.debug(
                                "Failed to register transaction synchronization for IAM update: {}",
                                registrationException.getMessage());
                    }
                }
            } catch (Throwable t) {
                logger.debug("Error checking transaction synchronization status: {}", t.getMessage());
            }

            if (!synchronizationRegistered) {
                logger.debug("Skipping IAM update - transaction synchronization not available or registration failed");
            }
        }
        return userProfileMapper.toModel(persistedCopy);
    }

    private List<UserProfile> getAllUserProfilesInGatewayInternal(String gatewayId, int offset, int limit) {
        if (limit > 0) {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<UserEntity> page = userRepository.findByGatewayId(gatewayId, pageable);
            return userProfileMapper.toModelList(page.getContent());
        } else {
            List<UserEntity> entities = userRepository.findByGatewayId(gatewayId);
            return userProfileMapper.toModelList(entities);
        }
    }

    private boolean deleteUserProfileByInternalId(String userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

}
