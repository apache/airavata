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
 * Admin user for a group. Admin will have access to add or remove users from the group.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.GroupAdmin}.
 */
public record GroupAdmin(String groupId, String domainId, String adminId) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String groupId = "";
        private String domainId = "";
        private String adminId = "";

        private Builder() {}

        private Builder(GroupAdmin source) {
            this.groupId = source.groupId;
            this.domainId = source.domainId;
            this.adminId = source.adminId;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setDomainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder setAdminId(String adminId) {
            this.adminId = adminId;
            return this;
        }

        public GroupAdmin build() {
            return new GroupAdmin(groupId, domainId, adminId);
        }
    }
}
