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
package org.apache.airavata.models.data.replica;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel}.
 */
public record DataReplicaLocationModel(
        String replicaId,
        String productUri,
        String replicaName,
        String replicaDescription,
        long creationTime,
        long lastModifiedTime,
        long validUntilTime,
        ReplicaLocationCategory replicaLocationCategory,
        ReplicaPersistentType replicaPersistentType,
        String storageResourceId,
        String filePath,
        Map<String, String> replicaMetadata) {

    public DataReplicaLocationModel {
        replicaMetadata = replicaMetadata == null ? Map.of() : Map.copyOf(replicaMetadata);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String replicaId = "";
        private String productUri = "";
        private String replicaName = "";
        private String replicaDescription = "";
        private long creationTime;
        private long lastModifiedTime;
        private long validUntilTime;
        private ReplicaLocationCategory replicaLocationCategory =
                ReplicaLocationCategory.REPLICA_LOCATION_CATEGORY_UNKNOWN;
        private ReplicaPersistentType replicaPersistentType = ReplicaPersistentType.REPLICA_PERSISTENT_TYPE_UNKNOWN;
        private String storageResourceId = "";
        private String filePath = "";
        private Map<String, String> replicaMetadata = new LinkedHashMap<>();

        private Builder() {}

        private Builder(DataReplicaLocationModel source) {
            this.replicaId = source.replicaId;
            this.productUri = source.productUri;
            this.replicaName = source.replicaName;
            this.replicaDescription = source.replicaDescription;
            this.creationTime = source.creationTime;
            this.lastModifiedTime = source.lastModifiedTime;
            this.validUntilTime = source.validUntilTime;
            this.replicaLocationCategory = source.replicaLocationCategory;
            this.replicaPersistentType = source.replicaPersistentType;
            this.storageResourceId = source.storageResourceId;
            this.filePath = source.filePath;
            this.replicaMetadata = new LinkedHashMap<>(source.replicaMetadata);
        }

        public Builder setReplicaId(String replicaId) {
            this.replicaId = replicaId;
            return this;
        }

        public Builder setProductUri(String productUri) {
            this.productUri = productUri;
            return this;
        }

        public Builder setReplicaName(String replicaName) {
            this.replicaName = replicaName;
            return this;
        }

        public Builder setReplicaDescription(String replicaDescription) {
            this.replicaDescription = replicaDescription;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setLastModifiedTime(long lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder setValidUntilTime(long validUntilTime) {
            this.validUntilTime = validUntilTime;
            return this;
        }

        public Builder setReplicaLocationCategory(ReplicaLocationCategory replicaLocationCategory) {
            this.replicaLocationCategory = replicaLocationCategory;
            return this;
        }

        public Builder setReplicaPersistentType(ReplicaPersistentType replicaPersistentType) {
            this.replicaPersistentType = replicaPersistentType;
            return this;
        }

        public Builder setStorageResourceId(String storageResourceId) {
            this.storageResourceId = storageResourceId;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder putReplicaMetadata(String key, String value) {
            this.replicaMetadata.put(key, value);
            return this;
        }

        public Builder putAllReplicaMetadata(Map<String, String> values) {
            this.replicaMetadata.putAll(values);
            return this;
        }

        public Builder clearReplicaMetadata() {
            this.replicaMetadata.clear();
            return this;
        }

        public DataReplicaLocationModel build() {
            return new DataReplicaLocationModel(
                    replicaId, productUri, replicaName, replicaDescription, creationTime, lastModifiedTime,
                    validUntilTime, replicaLocationCategory, replicaPersistentType, storageResourceId, filePath,
                    replicaMetadata);
        }
    }
}
