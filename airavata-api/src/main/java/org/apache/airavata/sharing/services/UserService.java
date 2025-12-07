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
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.entities.UserEntity;
import org.apache.airavata.sharing.entities.UserPK;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.models.SharingType;
import org.apache.airavata.sharing.models.User;
import org.apache.airavata.sharing.repositories.UserRepository;
import org.apache.airavata.sharing.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionTypeService permissionTypeService;

    @PersistenceContext(unitName = "airavata-sharing-registry")
    private EntityManager entityManager;

    public User get(UserPK pk) throws SharingRegistryException {
        UserEntity entity = userRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, User.class);
    }

    public User create(User user) throws SharingRegistryException {
        return update(user);
    }

    public User update(User user) throws SharingRegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UserEntity entity = mapper.map(user, UserEntity.class);
        UserEntity saved = userRepository.save(entity);
        return mapper.map(saved, User.class);
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);

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

        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        List<UserEntity> entities = typedQuery.getResultList();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, User.class)).toList();
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> userRoot = query.from(UserEntity.class);
        Root<SharingEntity> sharingRoot = query.from(SharingEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(userRoot.get("userId"), sharingRoot.get("groupId")));
        predicates.add(cb.equal(userRoot.get("domainId"), sharingRoot.get("domainId")));
        predicates.add(cb.equal(userRoot.get("domainId"), domainId));
        predicates.add(cb.equal(sharingRoot.get("entityId"), entityId));
        predicates.add(cb.equal(sharingRoot.get("permissionTypeId"), permissionTypeId));

        if (!Arrays.asList(sharingTypes).isEmpty()) {
            List<String> sharingTypeNames =
                    Arrays.asList(sharingTypes).stream().map(SharingType::name).collect(Collectors.toList());
            predicates.add(sharingRoot.get("sharingType").in(sharingTypeNames));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);
        query.orderBy(cb.desc(sharingRoot.get("createdTime")));

        List<UserEntity> entities = entityManager.createQuery(query).getResultList();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, User.class)).toList();
    }
}
