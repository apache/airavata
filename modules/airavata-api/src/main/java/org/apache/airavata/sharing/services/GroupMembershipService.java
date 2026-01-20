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
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.sharing.entities.GroupMembershipEntity;
import org.apache.airavata.sharing.entities.GroupMembershipPK;
import org.apache.airavata.sharing.entities.UserEntity;
import org.apache.airavata.sharing.entities.UserGroupEntity;
import org.apache.airavata.sharing.mappers.GroupMembershipMapper;
import org.apache.airavata.sharing.mappers.UserGroupMapper;
import org.apache.airavata.sharing.mappers.UserMapper;
import org.apache.airavata.sharing.model.GroupChildType;
import org.apache.airavata.sharing.model.GroupMembership;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.model.UserGroup;
import org.apache.airavata.sharing.repositories.GroupMembershipRepository;
import org.apache.airavata.sharing.utils.DBConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupMembershipService {
    private final GroupMembershipRepository groupMembershipRepository;
    private final UserService userService;
    private final UserGroupService userGroupService;
    private final GroupMembershipMapper groupMembershipMapper;
    private final UserMapper userMapper;
    private final UserGroupMapper userGroupMapper;
    private final EntityManager entityManager;

    public GroupMembershipService(
            GroupMembershipRepository groupMembershipRepository,
            @Qualifier("sharingUserService") UserService userService,
            UserGroupService userGroupService,
            GroupMembershipMapper groupMembershipMapper,
            UserMapper userMapper,
            UserGroupMapper userGroupMapper,
            EntityManager entityManager) {
        this.groupMembershipRepository = groupMembershipRepository;
        this.userService = userService;
        this.userGroupService = userGroupService;
        this.groupMembershipMapper = groupMembershipMapper;
        this.userMapper = userMapper;
        this.userGroupMapper = userGroupMapper;
        this.entityManager = entityManager;
    }

    public GroupMembership get(GroupMembershipPK pk) throws SharingRegistryException {
        GroupMembershipEntity entity = groupMembershipRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return groupMembershipMapper.toModel(entity);
    }

    public GroupMembership create(GroupMembership groupMembership) throws SharingRegistryException {
        return update(groupMembership);
    }

    public GroupMembership update(GroupMembership groupMembership) throws SharingRegistryException {
        GroupMembershipEntity entity = groupMembershipMapper.toEntity(groupMembership);
        GroupMembershipEntity saved = groupMembershipRepository.save(entity);
        return groupMembershipMapper.toModel(saved);
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
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(GroupMembershipEntity.class);
        var root = query.from(GroupMembershipEntity.class);

        var predicates = new ArrayList<Predicate>();
        if (filters != null) {
            for (var entry : filters.entrySet()) {
                predicates.add(cb.equal(root.get(entry.getKey()), entry.getValue()));
            }
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        var typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        var entities = typedQuery.getResultList();
        return groupMembershipMapper.toModelList(entities);
    }

    public List<User> getAllChildUsers(String domainId, String groupId) throws SharingRegistryException {
        var cb = entityManager.getCriteriaBuilder();
        // Query GroupMembership first to get child user IDs
        var membershipQuery = cb.createQuery(GroupMembershipEntity.class);
        var membershipQueryRoot = membershipQuery.from(GroupMembershipEntity.class);
        var membershipPredicates = new ArrayList<Predicate>();
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("domainId"), domainId));
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("parentId"), groupId));
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("childType"), GroupChildType.USER.toString()));
        membershipQuery.where(cb.and(membershipPredicates.toArray(new Predicate[0])));

        List<GroupMembershipEntity> memberships =
                entityManager.createQuery(membershipQuery).getResultList();
        List<String> userIds =
                memberships.stream().map(m -> m.getChildId()).distinct().toList();

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Then query User entities for those IDs
        var userQuery = cb.createQuery(UserEntity.class);
        var userQueryRoot = userQuery.from(UserEntity.class);
        var userPredicates = new ArrayList<Predicate>();
        userPredicates.add(cb.equal(userQueryRoot.get("domainId"), domainId));
        userPredicates.add(userQueryRoot.get("userId").in(userIds));
        userQuery.where(cb.and(userPredicates.toArray(new Predicate[0])));

        List<UserEntity> entities = entityManager.createQuery(userQuery).getResultList();
        return userMapper.toModelList(entities);
    }

    public List<UserGroup> getAllChildGroups(String domainId, String groupId) throws SharingRegistryException {
        var cb = entityManager.getCriteriaBuilder();
        // Query GroupMembership first to get child group IDs
        var membershipQuery = cb.createQuery(GroupMembershipEntity.class);
        var membershipQueryRoot = membershipQuery.from(GroupMembershipEntity.class);
        var membershipPredicates = new ArrayList<Predicate>();
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("domainId"), domainId));
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("parentId"), groupId));
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("childType"), GroupChildType.GROUP.toString()));
        membershipQuery.where(cb.and(membershipPredicates.toArray(new Predicate[0])));

        List<GroupMembershipEntity> memberships =
                entityManager.createQuery(membershipQuery).getResultList();
        List<String> groupIds =
                memberships.stream().map(m -> m.getChildId()).distinct().toList();

        if (groupIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Then query UserGroup entities for those IDs
        var groupQuery = cb.createQuery(UserGroupEntity.class);
        var groupQueryRoot = groupQuery.from(UserGroupEntity.class);
        var groupPredicates = new ArrayList<Predicate>();
        groupPredicates.add(cb.equal(groupQueryRoot.get("domainId"), domainId));
        groupPredicates.add(groupQueryRoot.get("groupId").in(groupIds));
        groupQuery.where(cb.and(groupPredicates.toArray(new Predicate[0])));

        List<UserGroupEntity> entities = entityManager.createQuery(groupQuery).getResultList();
        return userGroupMapper.toModelList(entities);
    }

    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException {
        var cb = entityManager.getCriteriaBuilder();
        // Query GroupMembership first to get parent group IDs
        var membershipQuery = cb.createQuery(GroupMembershipEntity.class);
        var membershipQueryRoot = membershipQuery.from(GroupMembershipEntity.class);
        var membershipPredicates = new ArrayList<Predicate>();
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("domainId"), domainId));
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("childId"), userId));
        membershipPredicates.add(cb.equal(membershipQueryRoot.get("childType"), GroupChildType.USER.toString()));
        membershipQuery.where(cb.and(membershipPredicates.toArray(new Predicate[0])));

        List<GroupMembershipEntity> memberships =
                entityManager.createQuery(membershipQuery).getResultList();
        List<String> groupIds =
                memberships.stream().map(m -> m.getParentId()).distinct().toList();

        if (groupIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Then query UserGroup entities for those IDs
        var groupQuery = cb.createQuery(UserGroupEntity.class);
        var groupQueryRoot = groupQuery.from(UserGroupEntity.class);
        var groupPredicates = new ArrayList<Predicate>();
        groupPredicates.add(cb.equal(groupQueryRoot.get("domainId"), domainId));
        groupPredicates.add(groupQueryRoot.get("groupId").in(groupIds));
        groupPredicates.add(cb.equal(groupQueryRoot.get("groupCardinality"), "MULTI_USER"));
        groupQuery.where(cb.and(groupPredicates.toArray(new Predicate[0])));

        List<UserGroupEntity> entities = entityManager.createQuery(groupQuery).getResultList();
        return userGroupMapper.toModelList(entities);
    }

    public List<GroupMembership> getAllParentMembershipsForChild(String domainId, String childId)
            throws SharingRegistryException {
        List<GroupMembership> finalParentGroups = new ArrayList<>();
        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.GroupMembershipTable.CHILD_ID, childId);
        filters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
        LinkedList<GroupMembership> temp = new LinkedList<>(select(filters, 0, -1));
        while (!temp.isEmpty()) {
            GroupMembership gm = temp.pop();
            filters = new HashMap<>();
            filters.put(DBConstants.GroupMembershipTable.CHILD_ID, gm.getParentId());
            filters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
            temp.addAll(select(filters, 0, -1));
            finalParentGroups.add(gm);
        }
        return finalParentGroups;
    }
}
