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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.data.replica.proto.DataProductModel}.
 */
public record DataProductModel(
        String productUri,
        String gatewayId,
        String parentProductUri,
        String productName,
        String productDescription,
        String ownerName,
        DataProductType dataProductType,
        int productSize,
        long creationTime,
        long lastModifiedTime,
        Map<String, String> productMetadata,
        List<DataReplicaLocationModel> replicaLocations) {

    public DataProductModel {
        productMetadata = productMetadata == null ? Map.of() : Map.copyOf(productMetadata);
        replicaLocations = replicaLocations == null ? List.of() : List.copyOf(replicaLocations);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String productUri = "";
        private String gatewayId = "";
        private String parentProductUri = "";
        private String productName = "";
        private String productDescription = "";
        private String ownerName = "";
        private DataProductType dataProductType = DataProductType.DATA_PRODUCT_TYPE_UNKNOWN;
        private int productSize;
        private long creationTime;
        private long lastModifiedTime;
        private Map<String, String> productMetadata = new LinkedHashMap<>();
        private List<DataReplicaLocationModel> replicaLocations = new ArrayList<>();

        private Builder() {}

        private Builder(DataProductModel source) {
            this.productUri = source.productUri;
            this.gatewayId = source.gatewayId;
            this.parentProductUri = source.parentProductUri;
            this.productName = source.productName;
            this.productDescription = source.productDescription;
            this.ownerName = source.ownerName;
            this.dataProductType = source.dataProductType;
            this.productSize = source.productSize;
            this.creationTime = source.creationTime;
            this.lastModifiedTime = source.lastModifiedTime;
            this.productMetadata = new LinkedHashMap<>(source.productMetadata);
            this.replicaLocations = new ArrayList<>(source.replicaLocations);
        }

        public Builder setProductUri(String productUri) {
            this.productUri = productUri;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setParentProductUri(String parentProductUri) {
            this.parentProductUri = parentProductUri;
            return this;
        }

        public Builder setProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder setProductDescription(String productDescription) {
            this.productDescription = productDescription;
            return this;
        }

        public Builder setOwnerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder setDataProductType(DataProductType dataProductType) {
            this.dataProductType = dataProductType;
            return this;
        }

        public Builder setProductSize(int productSize) {
            this.productSize = productSize;
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

        public Builder putProductMetadata(String key, String value) {
            this.productMetadata.put(key, value);
            return this;
        }

        public Builder putAllProductMetadata(Map<String, String> values) {
            this.productMetadata.putAll(values);
            return this;
        }

        public Builder clearProductMetadata() {
            this.productMetadata.clear();
            return this;
        }

        public Builder addReplicaLocations(DataReplicaLocationModel value) {
            this.replicaLocations.add(value);
            return this;
        }

        public Builder addAllReplicaLocations(Iterable<DataReplicaLocationModel> values) {
            values.forEach(this.replicaLocations::add);
            return this;
        }

        public Builder clearReplicaLocations() {
            this.replicaLocations.clear();
            return this;
        }

        public DataProductModel build() {
            return new DataProductModel(
                    productUri, gatewayId, parentProductUri, productName, productDescription, ownerName,
                    dataProductType, productSize, creationTime, lastModifiedTime, productMetadata,
                    replicaLocations);
        }
    }
}
