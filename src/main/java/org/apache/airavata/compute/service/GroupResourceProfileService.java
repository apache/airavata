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
import org.apache.airavata.api.groupprofile.GroupResourceProfileWithAccess;
import org.apache.airavata.common.RequestContext;
import org.apache.airavata.iam.GatewayGroupsInitializer;
import org.apache.airavata.iam.service.GatewayService;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.commons.proto.AccessFlags;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.model.group.proto.ResourceType;
import org.apache.airavata.sharing.registry.models.proto.EntitySearchField;
import org.apache.airavata.sharing.registry.models.proto.SearchCondition;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;
import org.apache.airavata.sharing.service.SharingService;
import org.apache.airavata.sharing.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GroupResourceProfileService {

    private static final Logger logger = LoggerFactory.getLogger(GroupResourceProfileService.class);

    private final SharingService sharingHandler;
    private final GatewayService gatewayService;
    private final GatewayGroupsInitializer gatewayGroupsInitializer;
    private final ResourceProfileRegistryService resourceProfileRegistryService;

    public GroupResourceProfileService(
            SharingService sharingHandler,
            GatewayService gatewayService,
            GatewayGroupsInitializer gatewayGroupsInitializer,
            ResourceProfileRegistryService resourceProfileRegistryService) {
        this.sharingHandler = sharingHandler;
        this.gatewayService = gatewayService;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
        this.resourceProfileRegistryService = resourceProfileRegistryService;
    }

    public String createGroupResourceProfile(RequestContext ctx, GroupResourceProfile groupResourceProfile)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            validateGroupResourceProfileCredentials(ctx, groupResourceProfile);
            String groupResourceProfileId = resourceProfileRegistryService
                    .createGroupResourceProfile(groupResourceProfile);
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
                            gatewayService,
                            gatewayGroupsInitializer,
                            domainId,
                            groupResourceProfileId);
                } catch (Exception ex) {
                    logger.error("Rolling back group resource profile creation ID: {}", groupResourceProfileId, ex);
                    try {
                        sharingHandler.deleteEntity(gatewayId, groupResourceProfileId);
                    } catch (Exception deleteEx) {
                        logger.error("Failed to delete shared entity for group resource profile ID: {}",
                                groupResourceProfileId, deleteEx);
                    }
                    resourceProfileRegistryService.removeGroupResourceProfile(groupResourceProfileId);
                    throw ex;
                }
            }
            logger.debug("Created group resource profile {} for gateway {}", groupResourceProfileId, gatewayId);
            return groupResourceProfileId;
        } catch (Exception e) {
            logger.error("Error creating group resource profile: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void updateGroupResourceProfile(RequestContext ctx, GroupResourceProfile groupResourceProfile)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        String profileId = groupResourceProfile.getGroupResourceProfileId();
        try {
            validateGroupResourceProfileCredentials(ctx, groupResourceProfile);
            if (SharingHelper.isSharingEnabled()
                    && !SharingHelper.userHasAccess(
                            sharingHandler, gatewayId, userId, profileId, ResourcePermissionType.WRITE)) {
                throw new RuntimeException(
                        "User does not have permission to update group resource profile");
            }
            resourceProfileRegistryService.updateGroupResourceProfile(groupResourceProfile);
            logger.debug("Updated group resource profile {} for gateway {}", profileId, gatewayId);
        } catch (Exception e) {
            logger.error("Error updating group resource profile {}: {}", profileId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Reconcile-then-update: removes the child compute preferences / resource
     * policies / batch-queue
     * policies that are no longer present in the incoming profile, applies the
     * update, and returns the
     * refreshed profile with the caller's access flags. Composes the existing
     * single-purpose service
     * methods — the reconcile orchestration the SDK helper did client-side now
     * lives server-side.
     */
    public GroupResourceProfileWithAccess updateGroupResourceProfileReconciled(
            RequestContext ctx, GroupResourceProfile groupResourceProfile) throws Exception {
        String profileId = groupResourceProfile.getGroupResourceProfileId();
        try {
            GroupResourceProfile original = getGroupResourceProfile(ctx, profileId);

            Set<String> newPrefIds = new HashSet<>();
            for (GroupComputeResourcePreference cp : groupResourceProfile.getComputePreferencesList()) {
                newPrefIds.add(cp.getComputeResourceId());
            }
            for (GroupComputeResourcePreference cp : original.getComputePreferencesList()) {
                if (!newPrefIds.contains(cp.getComputeResourceId())) {
                    removeGroupComputePrefs(ctx, cp.getComputeResourceId(), cp.getGroupResourceProfileId());
                }
            }

            Set<String> newPolicyIds = new HashSet<>();
            for (ComputeResourcePolicy p : groupResourceProfile.getComputeResourcePoliciesList()) {
                newPolicyIds.add(p.getResourcePolicyId());
            }
            for (ComputeResourcePolicy p : original.getComputeResourcePoliciesList()) {
                if (!p.getResourcePolicyId().isEmpty() && !newPolicyIds.contains(p.getResourcePolicyId())) {
                    removeGroupComputeResourcePolicy(ctx, p.getResourcePolicyId());
                }
            }

            Set<String> newBqIds = new HashSet<>();
            for (BatchQueueResourcePolicy p : groupResourceProfile.getBatchQueueResourcePoliciesList()) {
                newBqIds.add(p.getResourcePolicyId());
            }
            for (BatchQueueResourcePolicy p : original.getBatchQueueResourcePoliciesList()) {
                if (!p.getResourcePolicyId().isEmpty() && !newBqIds.contains(p.getResourcePolicyId())) {
                    removeGroupBatchQueueResourcePolicy(ctx, p.getResourcePolicyId());
                }
            }

            updateGroupResourceProfile(ctx, groupResourceProfile);
            return getGroupResourceProfileWithAccess(ctx, profileId);
        } catch (Exception e) {
            logger.error("Error reconciling group resource profile {}: {}", profileId, e.getMessage(), e);
            throw e;
        }
    }

    public GroupResourceProfile getGroupResourceProfile(RequestContext ctx, String groupResourceProfileId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            GroupResourceProfile groupResourceProfile = resourceProfileRegistryService
                    .getGroupResourceProfile(groupResourceProfileId);
            logger.debug("Retrieved group resource profile {}", groupResourceProfileId);
            return groupResourceProfile;
        } catch (Exception e) {
            logger.error("Error retrieving group resource profile {}: {}", groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@link #getGroupResourceProfile} plus the caller's server-computed access
     * flags (additive).
     * Reuses {@code getGroupResourceProfile} for READ enforcement so a caller can
     * never
     * self-authorize. {@code GroupResourceProfile} carries no owner field, so
     * ownership is derived
     * from the sharing OWNER grant established at creation.
     *
     * <p>
     * {@code userHasWriteAccess} is a COMPOSITE that mirrors what
     * {@link #updateGroupResourceProfile} actually enforces: the caller must have
     * sharing WRITE
     * (or OWNER) on the profile AND READ on every credential token the profile
     * references — the
     * {@code default_credential_store_token} and each compute preference's
     * {@code resource_specific_credential_store_token}.
     * {@link #updateGroupResourceProfile}
     * re-validates those token READs
     * ({@link #validateGroupResourceProfileCredentials}), so a
     * profile that looks editable but whose update would be rejected is reported as
     * not writable.
     */
    public GroupResourceProfileWithAccess getGroupResourceProfileWithAccess(
            RequestContext ctx, String groupResourceProfileId) throws Exception {
        GroupResourceProfile groupResourceProfile = getGroupResourceProfile(ctx, groupResourceProfileId);
        if (groupResourceProfile == null) {
            throw new RuntimeException("User does not have permission to access this resource");
        }
        try {
            return computeProfileAccess(ctx, groupResourceProfile);
        } catch (Exception e) {
            logger.error("Error while computing group resource profile access: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Computes the caller's access flags for an already-loaded
     * {@link GroupResourceProfile} and unions
     * them onto it, without re-fetching the profile or re-enforcing READ (the
     * caller must have already
     * passed a READ gate before reaching this point). This is the per-profile core
     * shared by
     * {@link #getGroupResourceProfileWithAccess} and
     * {@link #getGroupResourceListWithAccess}, so the list
     * variant reuses the exact same token-composite write logic per row without N
     * extra fetches.
     *
     * <p>
     * {@code userHasWriteAccess} is a COMPOSITE that mirrors what
     * {@link #updateGroupResourceProfile}
     * actually enforces: the caller must have sharing WRITE (or OWNER) on the
     * profile AND READ on every
     * credential token the profile references — the
     * {@code default_credential_store_token} and each
     * compute preference's {@code resource_specific_credential_store_token}.
     */
    private GroupResourceProfileWithAccess computeProfileAccess(
            RequestContext ctx, GroupResourceProfile groupResourceProfile) {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        String groupResourceProfileId = groupResourceProfile.getGroupResourceProfileId();
        boolean isOwner = false;
        boolean userHasWriteAccess = false;
        if (SharingHelper.isSharingEnabled()) {
            isOwner = SharingHelper.userHasAccess(
                    sharingHandler, gatewayId, userId, groupResourceProfileId, ResourcePermissionType.OWNER);
            userHasWriteAccess = isOwner
                    || SharingHelper.userHasAccess(
                            sharingHandler, gatewayId, userId, groupResourceProfileId, ResourcePermissionType.WRITE);
            if (userHasWriteAccess) {
                // Token READ uses the OWNER-inclusive helper, matching the check
                // validateGroupResourceProfileCredentials enforces on update, so the write flag
                // accurately predicts whether an update would be permitted.
                String defaultToken = groupResourceProfile.getDefaultCredentialStoreToken();
                if (!defaultToken.isEmpty()
                        && !SharingHelper.userHasAccess(
                                sharingHandler, gatewayId, userId, defaultToken, ResourcePermissionType.READ)) {
                    userHasWriteAccess = false;
                }
                if (userHasWriteAccess) {
                    for (GroupComputeResourcePreference pref : groupResourceProfile.getComputePreferencesList()) {
                        String token = pref.getResourceSpecificCredentialStoreToken();
                        if (!token.isEmpty()
                                && !SharingHelper.userHasAccess(
                                        sharingHandler, gatewayId, userId, token, ResourcePermissionType.READ)) {
                            userHasWriteAccess = false;
                            break;
                        }
                    }
                }
            }
        }
        return GroupResourceProfileWithAccess.newBuilder()
                .setGroupResourceProfile(groupResourceProfile)
                .setAccess(AccessFlags.newBuilder()
                        .setIsOwner(isOwner)
                        .setUserHasWriteAccess(userHasWriteAccess)
                        .build())
                .build();
    }

    public boolean removeGroupResourceProfile(RequestContext ctx, String groupResourceProfileId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new RuntimeException(
                                "User does not have permission to remove group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to remove group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            boolean result = resourceProfileRegistryService.removeGroupResourceProfile(groupResourceProfileId);
            sharingHandler.deleteEntity(gatewayId, groupResourceProfileId);
            logger.debug("Removed group resource profile {} for gateway {}", groupResourceProfileId, gatewayId);
            return result;
        } catch (Exception e) {
            logger.error("Error removing group resource profile {}: {}", groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public List<GroupResourceProfile> getGroupResourceList(RequestContext ctx, String gatewayId)
            throws Exception {
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
            List<GroupResourceProfile> groupResourceProfileList = resourceProfileRegistryService.getGroupResourceList(
                    gatewayId,
                    accessibleGroupResProfileIds);
            logger.debug(
                    "Retrieved {} group resource profiles for gateway {}", groupResourceProfileList.size(), gatewayId);
            return groupResourceProfileList;
        } catch (Exception e) {
            logger.error("Error retrieving group resource profile list for gateway {}: {}", gatewayId, e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * {@link #getGroupResourceList} plus the caller's server-computed access flags
     * per profile (additive).
     * Reuses {@code getGroupResourceList} for READ enforcement (outside the try, so
     * it can never be
     * self-authorized) and maps each already-loaded profile through
     * {@link #computeProfileAccess}, so
     * the per-row flags use the exact same token-composite write logic as
     * {@link #getGroupResourceProfileWithAccess} without re-fetching any profile.
     */
    public List<GroupResourceProfileWithAccess> getGroupResourceListWithAccess(RequestContext ctx, String gatewayId)
            throws Exception {
        List<GroupResourceProfile> profiles = getGroupResourceList(ctx, gatewayId);
        try {
            List<GroupResourceProfileWithAccess> result = new ArrayList<>(profiles.size());
            for (GroupResourceProfile profile : profiles) {
                result.add(computeProfileAccess(ctx, profile));
            }
            logger.debug(
                    "Computed access flags for {} group resource profiles in gateway {}", result.size(), gatewayId);
            return result;
        } catch (Exception e) {
            logger.error("Error while computing group resource profile list access for gateway {}: {}", gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupComputePrefs(RequestContext ctx, String computeResourceId, String groupResourceProfileId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":WRITE")) {
                        throw new RuntimeException(
                                "User does not have permission to remove group compute preferences");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to remove group compute preferences: {}",
                            e.getMessage(), e);
                    throw e;
                }
            }
            boolean result = resourceProfileRegistryService.removeGroupComputePrefs(computeResourceId,
                    groupResourceProfileId);
            logger.debug(
                    "Removed group compute prefs for resource {} in profile {}",
                    computeResourceId,
                    groupResourceProfileId);
            return result;
        } catch (Exception e) {
            logger.error("Error removing group compute preferences for profile {}: {}", groupResourceProfileId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupComputeResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy = resourceProfileRegistryService
                            .getGroupComputeResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new RuntimeException(
                                "User does not have permission to remove group compute resource policy");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to remove group compute resource policy: {}",
                            e.getMessage(), e);
                    throw e;
                }
            }
            boolean result = resourceProfileRegistryService.removeGroupComputeResourcePolicy(resourcePolicyId);
            logger.debug("Removed group compute resource policy {}", resourcePolicyId);
            return result;
        } catch (Exception e) {
            logger.error("Error removing group compute resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGroupBatchQueueResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy = resourceProfileRegistryService
                            .getBatchQueueResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":WRITE")) {
                        throw new RuntimeException(
                                "User does not have permission to remove batch queue resource policy");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to remove batch queue resource policy: {}",
                            e.getMessage(), e);
                    throw e;
                }
            }
            boolean result = resourceProfileRegistryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
            logger.debug("Removed group batch queue resource policy {}", resourcePolicyId);
            return result;
        } catch (Exception e) {
            logger.error("Error removing batch queue resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            RequestContext ctx, String computeResourceId, String groupResourceProfileId) throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            GroupComputeResourcePreference result = resourceProfileRegistryService.getGroupComputeResourcePreference(
                    computeResourceId,
                    groupResourceProfileId);
            logger.debug(
                    "Retrieved group compute resource preference for resource {} in profile {}",
                    computeResourceId,
                    groupResourceProfileId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving group compute resource preference for profile {}: {}",
                    groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    ComputeResourcePolicy computeResourcePolicy = resourceProfileRegistryService
                            .getGroupComputeResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            computeResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            ComputeResourcePolicy result = resourceProfileRegistryService
                    .getGroupComputeResourcePolicy(resourcePolicyId);
            logger.debug("Retrieved group compute resource policy {}", resourcePolicyId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving group compute resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(RequestContext ctx, String resourcePolicyId)
            throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    BatchQueueResourcePolicy batchQueueResourcePolicy = resourceProfileRegistryService
                            .getBatchQueueResourcePolicy(resourcePolicyId);
                    if (!sharingHandler.userHasAccess(
                            gatewayId,
                            userId + "@" + gatewayId,
                            batchQueueResourcePolicy.getGroupResourceProfileId(),
                            gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            BatchQueueResourcePolicy result = resourceProfileRegistryService
                    .getBatchQueueResourcePolicy(resourcePolicyId);
            logger.debug("Retrieved batch queue resource policy {}", resourcePolicyId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving batch queue resource policy {}: {}", resourcePolicyId, e.getMessage(), e);
            throw e;
        }
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            RequestContext ctx, String groupResourceProfileId) throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            List<GroupComputeResourcePreference> result = resourceProfileRegistryService
                    .getGroupComputeResourcePrefList(groupResourceProfileId);
            logger.debug(
                    "Retrieved {} compute prefs for group resource profile {}", result.size(), groupResourceProfileId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving compute pref list for group resource profile {}: {}", groupResourceProfileId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            RequestContext ctx, String groupResourceProfileId) throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            List<BatchQueueResourcePolicy> result = resourceProfileRegistryService
                    .getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
            logger.debug(
                    "Retrieved {} batch queue policies for group resource profile {}",
                    result.size(),
                    groupResourceProfileId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving batch queue policy list for group resource profile {}: {}",
                    groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            RequestContext ctx, String groupResourceProfileId) throws Exception {
        String userId = ctx.getUserId();
        String gatewayId = ctx.getGatewayId();
        try {
            if (SharingHelper.isSharingEnabled()) {
                try {
                    if (!sharingHandler.userHasAccess(
                            gatewayId, userId + "@" + gatewayId, groupResourceProfileId, gatewayId + ":READ")) {
                        throw new RuntimeException(
                                "User does not have permission to access group resource profile");
                    }
                } catch (Exception e) {
                    logger.error("User does not have permission to access group resource profile: {}", e.getMessage(),
                            e);
                    throw e;
                }
            }
            List<ComputeResourcePolicy> result = resourceProfileRegistryService
                    .getGroupComputeResourcePolicyList(groupResourceProfileId);
            logger.debug(
                    "Retrieved {} compute resource policies for group resource profile {}",
                    result.size(),
                    groupResourceProfileId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving compute resource policy list for group resource profile {}: {}",
                    groupResourceProfileId, e.getMessage(), e);
            throw e;
        }
    }

    public GatewayGroups getGatewayGroups(RequestContext ctx) throws Exception {
        String gatewayId = ctx.getGatewayId();
        try {
            GatewayGroups gatewayGroups = SharingHelper.retrieveGatewayGroups(gatewayService, gatewayGroupsInitializer,
                    gatewayId);
            logger.debug("Retrieved GatewayGroups for gateway {}", gatewayId);
            return gatewayGroups;
        } catch (Exception e) {
            logger.error("Error retrieving GatewayGroups for gateway {}: {}", gatewayId, e.getMessage(), e);
            throw e;
        }
    }

    // Private helpers

    private void validateGroupResourceProfileCredentials(RequestContext ctx, GroupResourceProfile groupResourceProfile)
            throws Exception {
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
                logger.error("User does not have READ permission to credential token {}", tokenId);
                throw new RuntimeException(
                        "User does not have READ permission to credential token " + tokenId + ".");
            }
        }
    }
}
