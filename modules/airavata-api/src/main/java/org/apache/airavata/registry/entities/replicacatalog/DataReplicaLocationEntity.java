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
package org.apache.airavata.registry.entities.replicacatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.model.ReplicaLocationCategory;
import org.apache.airavata.common.model.ReplicaPersistentType;

/**
 * The persistent class for the data_replica_location database table.
 */
@Entity
@Table(name = "DATA_REPLICA_LOCATION")
public class DataReplicaLocationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "REPLICA_ID", nullable = false)
    private String replicaId;

    @Column(name = "PRODUCT_URI", nullable = false, insertable = false, updatable = false)
    private String productUri;

    @Lob
    @Column(name = "REPLICA_NAME")
    private String replicaName;

    @Column(name = "REPLICA_DESCRIPTION")
    private String replicaDescription;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "CREATION_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    @Column(
            name = "LAST_MODIFIED_TIME",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp lastModifiedTime;

    @Column(name = "VALID_UNTIL_TIME")
    private Timestamp validUntilTime;

    @Column(name = "REPLICA_LOCATION_CATEGORY")
    @Enumerated(EnumType.STRING)
    private ReplicaLocationCategory replicaLocationCategory;

    @Column(name = "REPLICA_PERSISTENT_TYPE")
    @Enumerated(EnumType.STRING)
    private ReplicaPersistentType replicaPersistentType;

    /**
     * Replica metadata stored in the unified METADATA table.
     * This field is transient and populated by the service layer from MetadataRepository.
     * Use MetadataRepository.findByDataReplicaId(replicaId) to manage metadata.
     */
    @Transient
    private Map<String, String> replicaMetadata = new HashMap<>();

    @ManyToOne(targetEntity = DataProductEntity.class)
    @JoinColumn(name = "PRODUCT_URI", nullable = false, updatable = false)
    private DataProductEntity dataProduct;

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

    public Timestamp getValidUntilTime() {
        return validUntilTime;
    }

    public void setValidUntilTime(Timestamp validUntilTime) {
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

    public Map<String, String> getReplicaMetadata() {
        return replicaMetadata;
    }

    public void setReplicaMetadata(Map<String, String> replicaMetadata) {
        this.replicaMetadata = replicaMetadata;
    }

    public DataProductEntity getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductEntity dataProduct) {
        this.dataProduct = dataProduct;
    }
}
