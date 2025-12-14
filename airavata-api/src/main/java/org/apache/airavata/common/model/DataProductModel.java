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
 * Domain model: DataProductModel
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
    private java.util.Map<java.lang.String, java.lang.String> productMetadata;
    private List<DataReplicaLocationModel> replicaLocations;

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
                && Objects.equals(replicaLocations, that.replicaLocations);
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
                replicaLocations);
    }

    @Override
    public String toString() {
        return "DataProductModel{" + "productUri=" + productUri + ", gatewayId=" + gatewayId + ", parentProductUri="
                + parentProductUri + ", productName=" + productName + ", productDescription=" + productDescription
                + ", ownerName=" + ownerName + ", dataProductType=" + dataProductType + ", productSize=" + productSize
                + ", creationTime=" + creationTime + ", lastModifiedTime=" + lastModifiedTime + ", productMetadata="
                + productMetadata + ", replicaLocations=" + replicaLocations + "}";
    }
}
