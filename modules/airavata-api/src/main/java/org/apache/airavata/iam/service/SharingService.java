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

import java.util.List;
import java.util.Map;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.core.model.SearchCriteria;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.Domain;
import org.apache.airavata.iam.model.EntityType;
import org.apache.airavata.iam.model.GroupMember;
import org.apache.airavata.iam.model.PermissionType;
import org.apache.airavata.iam.model.Sharing;
import org.apache.airavata.iam.model.SharingEntity;
import org.apache.airavata.iam.model.User;
import org.apache.airavata.iam.model.UserGroup;

/**
 * Unified sharing service interface that consolidates all sharing-registry concerns:
 * domain management, entity types, permission types, entity CRUD, share/revoke,
 * user/group management, group membership, and low-level permission records.
 *
 * <p>This interface replaces the seven former fine-grained services:
 * {@code SharingRegistryService}, {@code SharingAccessService}, {@code SharingTeamService},
 * {@code GroupMembershipService}, {@code DomainService}, {@code EntityTypeService},
 * and {@code PermissionTypeService}.
 */
public interface SharingService {

    // =========================================================================
    // Domain Operations (formerly DomainService + SharingRegistryService)
    // =========================================================================

    String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException;

    boolean isDomainExists(String domainId) throws SharingRegistryException;

    boolean deleteDomain(String domainId) throws SharingRegistryException;

    Domain getDomain(String domainId) throws SharingRegistryException;

    List<Domain> getDomains(int offset, int limit) throws SharingRegistryException;

    // =========================================================================
    // EntityType Operations (formerly EntityTypeService + SharingRegistryService)
    // =========================================================================

    String createEntityType(EntityType entityType) throws SharingRegistryException, DuplicateEntryException;

    boolean isEntityTypeExists(String domainId, String entityTypeId) throws SharingRegistryException;

    EntityType getEntityType(String domainId, String entityTypeId) throws SharingRegistryException;

    List<EntityType> getEntityTypes(String domain, int offset, int limit) throws SharingRegistryException;

    // =========================================================================
    // PermissionType Operations (formerly PermissionTypeService + SharingRegistryService)
    // =========================================================================

    String createPermissionType(PermissionType permissionType) throws SharingRegistryException, DuplicateEntryException;

    boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException;

    boolean isPermissionExists(String domainId, String permissionId) throws SharingRegistryException;

    boolean deletePermissionType(String domainId, String permissionTypeId) throws SharingRegistryException;

    PermissionType getPermissionType(String domainId, String permissionTypeId) throws SharingRegistryException;

    List<PermissionType> getPermissionTypes(String domain, int offset, int limit) throws SharingRegistryException;

    String getOwnerPermissionTypeIdForDomain(String domainId) throws SharingRegistryException;

    // =========================================================================
    // Entity CRUD (formerly SharingAccessService)
    // =========================================================================

    String createEntity(SharingEntity entity) throws SharingRegistryException, DuplicateEntryException;

    boolean updateEntity(SharingEntity entity) throws SharingRegistryException;

    boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException;

    boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException;

    SharingEntity getEntity(String domainId, String entityId) throws SharingRegistryException;

    List<SharingEntity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException;

    // =========================================================================
    // Share / Revoke Operations (formerly SharingAccessService)
    // =========================================================================

    boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException;

    boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException;

    boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException;

    boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException;

    boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException;

    List<User> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException;

    List<User> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException;

    List<UserGroup> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException;

    List<UserGroup> getListOfDirectlySharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException;

    // =========================================================================
    // User Operations (formerly SharingTeamService)
    // =========================================================================

    String createUser(User user) throws SharingRegistryException;

    boolean isUserExists(String domainId, String userId) throws SharingRegistryException;

    boolean deleteUser(String domainId, String userId) throws SharingRegistryException;

    User getUser(String domainId, String userId) throws SharingRegistryException;

    User getUserByOidcSub(String userId, String domainId) throws SharingRegistryException;

    User updateUser(User user) throws SharingRegistryException;

    List<User> queryUsers(String queryString, Map<String, String> filters, int offset, int limit)
            throws SharingRegistryException;

    // =========================================================================
    // Group Operations (formerly SharingTeamService)
    // =========================================================================

    String createGroup(UserGroup group) throws SharingRegistryException;

    boolean updateGroup(UserGroup group) throws SharingRegistryException;

    boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException;

    boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException;

    UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException;

    List<UserGroup> getGroups(String domain, int offset, int limit) throws SharingRegistryException;

    boolean addUsersToGroup(String domainId, List<String> userIds, String groupId) throws SharingRegistryException;

    boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId) throws SharingRegistryException;

    boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException, DuplicateEntryException;

    boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, DuplicateEntryException;

    boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds) throws SharingRegistryException;

    boolean hasAdminAccess(String domainId, String groupId, String adminId) throws SharingRegistryException;

    boolean hasOwnerAccess(String domainId, String groupId, String ownerId) throws SharingRegistryException;

    List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException;

    boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException;

    List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException;

    // =========================================================================
    // Group Membership Operations (formerly GroupMembershipService)
    // =========================================================================

    GroupMember getMember(String domainId, String parentId, String childId) throws SharingRegistryException;

    GroupMember createMember(GroupMember groupMember) throws SharingRegistryException;

    GroupMember updateMember(GroupMember groupMember) throws SharingRegistryException;

    boolean deleteMember(String domainId, String parentId, String childId) throws SharingRegistryException;

    boolean isMemberExists(String domainId, String parentId, String childId) throws SharingRegistryException;

    boolean isAdmin(String domainId, String groupId, String memberId) throws SharingRegistryException;

    List<GroupMember> getGroupAdmins(String domainId, String groupId) throws SharingRegistryException;

    List<User> getAllChildUsers(String domainId, String groupId) throws SharingRegistryException;

    List<GroupMember> getAllParentMembershipsForChild(String domainId, String childId) throws SharingRegistryException;

    boolean isShared(String domainId, String entityId) throws SharingRegistryException;

    // =========================================================================
    // Registry / Permission Record Operations (formerly SharingRegistryService)
    // =========================================================================

    Sharing getPermission(
            String domainId, String entityId, String groupId, String permissionTypeId, String inheritedParentId)
            throws SharingRegistryException;

    Sharing createPermission(Sharing sharing) throws SharingRegistryException;

    Sharing updatePermission(Sharing sharing) throws SharingRegistryException;

    boolean deletePermission(
            String domainId, String entityId, String groupId, String permissionTypeId, String inheritedParentId)
            throws SharingRegistryException;

    boolean permissionExists(
            String domainId, String entityId, String groupId, String permissionTypeId, String inheritedParentId)
            throws SharingRegistryException;

    List<Sharing> selectPermissions(Map<String, String> filters, int offset, int limit) throws SharingRegistryException;

    boolean hasAccess(String domainId, String entityId, List<String> groupIds, List<String> permissionTypeIds)
            throws SharingRegistryException;

    int getSharedCount(String domainId, String entityId) throws SharingRegistryException;
}
