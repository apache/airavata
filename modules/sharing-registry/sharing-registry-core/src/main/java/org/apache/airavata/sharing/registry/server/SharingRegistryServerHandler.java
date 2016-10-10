/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.sharing.registry.server;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.sharing.registry.db.entities.GroupMembershipEntityPK;
import org.apache.airavata.sharing.registry.db.entities.SharingEntityPK;
import org.apache.airavata.sharing.registry.db.repositories.*;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.db.utils.JPAUtils;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.GovRegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class SharingRegistryServerHandler implements GovRegistryService.Iface{
    private final static Logger logger = LoggerFactory.getLogger(SharingRegistryServerHandler.class);

    public static String GLOBAL_PERMISSION_NAME = "OWNER";

    private DomainRepository domainRepository;
    private UserRepository userRepository;
    private UserGroupRepository userGroupRepository;
    private GroupMembershipRepository groupMembershipRepository;
    private EntityTypeRepository entityTypeRepository;
    private PermissionTypeRepository permissionTypeRepository;
    private EntityRepository entityRepository;
    private SharingRepository sharingRepository;

    public SharingRegistryServerHandler() throws ApplicationSettingsException, TException {
        JPAUtils.initializeDB();

        this.domainRepository = new DomainRepository();
        this.userRepository = new UserRepository();
        this.userGroupRepository = new UserGroupRepository();
        this.groupMembershipRepository = new GroupMembershipRepository();
        this.entityTypeRepository = new EntityTypeRepository();
        this.permissionTypeRepository = new PermissionTypeRepository();
        this.entityRepository = new EntityRepository();
        this.sharingRepository = new SharingRepository();

        if(!domainRepository.isExists(ServerSettings.getDefaultUserGateway())){
            Domain domain = new Domain();
            domain.setDomainId(ServerSettings.getDefaultUserGateway());
            domain.setName(ServerSettings.getDefaultUserGateway());
            domain.setDescription("Domain entry for " + domain.name);
            createDomain(domain);

            User user = new User();
            user.setDomainId(domain.domainId);
            user.setUserId(ServerSettings.getDefaultUser()+"@"+ServerSettings.getDefaultUserGateway());
            user.setUserName(ServerSettings.getDefaultUser());
            createUser(user);

            //Creating Entity Types for each domain
            EntityType entityType = new EntityType();
            entityType.setEntityTypeId(domain.domainId+":PROJECT");
            entityType.setDomainId(domain.domainId);
            entityType.setName("PROJECT");
            entityType.setDescription("Project entity type");
            createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.domainId+":EXPERIMENT");
            entityType.setDomainId(domain.domainId);
            entityType.setName("EXPERIMENT");
            entityType.setDescription("Experiment entity type");
            createEntityType(entityType);

            entityType = new EntityType();
            entityType.setEntityTypeId(domain.domainId+":FILE");
            entityType.setDomainId(domain.domainId);
            entityType.setName("FILE");
            entityType.setDescription("File entity type");
            createEntityType(entityType);

            //Creating Permission Types for each domain
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.domainId+":READ");
            permissionType.setDomainId(domain.domainId);
            permissionType.setName("READ");
            permissionType.setDescription("Read permission type");
            createPermissionType(permissionType);

            permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.domainId+":WRITE");
            permissionType.setDomainId(domain.domainId);
            permissionType.setName("WRITE");
            permissionType.setDescription("Write permission type");
            createPermissionType(permissionType);

        }
    }

    /**
     * * Domain Operations
     * *
     */
    @Override
    public String createDomain(Domain domain) throws SharingRegistryException, TException {
        if(domainRepository.get(domain.domainId) != null)
            throw new SharingRegistryException("There exist domain with given domain id");

        domain.setCreatedTime(System.currentTimeMillis());
        domain.setUpdatedTime(System.currentTimeMillis());
        domainRepository.create(domain);

        //create the global permission for the domain
        PermissionType permissionType = new PermissionType();
        permissionType.setPermissionTypeId(domain.domainId+":"+GLOBAL_PERMISSION_NAME);
        permissionType.setDomainId(domain.domainId);
        permissionType.setName(GLOBAL_PERMISSION_NAME);
        permissionType.setDescription("GLOBAL permission to " + domain.domainId);
        permissionType.setCreatedTime(System.currentTimeMillis());
        permissionType.setUpdatedTime(System.currentTimeMillis());
        permissionTypeRepository.create(permissionType);

        return domain.domainId;
    }

    @Override
    public boolean updateDomain(Domain domain) throws SharingRegistryException, TException {
        Domain oldDomain = domainRepository.get(domain.domainId);
        domain.setCreatedTime(oldDomain.createdTime);
        domain.setUpdatedTime(System.currentTimeMillis());
        domain = getUpdatedObject(oldDomain, domain);
        domainRepository.update(domain);
        return true;
    }

    @Override
    public boolean deleteDomain(String domainId) throws SharingRegistryException, TException {
        domainRepository.delete(domainId);
        return true;
    }

    @Override
    public Domain getDomain(String domainId) throws SharingRegistryException, TException {
        return domainRepository.get(domainId);
    }

    @Override
    public List<Domain> getDomains(int offset, int limit) throws TException {
        return domainRepository.select(new HashMap<>(), offset, limit);
    }

    /**
     * * User Operations
     * *
     */
    @Override
    public String createUser(User user) throws SharingRegistryException, TException {
        if(userRepository.get(user.userId) != null)
            throw new SharingRegistryException("There exist user with given user id");

        user.setCreatedTime(System.currentTimeMillis());
        user.setUpdatedTime(System.currentTimeMillis());
        userRepository.create(user);

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(user.userId);
        userGroup.setDomainId(user.domainId);
        userGroup.setName(user.userName);
        userGroup.setDescription("user " + user.userName + " group");
        userGroup.setOwnerId(user.userId);
        userGroup.setGroupType(GroupType.SINGLE_USER);
        createGroup(userGroup);

        return user.userId;
    }

    @Override
    public boolean updatedUser(User user) throws SharingRegistryException, TException {
        User oldUser = userRepository.get(user.userId);
        user.setCreatedTime(oldUser.createdTime);
        user.setUpdatedTime(System.currentTimeMillis());
        user = getUpdatedObject(oldUser, user);
        userRepository.update(user);

        UserGroup userGroup = userGroupRepository.get(user.userId);
        userGroup.setName(user.userName);
        userGroup.setDescription("user " + user.userName + " group");
        updateGroup(userGroup);
        return true;
    }

    @Override
    public boolean deleteUser(String userId) throws SharingRegistryException, TException {
        userRepository.delete(userId);
        userGroupRepository.delete(userId);
        return true;
    }

    @Override
    public User getUser(String userId) throws SharingRegistryException, TException {
        return userRepository.get(userId);
    }

    @Override
    public List<User> getUsers(String domain, int offset, int limit) throws SharingRegistryException, TException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.UserTable.DOMAIN_ID, domain);
        return userRepository.select(filters, offset, limit);
    }

    /**
     * * Group Operations
     * *
     */
    @Override
    public String createGroup(UserGroup group) throws SharingRegistryException, TException {
        if(userGroupRepository.get(group.groupId) != null)
            throw new SharingRegistryException("There exist group with given group id");

        group.setCreatedTime(System.currentTimeMillis());
        group.setUpdatedTime(System.currentTimeMillis());
        userGroupRepository.create(group);
        return group.groupId;
    }

    @Override
    public boolean updateGroup(UserGroup group) throws SharingRegistryException, TException {
        group.setUpdatedTime(System.currentTimeMillis());
        UserGroup oldGroup = userGroupRepository.get(group.groupId);
        group.setCreatedTime(oldGroup.createdTime);
        group = getUpdatedObject(oldGroup, group);
        userGroupRepository.update(group);
        return true;
    }

    @Override
    public boolean deleteGroup(String groupId) throws SharingRegistryException, TException {
        userGroupRepository.delete(groupId);
        return true;
    }

    @Override
    public UserGroup getGroup(String groupId) throws SharingRegistryException, TException {
        return userGroupRepository.get(groupId);
    }

    @Override
    public List<UserGroup> getGroups(String domain, int offset, int limit) throws TException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.UserTable.DOMAIN_ID, domain);
        return userGroupRepository.select(filters, offset, limit);
    }

    @Override
    public boolean addUsersToGroup(List<String> userIds, String groupId) throws SharingRegistryException, TException {
        for(int i=0; i < userIds.size(); i++){
            GroupMembership groupMembership = new GroupMembership();
            groupMembership.setParentId(groupId);
            groupMembership.setChildId(userIds.get(i));
            groupMembership.setChildType(GroupChildType.USER);
            groupMembership.setCreatedTime(System.currentTimeMillis());
            groupMembership.setUpdatedTime(System.currentTimeMillis());
            groupMembershipRepository.create(groupMembership);
        }
        return true;
    }

    @Override
    public boolean removeUsersFromGroup(List<String> userIds, String groupId) throws SharingRegistryException, TException {
        for(int i=0; i < userIds.size(); i++){
            GroupMembershipEntityPK groupMembershipEntityPK = new GroupMembershipEntityPK();
            groupMembershipEntityPK.setParentId(groupId);
            groupMembershipEntityPK.setChildId(userIds.get(i));
            groupMembershipRepository.delete(groupMembershipEntityPK);
        }
        return true;
    }

    @Override
    public Map<String, GroupChildType> getGroupMembers(String groupId, int offset, int limit) throws SharingRegistryException, TException {
        HashMap<String, GroupChildType> groupMembers = new HashMap<>();
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.GroupMembershipTable.PARENT_ID, groupId);
        List<GroupMembership> groupMembershipList = groupMembershipRepository.select(filters, 0, -1);
        groupMembershipList.stream().forEach(gm->{groupMembers.put(gm.getChildId(), gm.getChildType());});
        return groupMembers;
    }

    @Override
    public boolean addChildGroupToParentGroup(String childId, String groupId) throws SharingRegistryException, TException {
        //Todo check for cyclic dependencies
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setParentId(groupId);
        groupMembership.setChildId(childId);
        groupMembership.setChildType(GroupChildType.GROUP);
        groupMembership.setCreatedTime(System.currentTimeMillis());
        groupMembership.setUpdatedTime(System.currentTimeMillis());
        groupMembershipRepository.create(groupMembership);
        return true;
    }

    @Override
    public boolean removeChildGroupFromParentGroup(String childId, String groupId) throws SharingRegistryException, TException {
        GroupMembershipEntityPK groupMembershipEntityPK = new GroupMembershipEntityPK();
        groupMembershipEntityPK.setParentId(groupId);
        groupMembershipEntityPK.setChildId(childId);
        groupMembershipRepository.delete(groupMembershipEntityPK);
        return true;
    }

    /**
     * * EntityType Operations
     * *
     */
    @Override
    public String createEntityType(EntityType entityType) throws SharingRegistryException, TException {
        if(entityTypeRepository.get(entityType.entityTypeId) != null)
            throw new SharingRegistryException("There exist EntityType with given EntityType id");

        entityType.setCreatedTime(System.currentTimeMillis());
        entityType.setUpdatedTime(System.currentTimeMillis());
        entityTypeRepository.create(entityType);
        return entityType.entityTypeId;
    }

    @Override
    public boolean updateEntityType(EntityType entityType) throws SharingRegistryException, TException {
        entityType.setUpdatedTime(System.currentTimeMillis());
        EntityType oldEntityType = entityTypeRepository.get(entityType.entityTypeId);
        entityType.setCreatedTime(oldEntityType.createdTime);
        entityType = getUpdatedObject(oldEntityType, entityType);
        entityTypeRepository.update(entityType);
        return true;
    }

    @Override
    public boolean deleteEntityType(String entityTypeId) throws SharingRegistryException, TException {
        entityTypeRepository.delete(entityTypeId);
        return true;
    }

    @Override
    public EntityType getEntityType(String entityTypeId) throws SharingRegistryException, TException {
        return entityTypeRepository.get(entityTypeId);
    }

    @Override
    public List<EntityType> getEntityTypes(String domain, int offset, int limit) throws TException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.EntityTypeTable.DOMAIN_ID, domain);
        return entityTypeRepository.select(domain, offset, limit);
    }

    /**
     * * Permission Operations
     * *
     */
    @Override
    public String createPermissionType(PermissionType permissionType) throws SharingRegistryException, TException {
        if(permissionTypeRepository.get(permissionType.permissionTypeId) != null)
            throw new SharingRegistryException("There exist PermissionType with given PermissionType id");
        permissionType.setCreatedTime(System.currentTimeMillis());
        permissionType.setUpdatedTime(System.currentTimeMillis());
        permissionTypeRepository.create(permissionType);
        return permissionType.permissionTypeId;
    }

    @Override
    public boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException, TException {
        permissionType.setUpdatedTime(System.currentTimeMillis());
        PermissionType oldPermissionType = permissionTypeRepository.get(permissionType.permissionTypeId);
        permissionType = getUpdatedObject(oldPermissionType, permissionType);
        permissionTypeRepository.update(permissionType);
        return true;
    }

    @Override
    public boolean deletePermissionType(String entityTypeId) throws SharingRegistryException, TException {
        permissionTypeRepository.delete(entityTypeId);
        return true;
    }

    @Override
    public PermissionType getPermissionType(String permissionTypeId) throws SharingRegistryException, TException {
        return permissionTypeRepository.get(permissionTypeId);
    }

    @Override
    public List<PermissionType> getPermissionTypes(String domain, int offset, int limit) throws SharingRegistryException, TException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.PermissionTypeTable.DOMAIN_ID, domain);
        return permissionTypeRepository.select(filters, offset, limit);
    }

    /**
     * * Entity Operations
     * *
     */
    @Override
    public String createEntity(Entity entity) throws SharingRegistryException, TException {
        if(entityRepository.get(entity.entityId) != null)
            throw new SharingRegistryException("There exist Entity with given Entity id");

        if(!userRepository.isExists(entity.getOwnerId())){
            User user = new User();
            user.setUserId(entity.getOwnerId());
            user.setDomainId(entity.domainId);
            user.setUserName(user.userId.split("@")[0]);

            createUser(user);

            UserGroup userGroup = new UserGroup();
            userGroup.setGroupId(user.userId);
            userGroup.setDomainId(user.domainId);
            userGroup.setOwnerId(user.userId);
            userGroup.setName(user.userName);
            userGroup.setDescription("Single user group for " + user.userName);
            userGroup.setGroupType(GroupType.SINGLE_USER);

            createGroup(userGroup);
        }

        entity.setCreatedTime(System.currentTimeMillis());
        entity.setUpdatedTime(System.currentTimeMillis());
        entityRepository.create(entity);

        //Assigning global permission for the owner
        Sharing newSharing = new Sharing();
        newSharing.setPermissionTypeId(permissionTypeRepository.getGlobalPermissionTypeIdForDomain(entity.domainId));
        newSharing.setEntityId(entity.entityId);
        newSharing.setGroupId(entity.ownerId);
        newSharing.setSharingType(SharingType.DIRECT_CASCADING);
        newSharing.setInheritedParentId(entity.entityId);
        newSharing.setCreatedTime(System.currentTimeMillis());
        newSharing.setUpdatedTime(System.currentTimeMillis());

        sharingRepository.create(newSharing);

        //creating records for inherited permissions
        if(entity.getParentEntityId() != null && entity.getParentEntityId() != ""){
            List<Sharing> sharings = sharingRepository.getCascadingPermissionsForEntity(entity.parentEntityId);
            for(Sharing sharing : sharings){
                newSharing = new Sharing();
                newSharing.setPermissionTypeId(sharing.permissionTypeId);
                newSharing.setEntityId(entity.entityId);
                newSharing.setGroupId(sharing.groupId);
                newSharing.setInheritedParentId(sharing.inheritedParentId);
                newSharing.setSharingType(SharingType.INDIRECT_CASCADING);
                newSharing.setCreatedTime(System.currentTimeMillis());
                newSharing.setUpdatedTime(System.currentTimeMillis());

                sharingRepository.create(newSharing);
            }
        }

        return entity.entityId;
    }

    @Override
    public boolean updateEntity(Entity entity) throws SharingRegistryException, TException {
        //TODO Check for permission changes
        entity.setUpdatedTime(System.currentTimeMillis());
        Entity oldEntity = entityRepository.get(entity.getEntityId());
        entity.setCreatedTime(oldEntity.createdTime);
        entity = getUpdatedObject(oldEntity, entity);
        entityRepository.update(entity);
        return true;
    }

    @Override
    public boolean deleteEntity(String entityId) throws SharingRegistryException, TException {
        //TODO Check for permission changes
        entityRepository.delete(entityId);
        return true;
    }

    @Override
    public Entity getEntity(String entityId) throws SharingRegistryException, TException {
        return entityRepository.get(entityId);
    }

    @Override
    public List<Entity> searchEntities(String userId, String entityTypeId, Map<EntitySearchFields, String> filters,
                                       int offset, int limit) throws SharingRegistryException, TException {
        List<String> groupIds = new ArrayList<>();
        groupIds.add(userId);
        groupMembershipRepository.getAllParentMembershipsForChild(userId).stream().forEach(gm->groupIds.add(gm.parentId));
        return entityRepository.searchEntities(groupIds, entityTypeId, filters, offset, limit);
    }

    @Override
    public List<User> getListOfSharedUsers(String entityId, String permissionTypeId) throws SharingRegistryException, TException {
        return userRepository.getAccessibleUsers(entityId, permissionTypeId);
    }

    @Override
    public List<UserGroup> getListOfSharedGroups(String entityId, String permissionTypeId) throws SharingRegistryException, TException {
        return userGroupRepository.getAccessibleGroups(entityId, permissionTypeId);
    }

    /**
     * * Sharing Entity with Users and Groups
     * *
     *
     * @param entityId
     * @param userList
     * @param permissionType
     */
    @Override
    public boolean shareEntityWithUsers(String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission) throws SharingRegistryException, TException {
        return shareEntity(entityId, userList, permissionTypeId, GroupType.SINGLE_USER, cascadePermission);
    }

    @Override
    public boolean shareEntityWithGroups(String entityId, List<String> groupList, String permissionTypeId, boolean cascadePermission) throws SharingRegistryException, TException {
        return shareEntity(entityId, groupList, permissionTypeId, GroupType.MULTI_USER, cascadePermission);
    }

    private boolean shareEntity(String entityId, List<String> groupOrUserList, String permissionTypeId, GroupType groupType, boolean cascadePermission)  throws SharingRegistryException, TException {
        //Adding permission for the specified users/groups for the specified entity
        LinkedList<Entity> temp = new LinkedList<>();
        for(String userId : groupOrUserList){
            Sharing sharing = new Sharing();
            sharing.setPermissionTypeId(permissionTypeId);
            sharing.setEntityId(entityId);
            sharing.setGroupId(userId);
            sharing.setInheritedParentId(entityId);
            if(cascadePermission) {
                sharing.setSharingType(SharingType.DIRECT_CASCADING);
            }else {
                sharing.setSharingType(SharingType.DIRECT_NON_CASCADING);
            }
            sharing.setCreatedTime(System.currentTimeMillis());
            sharing.setUpdatedTime(System.currentTimeMillis());

            sharingRepository.create(sharing);
        }

        if(cascadePermission){
            //Adding permission for the specified users/groups for all child entities
            entityRepository.getChildEntities(entityId).stream().forEach(e-> temp.addLast(e));
            while(temp.size() > 0){
                Entity entity = temp.pop();
                String childEntityId = entity.entityId;
                for(String userId : groupOrUserList){
                    Sharing sharing = new Sharing();
                    sharing.setPermissionTypeId(permissionTypeId);
                    sharing.setEntityId(childEntityId);
                    sharing.setGroupId(userId);
                    sharing.setInheritedParentId(entityId);
                    sharing.setSharingType(SharingType.INDIRECT_CASCADING);
                    sharing.setInheritedParentId(entityId);
                    sharing.setCreatedTime(System.currentTimeMillis());
                    sharing.setUpdatedTime(System.currentTimeMillis());
                    sharingRepository.create(sharing);
                    entityRepository.getChildEntities(childEntityId).stream().forEach(e-> temp.addLast(e));
                }
            }
        }
        return true;
    }

    @Override
    public boolean revokeEntitySharingFromUsers(String entityId, List<String> userList, String permissionTypeId) throws SharingRegistryException, TException {
        return revokeEntitySharing(entityId, userList, permissionTypeId);
    }


    @Override
    public boolean revokeEntitySharingFromGroups(String entityId, List<String> groupList, String permissionTypeId) throws SharingRegistryException, TException {
        return revokeEntitySharing(entityId, groupList, permissionTypeId);
    }

    @Override
    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId) throws SharingRegistryException, TException {
        //check whether the user has permission directly or indirectly
        List<GroupMembership> parentMemberships = groupMembershipRepository.getAllParentMembershipsForChild(userId);
        List<String> groupIds = new ArrayList<>();
        parentMemberships.stream().forEach(pm->groupIds.add(pm.parentId));
        groupIds.add(userId);
        return sharingRepository.hasAccess(entityId, groupIds, Arrays.asList(permissionTypeId,
                permissionTypeRepository.getGlobalPermissionTypeIdForDomain(domainId)));
    }

    public boolean revokeEntitySharing(String entityId, List<String> groupOrUserList, String permissionTypeId) throws SharingRegistryException {
        //revoking permission for the entity
        for(String groupId : groupOrUserList){
            SharingEntityPK sharingEntityPK = new SharingEntityPK();
            sharingEntityPK.setEntityId(entityId);
            sharingEntityPK.setGroupId(groupId);
            sharingEntityPK.setPermissionTypeId(permissionTypeId);
            sharingEntityPK.setInheritedParentId(entityId);

            sharingRepository.delete(sharingEntityPK);
        }

        //revoking permission from inheritance
        List<Sharing> temp = new ArrayList<>();
        sharingRepository.getIndirectSharedChildren(entityId, permissionTypeId).stream().forEach(s->temp.add(s));
        for(Sharing sharing : temp){
            String childEntityId = sharing.entityId;
            for(String groupId : groupOrUserList){
                SharingEntityPK sharingEntityPK = new SharingEntityPK();
                sharingEntityPK.setEntityId(childEntityId);
                sharingEntityPK.setGroupId(groupId);
                sharingEntityPK.setPermissionTypeId(permissionTypeId);
                sharingEntityPK.setInheritedParentId(entityId);

                sharingRepository.delete(sharingEntityPK);
            }
        }
        return true;
    }



    private <T> T getUpdatedObject(T oldEntity, T newEntity) throws SharingRegistryException {
        Field[] newEntityFields = newEntity.getClass().getDeclaredFields();
        Hashtable newHT = fieldsToHT(newEntityFields, newEntity);

        Class oldEntityClass = oldEntity.getClass();
        Field[] oldEntityFields = oldEntityClass.getDeclaredFields();

        for (Field field : oldEntityFields){
            field.setAccessible(true);
            Object o = newHT.get(field.getName());
            if (o != null){
                Field f = null;
                try {
                    f = oldEntityClass.getDeclaredField(field.getName());
                    f.setAccessible(true);
                    logger.debug("setting " + f.getName());
                    f.set(oldEntity, o);
                } catch (Exception e) {
                    throw new SharingRegistryException(e.getMessage());
                }
            }
        }
        return oldEntity;
    }

    private static Hashtable<String, Object> fieldsToHT(Field[] fields, Object obj){
        Hashtable<String,Object> hashtable = new Hashtable<>();
        for (Field field: fields){
            field.setAccessible(true);
            try {
                Object retrievedObject = field.get(obj);
                if (retrievedObject != null){
                    logger.debug("scanning " + field.getName());
                    hashtable.put(field.getName(), field.get(obj));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return hashtable;
    }
}