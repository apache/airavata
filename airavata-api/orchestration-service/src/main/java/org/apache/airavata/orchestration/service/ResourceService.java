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
package org.apache.airavata.orchestration.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.interfaces.AgentException;
import org.apache.airavata.interfaces.ComputeResourceProvider;
import org.apache.airavata.interfaces.GroupResourceProfileProvider;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageDirectoryInfo;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.task.AdaptorSupportImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResourceService implements ComputeResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    private final RegistryServerHandler registryHandler;
    private final GroupResourceProfileProvider groupResourceProfileService;

    public ResourceService(
            RegistryServerHandler registryHandler, GroupResourceProfileProvider groupResourceProfileService) {
        this.registryHandler = registryHandler;
        this.groupResourceProfileService = groupResourceProfileService;
    }

    // -------------------------------------------------------------------------
    // Compute Resources
    // -------------------------------------------------------------------------

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws ServiceException {
        try {
            return registryHandler.registerComputeResource(computeResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while saving compute resource: " + e.getMessage(), e);
        }
    }

    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws ServiceException {
        try {
            return registryHandler.getComputeResource(computeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving compute resource: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> getAllComputeResourceNames() throws ServiceException {
        try {
            return registryHandler.getAllComputeResourceNames();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving compute resource names: " + e.getMessage(), e);
        }
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription) throws ServiceException {
        try {
            return registryHandler.updateComputeResource(computeResourceId, computeResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while updating compute resource: " + e.getMessage(), e);
        }
    }

    public boolean deleteComputeResource(String computeResourceId) throws ServiceException {
        try {
            return registryHandler.deleteComputeResource(computeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting compute resource: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Storage Resources
    // -------------------------------------------------------------------------

    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws ServiceException {
        try {
            return registryHandler.registerStorageResource(storageResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while saving storage resource: " + e.getMessage(), e);
        }
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws ServiceException {
        try {
            return registryHandler.getStorageResource(storageResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving storage resource: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAllStorageResourceNames() throws ServiceException {
        try {
            return registryHandler.getAllStorageResourceNames();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving storage resource names: " + e.getMessage(), e);
        }
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription) throws ServiceException {
        try {
            return registryHandler.updateStorageResource(storageResourceId, storageResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while updating storage resource: " + e.getMessage(), e);
        }
    }

    public boolean deleteStorageResource(String storageResourceId) throws ServiceException {
        try {
            return registryHandler.deleteStorageResource(storageResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting storage resource: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Resource Job Managers
    // -------------------------------------------------------------------------

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws ServiceException {
        try {
            return registryHandler.registerResourceJobManager(resourceJobManager);
        } catch (Exception e) {
            throw new ServiceException("Error while adding resource job manager: " + e.getMessage(), e);
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws ServiceException {
        try {
            return registryHandler.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        } catch (Exception e) {
            throw new ServiceException("Error while updating resource job manager: " + e.getMessage(), e);
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws ServiceException {
        try {
            return registryHandler.getResourceJobManager(resourceJobManagerId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving resource job manager: " + e.getMessage(), e);
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws ServiceException {
        try {
            return registryHandler.deleteResourceJobManager(resourceJobManagerId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting resource job manager: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Batch Queues
    // -------------------------------------------------------------------------

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws ServiceException {
        try {
            return registryHandler.deleteBatchQueue(computeResourceId, queueName);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting batch queue: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // ComputeResourceProvider SPI delegates
    // -------------------------------------------------------------------------

    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayId) throws ServiceException {
        try {
            return registryHandler.getGatewayResourceProfile(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving gateway resource profile: " + e.getMessage(), e);
        }
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws ServiceException {
        try {
            return registryHandler.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error while retrieving group compute resource preference: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Storage Info
    // -------------------------------------------------------------------------

    public StorageVolumeInfo getResourceStorageInfo(RequestContext ctx, String resourceId, String location)
            throws ServiceException {
        StorageInfoContext context = resolveStorageInfoContext(ctx, resourceId);
        try {
            return context.adaptor().getStorageVolumeInfo(location);
        } catch (AgentException e) {
            throw new ServiceException(
                    "Error while retrieving storage volume info for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public StorageDirectoryInfo getStorageDirectoryInfo(RequestContext ctx, String resourceId, String location)
            throws ServiceException {
        StorageInfoContext context = resolveStorageInfoContext(ctx, resourceId);
        try {
            return context.adaptor().getStorageDirectoryInfo(location);
        } catch (AgentException e) {
            throw new ServiceException(
                    "Error while retrieving storage directory info for resource " + resourceId + ": " + e.getMessage(),
                    e);
        }
    }

    /**
     * Detects whether resourceId is a compute or storage resource and resolves the appropriate context.
     */
    private StorageInfoContext resolveStorageInfoContext(RequestContext ctx, String resourceId)
            throws ServiceException {
        Optional<ComputeResourceDescription> computeResourceOp = Optional.empty();
        try {
            ComputeResourceDescription cr = registryHandler.getComputeResource(resourceId);
            if (cr != null) {
                computeResourceOp = Optional.of(cr);
            }
        } catch (RuntimeException e) {
            logger.debug("Compute resource {} not found (RuntimeException): {}", resourceId, e.getMessage());
        } catch (Exception e) {
            throw new ServiceException("Error looking up compute resource " + resourceId + ": " + e.getMessage(), e);
        }

        Optional<StorageResourceDescription> storageResourceOp = Optional.empty();
        if (computeResourceOp.isEmpty()) {
            try {
                StorageResourceDescription sr = registryHandler.getStorageResource(resourceId);
                if (sr != null) {
                    storageResourceOp = Optional.of(sr);
                }
            } catch (RuntimeException e) {
                logger.debug("Storage resource {} not found (RuntimeException): {}", resourceId, e.getMessage());
            } catch (Exception e) {
                throw new ServiceException(
                        "Error looking up storage resource " + resourceId + ": " + e.getMessage(), e);
            }
        }

        if (computeResourceOp.isEmpty() && storageResourceOp.isEmpty()) {
            throw new ServiceException(
                    "Resource with ID '" + resourceId + "' not found as either compute resource or storage resource");
        }

        try {
            if (computeResourceOp.isPresent()) {
                logger.debug("Found compute resource with ID {}. Resolving login username and credentials", resourceId);
                return resolveComputeStorageInfoContext(ctx, resourceId);
            } else {
                logger.debug("Found storage resource with ID {}. Resolving login username and credentials", resourceId);
                return resolveStorageStorageInfoContext(ctx, resourceId);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error resolving storage info context for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resolves compute resource storage info context (login username, credential token, and adaptor).
     * Handles user preference → group preference fallback for both login and credentials.
     */
    private StorageInfoContext resolveComputeStorageInfoContext(RequestContext ctx, String resourceId)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();

        String loginUserName = null;
        boolean loginFromUserPref = false;
        GroupComputeResourcePreference groupComputePref = null;
        GroupResourceProfile groupResourceProfile = null;

        UserComputeResourcePreference userComputePref = null;
        try {
            if (registryHandler.isUserResourceProfileExists(userId, gatewayId)) {
                userComputePref = registryHandler.getUserComputeResourcePreference(userId, gatewayId, resourceId);
            } else {
                logger.debug(
                        "User resource profile does not exist for user {} in gateway {}, will try group preferences",
                        userId,
                        gatewayId);
            }
        } catch (Exception e) {
            throw new ServiceException("Error retrieving user compute resource preference: " + e.getMessage(), e);
        }

        if (userComputePref != null
                && userComputePref.getLoginUserName() != null
                && !userComputePref.getLoginUserName().trim().isEmpty()) {
            loginUserName = userComputePref.getLoginUserName();
            loginFromUserPref = true;
            logger.debug("Using user preference login username: {}", loginUserName);
        } else {
            // Fallback to GroupComputeResourcePreference
            List<GroupResourceProfile> groupResourceProfiles;
            try {
                groupResourceProfiles = groupResourceProfileService.getGroupResourceList(ctx, gatewayId);
            } catch (Exception e) {
                throw new ServiceException("Error retrieving group resource profiles: " + e.getMessage(), e);
            }
            for (GroupResourceProfile groupProfile : groupResourceProfiles) {
                List<GroupComputeResourcePreference> groupComputePrefs = groupProfile.getComputePreferencesList();
                if (groupComputePrefs != null && !groupComputePrefs.isEmpty()) {
                    for (GroupComputeResourcePreference groupPref : groupComputePrefs) {
                        if (resourceId.equals(groupPref.getComputeResourceId())
                                && groupPref.getLoginUserName() != null
                                && !groupPref.getLoginUserName().trim().isEmpty()) {
                            loginUserName = groupPref.getLoginUserName();
                            groupComputePref = groupPref;
                            groupResourceProfile = groupProfile;
                            logger.debug(
                                    "Using login username from group compute resource preference for resource {}",
                                    resourceId);
                            break;
                        }
                    }
                }
                if (loginUserName != null) {
                    break;
                }
            }
            if (loginUserName == null) {
                throw new ServiceException("No login username found for compute resource " + resourceId);
            }
        }

        // Resolve credential token based on where login came from
        String credentialToken;
        if (loginFromUserPref) {
            if (userComputePref != null
                    && userComputePref.getResourceSpecificCredentialStoreToken() != null
                    && !userComputePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = userComputePref.getResourceSpecificCredentialStoreToken();
            } else {
                try {
                    UserResourceProfile userResourceProfile = registryHandler.getUserResourceProfile(userId, gatewayId);
                    if (userResourceProfile == null
                            || userResourceProfile.getCredentialStoreToken() == null
                            || userResourceProfile
                                    .getCredentialStoreToken()
                                    .trim()
                                    .isEmpty()) {
                        throw new ServiceAuthorizationException(
                                "No credential store token found for user " + userId + " in gateway " + gatewayId);
                    }
                    credentialToken = userResourceProfile.getCredentialStoreToken();
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Error retrieving user resource profile: " + e.getMessage(), e);
                }
            }
        } else {
            if (groupComputePref != null
                    && groupComputePref.getResourceSpecificCredentialStoreToken() != null
                    && !groupComputePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = groupComputePref.getResourceSpecificCredentialStoreToken();
            } else if (groupResourceProfile != null
                    && groupResourceProfile.getDefaultCredentialStoreToken() != null
                    && !groupResourceProfile
                            .getDefaultCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = groupResourceProfile.getDefaultCredentialStoreToken();
            } else {
                try {
                    UserResourceProfile userResourceProfile = registryHandler.getUserResourceProfile(userId, gatewayId);
                    if (userResourceProfile == null
                            || userResourceProfile.getCredentialStoreToken() == null
                            || userResourceProfile
                                    .getCredentialStoreToken()
                                    .trim()
                                    .isEmpty()) {
                        throw new ServiceAuthorizationException(
                                "No credential store token found for compute resource " + resourceId);
                    }
                    credentialToken = userResourceProfile.getCredentialStoreToken();
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Error retrieving user resource profile: " + e.getMessage(), e);
                }
            }
        }

        try {
            AgentAdaptor adaptor = AdaptorSupportImpl.getInstance()
                    .fetchComputeSSHAdaptor(gatewayId, resourceId, credentialToken, userId, loginUserName);
            logger.info("Resolved resource {} as compute resource to fetch storage details", resourceId);
            return new StorageInfoContext(loginUserName, credentialToken, adaptor);
        } catch (AgentException e) {
            throw new ServiceException(
                    "Error creating SSH adaptor for compute resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resolves storage resource storage info context (login username, credential token, and adaptor).
     * Handles user preference → gateway preference fallback for both login and credentials.
     */
    private StorageInfoContext resolveStorageStorageInfoContext(RequestContext ctx, String resourceId)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();

        UserStoragePreference userStoragePref = null;
        try {
            if (registryHandler.isUserResourceProfileExists(userId, gatewayId)) {
                userStoragePref = registryHandler.getUserStoragePreference(userId, gatewayId, resourceId);
            } else {
                logger.debug(
                        "User resource profile does not exist for user {} in gateway {}, will try gateway preferences",
                        userId,
                        gatewayId);
            }
        } catch (Exception e) {
            throw new ServiceException("Error retrieving user storage preference: " + e.getMessage(), e);
        }

        StoragePreference storagePref = null;
        try {
            GatewayResourceProfile gwProfile = registryHandler.getGatewayResourceProfile(gatewayId);
            if (gwProfile != null) {
                storagePref = registryHandler.getGatewayStoragePreference(gatewayId, resourceId);
            } else {
                logger.debug(
                        "Gateway resource profile does not exist for gateway {}, will check user preference",
                        gatewayId);
            }
        } catch (RuntimeException e) {
            logger.debug("Gateway resource profile does not exist for gateway {}: {}", gatewayId, e.getMessage());
        } catch (Exception e) {
            throw new ServiceException("Error retrieving gateway storage preference: " + e.getMessage(), e);
        }

        String loginUserName;
        boolean loginFromUserPref;

        if (userStoragePref != null
                && userStoragePref.getLoginUserName() != null
                && !userStoragePref.getLoginUserName().trim().isEmpty()) {
            loginUserName = userStoragePref.getLoginUserName();
            loginFromUserPref = true;
            logger.debug("Using login username from user storage preference for resource {}", resourceId);
        } else if (storagePref != null
                && storagePref.getLoginUserName() != null
                && !storagePref.getLoginUserName().trim().isEmpty()) {
            loginUserName = storagePref.getLoginUserName();
            loginFromUserPref = false;
            logger.debug("Using login username from gateway storage preference for resource {}", resourceId);
        } else {
            throw new ServiceException("No login username found for storage resource " + resourceId);
        }

        String credentialToken;
        if (loginFromUserPref) {
            if (userStoragePref != null
                    && userStoragePref.getResourceSpecificCredentialStoreToken() != null
                    && !userStoragePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = userStoragePref.getResourceSpecificCredentialStoreToken();
            } else {
                try {
                    UserResourceProfile userResourceProfile = registryHandler.getUserResourceProfile(userId, gatewayId);
                    if (userResourceProfile == null
                            || userResourceProfile.getCredentialStoreToken() == null
                            || userResourceProfile
                                    .getCredentialStoreToken()
                                    .trim()
                                    .isEmpty()) {
                        throw new ServiceAuthorizationException(
                                "No credential store token found for user " + userId + " in gateway " + gatewayId);
                    }
                    credentialToken = userResourceProfile.getCredentialStoreToken();
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Error retrieving user resource profile: " + e.getMessage(), e);
                }
            }
        } else {
            if (storagePref != null
                    && storagePref.getResourceSpecificCredentialStoreToken() != null
                    && !storagePref
                            .getResourceSpecificCredentialStoreToken()
                            .trim()
                            .isEmpty()) {
                credentialToken = storagePref.getResourceSpecificCredentialStoreToken();
            } else {
                try {
                    GatewayResourceProfile gatewayResourceProfile =
                            registryHandler.getGatewayResourceProfile(gatewayId);
                    if (gatewayResourceProfile == null
                            || gatewayResourceProfile.getCredentialStoreToken() == null
                            || gatewayResourceProfile
                                    .getCredentialStoreToken()
                                    .trim()
                                    .isEmpty()) {
                        throw new ServiceAuthorizationException(
                                "No credential store token found for gateway " + gatewayId);
                    }
                    credentialToken = gatewayResourceProfile.getCredentialStoreToken();
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Error retrieving gateway resource profile: " + e.getMessage(), e);
                }
            }
        }

        try {
            AgentAdaptor adaptor = AdaptorSupportImpl.getInstance()
                    .fetchStorageSSHAdaptor(gatewayId, resourceId, credentialToken, userId, loginUserName);
            logger.info("Resolved resource {} as storage resource to fetch storage details", resourceId);
            return new StorageInfoContext(loginUserName, credentialToken, adaptor);
        } catch (AgentException e) {
            throw new ServiceException(
                    "Error creating SSH adaptor for storage resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Holds storage info context: login username, credential token, and adaptor.
     */
    private record StorageInfoContext(String loginUserName, String credentialToken, AgentAdaptor adaptor) {}
}
