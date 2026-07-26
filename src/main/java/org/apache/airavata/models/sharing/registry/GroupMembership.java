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

/**
 * System internal data type to map group memberships.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.GroupMembership}.
 */
public record GroupMembership(
        String parentId, String childId, String domainId, GroupChildType childType, long createdTime,
        long updatedTime) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String parentId = "";
        private String childId = "";
        private String domainId = "";
        private GroupChildType childType = GroupChildType.GROUP_CHILD_TYPE_UNKNOWN;
        private long createdTime;
        private long updatedTime;

        private Builder() {}

        private Builder(GroupMembership source) {
            this.parentId = source.parentId;
            this.childId = source.childId;
            this.domainId = source.domainId;
            this.childType = source.childType;
            this.createdTime = source.createdTime;
            this.updatedTime = source.updatedTime;
        }

        public Builder setParentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder setChildId(String childId) {
            this.childId = childId;
            return this;
        }

        public Builder setDomainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder setChildType(GroupChildType childType) {
            this.childType = childType;
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

        public GroupMembership build() {
            return new GroupMembership(parentId, childId, domainId, childType, createdTime, updatedTime);
        }
    }
}
