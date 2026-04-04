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
package org.apache.airavata.sharing.service;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.interfaces.SharingProvider;
import org.apache.airavata.sharing.model.UserEntity;
import org.apache.airavata.sharing.model.UserGroupEntity;
import org.apache.airavata.sharing.registry.models.proto.GroupCardinality;
import org.apache.airavata.sharing.registry.models.proto.GroupType;
import org.apache.airavata.sharing.registry.models.proto.UserGroup;

/**
 * Spring-managed implementation of {@link SharingProvider} that
 * delegates to {@link SharingService}, converting between proto
 * types and JPA entities.
 */
@org.springframework.stereotype.Component
public class SharingProviderImpl implements SharingProvider {

    private SharingService getHandler() throws Exception {
        return new SharingService();
    }

    @Override
    public boolean isUserExists(String domainId, String userId) throws Exception {
        return getHandler().isUserExists(domainId, userId);
    }

    @Override
    public String createUser(String userId, String domainId, String userName) throws Exception {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setDomainId(domainId);
        user.setCreatedTime(System.currentTimeMillis());
        user.setUpdatedTime(System.currentTimeMillis());
        user.setUserName(userName);
        return getHandler().createUser(user);
    }

    @Override
    public String createGroup(UserGroup group) throws Exception {
        UserGroupEntity entity = new UserGroupEntity();
        entity.setGroupId(group.getGroupId());
        entity.setDomainId(group.getDomainId());
        entity.setGroupCardinality(group.getGroupCardinality().name());
        entity.setCreatedTime(group.getCreatedTime());
        entity.setUpdatedTime(group.getUpdatedTime());
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        entity.setOwnerId(group.getOwnerId());
        entity.setGroupType(group.getGroupType().name());
        getHandler().createGroup(entity);
        return entity.getGroupId();
    }

    @Override
    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws Exception {
        List<UserGroupEntity> entities = getHandler().getAllMemberGroupsForUser(domainId, userId);
        return entities.stream().map(this::toProto).collect(Collectors.toList());
    }

    private UserGroup toProto(UserGroupEntity entity) {
        UserGroup.Builder builder = UserGroup.newBuilder()
                .setGroupId(entity.getGroupId())
                .setDomainId(entity.getDomainId())
                .setName(entity.getName() != null ? entity.getName() : "")
                .setOwnerId(entity.getOwnerId() != null ? entity.getOwnerId() : "");
        if (entity.getDescription() != null) {
            builder.setDescription(entity.getDescription());
        }
        if (entity.getGroupType() != null) {
            try {
                builder.setGroupType(GroupType.valueOf(entity.getGroupType()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getGroupCardinality() != null) {
            try {
                builder.setGroupCardinality(GroupCardinality.valueOf(entity.getGroupCardinality()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getCreatedTime() != null) {
            builder.setCreatedTime(entity.getCreatedTime());
        }
        if (entity.getUpdatedTime() != null) {
            builder.setUpdatedTime(entity.getUpdatedTime());
        }
        return builder.build();
    }
}
