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
import org.apache.airavata.sharing.entities.UserGroupEntity;
import org.apache.airavata.sharing.entities.UserGroupPK;
import org.apache.airavata.sharing.mappers.UserGroupMapper;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.SharingType;
import org.apache.airavata.sharing.model.UserGroup;
import org.apache.airavata.sharing.repositories.UserGroupRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserGroupService {
    private final UserGroupRepository userGroupRepository;
    private final PermissionTypeService permissionTypeService;
    private final UserGroupMapper userGroupMapper;
    private final EntityManager entityManager;

    public UserGroupService(
            UserGroupRepository userGroupRepository,
            PermissionTypeService permissionTypeService,
            UserGroupMapper userGroupMapper,
            @Qualifier("sharingRegistryEntityManager") EntityManager entityManager) {
        this.userGroupRepository = userGroupRepository;
        this.permissionTypeService = permissionTypeService;
        this.userGroupMapper = userGroupMapper;
        this.entityManager = entityManager;
    }

    public UserGroup get(UserGroupPK pk) throws SharingRegistryException {
        UserGroupEntity entity = userGroupRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return userGroupMapper.toModel(entity);
    }

    public UserGroup create(UserGroup userGroup) throws SharingRegistryException {
        return update(userGroup);
    }

    public UserGroup update(UserGroup userGroup) throws SharingRegistryException {
        UserGroupEntity entity = userGroupMapper.toEntity(userGroup);
        UserGroupEntity saved = userGroupRepository.save(entity);
        return userGroupMapper.toModel(saved);
    }

    public boolean delete(UserGroupPK pk) throws SharingRegistryException {
        userGroupRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(UserGroupPK pk) throws SharingRegistryException {
        return userGroupRepository.existsById(pk);
    }

    public List<UserGroup> select(String queryString, Map<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        // Build query with filters using Criteria API
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
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        var entities = typedQuery.getResultList();
        return userGroupMapper.toModelList(entities);
    }

    public List<UserGroup> getAccessibleGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return getAccessibleGroupsInternal(domainId, entityId, permissionTypeId);
    }

    public List<UserGroup> getDirectlyAccessibleGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return getAccessibleGroupsInternal(
                domainId, entityId, permissionTypeId, SharingType.DIRECT_CASCADING, SharingType.DIRECT_NON_CASCADING);
    }

    private List<UserGroup> getAccessibleGroupsInternal(
            String domainId, String entityId, String permissionTypeId, SharingType... sharingTypes)
            throws SharingRegistryException {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(UserGroupEntity.class);
        var groupRoot = query.from(UserGroupEntity.class);
        var sharingRoot = query.from(SharingEntity.class);

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(groupRoot.get("groupId"), sharingRoot.get("groupId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), sharingRoot.get("domainId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), domainId));
        predicates.add(cb.equal(sharingRoot.get("entityId"), entityId));
        predicates.add(cb.equal(sharingRoot.get("permissionTypeId"), permissionTypeId));
        predicates.add(cb.equal(groupRoot.get("groupCardinality"), GroupCardinality.MULTI_USER.toString()));

        if (sharingTypes.length > 0) {
            List<String> sharingTypeNames =
                    Arrays.stream(sharingTypes).map(SharingType::name).toList();
            predicates.add(sharingRoot.get("sharingType").in(sharingTypeNames));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(sharingRoot.get("createdTime")));

        List<UserGroupEntity> entities = entityManager.createQuery(query).getResultList();
        return userGroupMapper.toModelList(entities);
    }

    public boolean isShared(String domainId, String entityId) throws SharingRegistryException {
        String ownerPermissionTypeId = permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId);
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var groupRoot = query.from(UserGroupEntity.class);
        var sharingRoot = query.from(SharingEntity.class);

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(groupRoot.get("groupId"), sharingRoot.get("groupId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), sharingRoot.get("domainId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), domainId));
        predicates.add(cb.equal(sharingRoot.get("entityId"), entityId));
        predicates.add(cb.notEqual(sharingRoot.get("permissionTypeId"), ownerPermissionTypeId));

        query.select(cb.count(groupRoot));
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Long count = entityManager.createQuery(query).getSingleResult();
        return count > 0;
    }
}
