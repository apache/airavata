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
package org.apache.airavata.models.workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.workspace.proto.Project}.
 */
public record Project(
        String projectId,
        String owner,
        String gatewayId,
        String name,
        String description,
        long creationTime,
        List<String> sharedUsers,
        List<String> sharedGroups) {

    public Project {
        sharedUsers = sharedUsers == null ? List.of() : List.copyOf(sharedUsers);
        sharedGroups = sharedGroups == null ? List.of() : List.copyOf(sharedGroups);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String projectId = "";
        private String owner = "";
        private String gatewayId = "";
        private String name = "";
        private String description = "";
        private long creationTime;
        private List<String> sharedUsers = new ArrayList<>();
        private List<String> sharedGroups = new ArrayList<>();

        private Builder() {}

        private Builder(Project source) {
            this.projectId = source.projectId;
            this.owner = source.owner;
            this.gatewayId = source.gatewayId;
            this.name = source.name;
            this.description = source.description;
            this.creationTime = source.creationTime;
            this.sharedUsers = new ArrayList<>(source.sharedUsers);
            this.sharedGroups = new ArrayList<>(source.sharedGroups);
        }

        public Builder setProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
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

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder addSharedUsers(String value) {
            this.sharedUsers.add(value);
            return this;
        }

        public Builder addAllSharedUsers(Iterable<String> values) {
            values.forEach(this.sharedUsers::add);
            return this;
        }

        public Builder clearSharedUsers() {
            this.sharedUsers.clear();
            return this;
        }

        public Builder addSharedGroups(String value) {
            this.sharedGroups.add(value);
            return this;
        }

        public Builder addAllSharedGroups(Iterable<String> values) {
            values.forEach(this.sharedGroups::add);
            return this;
        }

        public Builder clearSharedGroups() {
            this.sharedGroups.clear();
            return this;
        }

        public Project build() {
            return new Project(
                    projectId, owner, gatewayId, name, description, creationTime, sharedUsers, sharedGroups);
        }
    }
}
