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
package org.apache.airavata.models.sharing.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * User group is a collection of users.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.UserGroup}.
 *
 * @param groupId Group id provided by the client.
 * @param domainId Domain id for this user group.
 * @param name Name for the user group. Should be one word.
 * @param description Short description for the group.
 * @param ownerId Owner id of this group.
 * @param groupType Group type (DOMAIN_LEVEL_GROUP, USER_LEVEL_GROUP).
 * @param groupCardinality Group cardinality (SINGLE_USER, MULTI_USER).
 * @param createdTime Will be set by the system.
 * @param updatedTime Will be set by the system.
 * @param groupAdmins Admins for the group.
 */
public record UserGroup(
        String groupId,
        String domainId,
        String name,
        String description,
        String ownerId,
        GroupType groupType,
        GroupCardinality groupCardinality,
        long createdTime,
        long updatedTime,
        List<GroupAdmin> groupAdmins) {

    public UserGroup {
        groupAdmins = groupAdmins == null ? List.of() : List.copyOf(groupAdmins);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String groupId = "";
        private String domainId = "";
        private String name = "";
        private String description = "";
        private String ownerId = "";
        private GroupType groupType = GroupType.GROUP_TYPE_UNKNOWN;
        private GroupCardinality groupCardinality = GroupCardinality.GROUP_CARDINALITY_UNKNOWN;
        private long createdTime;
        private long updatedTime;
        private List<GroupAdmin> groupAdmins = new ArrayList<>();

        private Builder() {}

        private Builder(UserGroup source) {
            this.groupId = source.groupId;
            this.domainId = source.domainId;
            this.name = source.name;
            this.description = source.description;
            this.ownerId = source.ownerId;
            this.groupType = source.groupType;
            this.groupCardinality = source.groupCardinality;
            this.createdTime = source.createdTime;
            this.updatedTime = source.updatedTime;
            this.groupAdmins = new ArrayList<>(source.groupAdmins);
        }

        public Builder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setDomainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setOwnerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder setGroupType(GroupType groupType) {
            this.groupType = groupType;
            return this;
        }

        public Builder setGroupCardinality(GroupCardinality groupCardinality) {
            this.groupCardinality = groupCardinality;
            return this;
        }

        public Builder setCreatedTime(long createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder setUpdatedTime(long updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public Builder addGroupAdmins(GroupAdmin value) {
            this.groupAdmins.add(value);
            return this;
        }

        public Builder addAllGroupAdmins(Iterable<GroupAdmin> values) {
            values.forEach(this.groupAdmins::add);
            return this;
        }

        public Builder clearGroupAdmins() {
            this.groupAdmins.clear();
            return this;
        }

        public UserGroup build() {
            return new UserGroup(
                    groupId, domainId, name, description, ownerId, groupType, groupCardinality, createdTime,
                    updatedTime, groupAdmins);
        }
    }
}
