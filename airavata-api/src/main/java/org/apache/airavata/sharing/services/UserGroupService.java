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

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.entities.UserGroupEntity;
import org.apache.airavata.sharing.entities.UserGroupPK;
import org.apache.airavata.sharing.models.GroupCardinality;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.models.SharingType;
import org.apache.airavata.sharing.models.UserGroup;
import org.apache.airavata.sharing.repositories.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserGroupService {
    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private PermissionTypeService permissionTypeService;

    @Autowired
    private Mapper mapper;

    @Autowired
    @Qualifier("sharingRegistryEntityManager")
    private EntityManager entityManager;

    public UserGroup get(UserGroupPK pk) throws SharingRegistryException {
        UserGroupEntity entity = userGroupRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, UserGroup.class);
    }

    public UserGroup create(UserGroup userGroup) throws SharingRegistryException {
        return update(userGroup);
    }

    public UserGroup update(UserGroup userGroup) throws SharingRegistryException {
        UserGroupEntity entity = mapper.map(userGroup, UserGroupEntity.class);
        UserGroupEntity saved = userGroupRepository.save(entity);
        return mapper.map(saved, UserGroup.class);
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserGroupEntity> query = cb.createQuery(UserGroupEntity.class);
        Root<UserGroupEntity> root = query.from(UserGroupEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        if (filters != null) {
            for (String key : filters.keySet()) {
                String value = filters.get(key);
                predicates.add(cb.equal(root.get(key), value));
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

        List<UserGroupEntity> entities = typedQuery.getResultList();
        return entities.stream().map(e -> mapper.map(e, UserGroup.class)).toList();
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserGroupEntity> query = cb.createQuery(UserGroupEntity.class);
        Root<UserGroupEntity> groupRoot = query.from(UserGroupEntity.class);
        Root<SharingEntity> sharingRoot = query.from(SharingEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(groupRoot.get("groupId"), sharingRoot.get("groupId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), sharingRoot.get("domainId")));
        predicates.add(cb.equal(groupRoot.get("domainId"), domainId));
        predicates.add(cb.equal(sharingRoot.get("entityId"), entityId));
        predicates.add(cb.equal(sharingRoot.get("permissionTypeId"), permissionTypeId));
        predicates.add(cb.equal(groupRoot.get("groupCardinality"), GroupCardinality.MULTI_USER.toString()));

        if (!Arrays.asList(sharingTypes).isEmpty()) {
            List<String> sharingTypeNames =
                    Arrays.asList(sharingTypes).stream().map(SharingType::name).collect(Collectors.toList());
            predicates.add(sharingRoot.get("sharingType").in(sharingTypeNames));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(sharingRoot.get("createdTime")));

        List<UserGroupEntity> entities = entityManager.createQuery(query).getResultList();
        return entities.stream().map(e -> mapper.map(e, UserGroup.class)).toList();
    }

    public boolean isShared(String domainId, String entityId) throws SharingRegistryException {
        String ownerPermissionTypeId = permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserGroupEntity> groupRoot = query.from(UserGroupEntity.class);
        Root<SharingEntity> sharingRoot = query.from(SharingEntity.class);

        List<Predicate> predicates = new ArrayList<>();
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
