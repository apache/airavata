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
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.model.EntitySearchField;
import org.apache.airavata.core.model.SearchCondition;
import org.apache.airavata.core.model.SearchCriteria;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.gateway.entity.GatewayEntity;
import org.apache.airavata.gateway.repository.GatewayRepository;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.iam.entity.EntityTypePK;
import org.apache.airavata.iam.entity.GroupMemberPK;
import org.apache.airavata.iam.entity.GroupMembershipEntity;
import org.apache.airavata.iam.entity.PermissionTypePK;
import org.apache.airavata.iam.entity.SharingPK;
import org.apache.airavata.iam.entity.SharingPermissionEntity;
import org.apache.airavata.iam.entity.UserEntity;
import org.apache.airavata.iam.entity.UserGroupEntity;
import org.apache.airavata.iam.entity.UserGroupPK;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.mapper.DomainMapper;
import org.apache.airavata.iam.mapper.SharingUserMapper;
import org.apache.airavata.iam.model.Domain;
import org.apache.airavata.iam.model.EntityType;
import org.apache.airavata.iam.model.GroupCardinality;
import org.apache.airavata.iam.model.GroupChildType;
import org.apache.airavata.iam.model.GroupMember;
import org.apache.airavata.iam.model.GroupMemberRole;
import org.apache.airavata.iam.model.GroupType;
import org.apache.airavata.iam.model.PermissionType;
import org.apache.airavata.iam.model.Sharing;
import org.apache.airavata.iam.model.SharingEntity;
import org.apache.airavata.iam.model.SharingType;
import org.apache.airavata.iam.model.User;
import org.apache.airavata.iam.model.UserGroup;
import org.apache.airavata.iam.repository.GroupMembershipRepository;
import org.apache.airavata.iam.repository.SharingPermissionRepository;
import org.apache.airavata.iam.repository.UserGroupRepository;
import org.apache.airavata.iam.repository.UserRepository;
import org.apache.airavata.iam.util.ServiceOperationHelper;
import org.apache.airavata.iam.util.SharingDBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unified implementation of all sharing-registry concerns.
 *
 * <p>Consolidates the seven former fine-grained services:
 * <ul>
 *   <li>{@code SharingRegistryService} — domain/entity-type/permission-type + permission records</li>
 *   <li>{@code SharingAccessService} — entity CRUD + share/revoke + access check</li>
 *   <li>{@code SharingTeamService} — user + group management</li>
 *   <li>{@code GroupMembershipService} — raw membership via EntityRelationship</li>
 *   <li>{@code DomainService} — thin gateway wrapper</li>
 *   <li>{@code EntityTypeService} — in-memory entity type store</li>
 *   <li>{@code PermissionTypeService} — in-memory permission type store</li>
 * </ul>
 */
@Service
@Primary
@Transactional
public class DefaultSharingService implements SharingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSharingService.class);

    public static final String OWNER_PERMISSION_NAME = "OWNER";

    private static final String RESOURCE_TYPE_ENTITY = "ENTITY";
    private static final String GRANTEE_TYPE_GROUP = "GROUP";

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final SharingPermissionRepository sharingPermissionRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GatewayRepository gatewayRepository;
    private final SharingUserMapper userMapper;
    private final DomainMapper domainMapper;
    private final EntityManager entityManager;

    @Lazy
    private final GatewayService gatewayService;

    // =========================================================================
    // In-memory stores (formerly EntityTypeService / PermissionTypeService)
    // =========================================================================

    private final Map<String, EntityType> entityTypeStore = new ConcurrentHashMap<>();
    private final Map<String, PermissionType> permissionTypeStore = new ConcurrentHashMap<>();

    /** In-memory entity metadata cache. Keyed by "domainId:entityId". */
    private final Map<String, SharingEntity> entityCache = new ConcurrentHashMap<>();

    public DefaultSharingService(
            SharingPermissionRepository sharingPermissionRepository,
            GroupMembershipRepository groupMembershipRepository,
            UserRepository userRepository,
            UserGroupRepository userGroupRepository,
            GatewayRepository gatewayRepository,
            SharingUserMapper userMapper,
            DomainMapper domainMapper,
            EntityManager entityManager,
            @Lazy GatewayService gatewayService) {
        this.sharingPermissionRepository = sharingPermissionRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.gatewayRepository = gatewayRepository;
        this.userMapper = userMapper;
        this.domainMapper = domainMapper;
        this.entityManager = entityManager;
        this.gatewayService = gatewayService;
    }

    // =========================================================================
    // Domain Operations (formerly DomainService + SharingRegistryService)
    // =========================================================================

    @Override
    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException {
        try {
            // Use the OWNER permission type as the marker for domain initialization.
            // We cannot use getDomainInternal().createdTime because Domain is now backed by
            // GatewayEntity, and createdTime maps to GatewayEntity.createdAt which is set
            // when the gateway is first created — not when the domain is initialized.
            String ownerPermId = domain.getDomainId() + ":" + OWNER_PERMISSION_NAME;
            String ownerPermKey = permissionTypeKey(domain.getDomainId(), ownerPermId);
            if (permissionTypeStore.containsKey(ownerPermKey)) {
                throw new DuplicateEntryException("Domain already initialized for gateway: " + domain.getDomainId());
            }

            try {
                if (!gatewayService.isGatewayExist(domain.getDomainId())) {
                    throw new SharingRegistryException(String.format(
                            "Cannot create domain: No gateway exists with gatewayId '%s'. "
                                    + "A domain's domainId must correspond to an existing gateway's gatewayId.",
                            domain.getDomainId()));
                }
            } catch (RegistryException e) {
                String message =
                        String.format("Error while validating gateway for domain: domainId=%s", domain.getDomainId());
                logger.error(message, e);
                throw new SharingRegistryException(message);
            }

            domain.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            domain.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            updateDomainInternal(domain);

            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":" + OWNER_PERMISSION_NAME);
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName(OWNER_PERMISSION_NAME);
            permissionType.setDescription("GLOBAL permission to " + domain.getDomainId());
            permissionType.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            permissionType.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            storePermissionType(permissionType);

            return domain.getDomainId();
        } catch (DuplicateEntryException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while creating domain: domainId=%s", domain.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean isDomainExists(String domainId) throws SharingRegistryException {
        return ServiceOperationHelper.executeBool(
                () -> gatewayRepository.findByGatewayNameOrId(domainId).isPresent(),
                SharingRegistryException.class,
                "Error checking if domain exists: domainId=%s",
                domainId);
    }

    @Override
    public boolean deleteDomain(String domainId) throws SharingRegistryException {
        throw new SharingRegistryException("Domain deletion is not supported. Domains are part of gateways. "
                + "Delete the gateway instead through the appropriate gateway management service.");
    }

    @Override
    public Domain getDomain(String domainId) throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> getDomainInternal(domainId),
                SharingRegistryException.class,
                "Error getting domain: domainId=%s",
                domainId);
    }

    @Override
    public List<Domain> getDomains(int offset, int limit) throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> domainMapper.toModelList(gatewayRepository.findAll()),
                SharingRegistryException.class,
                "Error getting domains: offset=%d, limit=%d",
                offset,
                limit);
    }

    // =========================================================================
    // EntityType Operations (formerly EntityTypeService + SharingRegistryService)
    // =========================================================================

    @Override
    public String createEntityType(EntityType entityType) throws SharingRegistryException, DuplicateEntryException {
        EntityTypePK pk = new EntityTypePK();
        pk.setDomainId(entityType.getDomainId());
        pk.setEntityTypeId(entityType.getEntityTypeId());
        if (getEntityTypeInternal(pk) != null) {
            throw new DuplicateEntryException("There exist EntityType with given EntityType id");
        }
        entityType.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
        entityType.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
        storeEntityType(entityType);
        return entityType.getEntityTypeId();
    }

    @Override
    public boolean isEntityTypeExists(String domainId, String entityTypeId) throws SharingRegistryException {
        EntityTypePK pk = new EntityTypePK();
        pk.setDomainId(domainId);
        pk.setEntityTypeId(entityTypeId);
        return getEntityTypeInternal(pk) != null;
    }

    @Override
    public EntityType getEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        EntityTypePK pk = new EntityTypePK();
        pk.setDomainId(domainId);
        pk.setEntityTypeId(entityTypeId);
        return getEntityTypeInternal(pk);
    }

    @Override
    public List<EntityType> getEntityTypes(String domain, int offset, int limit) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(SharingDBConstants.EntityTypeTable.DOMAIN_ID, domain);
        return selectEntityTypes(filters, offset, limit);
    }

    // =========================================================================
    // PermissionType Operations (formerly PermissionTypeService + SharingRegistryService)
    // =========================================================================

    @Override
    public String createPermissionType(PermissionType permissionType)
            throws SharingRegistryException, DuplicateEntryException {
        PermissionTypePK pk = new PermissionTypePK();
        pk.setDomainId(permissionType.getDomainId());
        pk.setPermissionTypeId(permissionType.getPermissionTypeId());
        if (getPermissionTypeInternal(pk) != null) {
            throw new DuplicateEntryException("There exist PermissionType with given PermissionType id");
        }
        permissionType.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
        permissionType.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
        storePermissionType(permissionType);
        return permissionType.getPermissionTypeId();
    }

    @Override
    public boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException {
        permissionType.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
        PermissionTypePK pk = new PermissionTypePK();
        pk.setDomainId(permissionType.getDomainId());
        pk.setPermissionTypeId(permissionType.getPermissionTypeId());
        PermissionType old = getPermissionTypeInternal(pk);
        if (old != null) {
            permissionType = getUpdatedObject(old, permissionType);
        }
        storePermissionType(permissionType);
        return true;
    }

    @Override
    public boolean isPermissionExists(String domainId, String permissionId) throws SharingRegistryException {
        PermissionTypePK pk = new PermissionTypePK();
        pk.setDomainId(domainId);
        pk.setPermissionTypeId(permissionId);
        return getPermissionTypeInternal(pk) != null;
    }

    @Override
    public boolean deletePermissionType(String domainId, String permissionTypeId) throws SharingRegistryException {
        permissionTypeStore.remove(permissionTypeKey(domainId, permissionTypeId));
        return true;
    }

    @Override
    public PermissionType getPermissionType(String domainId, String permissionTypeId) throws SharingRegistryException {
        PermissionTypePK pk = new PermissionTypePK();
        pk.setDomainId(domainId);
        pk.setPermissionTypeId(permissionTypeId);
        return getPermissionTypeInternal(pk);
    }

    @Override
    public List<PermissionType> getPermissionTypes(String domain, int offset, int limit)
            throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(SharingDBConstants.PermissionTypeTable.DOMAIN_ID, domain);
        return selectPermissionTypes(filters, offset, limit);
    }

    @Override
    public String getOwnerPermissionTypeIdForDomain(String domainId) throws SharingRegistryException {
        return OWNER_PERMISSION_NAME;
    }

    // =========================================================================
    // Entity CRUD (formerly SharingAccessService)
    // =========================================================================

    @Override
    public String createEntity(SharingEntity entity) throws SharingRegistryException, DuplicateEntryException {
        try {
            String key = entityKey(entity.getDomainId(), entity.getEntityId());
            if (entityCache.containsKey(key)) {
                throw new DuplicateEntryException("There exist Entity with given Entity id");
            }

            // Normalize ownerId: sharing layer uses "sub@domainId" format
            String ownerSub = entity.getOwnerId();
            String domainId = entity.getDomainId();
            if (ownerSub != null && domainId != null && ownerSub.endsWith("@" + domainId)) {
                ownerSub = ownerSub.substring(0, ownerSub.length() - domainId.length() - 1);
            }
            boolean userExists = isUserExists(domainId, ownerSub);
            if (!userExists) {
                User user = new User();
                user.setUserId(ownerSub);
                user.setDomainId(domainId);
                user.setUserName(ownerSub != null ? ownerSub.split("@")[0] : null);
                createUser(user);
            }
            entity.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            entity.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());

            Long originalTime = entity.getOriginalEntityCreationTime();
            if (originalTime == null || originalTime == 0) {
                entity.setOriginalEntityCreationTime(entity.getCreatedTime());
            }
            entityCache.put(key, entity);

            // Assign owner permission
            Sharing newSharing = new Sharing();
            newSharing.setPermissionTypeId(getOwnerPermissionTypeIdForDomain(entity.getDomainId()));
            newSharing.setEntityId(entity.getEntityId());
            newSharing.setGroupId(entity.getOwnerId());
            newSharing.setSharingType(SharingType.DIRECT_CASCADING);
            newSharing.setInheritedParentId(entity.getEntityId());
            newSharing.setDomainId(entity.getDomainId());
            newSharing.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            newSharing.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            createPermission(newSharing);

            // Create records for inherited permissions
            if (entity.getParentEntityId() != null
                    && !entity.getParentEntityId().isEmpty()) {
                addCascadingPermissionsForEntity(entity);
            }

            return entity.getEntityId();
        } catch (DuplicateEntryException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating entity: entityId=%s, domainId=%s, ownerId=%s",
                    entity.getEntityId(), entity.getDomainId(), entity.getOwnerId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean updateEntity(SharingEntity entity) throws SharingRegistryException {
        try {
            entity.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            String key = entityKey(entity.getDomainId(), entity.getEntityId());
            SharingEntity old = entityCache.get(key);
            if (old != null) {
                entity.setCreatedTime(old.getCreatedTime());
            }
            if (old != null && !Objects.equals(old.getParentEntityId(), entity.getParentEntityId())) {
                if (old.getParentEntityId() != null && !old.getParentEntityId().isEmpty()) {
                    removeAllIndirectCascadingPermissionsForEntity(entity.getDomainId(), entity.getEntityId());
                }
                if (entity.getParentEntityId() != null
                        && !entity.getParentEntityId().isEmpty()) {
                    addCascadingPermissionsForEntity(entity);
                }
            }
            if (old != null) {
                entity = mergeEntityFields(old, entity);
            }
            long sharedCount = getSharedCount(entity.getDomainId(), entity.getEntityId());
            entity.setSharedCount(sharedCount);
            entityCache.put(key, entity);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating entity: entityId=%s, domainId=%s",
                    entity.getEntityId(), entity.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException {
        return entityCache.containsKey(entityKey(domainId, entityId));
    }

    @Override
    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException {
        entityCache.remove(entityKey(domainId, entityId));
        return true;
    }

    @Override
    public SharingEntity getEntity(String domainId, String entityId) throws SharingRegistryException {
        try {
            SharingEntity entity = entityCache.get(entityKey(domainId, entityId));
            if (entity != null) {
                long sharedCount = getSharedCount(domainId, entityId);
                entity.setSharedCount(sharedCount);
            }
            return entity;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while getting entity: domainId=%s, entityId=%s", domainId, entityId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public List<SharingEntity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        try {
            List<String> groupIds = new ArrayList<>();
            groupIds.add(userId);
            getAllParentMembershipsForChild(domainId, userId).forEach(gm -> groupIds.add(gm.getParentId()));

            String permissionTypeFilter = null;
            if (filters != null) {
                for (var c : filters) {
                    if (c.getSearchField() == EntitySearchField.PERMISSION_TYPE_ID && c.getValue() != null) {
                        permissionTypeFilter = c.getValue();
                        break;
                    }
                }
            }

            final String ptFilter = permissionTypeFilter;
            List<SharingPermissionEntity> rels = sharingPermissionRepository.findAll().stream()
                    .filter(e -> domainId.equals(e.getDomainId())
                            && RESOURCE_TYPE_ENTITY.equals(e.getResourceType())
                            && GRANTEE_TYPE_GROUP.equals(e.getGranteeType()))
                    .toList();
            List<String> accessibleEntityIds = rels.stream()
                    .filter(r -> groupIds.contains(r.getGranteeId()))
                    .filter(r -> ptFilter == null || ptFilter.equals(r.getPermission()))
                    .map(SharingPermissionEntity::getResourceId)
                    .distinct()
                    .toList();

            List<SharingEntity> result = new ArrayList<>();
            for (String entityId : accessibleEntityIds) {
                SharingEntity entity = entityCache.get(entityKey(domainId, entityId));
                if (entity == null) {
                    entity = new SharingEntity();
                    entity.setEntityId(entityId);
                    entity.setDomainId(domainId);
                    entity.setName(entityId);
                }
                if (matchesFilters(entity, filters)) {
                    result.add(entity);
                }
            }

            if (offset > 0 && offset < result.size()) {
                result = new ArrayList<>(result.subList(offset, result.size()));
            }
            if (limit > 0 && result.size() > limit) {
                result = new ArrayList<>(result.subList(0, limit));
            }
            return result;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while searching entities: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        } catch (Exception ex) {
            logger.error("Error searching entities", ex);
            throw new SharingRegistryException("Error searching entities: " + ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // Share / Revoke Operations (formerly SharingAccessService)
    // =========================================================================

    @Override
    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException {
        try {
            return shareEntity(domainId, entityId, userList, permissionTypeId, cascadePermission);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while sharing entity with users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException {
        try {
            return shareEntity(domainId, entityId, groupList, permissionTypeId, cascadePermission);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while sharing entity with groups: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals(getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }
            return revokeEntitySharingInternal(domainId, entityId, userList, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while revoking entity sharing from users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals(getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }
            return revokeEntitySharingInternal(domainId, entityId, groupList, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while revoking entity sharing from groups: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            List<GroupMember> parentMemberships = getAllParentMembershipsForChild(domainId, userId);
            List<String> groupIds = new ArrayList<>(
                    parentMemberships.stream().map(GroupMember::getParentId).toList());
            groupIds.add(userId);
            return hasAccess(
                    domainId,
                    entityId,
                    groupIds,
                    List.of(permissionTypeId, getOwnerPermissionTypeIdForDomain(domainId)));
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking user access: domainId=%s, userId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, userId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public List<User> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return getAccessibleUsers(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting list of shared users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public List<User> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return getAccessibleUsersInternal(
                domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
    }

    @Override
    public List<UserGroup> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return getAccessibleGroupsInternal(domainId, entityId, permissionTypeId);
    }

    @Override
    public List<UserGroup> getListOfDirectlySharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return getAccessibleGroupsInternal(
                domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
    }

    // =========================================================================
    // User Operations (formerly SharingTeamService)
    // =========================================================================

    @Override
    public String createUser(User user) throws SharingRegistryException {
        try {
            if (getUserInternal(user.getUserId(), user.getDomainId()) != null) {
                throw new SharingRegistryException("There exist user with given user id");
            }

            user.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            user.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            createUserInternal(user);

            // Auto-create personal group
            String personalGroupId = user.getUserId() + "_personal";
            UserGroup personalGroup = new UserGroup();
            personalGroup.setGroupId(personalGroupId);
            personalGroup.setDomainId(user.getDomainId());
            personalGroup.setName(user.getUserName() != null ? user.getUserName() : user.getUserId());
            personalGroup.setDescription(
                    "Personal group for " + (user.getUserName() != null ? user.getUserName() : user.getUserId()));
            personalGroup.setOwnerId(user.getUserId());
            personalGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            personalGroup.setGroupCardinality(GroupCardinality.SINGLE_USER);
            personalGroup.setIsPersonalGroup(true);
            personalGroup.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            personalGroup.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            createGroup(personalGroup);

            Domain domain = getDomainInternal(user.getDomainId());
            if (domain != null && domain.getInitialUserGroupId() != null) {
                addUsersToGroup(user.getDomainId(), List.of(user.getUserId()), domain.getInitialUserGroupId());
            }

            return user.getUserId();
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating user: userId=%s, domainId=%s", user.getUserId(), user.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException {
        return ServiceOperationHelper.executeBool(
                () -> userExistsInternal(userId, domainId),
                SharingRegistryException.class,
                "Error checking if user exists: domainId=%s, userId=%s",
                domainId,
                userId);
    }

    @Override
    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException {
        try {
            deleteUserInternal(userId, domainId);

            String personalGroupId = userId + "_personal";
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(personalGroupId);
            userGroupPK.setDomainId(domainId);
            if (getGroupEntityByPK(userGroupPK) != null) {
                deleteGroupByPK(userGroupPK);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while deleting user: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public User getUser(String domainId, String userId) throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> getUserInternal(userId, domainId),
                SharingRegistryException.class,
                "Error getting user: domainId=%s, userId=%s",
                domainId,
                userId);
    }

    @Override
    public User getUserByOidcSub(String userId, String domainId) throws SharingRegistryException {
        return getUserInternal(userId, domainId);
    }

    @Override
    public User updateUser(User user) throws SharingRegistryException {
        return updateUserInternal(user);
    }

    @Override
    public List<User> queryUsers(String queryString, Map<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserEntity.class);
        var root = query.from(UserEntity.class);

        var predicates = new ArrayList<Predicate>();
        if (filters != null) {
            for (var entry : filters.entrySet()) {
                String fieldName =
                        switch (entry.getKey()) {
                            case "domainId" -> "gatewayId";
                            case "userId" -> "sub";
                            default -> entry.getKey();
                        };
                predicates.add(cb.equal(root.get(fieldName), entry.getValue()));
            }
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) typedQuery.setFirstResult(offset);
        if (limit > 0) typedQuery.setMaxResults(limit);

        return userMapper.toModelList(typedQuery.getResultList());
    }

    // =========================================================================
    // Group Operations (formerly SharingTeamService)
    // =========================================================================

    @Override
    public String createGroup(UserGroup group) throws SharingRegistryException {
        try {
            UserGroupPK pk = new UserGroupPK();
            pk.setGroupId(group.getGroupId());
            pk.setDomainId(group.getDomainId());
            if (getGroupEntityByPK(pk) != null) {
                throw new SharingRegistryException("There exist group with given group id");
            }
            if (group.getGroupType() == null) {
                group.setGroupType(GroupType.USER_LEVEL_GROUP);
            }
            if (!Boolean.TRUE.equals(group.getIsPersonalGroup())) {
                group.setGroupCardinality(GroupCardinality.MULTI_USER);
            }
            if (group.getGroupCardinality() == null) {
                group.setGroupCardinality(GroupCardinality.MULTI_USER);
            }
            if (group.getIsPersonalGroup() == null) {
                group.setIsPersonalGroup(false);
            }
            group.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            group.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            if (group.getGroupAdmins() == null) {
                group.setGroupAdmins(new ArrayList<>());
            } else {
                group.getGroupAdmins().clear();
            }
            saveGroupEntity(group);
            addUsersToGroup(group.getDomainId(), List.of(group.getOwnerId()), group.getGroupId());
            return group.getGroupId();
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating group: groupId=%s, domainId=%s", group.getGroupId(), group.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean updateGroup(UserGroup group) throws SharingRegistryException {
        try {
            group.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            UserGroupPK pk = new UserGroupPK();
            pk.setGroupId(group.getGroupId());
            pk.setDomainId(group.getDomainId());
            UserGroup old = groupEntityToModel(getGroupEntityByPK(pk));
            group.setGroupCardinality(old.getGroupCardinality());
            group.setCreatedTime(old.getCreatedTime());
            group = getUpdatedObject(old, group);

            if (!group.getOwnerId().equals(old.getOwnerId())) {
                throw new SharingRegistryException("Group owner cannot be changed");
            }
            saveGroupEntity(group);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating group: groupId=%s, domainId=%s", group.getGroupId(), group.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException {
        return ServiceOperationHelper.executeBool(
                () -> {
                    UserGroupPK pk = new UserGroupPK();
                    pk.setDomainId(domainId);
                    pk.setGroupId(groupId);
                    return userGroupRepository.existsById(pk);
                },
                SharingRegistryException.class,
                "Error checking if group exists: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    @Override
    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException {
        UserGroup group = getGroup(domainId, groupId);
        if (Boolean.TRUE.equals(group.getIsPersonalGroup())) {
            throw new SharingRegistryException("Cannot delete personal group. Remove the user first.");
        }
        ServiceOperationHelper.executeVoid(
                () -> {
                    UserGroupPK pk = new UserGroupPK();
                    pk.setGroupId(groupId);
                    pk.setDomainId(domainId);
                    deleteGroupByPK(pk);
                },
                SharingRegistryException.class,
                "Error deleting group: domainId=%s, groupId=%s",
                domainId,
                groupId);
        return true;
    }

    @Override
    public UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> {
                    UserGroupPK pk = new UserGroupPK();
                    pk.setGroupId(groupId);
                    pk.setDomainId(domainId);
                    UserGroupEntity entity = getGroupEntityByPK(pk);
                    UserGroup group = entity != null ? groupEntityToModel(entity) : null;
                    if (group == null && isUserExists(domainId, groupId)) {
                        UserGroupPK personalPK = new UserGroupPK();
                        personalPK.setGroupId(groupId + "_personal");
                        personalPK.setDomainId(domainId);
                        UserGroupEntity personalEntity = getGroupEntityByPK(personalPK);
                        group = personalEntity != null ? groupEntityToModel(personalEntity) : null;
                    }
                    if (group != null) {
                        List<GroupMember> admins = getGroupAdmins(domainId, group.getGroupId());
                        group.setGroupAdmins(admins != null ? admins : Collections.emptyList());
                    }
                    return group;
                },
                SharingRegistryException.class,
                "Error getting group: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    @Override
    public List<UserGroup> getGroups(String domain, int offset, int limit) throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> {
                    HashMap<String, String> filters = new HashMap<>();
                    filters.put(SharingDBConstants.UserGroupTable.DOMAIN_ID, domain);
                    filters.put(
                            SharingDBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.name());
                    return selectGroupsInternal(null, filters, offset, limit);
                },
                SharingRegistryException.class,
                "Error getting groups: domain=%s, offset=%d, limit=%d",
                domain,
                offset,
                limit);
    }

    @Override
    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        try {
            UserGroup group = getGroup(domainId, groupId);
            if (Boolean.TRUE.equals(group.getIsPersonalGroup())) {
                if (userIds == null || userIds.size() != 1 || !userIds.get(0).equals(group.getOwnerId())) {
                    throw new SharingRegistryException("Cannot add members to personal group.");
                }
            }
            if (userIds == null) {
                userIds = Collections.emptyList();
            }
            for (int i = 0; i < userIds.size(); i++) {
                GroupMemberPK memberPK = new GroupMemberPK(groupId, userIds.get(i), domainId);
                GroupMember existing = getMember(memberPK);
                if (existing == null) {
                    GroupMember groupMember = new GroupMember();
                    groupMember.setParentId(groupId);
                    groupMember.setChildId(userIds.get(i));
                    groupMember.setChildType(GroupChildType.USER);
                    groupMember.setDomainId(domainId);
                    groupMember.setRole(GroupMemberRole.MEMBER);
                    groupMember.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    groupMember.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    createMember(groupMember);
                }
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while adding users to group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        try {
            UserGroup group = getGroup(domainId, groupId);
            if (Boolean.TRUE.equals(group.getIsPersonalGroup())) {
                throw new SharingRegistryException("Cannot remove user from their personal group.");
            }
            if (userIds == null) {
                userIds = Collections.emptyList();
            }
            for (String userId : userIds) {
                if (hasOwnerAccess(domainId, groupId, userId)) {
                    throw new SharingRegistryException(
                            "List of User Ids contains Owner Id. Cannot remove owner from the group");
                }
            }
            for (int i = 0; i < userIds.size(); i++) {
                GroupMemberPK memberPK = new GroupMemberPK(groupId, userIds.get(i), domainId);
                deleteMember(memberPK);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while removing users from group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException, DuplicateEntryException {
        try {
            List<User> groupUser = getGroupMembersOfTypeUser(domainId, groupId, 0, -1);
            if (!isUserBelongsToGroup(groupUser, newOwnerId)) {
                throw new SharingRegistryException("New group owner is not part of the group");
            }
            if (hasOwnerAccess(domainId, groupId, newOwnerId)) {
                throw new DuplicateEntryException("User already the current owner of the group");
            }
            if (hasAdminAccess(domainId, groupId, newOwnerId)) {
                removeGroupAdmins(domainId, groupId, List.of(newOwnerId));
            }

            UserGroupPK pk = new UserGroupPK();
            pk.setGroupId(groupId);
            pk.setDomainId(domainId);
            UserGroup userGroup = groupEntityToModel(getGroupEntityByPK(pk));
            UserGroup newUserGroup = new UserGroup();
            newUserGroup.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            newUserGroup.setOwnerId(newOwnerId);
            newUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
            newUserGroup.setCreatedTime(userGroup.getCreatedTime());
            newUserGroup = getUpdatedObject(userGroup, newUserGroup);
            saveGroupEntity(newUserGroup);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while transferring group ownership: domainId=%s, groupId=%s, newOwnerId=%s",
                    domainId, groupId, newOwnerId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, DuplicateEntryException {
        try {
            List<User> groupUser = getGroupMembersOfTypeUser(domainId, groupId, 0, -1);
            for (String adminId : adminIds) {
                if (!isUserBelongsToGroup(groupUser, adminId)) {
                    throw new SharingRegistryException(
                            "Admin not the user of the group. GroupId : " + groupId + ", AdminId : " + adminId);
                }
                GroupMemberPK memberPK = new GroupMemberPK(groupId, adminId, domainId);
                GroupMember existing = getMember(memberPK);
                if (existing != null && existing.getRole() == GroupMemberRole.ADMIN) {
                    throw new DuplicateEntryException("User already an admin for the group");
                }
                if (existing != null) {
                    existing.setRole(GroupMemberRole.ADMIN);
                    existing.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    updateMember(existing);
                } else {
                    GroupMember member = new GroupMember();
                    member.setParentId(groupId);
                    member.setChildId(adminId);
                    member.setDomainId(domainId);
                    member.setChildType(GroupChildType.USER);
                    member.setRole(GroupMemberRole.ADMIN);
                    member.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    member.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    createMember(member);
                }
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while adding group admins: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException {
        try {
            for (String adminId : adminIds) {
                GroupMemberPK memberPK = new GroupMemberPK(groupId, adminId, domainId);
                GroupMember existing = getMember(memberPK);
                if (existing != null && existing.getRole() == GroupMemberRole.ADMIN) {
                    existing.setRole(GroupMemberRole.MEMBER);
                    existing.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    updateMember(existing);
                }
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while removing group admins: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    @Override
    public boolean hasAdminAccess(String domainId, String groupId, String adminId) throws SharingRegistryException {
        return ServiceOperationHelper.executeBool(
                () -> isAdmin(domainId, groupId, adminId),
                SharingRegistryException.class,
                "Error checking admin access: domainId=%s, groupId=%s, adminId=%s",
                domainId,
                groupId,
                adminId);
    }

    @Override
    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId) throws SharingRegistryException {
        return ServiceOperationHelper.executeBool(
                () -> {
                    UserGroupPK pk = new UserGroupPK();
                    pk.setGroupId(groupId);
                    pk.setDomainId(domainId);
                    UserGroupEntity entity = getGroupEntityByPK(pk);
                    if (entity == null) return false;
                    return ownerId.equals(entity.getOwnerId());
                },
                SharingRegistryException.class,
                "Error checking owner access: domainId=%s, groupId=%s, ownerId=%s",
                domainId,
                groupId,
                ownerId);
    }

    @Override
    public List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> getAllChildUsers(domainId, groupId),
                SharingRegistryException.class,
                "Error getting group members of type user: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    @Override
    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException {
        ServiceOperationHelper.executeVoid(
                () -> {
                    GroupMemberPK pk = new GroupMemberPK(groupId, childId, domainId);
                    deleteMember(pk);
                },
                SharingRegistryException.class,
                "Error removing child group from parent: domainId=%s, childId=%s, groupId=%s",
                domainId,
                childId,
                groupId);
        return true;
    }

    @Override
    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException {
        return ServiceOperationHelper.execute(
                () -> {
                    List<GroupMembershipEntity> members =
                            groupMembershipRepository.findByDomainIdAndUserId(domainId, userId);
                    List<String> groupIds = members.stream()
                            .map(GroupMembershipEntity::getGroupId)
                            .distinct()
                            .toList();
                    if (groupIds.isEmpty()) return new ArrayList<>();
                    List<UserGroupPK> pks = groupIds.stream()
                            .map(gid -> new UserGroupPK(gid, domainId))
                            .toList();
                    return groupEntitiesToModelList(userGroupRepository.findAllById(pks));
                },
                SharingRegistryException.class,
                "Error getting member groups for user: domainId=%s, userId=%s",
                domainId,
                userId);
    }

    // =========================================================================
    // Group Membership Operations (formerly GroupMembershipService)
    // =========================================================================

    @Override
    public GroupMember getMember(GroupMemberPK pk) throws SharingRegistryException {
        var opt = groupMembershipRepository.findByDomainIdAndGroupIdAndUserId(
                pk.getDomainId(), pk.getParentId(), pk.getChildId());
        return opt.map(this::membershipToGroupMember).orElse(null);
    }

    @Override
    public GroupMember createMember(GroupMember groupMember) throws SharingRegistryException {
        return updateMember(groupMember);
    }

    @Override
    public GroupMember updateMember(GroupMember groupMember) throws SharingRegistryException {
        var existing = groupMembershipRepository.findByDomainIdAndGroupIdAndUserId(
                groupMember.getDomainId(), groupMember.getParentId(), groupMember.getChildId());
        var entity = groupMemberToMembership(groupMember);
        if (existing.isPresent()) {
            entity.setMembershipId(existing.get().getMembershipId());
            entity.setCreatedAt(existing.get().getCreatedAt());
        } else {
            entity.setMembershipId("gm_" + IdGenerator.getId("rel"));
            entity.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        var saved = groupMembershipRepository.save(entity);
        return membershipToGroupMember(saved);
    }

    @Override
    public boolean deleteMember(GroupMemberPK pk) throws SharingRegistryException {
        var opt = groupMembershipRepository.findByDomainIdAndGroupIdAndUserId(
                pk.getDomainId(), pk.getParentId(), pk.getChildId());
        opt.ifPresent(groupMembershipRepository::delete);
        return true;
    }

    @Override
    public boolean isMemberExists(GroupMemberPK pk) throws SharingRegistryException {
        return groupMembershipRepository
                .findByDomainIdAndGroupIdAndUserId(pk.getDomainId(), pk.getParentId(), pk.getChildId())
                .isPresent();
    }

    @Override
    public boolean isAdmin(String domainId, String groupId, String memberId) throws SharingRegistryException {
        var opt = groupMembershipRepository.findByDomainIdAndGroupIdAndUserId(domainId, groupId, memberId);
        return opt.isPresent() && GroupMemberRole.ADMIN.name().equals(opt.get().getRole());
    }

    @Override
    public List<GroupMember> getGroupAdmins(String domainId, String groupId) throws SharingRegistryException {
        return groupMembershipRepository
                .findByDomainIdAndGroupIdAndRole(domainId, groupId, GroupMemberRole.ADMIN.name())
                .stream()
                .map(this::membershipToGroupMember)
                .toList();
    }

    @Override
    public List<User> getAllChildUsers(String domainId, String groupId) throws SharingRegistryException {
        List<GroupMembershipEntity> members = groupMembershipRepository.findByDomainIdAndGroupId(domainId, groupId);
        List<String> userIds = members.stream()
                .map(GroupMembershipEntity::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) return new ArrayList<>();
        List<UserEntity> userEntities = userRepository.findByGatewayNameAndSubIn(domainId, userIds);
        return userMapper.toModelList(userEntities);
    }

    @Override
    public List<GroupMember> getAllParentMembershipsForChild(String domainId, String childId)
            throws SharingRegistryException {
        List<GroupMember> finalParentGroups = new ArrayList<>();
        Map<String, String> filters = new HashMap<>();
        filters.put("childId", childId);
        filters.put("domainId", domainId);
        LinkedList<GroupMember> temp = new LinkedList<>(selectMembersInternal(filters, 0, -1));
        while (!temp.isEmpty()) {
            GroupMember gm = temp.pop();
            finalParentGroups.add(gm);
            filters = new HashMap<>();
            filters.put("childId", gm.getParentId());
            filters.put("domainId", domainId);
            temp.addAll(selectMembersInternal(filters, 0, -1));
        }
        return finalParentGroups;
    }

    @Override
    public boolean isShared(String domainId, String entityId) throws SharingRegistryException {
        var ownerPermissionTypeId = getOwnerPermissionTypeIdForDomain(domainId);
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var groupRoot = query.from(UserGroupEntity.class);
        var permRoot = query.from(SharingPermissionEntity.class);

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(groupRoot.get("groupId"), permRoot.get("granteeId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), permRoot.get("domainId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), domainId));
        predicates.add(cb.equal(permRoot.get("resourceType"), "ENTITY"));
        predicates.add(cb.equal(permRoot.get("resourceId"), entityId));
        predicates.add(cb.equal(permRoot.get("granteeType"), "GROUP"));
        predicates.add(cb.notEqual(permRoot.get("permission"), ownerPermissionTypeId));

        query.select(cb.count(groupRoot));
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Long count = entityManager.createQuery(query).getSingleResult();
        return count > 0;
    }

    // =========================================================================
    // Registry / Permission Record Operations (formerly SharingRegistryService)
    // =========================================================================

    @Override
    public Sharing getPermission(SharingPK pk) throws SharingRegistryException {
        var opt =
                sharingPermissionRepository
                        .findByDomainIdAndResourceTypeAndResourceIdAndGranteeTypeAndGranteeIdAndPermission(
                                pk.getDomainId(),
                                RESOURCE_TYPE_ENTITY,
                                pk.getEntityId(),
                                GRANTEE_TYPE_GROUP,
                                pk.getGroupId(),
                                pk.getPermissionTypeId());
        return opt.map(this::sharingPermissionToSharing).orElse(null);
    }

    @Override
    public Sharing createPermission(Sharing sharing) throws SharingRegistryException {
        return updatePermission(sharing);
    }

    @Override
    public Sharing updatePermission(Sharing sharing) throws SharingRegistryException {
        var existing =
                sharingPermissionRepository
                        .findByDomainIdAndResourceTypeAndResourceIdAndGranteeTypeAndGranteeIdAndPermission(
                                sharing.getDomainId(),
                                RESOURCE_TYPE_ENTITY,
                                sharing.getEntityId(),
                                GRANTEE_TYPE_GROUP,
                                sharing.getGroupId(),
                                sharing.getPermissionTypeId() != null ? sharing.getPermissionTypeId() : "");
        var entity = sharingToSharingPermission(sharing);
        if (existing.isPresent()) {
            var e = existing.get();
            entity.setPermissionId(e.getPermissionId());
            entity.setCreatedAt(e.getCreatedAt());
        } else {
            entity.setPermissionId("sharing_" + IdGenerator.getId("rel"));
            entity.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        var saved = sharingPermissionRepository.save(entity);
        return sharingPermissionToSharing(saved);
    }

    @Override
    public boolean deletePermission(SharingPK pk) throws SharingRegistryException {
        var opt =
                sharingPermissionRepository
                        .findByDomainIdAndResourceTypeAndResourceIdAndGranteeTypeAndGranteeIdAndPermission(
                                pk.getDomainId(),
                                RESOURCE_TYPE_ENTITY,
                                pk.getEntityId(),
                                GRANTEE_TYPE_GROUP,
                                pk.getGroupId(),
                                pk.getPermissionTypeId());
        opt.ifPresent(sharingPermissionRepository::delete);
        return true;
    }

    @Override
    public boolean permissionExists(SharingPK pk) throws SharingRegistryException {
        return sharingPermissionRepository
                .findByDomainIdAndResourceTypeAndResourceIdAndGranteeTypeAndGranteeIdAndPermission(
                        pk.getDomainId(),
                        RESOURCE_TYPE_ENTITY,
                        pk.getEntityId(),
                        GRANTEE_TYPE_GROUP,
                        pk.getGroupId(),
                        pk.getPermissionTypeId())
                .isPresent();
    }

    @Override
    public List<Sharing> selectPermissions(Map<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        String domainId = filters != null ? filters.get("domainId") : null;
        return sharingPermissionRepository.findAll().stream()
                .filter(e -> RESOURCE_TYPE_ENTITY.equals(e.getResourceType())
                        && GRANTEE_TYPE_GROUP.equals(e.getGranteeType())
                        && (domainId == null || domainId.isEmpty() || domainId.equals(e.getDomainId())))
                .map(this::sharingPermissionToSharing)
                .toList();
    }

    @Override
    public boolean hasAccess(String domainId, String entityId, List<String> groupIds, List<String> permissionTypeIds)
            throws SharingRegistryException {
        return sharingPermissionRepository.hasAccess(
                domainId, RESOURCE_TYPE_ENTITY, entityId, permissionTypeIds, groupIds);
    }

    @Override
    public int getSharedCount(String domainId, String entityId) throws SharingRegistryException {
        String ownerPermissionTypeId = getOwnerPermissionTypeIdForDomain(domainId);
        return sharingPermissionRepository.countExcludingPermission(
                domainId, RESOURCE_TYPE_ENTITY, entityId, ownerPermissionTypeId);
    }

    // =========================================================================
    // Package-private utility for SharingTeamService-like getUpdatedObject usage
    // =========================================================================

    <T> T getUpdatedObject(T oldEntity, T newEntity) throws SharingRegistryException {
        Field[] newEntityFields = newEntity.getClass().getDeclaredFields();
        HashMap<String, Object> newHT = fieldsToHT(newEntityFields, newEntity);

        Class<?> oldEntityClass = oldEntity.getClass();
        Field[] oldEntityFields = oldEntityClass.getDeclaredFields();

        for (Field field : oldEntityFields) {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                Object o = newHT.get(field.getName());
                if (o != null) {
                    Field f = null;
                    try {
                        f = oldEntityClass.getDeclaredField(field.getName());
                        f.setAccessible(true);
                        logger.debug("setting " + f.getName());
                        f.set(oldEntity, o);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new SharingRegistryException(e.getMessage());
                    }
                }
            }
        }
        return oldEntity;
    }

    // =========================================================================
    // Private helpers — domain
    // =========================================================================

    private Domain getDomainInternal(String domainId) throws SharingRegistryException {
        var entity = gatewayRepository.findByGatewayNameOrId(domainId).orElse(null);
        if (entity == null) return null;
        return domainMapper.toModel(entity);
    }

    private void updateDomainInternal(Domain domain) throws SharingRegistryException {
        GatewayEntity existing =
                gatewayRepository.findByGatewayNameOrId(domain.getDomainId()).orElse(null);
        if (existing == null) {
            throw new SharingRegistryException("Gateway not found for domainId: " + domain.getDomainId());
        }
        existing.setInitialUserGroupId(domain.getInitialUserGroupId());
        if (domain.getName() != null && !domain.getName().equals(existing.getGatewayName())) {
            existing.setGatewayName(domain.getName());
        }
        gatewayRepository.save(existing);
    }

    // =========================================================================
    // Private helpers — entity type store
    // =========================================================================

    private String entityTypeKey(String domainId, String entityTypeId) {
        return domainId + ":" + entityTypeId;
    }

    private EntityType getEntityTypeInternal(EntityTypePK pk) {
        return entityTypeStore.get(entityTypeKey(pk.getDomainId(), pk.getEntityTypeId()));
    }

    private void storeEntityType(EntityType entityType) {
        entityTypeStore.put(entityTypeKey(entityType.getDomainId(), entityType.getEntityTypeId()), entityType);
    }

    private List<EntityType> selectEntityTypes(HashMap<String, String> filters, int offset, int limit) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(entityTypeStore.values());
        }
        String domainId = filters.get("DOMAIN_ID");
        List<EntityType> result = new ArrayList<>();
        for (EntityType et : entityTypeStore.values()) {
            if (domainId == null || domainId.equals(et.getDomainId())) {
                result.add(et);
            }
        }
        return result;
    }

    // =========================================================================
    // Private helpers — permission type store
    // =========================================================================

    private String permissionTypeKey(String domainId, String permissionTypeId) {
        return domainId + ":" + permissionTypeId;
    }

    private PermissionType getPermissionTypeInternal(PermissionTypePK pk) {
        return permissionTypeStore.get(permissionTypeKey(pk.getDomainId(), pk.getPermissionTypeId()));
    }

    private void storePermissionType(PermissionType permissionType) {
        permissionTypeStore.put(
                permissionTypeKey(permissionType.getDomainId(), permissionType.getPermissionTypeId()), permissionType);
    }

    private List<PermissionType> selectPermissionTypes(HashMap<String, String> filters, int offset, int limit) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(permissionTypeStore.values());
        }
        String domainId = filters.get("DOMAIN_ID");
        List<PermissionType> result = new ArrayList<>();
        for (PermissionType pt : permissionTypeStore.values()) {
            if (domainId == null || domainId.equals(pt.getDomainId())) {
                result.add(pt);
            }
        }
        return result;
    }

    // =========================================================================
    // Private helpers — entity cache key
    // =========================================================================

    private String entityKey(String domainId, String entityId) {
        return domainId + ":" + entityId;
    }

    // =========================================================================
    // Private helpers — sharing / permission record
    // =========================================================================

    private void addCascadingPermissionsForEntity(SharingEntity entity) throws SharingRegistryException {
        List<Sharing> sharings = getCascadingPermissionsForEntity(entity.getDomainId(), entity.getParentEntityId());
        for (Sharing sharing : sharings) {
            Sharing newSharing = new Sharing();
            newSharing.setPermissionTypeId(sharing.getPermissionTypeId());
            newSharing.setEntityId(entity.getEntityId());
            newSharing.setGroupId(sharing.getGroupId());
            newSharing.setInheritedParentId(sharing.getInheritedParentId());
            newSharing.setSharingType(SharingType.INDIRECT_CASCADING);
            newSharing.setDomainId(entity.getDomainId());
            newSharing.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            newSharing.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            createPermission(newSharing);
        }
    }

    private List<Sharing> getCascadingPermissionsForEntity(String domainId, String entityId) {
        var sharingTypes =
                Arrays.asList(SharingType.DIRECT_CASCADING.toString(), SharingType.INDIRECT_CASCADING.toString());
        var entities = sharingPermissionRepository.findByDomainIdAndResourceTypeAndResourceId(
                domainId, RESOURCE_TYPE_ENTITY, entityId);
        return entities.stream()
                .filter(e -> GRANTEE_TYPE_GROUP.equals(e.getGranteeType())
                        && sharingTypes.contains(metadataString(e, "sharingType")))
                .map(this::sharingPermissionToSharing)
                .toList();
    }

    private List<Sharing> getIndirectSharedChildren(String domainId, String parentId, String permissionTypeId) {
        var entities = sharingPermissionRepository.findAll().stream()
                .filter(e -> domainId.equals(e.getDomainId())
                        && RESOURCE_TYPE_ENTITY.equals(e.getResourceType())
                        && GRANTEE_TYPE_GROUP.equals(e.getGranteeType()))
                .toList();
        return entities.stream()
                .filter(e -> permissionTypeId.equals(e.getPermission())
                        && parentId.equals(metadataString(e, "inheritedParentId"))
                        && SharingType.INDIRECT_CASCADING.toString().equals(metadataString(e, "sharingType")))
                .map(this::sharingPermissionToSharing)
                .toList();
    }

    private void removeAllIndirectCascadingPermissionsForEntity(String domainId, String entityId) {
        var entities = sharingPermissionRepository.findByDomainIdAndResourceTypeAndResourceId(
                domainId, RESOURCE_TYPE_ENTITY, entityId);
        for (var e : entities) {
            if (GRANTEE_TYPE_GROUP.equals(e.getGranteeType())
                    && SharingType.INDIRECT_CASCADING.toString().equals(metadataString(e, "sharingType"))) {
                sharingPermissionRepository.delete(e);
            }
        }
    }

    private boolean shareEntity(
            String domainId,
            String entityId,
            List<String> groupOrUserList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException {
        if (permissionTypeId.equals(getOwnerPermissionTypeIdForDomain(domainId))) {
            throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
        }

        List<Sharing> sharings = new ArrayList<>();
        LinkedList<SharingEntity> temp = new LinkedList<>();
        for (String userId : groupOrUserList) {
            Sharing sharing = new Sharing();
            sharing.setPermissionTypeId(permissionTypeId);
            sharing.setEntityId(entityId);
            sharing.setGroupId(userId);
            sharing.setInheritedParentId(entityId);
            sharing.setDomainId(domainId);
            if (cascadePermission) {
                sharing.setSharingType(SharingType.DIRECT_CASCADING);
            } else {
                sharing.setSharingType(SharingType.DIRECT_NON_CASCADING);
            }
            sharing.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
            sharing.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
            sharings.add(sharing);
        }

        if (cascadePermission) {
            getChildEntitiesFromCache(domainId, entityId).forEach(temp::addLast);
            while (!temp.isEmpty()) {
                SharingEntity entity = temp.pop();
                String childEntityId = entity.getEntityId();
                for (String userId : groupOrUserList) {
                    Sharing sharing = new Sharing();
                    sharing.setPermissionTypeId(permissionTypeId);
                    sharing.setEntityId(childEntityId);
                    sharing.setGroupId(userId);
                    sharing.setInheritedParentId(entityId);
                    sharing.setSharingType(SharingType.INDIRECT_CASCADING);
                    sharing.setDomainId(domainId);
                    sharing.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    sharing.setUpdatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    sharings.add(sharing);
                    getChildEntitiesFromCache(domainId, childEntityId).forEach(temp::addLast);
                }
            }
        }
        for (Sharing sharing : sharings) {
            createPermission(sharing);
        }

        String key = entityKey(domainId, entityId);
        SharingEntity entity = entityCache.get(key);
        if (entity != null) {
            long sharedCount = getSharedCount(domainId, entityId);
            entity.setSharedCount(sharedCount);
            entityCache.put(key, entity);
        }
        return true;
    }

    private boolean revokeEntitySharingInternal(
            String domainId, String entityId, List<String> groupOrUserList, String permissionTypeId)
            throws SharingRegistryException {
        if (permissionTypeId.equals(getOwnerPermissionTypeIdForDomain(domainId))) {
            throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be removed");
        }

        for (String groupId : groupOrUserList) {
            SharingPK sharingPK = new SharingPK();
            sharingPK.setEntityId(entityId);
            sharingPK.setGroupId(groupId);
            sharingPK.setPermissionTypeId(permissionTypeId);
            sharingPK.setInheritedParentId(entityId);
            sharingPK.setDomainId(domainId);
            deletePermission(sharingPK);
        }

        List<Sharing> temp = new ArrayList<>(getIndirectSharedChildren(domainId, entityId, permissionTypeId));
        for (Sharing sharing : temp) {
            String childEntityId = sharing.getEntityId();
            for (String groupId : groupOrUserList) {
                SharingPK sharingPK = new SharingPK();
                sharingPK.setEntityId(childEntityId);
                sharingPK.setGroupId(groupId);
                sharingPK.setPermissionTypeId(permissionTypeId);
                sharingPK.setInheritedParentId(entityId);
                sharingPK.setDomainId(domainId);
                deletePermission(sharingPK);
            }
        }

        String key = entityKey(domainId, entityId);
        SharingEntity entity = entityCache.get(key);
        if (entity != null) {
            long sharedCount = getSharedCount(domainId, entityId);
            entity.setSharedCount(sharedCount);
            entityCache.put(key, entity);
        }
        return true;
    }

    private List<SharingEntity> getChildEntitiesFromCache(String domainId, String parentId) {
        List<SharingEntity> children = new ArrayList<>();
        for (SharingEntity entity : entityCache.values()) {
            if (domainId.equals(entity.getDomainId()) && parentId.equals(entity.getParentEntityId())) {
                children.add(entity);
            }
        }
        return children;
    }

    private Sharing sharingPermissionToSharing(SharingPermissionEntity e) {
        var s = new Sharing();
        s.setPermissionTypeId(e.getPermission());
        s.setEntityId(e.getResourceId());
        s.setGroupId(e.getGranteeId());
        s.setDomainId(e.getDomainId());
        s.setCreatedTime(e.getCreatedAt() != null ? e.getCreatedAt().getTime() : null);
        s.setInheritedParentId(metadataString(e, "inheritedParentId"));
        String sharingType = metadataString(e, "sharingType");
        s.setSharingType(sharingType != null ? SharingType.valueOf(sharingType) : null);
        return s;
    }

    private SharingPermissionEntity sharingToSharingPermission(Sharing s) {
        var e = new SharingPermissionEntity();
        e.setDomainId(s.getDomainId());
        e.setResourceType(RESOURCE_TYPE_ENTITY);
        e.setResourceId(s.getEntityId());
        e.setGranteeType(GRANTEE_TYPE_GROUP);
        e.setGranteeId(s.getGroupId());
        e.setPermission(s.getPermissionTypeId() != null ? s.getPermissionTypeId() : "");
        if (s.getInheritedParentId() != null || s.getSharingType() != null) {
            e.setMetadata(Map.of(
                    "inheritedParentId", s.getInheritedParentId() != null ? s.getInheritedParentId() : "",
                    "sharingType",
                            s.getSharingType() != null ? s.getSharingType().name() : ""));
        }
        return e;
    }

    private static String metadataString(SharingPermissionEntity e, String key) {
        if (e.getMetadata() == null) return null;
        var v = e.getMetadata().get(key);
        return v != null ? v.toString() : null;
    }

    // =========================================================================
    // Private helpers — user OIDC bridge
    // =========================================================================

    private String normalizeSub(String userId, String domainId) {
        if (userId != null && domainId != null && userId.endsWith("@" + domainId)) {
            return userId.substring(0, userId.length() - domainId.length() - 1);
        }
        return userId;
    }

    private User getUserInternal(String userId, String domainId) throws SharingRegistryException {
        var entity = userRepository
                .findByUserIdAndDomainId(normalizeSub(userId, domainId), domainId)
                .orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    private User createUserInternal(User user) throws SharingRegistryException {
        String sub = normalizeSub(user.getUserId(), user.getDomainId());
        var entity = new UserEntity(sub, user.getDomainId());
        userMapper.updateEntityFromModel(user, entity);
        var saved = userRepository.save(entity);
        return userMapper.toModel(saved);
    }

    private User updateUserInternal(User user) throws SharingRegistryException {
        String internalUserId =
                UserEntity.createUserId(normalizeSub(user.getUserId(), user.getDomainId()), user.getDomainId());
        var existing = userRepository.findById(internalUserId).orElse(null);
        if (existing != null) {
            userMapper.updateEntityFromModel(user, existing);
            var saved = userRepository.save(existing);
            return userMapper.toModel(saved);
        } else {
            return createUserInternal(user);
        }
    }

    private boolean deleteUserInternal(String userId, String domainId) throws SharingRegistryException {
        String internalUserId = UserEntity.createUserId(normalizeSub(userId, domainId), domainId);
        userRepository.deleteById(internalUserId);
        return true;
    }

    private boolean userExistsInternal(String userId, String domainId) throws SharingRegistryException {
        String normalized = normalizeSub(userId, domainId);
        return userRepository.existsByUserIdAndDomainId(normalized, domainId);
    }

    private List<User> getAccessibleUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        if (permissionTypeId.equals(getOwnerPermissionTypeIdForDomain(domainId))) {
            return getAccessibleUsersInternal(
                    domainId,
                    entityId,
                    permissionTypeId,
                    SharingType.DIRECT_CASCADING,
                    SharingType.DIRECT_NON_CASCADING);
        } else {
            return getAccessibleUsersInternal(domainId, entityId, permissionTypeId);
        }
    }

    private List<User> getAccessibleUsersInternal(
            String domainId, String entityId, String permissionTypeId, SharingType... sharingTypes) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserEntity.class);
        var userRoot = query.from(UserEntity.class);
        var permRoot = query.from(SharingPermissionEntity.class);
        var memRoot = query.from(GroupMembershipEntity.class);

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(userRoot.get("gatewayId"), domainId));
        predicates.add(cb.equal(permRoot.get("domainId"), domainId));
        predicates.add(cb.equal(permRoot.get("resourceType"), "ENTITY"));
        predicates.add(cb.equal(permRoot.get("resourceId"), entityId));
        predicates.add(cb.equal(permRoot.get("granteeType"), "GROUP"));
        predicates.add(cb.equal(permRoot.get("permission"), permissionTypeId));
        predicates.add(cb.equal(memRoot.get("domainId"), domainId));
        predicates.add(cb.equal(memRoot.get("groupId"), permRoot.get("granteeId")));
        predicates.add(cb.equal(memRoot.get("userId"), userRoot.get("sub")));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(permRoot.get("createdAt")));

        var entities = entityManager.createQuery(query).getResultList();
        return userMapper.toModelList(entities);
    }

    // =========================================================================
    // Private helpers — group entity CRUD
    // =========================================================================

    private UserGroupEntity getGroupEntityByPK(UserGroupPK pk) {
        return userGroupRepository.findById(pk).orElse(null);
    }

    private void saveGroupEntity(UserGroup group) {
        userGroupRepository.save(groupModelToEntity(group));
    }

    private void deleteGroupByPK(UserGroupPK pk) {
        userGroupRepository.deleteById(pk);
    }

    private List<UserGroup> selectGroupsInternal(
            String queryString, Map<String, String> filters, int offset, int limit) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserGroupEntity.class);
        var root = query.from(UserGroupEntity.class);

        var predicates = new ArrayList<Predicate>();
        if (filters != null) {
            for (var entry : filters.entrySet()) {
                predicates.add(cb.equal(root.get(entry.getKey()), entry.getValue()));
            }
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<UserGroupEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) typedQuery.setFirstResult(offset);
        if (limit > 0) typedQuery.setMaxResults(limit);

        return groupEntitiesToModelList(typedQuery.getResultList());
    }

    private List<UserGroup> getAccessibleGroupsInternal(
            String domainId, String entityId, String permissionTypeId, SharingType... sharingTypes) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserGroupEntity.class);
        var groupRoot = query.from(UserGroupEntity.class);
        var permRoot = query.from(SharingPermissionEntity.class);

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(groupRoot.get("groupId"), permRoot.get("granteeId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), permRoot.get("domainId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), domainId));
        predicates.add(cb.equal(permRoot.get("resourceType"), "ENTITY"));
        predicates.add(cb.equal(permRoot.get("resourceId"), entityId));
        predicates.add(cb.equal(permRoot.get("granteeType"), "GROUP"));
        predicates.add(cb.equal(permRoot.get("permission"), permissionTypeId));
        predicates.add(cb.equal(groupRoot.get("groupCardinality"), GroupCardinality.MULTI_USER.toString()));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(permRoot.get("createdAt")));

        return groupEntitiesToModelList(entityManager.createQuery(query).getResultList());
    }

    // =========================================================================
    // Private helpers — group membership
    // =========================================================================

    private List<GroupMember> selectMembersInternal(Map<String, String> filters, int offset, int limit) {
        String domainId = filters != null ? filters.get("domainId") : null;
        String parentId = filters != null ? filters.get("parentId") : null;
        String childId = filters != null ? filters.get("childId") : null;
        List<GroupMembershipEntity> entities;
        if (domainId == null || domainId.isEmpty()) {
            entities = groupMembershipRepository.findAll();
        } else if (parentId != null && !parentId.isEmpty()) {
            entities = groupMembershipRepository.findByDomainIdAndGroupId(domainId, parentId);
        } else if (childId != null && !childId.isEmpty()) {
            entities = groupMembershipRepository.findByDomainIdAndUserId(domainId, childId);
        } else {
            entities = groupMembershipRepository.findAll().stream()
                    .filter(e -> domainId.equals(e.getDomainId()))
                    .toList();
        }
        List<GroupMember> result =
                entities.stream().map(this::membershipToGroupMember).toList();
        if (offset > 0 || (limit > 0 && limit < result.size())) {
            int from = Math.min(offset, result.size());
            int to = limit > 0 ? Math.min(offset + limit, result.size()) : result.size();
            result = result.subList(from, to);
        }
        return result;
    }

    // =========================================================================
    // Private helpers — mapping
    // =========================================================================

    private UserGroup groupEntityToModel(UserGroupEntity entity) {
        if (entity == null) return null;
        var model = new UserGroup();
        model.setGroupId(entity.getGroupId());
        model.setDomainId(entity.getDomainId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setOwnerId(entity.getOwnerId());
        model.setGroupType(entity.getGroupType() != null ? GroupType.valueOf(entity.getGroupType()) : null);
        model.setGroupCardinality(
                entity.getGroupCardinality() != null ? GroupCardinality.valueOf(entity.getGroupCardinality()) : null);
        model.setCreatedTime(
                entity.getCreatedTime() != null ? entity.getCreatedTime().toEpochMilli() : null);
        model.setUpdatedTime(
                entity.getUpdatedTime() != null ? entity.getUpdatedTime().toEpochMilli() : null);
        model.setGroupAdmins(entity.getGroupAdmins());
        model.setIsPersonalGroup(entity.getIsPersonalGroup());
        return model;
    }

    private UserGroupEntity groupModelToEntity(UserGroup model) {
        var entity = new UserGroupEntity();
        entity.setGroupId(model.getGroupId());
        entity.setDomainId(model.getDomainId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setOwnerId(model.getOwnerId());
        entity.setGroupType(model.getGroupType() != null ? model.getGroupType().name() : null);
        entity.setGroupCardinality(
                model.getGroupCardinality() != null
                        ? model.getGroupCardinality().name()
                        : null);
        entity.setCreatedTime(model.getCreatedTime() != null ? Instant.ofEpochMilli(model.getCreatedTime()) : null);
        entity.setUpdatedTime(model.getUpdatedTime() != null ? Instant.ofEpochMilli(model.getUpdatedTime()) : null);
        entity.setGroupAdmins(model.getGroupAdmins());
        entity.setIsPersonalGroup(model.getIsPersonalGroup());
        return entity;
    }

    private List<UserGroup> groupEntitiesToModelList(List<UserGroupEntity> entities) {
        return entities.stream().map(this::groupEntityToModel).toList();
    }

    private GroupMember membershipToGroupMember(GroupMembershipEntity e) {
        GroupMember m = new GroupMember();
        m.setParentId(e.getGroupId());
        m.setChildId(e.getUserId());
        m.setDomainId(e.getDomainId());
        m.setChildType(GroupChildType.USER);
        m.setRole(e.getRole() != null ? GroupMemberRole.valueOf(e.getRole()) : GroupMemberRole.MEMBER);
        m.setCreatedTime(e.getCreatedAt() != null ? e.getCreatedAt().getTime() : null);
        return m;
    }

    private GroupMembershipEntity groupMemberToMembership(GroupMember m) {
        GroupMembershipEntity e = new GroupMembershipEntity();
        e.setDomainId(m.getDomainId());
        e.setGroupId(m.getParentId());
        e.setUserId(m.getChildId());
        e.setRole(m.getRole() != null ? m.getRole().name() : GroupMemberRole.MEMBER.name());
        return e;
    }

    // =========================================================================
    // Private helpers — entity search filtering
    // =========================================================================

    private boolean matchesFilters(SharingEntity entity, List<SearchCriteria> filters) {
        if (filters == null || filters.isEmpty()) return true;
        for (SearchCriteria criteria : filters) {
            if (criteria.getSearchField() == EntitySearchField.PERMISSION_TYPE_ID) continue;
            if (!matchesCriterion(entity, criteria)) return false;
        }
        return true;
    }

    private boolean matchesCriterion(SharingEntity entity, SearchCriteria criteria) {
        String value = criteria.getValue();
        if (value == null) return true;
        return switch (criteria.getSearchField()) {
            case NAME -> {
                String name = entity.getName();
                if (name == null) yield false;
                if (criteria.getSearchCondition() == SearchCondition.LIKE) {
                    yield name.toLowerCase().contains(value.toLowerCase());
                }
                yield name.equals(value);
            }
            case ENTITY_TYPE_ID -> value.equals(entity.getEntityTypeId());
            case FULL_TEXT -> {
                String ft = entity.getFullText();
                if (ft == null) yield false;
                yield ft.toLowerCase().contains(value.toLowerCase());
            }
            case DESCRIPTION -> {
                String desc = entity.getDescription();
                if (desc == null) yield false;
                if (criteria.getSearchCondition() == SearchCondition.LIKE) {
                    yield desc.toLowerCase().contains(value.toLowerCase());
                }
                yield desc.equals(value);
            }
            case OWNER_ID -> {
                String ownerId = entity.getOwnerId();
                if (ownerId == null) yield false;
                yield switch (criteria.getSearchCondition()) {
                    case NOT -> !value.equals(ownerId);
                    case LIKE -> ownerId.toLowerCase().contains(value.toLowerCase());
                    default -> value.equals(ownerId);
                };
            }
            case PARRENT_ENTITY_ID -> {
                String parentId = entity.getParentEntityId();
                if (parentId == null) yield value == null;
                yield value.equals(parentId);
            }
            case SHARED_COUNT -> {
                Long sc = entity.getSharedCount();
                if (sc == null) sc = 0L;
                long target = Long.parseLong(value);
                yield switch (criteria.getSearchCondition()) {
                    case GTE -> sc >= target;
                    case LTE -> sc <= target;
                    case EQUAL -> sc == target;
                    case NOT -> sc != target;
                    default -> true;
                };
            }
            default -> true;
        };
    }

    private SharingEntity mergeEntityFields(SharingEntity old, SharingEntity updated) {
        if (updated.getEntityId() != null) old.setEntityId(updated.getEntityId());
        if (updated.getDomainId() != null) old.setDomainId(updated.getDomainId());
        if (updated.getEntityTypeId() != null) old.setEntityTypeId(updated.getEntityTypeId());
        if (updated.getOwnerId() != null) old.setOwnerId(updated.getOwnerId());
        if (updated.getParentEntityId() != null) old.setParentEntityId(updated.getParentEntityId());
        if (updated.getName() != null) old.setName(updated.getName());
        if (updated.getDescription() != null) old.setDescription(updated.getDescription());
        if (updated.getBinaryData() != null) old.setBinaryData(updated.getBinaryData());
        if (updated.getFullText() != null) old.setFullText(updated.getFullText());
        if (updated.getSharedCount() != null) old.setSharedCount(updated.getSharedCount());
        if (updated.getOriginalEntityCreationTime() != null)
            old.setOriginalEntityCreationTime(updated.getOriginalEntityCreationTime());
        if (updated.getCreatedTime() != null) old.setCreatedTime(updated.getCreatedTime());
        if (updated.getUpdatedTime() != null) old.setUpdatedTime(updated.getUpdatedTime());
        return old;
    }

    // =========================================================================
    // Private helpers — reflection utility
    // =========================================================================

    private static HashMap<String, Object> fieldsToHT(Field[] fields, Object obj) {
        HashMap<String, Object> hashtable = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object retrievedObject = field.get(obj);
                if (retrievedObject != null) {
                    hashtable.put(field.getName(), field.get(obj));
                }
            } catch (IllegalAccessException e) {
                logger.debug("Could not access field: {}", field.getName(), e);
            }
        }
        return hashtable;
    }

    private boolean isUserBelongsToGroup(List<User> groupUser, String userId) {
        return groupUser.stream().anyMatch(user -> user.getUserId().equals(userId));
    }
}
