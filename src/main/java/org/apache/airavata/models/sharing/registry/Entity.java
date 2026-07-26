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

import java.util.Arrays;
import java.util.Objects;

/**
 * Entity object which is used to register an entity in the system.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.Entity}.
 *
 * @param entityId Entity id provided by the client.
 * @param domainId Domain id.
 * @param entityTypeId Entity type id.
 * @param ownerId Owner id.
 * @param parentEntityId Parent entity id.
 * @param name Name.
 * @param description Short description for the entity.
 * @param binaryData Any information stored in binary format.
 * @param fullText A string which will be considered for full text search.
 * @param sharedCount Number of groups this entity is shared with.
 * @param originalEntityCreationTime When registering old records, what is the original entity
 *     creation time. If not set will default to current time.
 * @param createdTime Will be set by the system.
 * @param updatedTime Will be set by the system.
 */
public record Entity(
        String entityId,
        String domainId,
        String entityTypeId,
        String ownerId,
        String parentEntityId,
        String name,
        String description,
        byte[] binaryData,
        String fullText,
        long sharedCount,
        long originalEntityCreationTime,
        long createdTime,
        long updatedTime) {

    public Entity {
        binaryData = binaryData == null ? new byte[0] : binaryData.clone();
    }

    @Override
    public byte[] binaryData() {
        return binaryData.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entity other)) {
            return false;
        }
        return sharedCount == other.sharedCount
                && originalEntityCreationTime == other.originalEntityCreationTime
                && createdTime == other.createdTime
                && updatedTime == other.updatedTime
                && Objects.equals(entityId, other.entityId)
                && Objects.equals(domainId, other.domainId)
                && Objects.equals(entityTypeId, other.entityTypeId)
                && Objects.equals(ownerId, other.ownerId)
                && Objects.equals(parentEntityId, other.parentEntityId)
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Arrays.equals(binaryData, other.binaryData)
                && Objects.equals(fullText, other.fullText);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                entityId, domainId, entityTypeId, ownerId, parentEntityId, name, description, fullText,
                sharedCount, originalEntityCreationTime, createdTime, updatedTime);
        return 31 * result + Arrays.hashCode(binaryData);
    }

    @Override
    public String toString() {
        return "Entity[entityId=" + entityId + ", domainId=" + domainId + ", entityTypeId=" + entityTypeId
                + ", ownerId=" + ownerId + ", parentEntityId=" + parentEntityId + ", name=" + name
                + ", description=" + description + ", binaryData=byte[" + binaryData.length + "], fullText="
                + fullText + ", sharedCount=" + sharedCount + ", originalEntityCreationTime="
                + originalEntityCreationTime + ", createdTime=" + createdTime + ", updatedTime=" + updatedTime
                + "]";
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String entityId = "";
        private String domainId = "";
        private String entityTypeId = "";
        private String ownerId = "";
        private String parentEntityId = "";
        private String name = "";
        private String description = "";
        private byte[] binaryData = new byte[0];
        private String fullText = "";
        private long sharedCount;
        private long originalEntityCreationTime;
        private long createdTime;
        private long updatedTime;

        private Builder() {}

        private Builder(Entity source) {
            this.entityId = source.entityId;
            this.domainId = source.domainId;
            this.entityTypeId = source.entityTypeId;
            this.ownerId = source.ownerId;
            this.parentEntityId = source.parentEntityId;
            this.name = source.name;
            this.description = source.description;
            this.binaryData = source.binaryData();
            this.fullText = source.fullText;
            this.sharedCount = source.sharedCount;
            this.originalEntityCreationTime = source.originalEntityCreationTime;
            this.createdTime = source.createdTime;
            this.updatedTime = source.updatedTime;
        }

        public Builder setEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder setDomainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder setEntityTypeId(String entityTypeId) {
            this.entityTypeId = entityTypeId;
            return this;
        }

        public Builder setOwnerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder setParentEntityId(String parentEntityId) {
            this.parentEntityId = parentEntityId;
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

        public Builder setBinaryData(byte[] binaryData) {
            this.binaryData = binaryData == null ? new byte[0] : binaryData.clone();
            return this;
        }

        public Builder setFullText(String fullText) {
            this.fullText = fullText;
            return this;
        }

        public Builder setSharedCount(long sharedCount) {
            this.sharedCount = sharedCount;
            return this;
        }

        public Builder setOriginalEntityCreationTime(long originalEntityCreationTime) {
            this.originalEntityCreationTime = originalEntityCreationTime;
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

        public Entity build() {
            return new Entity(
                    entityId, domainId, entityTypeId, ownerId, parentEntityId, name, description, binaryData,
                    fullText, sharedCount, originalEntityCreationTime, createdTime, updatedTime);
        }
    }
}
