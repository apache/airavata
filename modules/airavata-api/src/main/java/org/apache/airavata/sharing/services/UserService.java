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
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.entities.UserEntity;
import org.apache.airavata.sharing.entities.UserPK;
import org.apache.airavata.sharing.mappers.UserMapper;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.SharingType;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("sharingUserService")
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PermissionTypeService permissionTypeService;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    public UserService(
            @Qualifier("sharingUserRepository") UserRepository userRepository,
            PermissionTypeService permissionTypeService,
            UserMapper userMapper,
            @Qualifier("sharingRegistryEntityManager") EntityManager entityManager) {
        this.userRepository = userRepository;
        this.permissionTypeService = permissionTypeService;
        this.userMapper = userMapper;
        this.entityManager = entityManager;
    }

    public User get(UserPK pk) throws SharingRegistryException {
        UserEntity entity = userRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return userMapper.toModel(entity);
    }

    public User create(User user) throws SharingRegistryException {
        return update(user);
    }

    public User update(User user) throws SharingRegistryException {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toModel(saved);
    }

    public boolean delete(UserPK pk) throws SharingRegistryException {
        userRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(UserPK pk) throws SharingRegistryException {
        return userRepository.existsById(pk);
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
                predicates.add(cb.equal(root.get(entry.getKey()), entry.getValue()));
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
        predicates.add(cb.equal(userRoot.get("userId"), sharingRoot.get("groupId")));
        predicates.add(cb.equal(userRoot.get("domainId"), sharingRoot.get("domainId")));
        predicates.add(cb.equal(userRoot.get("domainId"), domainId));
        predicates.add(cb.equal(sharingRoot.get("entityId"), entityId));
        predicates.add(cb.equal(sharingRoot.get("permissionTypeId"), permissionTypeId));

        if (!Arrays.asList(sharingTypes).isEmpty()) {
            List<String> sharingTypeNames =
                    Arrays.stream(sharingTypes).map(SharingType::name).toList();
            predicates.add(sharingRoot.get("sharingType").in(sharingTypeNames));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(sharingRoot.get("createdTime")));

        List<UserEntity> entities = entityManager.createQuery(query).getResultList();
        return userMapper.toModelList(entities);
    }
}
