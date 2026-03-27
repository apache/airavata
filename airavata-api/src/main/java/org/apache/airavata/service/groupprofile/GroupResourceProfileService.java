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
package org.apache.airavata.service.groupprofile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.sharing.service.SharingHelper;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.EntitySearchField;
import org.apache.airavata.sharing.registry.models.SearchCondition;
import org.apache.airavata.sharing.registry.models.SearchCriteria;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupResourceProfileService {

    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileService.class);

    private final RegistryServerHandler registryHandler;
    private final SharingRegistryServerHandler sharingHandler;

    public GroupResourceProfileService(
            RegistryServerHandler registryHandler, SharingRegistryServerHandler sharingHandler) {
        this.registryHandler = registryHandler;
        this.sharingHandler = sharingHandler;
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
                    Entity entity = new Entity();
                    entity.setEntityId(groupResourceProfileId);
                    final String domainId = groupResourceProfile.getGatewayId();
                    entity.setDomainId(domainId);
                    entity.setEntityTypeId(domainId + ":" + "GROUP_RESOURCE_PROFILE");
                    entity.setOwnerId(userId + "@" + domainId);
                    entity.setName(groupResourceProfile.getGroupResourceProfileName());
                    sharingHandler.createEntity(entity);
                    SharingHelper.shareEntityWithAdminGatewayGroups(sharingHandler, registryHandler, entity);
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

    public List<GroupResourceProfile> getGroupResourceList(RequestContext ctx, String gatewayId)
            throws ServiceException {
        String userId = ctx.getUserId();
        try {
            List<String> accessibleGroupResProfileIds = new ArrayList<>();
            if (SharingHelper.isSharingEnabled()) {
                List<SearchCriteria> filters = new ArrayList<>();
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
                searchCriteria.setSearchCondition(SearchCondition.EQUAL);
                searchCriteria.setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
                filters.add(searchCriteria);
                sharingHandler
                        .searchEntities(gatewayId, userId + "@" + gatewayId, filters, 0, -1)
                        .forEach(p -> accessibleGroupResProfileIds.add(p.getEntityId()));
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
            GatewayGroups gatewayGroups = SharingHelper.retrieveGatewayGroups(registryHandler, gatewayId);
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
        if (groupResourceProfile.getComputePreferences() != null) {
            for (GroupComputeResourcePreference pref : groupResourceProfile.getComputePreferences()) {
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
