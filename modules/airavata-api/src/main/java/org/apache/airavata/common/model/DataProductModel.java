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
package org.apache.airavata.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain model: DataProductModel.
 * Unified model for catalog datasets and data products with catalog metadata,
 * primary storage path, and optional replica locations.
 */
public class DataProductModel {
    private String productUri;
    private String gatewayId;
    private String parentProductUri;
    private String productName;
    private String productDescription;
    private String ownerName;
    private DataProductType dataProductType;
    private int productSize;
    private long creationTime;
    private long lastModifiedTime;
    private Map<String, String> productMetadata;
    private List<DataReplicaLocationModel> replicaLocations;

    private String primaryStorageResourceId;
    private String primaryFilePath;
    private String status;
    private String privacy;
    private String scope; // USER, GATEWAY (stored) or DELEGATED (inferred)
    private String ownerId;
    private String groupResourceProfileId;
    private String headerImage;
    private String format;
    private long updatedAt;
    private List<String> authors = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();

    public DataProductModel() {}

    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getParentProductUri() {
        return parentProductUri;
    }

    public void setParentProductUri(String parentProductUri) {
        this.parentProductUri = parentProductUri;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public DataProductType getDataProductType() {
        return dataProductType;
    }

    public void setDataProductType(DataProductType dataProductType) {
        this.dataProductType = dataProductType;
    }

    public int getProductSize() {
        return productSize;
    }

    public void setProductSize(int productSize) {
        this.productSize = productSize;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Map<String, String> getProductMetadata() {
        if (productMetadata == null) {
            productMetadata = new HashMap<>();
        }
        return productMetadata;
    }

    public void setProductMetadata(Map<String, String> productMetadata) {
        this.productMetadata = productMetadata;
    }

    public List<DataReplicaLocationModel> getReplicaLocations() {
        if (replicaLocations == null) {
            replicaLocations = new ArrayList<>();
        }
        return replicaLocations;
    }

    public void setReplicaLocations(List<DataReplicaLocationModel> replicaLocations) {
        this.replicaLocations = replicaLocations;
    }

    public String getPrimaryStorageResourceId() {
        return primaryStorageResourceId;
    }

    public void setPrimaryStorageResourceId(String primaryStorageResourceId) {
        this.primaryStorageResourceId = primaryStorageResourceId;
    }

    public String getPrimaryFilePath() {
        return primaryFilePath;
    }

    public void setPrimaryFilePath(String primaryFilePath) {
        this.primaryFilePath = primaryFilePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getAuthors() {
        if (authors == null) {
            authors = new ArrayList<>();
        }
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors != null ? authors : new ArrayList<>();
    }

    public List<Tag> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataProductModel that = (DataProductModel) o;
        return Objects.equals(productUri, that.productUri)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(parentProductUri, that.parentProductUri)
                && Objects.equals(productName, that.productName)
                && Objects.equals(productDescription, that.productDescription)
                && Objects.equals(ownerName, that.ownerName)
                && Objects.equals(dataProductType, that.dataProductType)
                && Objects.equals(productSize, that.productSize)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(lastModifiedTime, that.lastModifiedTime)
                && Objects.equals(productMetadata, that.productMetadata)
                && Objects.equals(replicaLocations, that.replicaLocations)
                && Objects.equals(primaryStorageResourceId, that.primaryStorageResourceId)
                && Objects.equals(primaryFilePath, that.primaryFilePath)
                && Objects.equals(status, that.status)
                && Objects.equals(privacy, that.privacy)
                && Objects.equals(scope, that.scope)
                && Objects.equals(ownerId, that.ownerId)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(headerImage, that.headerImage)
                && Objects.equals(format, that.format)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(authors, that.authors)
                && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                productUri,
                gatewayId,
                parentProductUri,
                productName,
                productDescription,
                ownerName,
                dataProductType,
                productSize,
                creationTime,
                lastModifiedTime,
                productMetadata,
                replicaLocations,
                primaryStorageResourceId,
                primaryFilePath,
                status,
                privacy,
                scope,
                ownerId,
                groupResourceProfileId,
                headerImage,
                format,
                updatedAt,
                authors,
                tags);
    }

    @Override
    public String toString() {
        return "DataProductModel{" + "productUri=" + productUri + ", gatewayId=" + gatewayId + ", parentProductUri="
                + parentProductUri + ", productName=" + productName + ", productDescription=" + productDescription
                + ", ownerName=" + ownerName + ", dataProductType=" + dataProductType + ", productSize=" + productSize
                + ", creationTime=" + creationTime + ", lastModifiedTime=" + lastModifiedTime + ", productMetadata="
                + productMetadata + ", replicaLocations=" + replicaLocations + ", primaryStorageResourceId="
                + primaryStorageResourceId + ", primaryFilePath=" + primaryFilePath + ", status=" + status
                + ", privacy=" + privacy + ", scope=" + scope + ", ownerId=" + ownerId + ", format=" + format + "}";
    }

    /**
     * Tag model for data product catalog metadata.
     */
    public static class Tag {
        private String id;
        private String name;
        private String color;

        public Tag() {}

        public Tag(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
