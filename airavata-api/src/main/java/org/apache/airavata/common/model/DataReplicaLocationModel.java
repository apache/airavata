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

import java.util.Objects;

/**
 * Domain model: DataReplicaLocationModel
 */
public class DataReplicaLocationModel {
    private String replicaId;
    private String productUri;
    private String replicaName;
    private String replicaDescription;
    private long creationTime;
    private long lastModifiedTime;
    private long validUntilTime;
    private ReplicaLocationCategory replicaLocationCategory;
    private ReplicaPersistentType replicaPersistentType;
    private String storageResourceId;
    private String filePath;
    private java.util.Map<java.lang.String, java.lang.String> replicaMetadata;

    public DataReplicaLocationModel() {}

    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
    }

    public String getReplicaName() {
        return replicaName;
    }

    public void setReplicaName(String replicaName) {
        this.replicaName = replicaName;
    }

    public String getReplicaDescription() {
        return replicaDescription;
    }

    public void setReplicaDescription(String replicaDescription) {
        this.replicaDescription = replicaDescription;
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

    public long getValidUntilTime() {
        return validUntilTime;
    }

    public void setValidUntilTime(long validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    public ReplicaLocationCategory getReplicaLocationCategory() {
        return replicaLocationCategory;
    }

    public void setReplicaLocationCategory(ReplicaLocationCategory replicaLocationCategory) {
        this.replicaLocationCategory = replicaLocationCategory;
    }

    public ReplicaPersistentType getReplicaPersistentType() {
        return replicaPersistentType;
    }

    public void setReplicaPersistentType(ReplicaPersistentType replicaPersistentType) {
        this.replicaPersistentType = replicaPersistentType;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public java.util.Map<java.lang.String, java.lang.String> getReplicaMetadata() {
        return replicaMetadata;
    }

    public void setReplicaMetadata(java.util.Map<java.lang.String, java.lang.String> replicaMetadata) {
        this.replicaMetadata = replicaMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataReplicaLocationModel that = (DataReplicaLocationModel) o;
        return Objects.equals(replicaId, that.replicaId)
                && Objects.equals(productUri, that.productUri)
                && Objects.equals(replicaName, that.replicaName)
                && Objects.equals(replicaDescription, that.replicaDescription)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(lastModifiedTime, that.lastModifiedTime)
                && Objects.equals(validUntilTime, that.validUntilTime)
                && Objects.equals(replicaLocationCategory, that.replicaLocationCategory)
                && Objects.equals(replicaPersistentType, that.replicaPersistentType)
                && Objects.equals(storageResourceId, that.storageResourceId)
                && Objects.equals(filePath, that.filePath)
                && Objects.equals(replicaMetadata, that.replicaMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                replicaId,
                productUri,
                replicaName,
                replicaDescription,
                creationTime,
                lastModifiedTime,
                validUntilTime,
                replicaLocationCategory,
                replicaPersistentType,
                storageResourceId,
                filePath,
                replicaMetadata);
    }

    @Override
    public String toString() {
        return "DataReplicaLocationModel{" + "replicaId=" + replicaId + ", productUri=" + productUri + ", replicaName="
                + replicaName + ", replicaDescription=" + replicaDescription + ", creationTime=" + creationTime
                + ", lastModifiedTime=" + lastModifiedTime + ", validUntilTime=" + validUntilTime
                + ", replicaLocationCategory=" + replicaLocationCategory + ", replicaPersistentType="
                + replicaPersistentType + ", storageResourceId=" + storageResourceId + ", filePath=" + filePath
                + ", replicaMetadata=" + replicaMetadata + "}";
    }
}
