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
package org.apache.airavata.models.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.group.proto.GroupModel}.
 */
public record GroupModel(
        String id, String name, String ownerId, String description, List<String> members, List<String> admins) {

    public GroupModel {
        members = members == null ? List.of() : List.copyOf(members);
        admins = admins == null ? List.of() : List.copyOf(admins);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String name = "";
        private String ownerId = "";
        private String description = "";
        private List<String> members = new ArrayList<>();
        private List<String> admins = new ArrayList<>();

        private Builder() {}

        private Builder(GroupModel source) {
            this.id = source.id;
            this.name = source.name;
            this.ownerId = source.ownerId;
            this.description = source.description;
            this.members = new ArrayList<>(source.members);
            this.admins = new ArrayList<>(source.admins);
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setOwnerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addMembers(String value) {
            this.members.add(value);
            return this;
        }

        public Builder addAllMembers(Iterable<String> values) {
            values.forEach(this.members::add);
            return this;
        }

        public Builder clearMembers() {
            this.members.clear();
            return this;
        }

        public Builder addAdmins(String value) {
            this.admins.add(value);
            return this;
        }

        public Builder addAllAdmins(Iterable<String> values) {
            values.forEach(this.admins::add);
            return this;
        }

        public Builder clearAdmins() {
            this.admins.clear();
            return this;
        }

        public GroupModel build() {
            return new GroupModel(id, name, ownerId, description, members, admins);
        }
    }
}
