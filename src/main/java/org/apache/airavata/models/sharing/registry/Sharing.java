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
 * Internal data type for managing sharings.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.Sharing}.
 */
public record Sharing(
        String permissionTypeId, String entityId, String groupId, SharingType sharingType, String domainId,
        String inheritedParentId, long createdTime, long updatedTime) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String permissionTypeId = "";
        private String entityId = "";
        private String groupId = "";
        private SharingType sharingType = SharingType.SHARING_TYPE_UNKNOWN;
        private String domainId = "";
        private String inheritedParentId = "";
        private long createdTime;
        private long updatedTime;

        private Builder() {}

        private Builder(Sharing source) {
            this.permissionTypeId = source.permissionTypeId;
            this.entityId = source.entityId;
            this.groupId = source.groupId;
            this.sharingType = source.sharingType;
            this.domainId = source.domainId;
            this.inheritedParentId = source.inheritedParentId;
            this.createdTime = source.createdTime;
            this.updatedTime = source.updatedTime;
        }

        public Builder setPermissionTypeId(String permissionTypeId) {
            this.permissionTypeId = permissionTypeId;
            return this;
        }

        public Builder setEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setSharingType(SharingType sharingType) {
            this.sharingType = sharingType;
            return this;
        }

        public Builder setDomainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder setInheritedParentId(String inheritedParentId) {
            this.inheritedParentId = inheritedParentId;
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

        public Sharing build() {
            return new Sharing(
                    permissionTypeId, entityId, groupId, sharingType, domainId, inheritedParentId, createdTime,
                    updatedTime);
        }
    }
}
