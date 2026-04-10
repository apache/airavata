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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.GatewayGroupsProvider;
import org.apache.airavata.interfaces.GroupResourceProfileProvider;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.model.group.proto.ResourceType;
import org.apache.airavata.sharing.registry.models.proto.EntitySearchField;
import org.apache.airavata.sharing.registry.models.proto.SearchCondition;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;
import org.apache.airavata.util.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GroupResourceProfileService implements GroupResourceProfileProvider {

    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileService.class);

    private final RegistryHandler registryHandler;
    private final SharingFacade sharingHandler;
    private final GatewayGroupsProvider gatewayGroupsInitializer;

    public GroupResourceProfileService(
            RegistryHandler registryHandler,
            SharingFacade sharingHandler,
            GatewayGroupsProvider gatewayGroupsInitializer) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
    }

    public String createGroupResourceProfile(RequestContext ctx, GroupResourceProfile groupResourceProfile)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            validateGroupResourceProfileCredentials(ctx, groupResourceProfile);
            String groupResourceProfileId = registryHandler.createGroupResourceProfile(groupResourceProfile);
            if (SharingHelper.isSharingEnabled()) {
                try {
                    final String domainId = groupResourceProfile.getGatewayId();
                    sharingHandler.createEntity(
                            groupResourceProfileId,
                            domainId,
                            domainId + ":" + "GROUP_RESOURCE_PROFILE",
                            userId + "@" + domainId,
                            groupResourceProfile.getGroupResourceProfileName(),
                            null,
                            null);
                    SharingHelper.shareEntityWithAdminGatewayGroups(
                            sharingHandler,
                            registryHandler,
                            gatewayGroupsInitializer,
                            domainId,
                            groupResourceProfileId);
                } catch (Exception ex) {
                    logger.error("Rolling back group resource profile creation ID: {}", groupResourceProfileId, ex);
                    registryHandler.removeGroupResourceProfile(groupResourceProfileId);
                    throw new ServiceException("Failed to create sharing registry record", ex);
                }
            }
            logger.debug("Created group resource profile {} for gateway {}", groupResourceProfileId, gatewayId);
            return groupResourceProfileId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error creating group resource profile: " + e.getMessage(), e);
        }
    }

    public void updateGroupResourceProfile(RequestContext ctx, GroupResourceProfile groupResourceProfile)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        String profileId = groupResourceProfile.getGroupResourceProfileId();
        try {
            validateGroupResourceProfileCredentials(ctx, groupResourceProfile);
            if (SharingHelper.isSharingEnabled()
                    && !SharingHelper.userHasAccess(
                            sharingHandler, gatewayId, userId, profileId, ResourcePermissionType.WRITE)) {
                throw new ServiceAuthorizationException(
                        "User does not have permission to update group resource profile");
            }
            registryHandler.updateGroupResourceProfile(groupResourceProfile);
            logger.debug("Updated group resource profile {} for gateway {}", profileId, gatewayId);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error updating group resource profile " + profileId + ": " + e.getMessage(), e);
        }
    }

    public GroupResourceProfile getGroupResourceProfile(RequestContext ctx, String groupResourceProfileId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            GroupResourceProfile groupResourceProfile = registryHandler.getGroupResourceProfile(groupResourceProfileId);
            logger.debug("Retrieved group resource profile {}", groupResourceProfileId);
            return groupResourceProfile;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving group resource profile " + groupResourceProfileId + ": " + e.getMessage(), e);
        }
    }

    public boolean removeGroupResourceProfile(RequestContext ctx, String groupResourceProfileId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to remove group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to remove group resource profile");
                }
            }
            boolean result = registryHandler.removeGroupResourceProfile(groupResourceProfileId);
            sharingHandler.deleteEntity(gatewayId, groupResourceProfileId);
            logger.debug("Removed group resource profile {} for gateway {}", groupResourceProfileId, gatewayId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error removing group resource profile " + groupResourceProfileId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<GroupResourceProfile> getGroupResourceList(RequestContext ctx, String gatewayId)
            throws ServiceException {
        String userId = ctx.getUserId();
        try {
            List<String> accessibleGroupResProfileIds = new ArrayList<>();
            if (SharingHelper.isSharingEnabled()) {
                List<SearchCriteria> filters = new ArrayList<>();
                SearchCriteria searchCriteria = SearchCriteria.newBuilder()
                        .setSearchField(EntitySearchField.ENTITY_TYPE_ID)
                        .setSearchCondition(SearchCondition.EQUAL)
                        .setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name())
                        .build();
                filters.add(searchCriteria);
                accessibleGroupResProfileIds.addAll(
                        sharingHandler.searchEntityIds(gatewayId, userId + "@" + gatewayId, filters, 0, -1));
            }
            List<GroupResourceProfile> groupResourceProfileList =
                    registryHandler.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
            logger.debug(
                    "Retrieved {} group resource profiles for gateway {}", groupResourceProfileList.size(), gatewayId);
            return groupResourceProfileList;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving group resource profile list for gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }

    public boolean removeGroupComputePrefs(RequestContext ctx, String computeResourceId, String groupResourceProfileId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to remove group compute preferences");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to remove group compute preferences");
                }
            }
            boolean result = registryHandler.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
            logger.debug(
                    "Removed group compute prefs for resource {} in profile {}",
                    computeResourceId,
                    groupResourceProfileId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error removing group compute preferences for profile " + groupResourceProfileId + ": "
                            + e.getMessage(),
                    e);
        }
    }

    public boolean removeGroupComputeResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy =
                            registryHandler.getGroupComputeResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to remove group compute resource policy");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to remove group compute resource policy");
                }
            }
            boolean result = registryHandler.removeGroupComputeResourcePolicy(resourcePolicyId);
            logger.debug("Removed group compute resource policy {}", resourcePolicyId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error removing group compute resource policy " + resourcePolicyId + ": " + e.getMessage(), e);
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy =
                            registryHandler.getBatchQueueResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to remove batch queue resource policy");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to remove batch queue resource policy");
                }
            }
            boolean result = registryHandler.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
            logger.debug("Removed group batch queue resource policy {}", resourcePolicyId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error removing batch queue resource policy " + resourcePolicyId + ": " + e.getMessage(), e);
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            RequestContext ctx, String computeResourceId, String groupResourceProfileId) throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            GroupComputeResourcePreference result =
                    registryHandler.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
            logger.debug(
                    "Retrieved group compute resource preference for resource {} in profile {}",
                    computeResourceId,
                    groupResourceProfileId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving group compute resource preference for profile " + groupResourceProfileId + ": "
                            + e.getMessage(),
                    e);
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy =
                            registryHandler.getGroupComputeResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            ComputeResourcePolicy result = registryHandler.getGroupComputeResourcePolicy(resourcePolicyId);
            logger.debug("Retrieved group compute resource policy {}", resourcePolicyId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving group compute resource policy " + resourcePolicyId + ": " + e.getMessage(), e);
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy =
                            registryHandler.getBatchQueueResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            BatchQueueResourcePolicy result = registryHandler.getBatchQueueResourcePolicy(resourcePolicyId);
            logger.debug("Retrieved batch queue resource policy {}", resourcePolicyId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving batch queue resource policy " + resourcePolicyId + ": " + e.getMessage(), e);
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            RequestContext ctx, String groupResourceProfileId) throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            List<GroupComputeResourcePreference> result =
                    registryHandler.getGroupComputeResourcePrefList(groupResourceProfileId);
            logger.debug(
                    "Retrieved {} compute prefs for group resource profile {}", result.size(), groupResourceProfileId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving compute pref list for group resource profile " + groupResourceProfileId + ": "
                            + e.getMessage(),
                    e);
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            RequestContext ctx, String groupResourceProfileId) throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            List<BatchQueueResourcePolicy> result =
                    registryHandler.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
            logger.debug(
                    "Retrieved {} batch queue policies for group resource profile {}",
                    result.size(),
                    groupResourceProfileId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving batch queue policy list for group resource profile " + groupResourceProfileId
                            + ": " + e.getMessage(),
                    e);
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            RequestContext ctx, String groupResourceProfileId) throws ServiceException {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new ServiceAuthorizationException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (ServiceAuthorizationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceAuthorizationException(
                            "User does not have permission to access group resource profile");
                }
            }
            List<ComputeResourcePolicy> result =
                    registryHandler.getGroupComputeResourcePolicyList(groupResourceProfileId);
            logger.debug(
                    "Retrieved {} compute resource policies for group resource profile {}",
                    result.size(),
                    groupResourceProfileId);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving compute resource policy list for group resource profile " + groupResourceProfileId
                            + ": " + e.getMessage(),
                    e);
        }
    }

    public GatewayGroups getGatewayGroups(RequestContext ctx) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            GatewayGroups gatewayGroups =
                    SharingHelper.retrieveGatewayGroups(registryHandler, gatewayGroupsInitializer, gatewayId);
            logger.debug("Retrieved GatewayGroups for gateway {}", gatewayId);
            return gatewayGroups;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving GatewayGroups for gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }

    // Private helpers

    private void validateGroupResourceProfileCredentials(RequestContext ctx, GroupResourceProfile groupResourceProfile)
            throws ServiceAuthorizationException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();
        Set<String> tokenIds = new HashSet<>();
        if (groupResourceProfile.getComputePreferencesList() != null) {
            for (GroupComputeResourcePreference pref : groupResourceProfile.getComputePreferencesList()) {
                if (pref.getResourceSpecificCredentialStoreToken() != null) {
                    tokenIds.add(pref.getResourceSpecificCredentialStoreToken());
                }
            }
        }
        if (groupResourceProfile.getDefaultCredentialStoreToken() != null) {
            tokenIds.add(groupResourceProfile.getDefaultCredentialStoreToken());
        }
        for (String tokenId : tokenIds) {
            if (!SharingHelper.userHasAccess(sharingHandler, gatewayId, userId, tokenId, ResourcePermissionType.READ)) {
                throw new ServiceAuthorizationException(
                        "User does not have READ permission to credential token " + tokenId + ".");
            }
        }
    }
}
