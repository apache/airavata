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
 * Client defined entity types.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.EntityType}.
 *
 * @param entityTypeId Entity type id provided by the client.
 * @param domainId Domain id of the domain.
 * @param name Name for the entity type. Should be a single word.
 * @param description Short description for the entity type.
 * @param createdTime Will be set by the system.
 * @param updatedTime Will be set by the system.
 */
public record EntityType(
        String entityTypeId, String domainId, String name, String description, long createdTime,
        long updatedTime) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String entityTypeId = "";
        private String domainId = "";
        private String name = "";
        private String description = "";
        private long createdTime;
        private long updatedTime;

        private Builder() {}

        private Builder(EntityType source) {
            this.entityTypeId = source.entityTypeId;
            this.domainId = source.domainId;
            this.name = source.name;
            this.description = source.description;
            this.createdTime = source.createdTime;
            this.updatedTime = source.updatedTime;
        }

        public Builder setEntityTypeId(String entityTypeId) {
            this.entityTypeId = entityTypeId;
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

        public Builder setCreatedTime(long createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder setUpdatedTime(long updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public EntityType build() {
            return new EntityType(entityTypeId, domainId, name, description, createdTime, updatedTime);
        }
    }
}
