/**
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
 */
package org.apache.airavata.registry.core.replica.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "DATA_PRODUCT")
public class DataProduct {
    private final static Logger logger = LoggerFactory.getLogger(DataProduct.class);
    private String productUri;
    private String gatewayId;
    private String productName;
    private String productDescription;
    private String dataProductType;
    private String ownerName;
    private String parentProductUri;
    private int productSize;
    private Timestamp creationTime;
    private Timestamp lastModifiedTime;

    private DataProduct parentDataProduct;
    private Collection<DataReplicaLocation> dataReplicaLocations;
    private Collection<DataProductMetaData> dataProductMetaData;
    private Collection<DataProduct> childDataProducts;

    @Id
    @Column(name = "PRODUCT_URI")
    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "PRODUCT_NAME")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Column(name = "PRODUCT_DESCRIPTION")
    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Column(name = "OWNER_NAME")
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Column(name = "PARENT_PRODUCT_URI")
    public String getParentProductUri() {
        return parentProductUri;
    }

    public void setParentProductUri(String parentProductUri) {
        this.parentProductUri = parentProductUri;
    }

    @Column(name = "PRODUCT_TYPE")
    public String getDataProductType() {
        return dataProductType;
    }

    public void setDataProductType(String dataProductType) {
        this.dataProductType = dataProductType;
    }

    @Column(name = "PRODUCT_SIZE")
    public int getProductSize() {
        return productSize;
    }

    public void setProductSize(int productSize) {
        this.productSize = productSize;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_MODIFIED_TIME")
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @OneToMany(mappedBy = "dataProduct", cascade = {CascadeType.ALL})
    public Collection<DataReplicaLocation> getDataReplicaLocations() {
        return dataReplicaLocations;
    }

    public void setDataReplicaLocations(Collection<DataReplicaLocation> dataReplicaLocations) {
        this.dataReplicaLocations = dataReplicaLocations;
    }

    @OneToMany(mappedBy = "dataProduct", cascade = {CascadeType.ALL})
    public Collection<DataProductMetaData> getDataProductMetaData() {
        return dataProductMetaData;
    }

    public void setDataProductMetaData(Collection<DataProductMetaData> dataProductMetaData) {
        this.dataProductMetaData = dataProductMetaData;
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "PARENT_PRODUCT_URI", referencedColumnName = "PRODUCT_URI")
    public DataProduct getParentDataProduct() {
        return parentDataProduct;
    }

    public void setParentDataProduct(DataProduct parentDataProduct) {
        this.parentDataProduct = parentDataProduct;
    }

    @OneToMany(mappedBy = "parentDataProduct", cascade = {CascadeType.ALL})
    public Collection<DataProduct> getChildDataProducts() {
        return childDataProducts;
    }

    public void setChildDataProducts(Collection<DataProduct> childDataProducts) {
        this.childDataProducts = childDataProducts;
    }
}