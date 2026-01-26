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
package org.apache.airavata.sharing.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.airavata.registry.entities.UserEntity;
import org.apache.airavata.registry.repositories.UserRepository;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.mappers.UserMapper;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.SharingType;
import org.apache.airavata.sharing.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing users in the sharing registry.
 * Uses the unified OIDC-based UserEntity from registry.entities.
 *
 * <p>Note: In the unified model:
 * <ul>
 *   <li>sharing 'domainId' maps to 'gatewayId'</li>
 *   <li>sharing 'userId' maps to 'sub' (OIDC subject identifier)</li>
 * </ul>
 */
@Service("sharingUserService")
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PermissionTypeService permissionTypeService;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    public UserService(
            UserRepository userRepository,
            PermissionTypeService permissionTypeService,
            UserMapper userMapper,
            EntityManager entityManager) {
        this.userRepository = userRepository;
        this.permissionTypeService = permissionTypeService;
        this.userMapper = userMapper;
        this.entityManager = entityManager;
    }

    /**
     * Get a user by userId (sub) and domainId (gatewayId).
     *
     * @param userId the user identifier (maps to sub)
     * @param domainId the domain identifier (same as gatewayId)
     * @return the User model, or null if not found
     */
    public User get(String userId, String domainId) throws SharingRegistryException {
        var entity = userRepository.findByUserIdAndDomainId(userId, domainId).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    /**
     * Create a new user. This creates a new entity with @PrePersist setting timestamps.
     *
     * @param user the user model to create
     * @return the created User model
     */
    public User create(User user) throws SharingRegistryException {
        var entity = userMapper.toEntity(user);
        // Set the airavataInternalUserId if not set (userId maps to sub, domainId maps to gatewayId)
        if (entity.getAiravataInternalUserId() == null && entity.getSub() != null && entity.getGatewayId() != null) {
            entity.setAiravataInternalUserId(UserEntity.createInternalUserId(entity.getSub(), entity.getGatewayId()));
        }
        var saved = userRepository.save(entity);
        return userMapper.toModel(saved);
    }

    /**
     * Update an existing user. Fetches the existing entity and updates only the
     * fields from the User model, preserving database-managed fields like createdAt.
     *
     * @param user the user model with updated values
     * @return the updated User model
     */
    public User update(User user) throws SharingRegistryException {
        String airavataInternalUserId = UserEntity.createInternalUserId(user.getUserId(), user.getDomainId());
        
        // Fetch the existing entity to preserve NOT NULL fields
        var existingEntity = userRepository.findById(airavataInternalUserId).orElse(null);
        
        if (existingEntity != null) {
            // Update existing entity - preserves createdAt, etc.
            userMapper.updateEntityFromModel(user, existingEntity);
            var saved = userRepository.save(existingEntity);
            return userMapper.toModel(saved);
        } else {
            // New entity - delegate to create which allows @PrePersist to set timestamps
            return create(user);
        }
    }

    /**
     * Delete a user by userId (sub) and domainId (gatewayId).
     *
     * @param userId the user identifier (maps to sub)
     * @param domainId the domain identifier (same as gatewayId)
     * @return true if deleted
     */
    public boolean delete(String userId, String domainId) throws SharingRegistryException {
        String airavataInternalUserId = UserEntity.createInternalUserId(userId, domainId);
        userRepository.deleteById(airavataInternalUserId);
        return true;
    }

    /**
     * Check if a user exists by userId (sub) and domainId (gatewayId).
     *
     * @param userId the user identifier (maps to sub)
     * @param domainId the domain identifier (same as gatewayId)
     * @return true if exists
     */
    public boolean isExists(String userId, String domainId) throws SharingRegistryException {
        return userRepository.existsByUserIdAndDomainId(userId, domainId);
    }

    public List<User> select(String queryString, Map<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        // Build query with filters using Criteria API
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserEntity.class);
        var root = query.from(UserEntity.class);

        var predicates = new ArrayList<Predicate>();
        if (filters != null) {
            for (var entry : filters.entrySet()) {
                // Map sharing fields to OIDC-based entity fields
                String fieldName = switch (entry.getKey()) {
                    case "domainId" -> "gatewayId";
                    case "userId" -> "sub";
                    case "firstName" -> "givenName";
                    case "lastName" -> "familyName";
                    case "userName" -> "preferredUsername";
                    default -> entry.getKey();
                };
                predicates.add(cb.equal(root.get(fieldName), entry.getValue()));
            }
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        var entities = typedQuery.getResultList();
        return userMapper.toModelList(entities);
    }

    public List<User> getAccessibleUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        if (permissionTypeId.equals(permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId))) {
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

    public List<User> getDirectlyAccessibleUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return getAccessibleUsersInternal(
                domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
    }

    private List<User> getAccessibleUsersInternal(
            String domainId, String entityId, String permissionTypeId, SharingType... sharingTypes)
            throws SharingRegistryException {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserEntity.class);
        var userRoot = query.from(UserEntity.class);
        var sharingRoot = query.from(SharingEntity.class);

        var predicates = new ArrayList<Predicate>();
        // userId in sharing maps to sub in UserEntity
        predicates.add(cb.equal(userRoot.get("sub"), sharingRoot.get("groupId")));
        // domainId maps to gatewayId for unified entity
        predicates.add(cb.equal(userRoot.get("gatewayId"), sharingRoot.get("domainId")));
        predicates.add(cb.equal(userRoot.get("gatewayId"), domainId));
        predicates.add(cb.equal(sharingRoot.get("entityId"), entityId));
        predicates.add(cb.equal(sharingRoot.get("permissionTypeId"), permissionTypeId));

        if (sharingTypes.length > 0) {
            var sharingTypeNames =
                    Arrays.stream(sharingTypes).map(SharingType::name).toList();
            predicates.add(sharingRoot.get("sharingType").in(sharingTypeNames));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(sharingRoot.get("createdTime")));

        var entities = entityManager.createQuery(query).getResultList();
        return userMapper.toModelList(entities);
    }
}
