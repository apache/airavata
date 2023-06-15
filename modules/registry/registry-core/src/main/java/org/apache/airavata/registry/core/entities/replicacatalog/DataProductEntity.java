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

import org.apache.airavata.model.data.replica.DataProductType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * The persistent class for the data_product database table.
 */
@Entity
@Table(name = "DATA_PRODUCT")
public class DataProductEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_URI")
    private String productUri;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "PRODUCT_DESCRIPTION")
    private String productDescription;

    @Column(name = "OWNER_NAME")
    private String ownerName;

    @Column(name = "PARENT_PRODUCT_URI")
    private String parentProductUri;

    @Column(name = "PRODUCT_SIZE")
    private int productSize;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "LAST_MODIFIED_TIME")
    private Timestamp lastModifiedTime;

    @Column(name = "PRODUCT_TYPE")
    @Enumerated(EnumType.STRING)
    private DataProductType dataProductType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="DATA_PRODUCT_METADATA", joinColumns = @JoinColumn(name="PRODUCT_URI"))
    @MapKeyColumn(name = "METADATA_KEY")
    @Column(name = "METADATA_VALUE")
    private Map<String, String> productMetadata;

    @OneToMany(targetEntity = DataReplicaLocationEntity.class, cascade = CascadeType.ALL,
            mappedBy = "dataProduct", fetch = FetchType.EAGER)
    private List<DataReplicaLocationEntity> replicaLocations;

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

    public String getParentProductUri() {
        return parentProductUri;
    }

    public void setParentProductUri(String parentProductUri) {
        this.parentProductUri = parentProductUri;
    }

    public int getProductSize() {
        return productSize;
    }

    public void setProductSize(int productSize) {
        this.productSize = productSize;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public DataProductType getDataProductType() {
        return dataProductType;
    }

    public void setDataProductType(DataProductType dataProductType) {
        this.dataProductType = dataProductType;
    }

    public Map<String, String> getProductMetadata() { return productMetadata; }

    public void setProductMetadata(Map<String, String> productMetadata) { this.productMetadata = productMetadata; }

    public List<DataReplicaLocationEntity> getReplicaLocations() {
        return replicaLocations;
    }

    public void setReplicaLocations(List<DataReplicaLocationEntity> replicaLocations) {
        this.replicaLocations = replicaLocations;
    }

}
