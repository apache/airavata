/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.replicacatalog;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "data_product", schema = "airavata_catalog", catalog = "")
public class DataProductEntity {
    private String productUri;
    private String gatewayId;
    private String productName;
    private String productDescription;
    private String ownerName;
    private String parentProductUri;
    private Integer productSize;
    private Timestamp creationTime;
    private Timestamp lastModifiedTime;

    @Id
    @Column(name = "PRODUCT_URI")
    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
    }

    @Basic
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Basic
    @Column(name = "PRODUCT_NAME")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Basic
    @Column(name = "PRODUCT_DESCRIPTION")
    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Basic
    @Column(name = "OWNER_NAME")
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Basic
    @Column(name = "PARENT_PRODUCT_URI")
    public String getParentProductUri() {
        return parentProductUri;
    }

    public void setParentProductUri(String parentProductUri) {
        this.parentProductUri = parentProductUri;
    }

    @Basic
    @Column(name = "PRODUCT_SIZE")
    public Integer getProductSize() {
        return productSize;
    }

    public void setProductSize(Integer productSize) {
        this.productSize = productSize;
    }

    @Basic
    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Basic
    @Column(name = "LAST_MODIFIED_TIME")
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataProductEntity that = (DataProductEntity) o;

        if (productUri != null ? !productUri.equals(that.productUri) : that.productUri != null) return false;
        if (gatewayId != null ? !gatewayId.equals(that.gatewayId) : that.gatewayId != null) return false;
        if (productName != null ? !productName.equals(that.productName) : that.productName != null) return false;
        if (productDescription != null ? !productDescription.equals(that.productDescription) : that.productDescription != null)
            return false;
        if (ownerName != null ? !ownerName.equals(that.ownerName) : that.ownerName != null) return false;
        if (parentProductUri != null ? !parentProductUri.equals(that.parentProductUri) : that.parentProductUri != null)
            return false;
        if (productSize != null ? !productSize.equals(that.productSize) : that.productSize != null) return false;
        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
        if (lastModifiedTime != null ? !lastModifiedTime.equals(that.lastModifiedTime) : that.lastModifiedTime != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = productUri != null ? productUri.hashCode() : 0;
        result = 31 * result + (gatewayId != null ? gatewayId.hashCode() : 0);
        result = 31 * result + (productName != null ? productName.hashCode() : 0);
        result = 31 * result + (productDescription != null ? productDescription.hashCode() : 0);
        result = 31 * result + (ownerName != null ? ownerName.hashCode() : 0);
        result = 31 * result + (parentProductUri != null ? parentProductUri.hashCode() : 0);
        result = 31 * result + (productSize != null ? productSize.hashCode() : 0);
        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
        result = 31 * result + (lastModifiedTime != null ? lastModifiedTime.hashCode() : 0);
        return result;
    }
}
