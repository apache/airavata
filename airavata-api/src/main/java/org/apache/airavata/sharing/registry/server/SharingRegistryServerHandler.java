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
package org.apache.airavata.sharing.registry.server;

import java.util.List;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.registry.service.cpi.sharing_cpiConstants;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharingRegistryServerHandler implements SharingRegistryService.Iface {
    private static final Logger logger = LoggerFactory.getLogger(SharingRegistryServerHandler.class);
    private org.apache.airavata.service.SharingRegistryService sharingRegistryService;

    public static String OWNER_PERMISSION_NAME =
            org.apache.airavata.service.SharingRegistryService.OWNER_PERMISSION_NAME;

    public SharingRegistryServerHandler() throws ApplicationSettingsException, TException {
        this(new SharingRegistryDBInitConfig());
    }

    public SharingRegistryServerHandler(SharingRegistryDBInitConfig sharingRegistryDBInitConfig)
            throws ApplicationSettingsException, TException {
        DBInitializer.initializeDB(sharingRegistryDBInitConfig);
        sharingRegistryService = new org.apache.airavata.service.SharingRegistryService();
    }

    @Override
    public String getAPIVersion() throws TException {
        return sharing_cpiConstants.SHARING_CPI_VERSION;
    }

    /**
     * * Domain Operations
     * *
     */
    @Override
    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException, TException {
        try {
            return sharingRegistryService.createDomain(domain);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean updateDomain(Domain domain) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.updateDomain(domain);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check Domain Exists</p>
     *
     * @param domainId
     */
    @Override
    public boolean isDomainExists(String domainId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.isDomainExists(domainId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean deleteDomain(String domainId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteDomain(domainId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public Domain getDomain(String domainId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getDomain(domainId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<Domain> getDomains(int offset, int limit) throws TException {
        try {
            return sharingRegistryService.getDomains(offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * User Operations
     * *
     */
    @Override
    public String createUser(User user) throws SharingRegistryException, DuplicateEntryException, TException {
        try {
            return sharingRegistryService.createUser(user);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean updatedUser(User user) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.updatedUser(user);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check User Exists</p>
     *
     * @param userId
     */
    @Override
    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.isUserExists(domainId, userId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteUser(domainId, userId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public User getUser(String domainId, String userId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getUser(domainId, userId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<User> getUsers(String domain, int offset, int limit) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getUsers(domain, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * Group Operations
     * *
     */
    @Override
    public String createGroup(UserGroup group) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.createGroup(group);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean updateGroup(UserGroup group) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.updateGroup(group);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * API method to check Group Exists
     * @param domainId
     * @param groupId
     * @return
     * @throws SharingRegistryException
     * @throws TException
     */
    @Override
    public boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.isGroupExists(domainId, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteGroup(domainId, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getGroup(domainId, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<UserGroup> getGroups(String domain, int offset, int limit) throws TException {
        try {
            return sharingRegistryService.getGroups(domain, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.addUsersToGroup(domainId, userIds, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.removeUsersFromGroup(domainId, userIds, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.transferGroupOwnership(domainId, groupId, newOwnerId);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.addGroupAdmins(domainId, groupId, adminIds);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.removeGroupAdmins(domainId, groupId, adminIds);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean hasAdminAccess(String domainId, String groupId, String adminId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.hasAdminAccess(domainId, groupId, adminId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.hasOwnerAccess(domainId, groupId, ownerId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getGroupMembersOfTypeUser(domainId, groupId, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<UserGroup> getGroupMembersOfTypeGroup(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getGroupMembersOfTypeGroup(domainId, groupId, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.addChildGroupsToParentGroup(domainId, childIds, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.removeChildGroupFromParentGroup(domainId, childId, groupId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getAllMemberGroupsForUser(domainId, userId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * EntityType Operations
     * *
     */
    @Override
    public String createEntityType(EntityType entityType)
            throws SharingRegistryException, DuplicateEntryException, TException {
        try {
            return sharingRegistryService.createEntityType(entityType);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean updateEntityType(EntityType entityType) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.updateEntityType(entityType);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check EntityType Exists</p>
     *
     * @param entityTypeId
     */
    @Override
    public boolean isEntityTypeExists(String domainId, String entityTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.isEntityTypeExists(domainId, entityTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean deleteEntityType(String domainId, String entityTypeId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteEntityType(domainId, entityTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public EntityType getEntityType(String domainId, String entityTypeId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getEntityType(domainId, entityTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<EntityType> getEntityTypes(String domain, int offset, int limit) throws TException {
        try {
            return sharingRegistryService.getEntityTypes(domain, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * Permission Operations
     * *
     */
    @Override
    public String createPermissionType(PermissionType permissionType)
            throws SharingRegistryException, DuplicateEntryException, TException {
        try {
            return sharingRegistryService.createPermissionType(permissionType);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.updatePermissionType(permissionType);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check Permission Exists</p>
     *
     * @param permissionId
     */
    @Override
    public boolean isPermissionExists(String domainId, String permissionId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.isPermissionExists(domainId, permissionId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean deletePermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.deletePermissionType(domainId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public PermissionType getPermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getPermissionType(domainId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<PermissionType> getPermissionTypes(String domain, int offset, int limit)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getPermissionTypes(domain, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * Entity Operations
     * *
     */
    @Override
    public String createEntity(Entity entity) throws SharingRegistryException, DuplicateEntryException, TException {
        try {
            return sharingRegistryService.createEntity(entity);
        } catch (SharingRegistryException | DuplicateEntryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            SharingRegistryException sharingRegistryException = new SharingRegistryException();
            sharingRegistryException.setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
            throw sharingRegistryException;
        }
    }

    @Override
    public boolean updateEntity(Entity entity) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.updateEntity(entity);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check Entity Exists</p>
     *
     * @param entityId
     */
    @Override
    public boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.isEntityExists(domainId, entityId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteEntity(domainId, entityId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public Entity getEntity(String domainId, String entityId) throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getEntity(domainId, entityId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<Entity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.searchEntities(domainId, userId, filters, offset, limit);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<User> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getListOfSharedUsers(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<User> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getListOfDirectlySharedUsers(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            SharingRegistryException sharingRegistryException = new SharingRegistryException();
            sharingRegistryException.setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
            throw sharingRegistryException;
        }
    }

    @Override
    public List<UserGroup> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getListOfSharedGroups(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public List<UserGroup> getListOfDirectlySharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.getListOfDirectlySharedGroups(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            SharingRegistryException sharingRegistryException = new SharingRegistryException();
            sharingRegistryException.setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
            throw sharingRegistryException;
        }
    }

    /**
     * Sharing Entity with Users and Groups
     * @param domainId
     * @param entityId
     * @param userList
     * @param permissionTypeId
     * @param cascadePermission
     * @return
     * @throws SharingRegistryException
     * @throws TException
     */
    @Override
    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.shareEntityWithUsers(
                    domainId, entityId, userList, permissionTypeId, cascadePermission);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.shareEntityWithGroups(
                    domainId, entityId, groupList, permissionTypeId, cascadePermission);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.revokeEntitySharingFromUsers(domainId, entityId, userList, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.revokeEntitySharingFromGroups(
                    domainId, entityId, groupList, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        try {
            return sharingRegistryService.userHasAccess(domainId, userId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            throw e;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
}
