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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.sharing.entities.GroupMembershipEntity;
import org.apache.airavata.sharing.entities.GroupMembershipPK;
import org.apache.airavata.sharing.entities.UserEntity;
import org.apache.airavata.sharing.entities.UserGroupEntity;
import org.apache.airavata.sharing.models.GroupChildType;
import org.apache.airavata.sharing.models.GroupMembership;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.models.User;
import org.apache.airavata.sharing.models.UserGroup;
import org.apache.airavata.sharing.repositories.GroupMembershipRepository;
import org.apache.airavata.sharing.utils.DBConstants;
import org.apache.airavata.sharing.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupMembershipService {
    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserGroupService userGroupService;

    @PersistenceContext(unitName = "airavata-sharing-registry")
    private EntityManager entityManager;

    public GroupMembership get(GroupMembershipPK pk) throws SharingRegistryException {
        GroupMembershipEntity entity = groupMembershipRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, GroupMembership.class);
    }

    public GroupMembership create(GroupMembership groupMembership) throws SharingRegistryException {
        return update(groupMembership);
    }

    public GroupMembership update(GroupMembership groupMembership) throws SharingRegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GroupMembershipEntity entity = mapper.map(groupMembership, GroupMembershipEntity.class);
        GroupMembershipEntity saved = groupMembershipRepository.save(entity);
        return mapper.map(saved, GroupMembership.class);
    }

    public boolean delete(GroupMembershipPK pk) throws SharingRegistryException {
        groupMembershipRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(GroupMembershipPK pk) throws SharingRegistryException {
        return groupMembershipRepository.existsById(pk);
    }

    public List<GroupMembership> select(Map<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupMembershipEntity> query = cb.createQuery(GroupMembershipEntity.class);
        Root<GroupMembershipEntity> root = query.from(GroupMembershipEntity.class);

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

        TypedQuery<GroupMembershipEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        List<GroupMembershipEntity> entities = typedQuery.getResultList();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, GroupMembership.class)).toList();
    }

    public List<User> getAllChildUsers(String domainId, String groupId) throws SharingRegistryException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> userRoot = query.from(UserEntity.class);
        Root<GroupMembershipEntity> membershipRoot = query.from(GroupMembershipEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(membershipRoot.get("childId"), userRoot.get("userId")));
        predicates.add(cb.equal(membershipRoot.get("domainId"), userRoot.get("domainId")));
        predicates.add(cb.equal(membershipRoot.get("domainId"), domainId));
        predicates.add(cb.equal(membershipRoot.get("parentId"), groupId));
        predicates.add(cb.equal(membershipRoot.get("childType"), GroupChildType.USER.toString()));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);

        List<UserEntity> entities = entityManager.createQuery(query).getResultList();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, User.class)).toList();
    }

    public List<UserGroup> getAllChildGroups(String domainId, String groupId) throws SharingRegistryException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserGroupEntity> query = cb.createQuery(UserGroupEntity.class);
        Root<UserGroupEntity> groupRoot = query.from(UserGroupEntity.class);
        Root<GroupMembershipEntity> membershipRoot = query.from(GroupMembershipEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(membershipRoot.get("childId"), groupRoot.get("groupId")));
        predicates.add(cb.equal(membershipRoot.get("domainId"), groupRoot.get("domainId")));
        predicates.add(cb.equal(membershipRoot.get("domainId"), domainId));
        predicates.add(cb.equal(membershipRoot.get("parentId"), groupId));
        predicates.add(cb.equal(membershipRoot.get("childType"), GroupChildType.GROUP.toString()));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);

        List<UserGroupEntity> entities = entityManager.createQuery(query).getResultList();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, UserGroup.class)).toList();
    }

    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserGroupEntity> query = cb.createQuery(UserGroupEntity.class);
        Root<UserGroupEntity> groupRoot = query.from(UserGroupEntity.class);
        Root<GroupMembershipEntity> membershipRoot = query.from(GroupMembershipEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(membershipRoot.get("parentId"), groupRoot.get("groupId")));
        predicates.add(cb.equal(membershipRoot.get("domainId"), groupRoot.get("domainId")));
        predicates.add(cb.equal(membershipRoot.get("domainId"), domainId));
        predicates.add(cb.equal(membershipRoot.get("childId"), userId));
        predicates.add(cb.equal(membershipRoot.get("childType"), GroupChildType.USER.toString()));
        predicates.add(cb.equal(groupRoot.get("groupCardinality"), "MULTI_USER"));

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.distinct(true);

        List<UserGroupEntity> entities = entityManager.createQuery(query).getResultList();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, UserGroup.class)).toList();
    }

    public List<GroupMembership> getAllParentMembershipsForChild(String domainId, String childId)
            throws SharingRegistryException {
        List<GroupMembership> finalParentGroups = new ArrayList<>();
        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.GroupMembershipTable.CHILD_ID, childId);
        filters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
        LinkedList<GroupMembership> temp = new LinkedList<>();
        select(filters, 0, -1).stream().forEach(m -> temp.addLast(m));
        while (temp.size() > 0) {
            GroupMembership gm = temp.pop();
            filters = new HashMap<>();
            filters.put(DBConstants.GroupMembershipTable.CHILD_ID, gm.getParentId());
            filters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
            select(filters, 0, -1).stream().forEach(m -> temp.addLast(m));
            finalParentGroups.add(gm);
        }
        return finalParentGroups;
    }
}
