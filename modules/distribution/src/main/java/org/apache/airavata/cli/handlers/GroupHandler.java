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
package org.apache.airavata.cli.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.DataMovementProtocol;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.services.GroupResourceProfileService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.GroupType;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.UserGroup;
import org.springframework.stereotype.Service;

@Service
public class GroupHandler {
    private final GroupResourceProfileService groupResourceProfileService;
    private final SharingRegistryService sharingRegistryService;

    public GroupHandler(
            GroupResourceProfileService groupResourceProfileService, SharingRegistryService sharingRegistryService) {
        this.groupResourceProfileService = groupResourceProfileService;
        this.sharingRegistryService = sharingRegistryService;
    }

    public String createGroupResourceProfile(String gatewayId, String name, String description, String owner) {
        try {
            // Create group in sharing registry
            UserGroup group = new UserGroup();
            group.setGroupId(UUID.randomUUID().toString());
            group.setName(name);
            group.setDescription(description);
            group.setGroupType(GroupType.USER_LEVEL_GROUP);
            group.setGroupCardinality(GroupCardinality.MULTI_USER);
            group.setDomainId(gatewayId);

            String ownerId = owner != null ? owner + "@" + gatewayId : null;
            if (ownerId == null) {
                ownerId = "admin@" + gatewayId;
            }
            group.setOwnerId(ownerId);
            long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
            group.setCreatedTime(currentTime);
            group.setUpdatedTime(currentTime);

            String groupId = sharingRegistryService.createGroup(group);
            System.out.println("✓ Group created: " + groupId + " (" + name + ")");

            // Add owner to group if specified
            if (owner != null) {
                sharingRegistryService.addUsersToGroup(gatewayId, Arrays.asList(ownerId), groupId);
                System.out.println("✓ Owner added to group");
            }

            // Create group resource profile
            GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
            groupResourceProfile.setGatewayId(gatewayId);
            groupResourceProfile.setGroupResourceProfileName(name + " Resource Profile");
            groupResourceProfile.setComputePreferences(new ArrayList<>());

            String groupResourceProfileId = groupResourceProfileService.addGroupResourceProfile(groupResourceProfile);
            System.out.println("✓ Group resource profile created: " + groupResourceProfileId);
            return groupResourceProfileId;
        } catch (SharingRegistryException e) {
            throw new RuntimeException("Failed to create group: " + e.getMessage(), e);
        }
    }

    public void updateGroupResourceProfile(String groupId, String name) {
        try {
            GroupResourceProfile group = groupResourceProfileService.getGroupResourceProfile(groupId);
            if (group == null) {
                throw new RuntimeException("Group resource profile not found: " + groupId);
            }

            if (name != null) {
                group.setGroupResourceProfileName(name);
            }

            groupResourceProfileService.updateGroupResourceProfile(group);
            System.out.println("✓ Group resource profile updated: " + groupId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update group resource profile: " + e.getMessage(), e);
        }
    }

    public void deleteGroupResourceProfile(String groupId, String gatewayId) {
        try {
            boolean deleted = groupResourceProfileService.removeGroupResourceProfile(groupId);
            if (deleted) {
                System.out.println("✓ Group resource profile deleted: " + groupId);
            } else {
                System.out.println("⚠ Group resource profile not found or could not be deleted: " + groupId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete group resource profile: " + e.getMessage(), e);
        }
    }

    public GroupResourceProfile getGroupResourceProfile(String groupId) {
        try {
            GroupResourceProfile group = groupResourceProfileService.getGroupResourceProfile(groupId);
            if (group == null) {
                throw new RuntimeException("Group resource profile not found: " + groupId);
            }

            System.out.println("Group Resource Profile Details:");
            System.out.println("  ID: " + groupId);
            System.out.println("  Name: " + group.getGroupResourceProfileName());
            System.out.println("  Gateway: " + group.getGatewayId());
            if (group.getComputePreferences() != null
                    && !group.getComputePreferences().isEmpty()) {
                System.out.println(
                        "  Compute Resources: " + group.getComputePreferences().size());
                for (GroupComputeResourcePreference pref : group.getComputePreferences()) {
                    System.out.println("    - " + pref.getComputeResourceId());
                }
            }
            return group;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get group resource profile: " + e.getMessage(), e);
        }
    }

    public void addUserToGroup(String groupId, String userId, String gatewayId) {
        try {
            String sharingUserId = userId + "@" + gatewayId;
            sharingRegistryService.addUsersToGroup(gatewayId, Arrays.asList(sharingUserId), groupId);
            System.out.println("✓ User " + userId + " added to group " + groupId);
        } catch (SharingRegistryException e) {
            throw new RuntimeException("Failed to add user to group: " + e.getMessage(), e);
        }
    }

    public void removeUserFromGroup(String groupId, String userId, String gatewayId) {
        try {
            String sharingUserId = userId + "@" + gatewayId;
            sharingRegistryService.removeUsersFromGroup(gatewayId, Arrays.asList(sharingUserId), groupId);
            System.out.println("✓ User " + userId + " removed from group " + groupId);
        } catch (SharingRegistryException e) {
            throw new RuntimeException("Failed to remove user from group: " + e.getMessage(), e);
        }
    }

    public void addComputeToGroup(String groupId, String computeId, String loginUser, String credentialToken) {
        try {
            GroupResourceProfile groupResourceProfile = groupResourceProfileService.getGroupResourceProfile(groupId);
            if (groupResourceProfile == null) {
                throw new RuntimeException("Group resource profile not found: " + groupId);
            }

            if (groupResourceProfile.getComputePreferences() == null) {
                groupResourceProfile.setComputePreferences(new ArrayList<>());
            }

            GroupComputeResourcePreference computePref = new GroupComputeResourcePreference();
            computePref.setComputeResourceId(computeId);
            computePref.setGroupResourceProfileId(groupId);
            if (loginUser != null) {
                computePref.setLoginUserName(loginUser);
            }
            if (credentialToken != null) {
                computePref.setResourceSpecificCredentialStoreToken(credentialToken);
            }
            computePref.setPreferredJobSubmissionProtocol(JobSubmissionProtocol.SSH);
            computePref.setPreferredDataMovementProtocol(DataMovementProtocol.SCP);
            computePref.setOverridebyAiravata(true);
            computePref.setResourceType(ComputeResourceType.SLURM);

            groupResourceProfile.getComputePreferences().add(computePref);
            groupResourceProfileService.updateGroupResourceProfile(groupResourceProfile);
            System.out.println("✓ Compute resource " + computeId + " added to group " + groupId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add compute resource to group: " + e.getMessage(), e);
        }
    }

    public void removeComputeFromGroup(String groupId, String computeId) {
        try {
            GroupResourceProfile group = groupResourceProfileService.getGroupResourceProfile(groupId);
            if (group == null) {
                throw new RuntimeException("Group resource profile not found: " + groupId);
            }

            if (group.getComputePreferences() != null) {
                group.getComputePreferences().removeIf(pref -> computeId.equals(pref.getComputeResourceId()));
                groupResourceProfileService.updateGroupResourceProfile(group);
                System.out.println("✓ Compute resource " + computeId + " removed from group " + groupId);
            } else {
                System.out.println("No compute resources in group " + groupId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove compute resource from group: " + e.getMessage(), e);
        }
    }

    public void addStorageToGroup(String groupId, String storageId) {
        System.out.println("Note: Storage preferences are not directly supported in group resource profiles.");
        System.out.println("Storage resource " + storageId + " is available for use with group " + groupId);
    }

    public void removeStorageFromGroup(String groupId, String storageId) {
        System.out.println("Note: Storage preferences are not directly supported in group resource profiles.");
        System.out.println("Storage resource " + storageId + " removal from group " + groupId + " is not implemented.");
    }

    public void listGroupResourceProfiles(String gatewayId) {
        try {
            if (gatewayId == null || gatewayId.isEmpty()) {
                throw new RuntimeException("Gateway ID is required for listing group resource profiles");
            }
            List<GroupResourceProfile> profiles =
                    groupResourceProfileService.getAllGroupResourceProfiles(gatewayId, null);
            if (profiles == null || profiles.isEmpty()) {
                System.out.println("No group resource profiles found.");
            } else {
                System.out.println("Group Resource Profiles:");
                for (GroupResourceProfile profile : profiles) {
                    System.out.println("  " + profile.getGroupResourceProfileId() + " -> "
                            + profile.getGroupResourceProfileName() + " (gateway: " + profile.getGatewayId() + ")");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list group resource profiles: " + e.getMessage(), e);
        }
    }
}
