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
package org.apache.airavata.compute.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.compute.util.AgentAdaptor;
import org.apache.airavata.compute.util.AgentException;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.storageresource.StorageDirectoryInfo;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.storageresource.StorageVolumeInfo;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.UnicoreDataMovement;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.compute.service.GroupResourceProfileService;
import org.apache.thrift.TApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    private final RegistryServerHandler registryHandler;
    private final GroupResourceProfileService groupResourceProfileService;

    public ResourceService(
            RegistryServerHandler registryHandler, GroupResourceProfileService groupResourceProfileService) {
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

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws ServiceException {
        try {
            return registryHandler.getComputeResource(computeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving compute resource: " + e.getMessage(), e);
        }
    }

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
    // Job Submission
    // -------------------------------------------------------------------------

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws ServiceException {
        try {
            return registryHandler.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding local job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws ServiceException {
        try {
            return registryHandler.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating local job submission: " + e.getMessage(), e);
        }
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getLocalJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving local job submission: " + e.getMessage(), e);
        }
    }

    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws ServiceException {
        try {
            return registryHandler.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding SSH job submission: " + e.getMessage(), e);
        }
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws ServiceException {
        try {
            return registryHandler.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding SSH fork job submission: " + e.getMessage(), e);
        }
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getSSHJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving SSH job submission: " + e.getMessage(), e);
        }
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudJobSubmission)
            throws ServiceException {
        try {
            return registryHandler.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding cloud job submission: " + e.getMessage(), e);
        }
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getCloudJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving cloud job submission: " + e.getMessage(), e);
        }
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws ServiceException {
        try {
            return registryHandler.addUNICOREJobSubmissionDetails(
                    computeResourceId, priorityOrder, unicoreJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding UNICORE job submission: " + e.getMessage(), e);
        }
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getUnicoreJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving UNICORE job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws ServiceException {
        try {
            return registryHandler.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating SSH job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateCloudJobSubmissionDetails(
            String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws ServiceException {
        try {
            return registryHandler.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating cloud job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws ServiceException {
        try {
            return registryHandler.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating UNICORE job submission: " + e.getMessage(), e);
        }
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws ServiceException {
        try {
            return registryHandler.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting job submission interface: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Data Movement
    // -------------------------------------------------------------------------

    public String addLocalDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws ServiceException {
        try {
            return registryHandler.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding local data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws ServiceException {
        try {
            return registryHandler.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating local data movement: " + e.getMessage(), e);
        }
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getLocalDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving local data movement: " + e.getMessage(), e);
        }
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws ServiceException {
        try {
            return registryHandler.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding SCP data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws ServiceException {
        try {
            return registryHandler.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating SCP data movement: " + e.getMessage(), e);
        }
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getSCPDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving SCP data movement: " + e.getMessage(), e);
        }
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws ServiceException {
        try {
            return registryHandler.addUnicoreDataMovementDetails(
                    resourceId, dmType, priorityOrder, unicoreDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding UNICORE data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws ServiceException {
        try {
            return registryHandler.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating UNICORE data movement: " + e.getMessage(), e);
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getUnicoreDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving UNICORE data movement: " + e.getMessage(), e);
        }
    }

    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws ServiceException {
        try {
            return registryHandler.addGridFTPDataMovementDetails(
                    computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding GridFTP data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws ServiceException {
        try {
            return registryHandler.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating GridFTP data movement: " + e.getMessage(), e);
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getGridFTPDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving GridFTP data movement: " + e.getMessage(), e);
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws ServiceException {
        try {
            return registryHandler.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting data movement interface: " + e.getMessage(), e);
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
        } catch (TApplicationException e) {
            logger.debug("Compute resource {} not found (TApplicationException): {}", resourceId, e.getMessage());
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
            } catch (TApplicationException e) {
                logger.debug("Storage resource {} not found (TApplicationException): {}", resourceId, e.getMessage());
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
                List<GroupComputeResourcePreference> groupComputePrefs = groupProfile.getComputePreferences();
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
        } catch (TApplicationException e) {
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
