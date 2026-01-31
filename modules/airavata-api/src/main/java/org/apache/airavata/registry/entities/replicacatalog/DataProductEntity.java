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

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.DataProductType;

/**
 * The persistent class for the data_product database table.
 * Unified entity for catalog datasets and data products: catalog metadata, primary storage path,
 * and optional replica locations.
 */
@Entity
@Table(name = "DATA_PRODUCT")
public class DataProductEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ResourceStatus {
        NONE, PENDING, VERIFIED, REJECTED
    }

    public enum Privacy {
        PUBLIC, PRIVATE
    }

    /**
     * Resource scope stored in database. Only USER and GATEWAY are stored. DELEGATED is inferred at runtime.
     */
    public enum ResourceScope {
        USER, GATEWAY
    }

    @Id
    @Column(name = "PRODUCT_URI", nullable = false)
    private String productUri;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Lob
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

    @Column(name = "CREATION_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    @Column(
            name = "LAST_MODIFIED_TIME",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp lastModifiedTime;

    @Column(name = "PRODUCT_TYPE")
    @Enumerated(EnumType.STRING)
    private DataProductType dataProductType;

    @Column(name = "PRIMARY_STORAGE_RESOURCE_ID")
    private String primaryStorageResourceId;

    @Column(name = "PRIMARY_FILE_PATH", length = 1024)
    private String primaryFilePath;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private ResourceStatus status = ResourceStatus.NONE;

    @Column(name = "PRIVACY")
    @Enumerated(EnumType.STRING)
    private Privacy privacy = Privacy.PRIVATE;

    @Column(name = "RESOURCE_SCOPE")
    @Enumerated(EnumType.STRING)
    private ResourceScope resourceScope = ResourceScope.USER;

    @Column(name = "OWNER_ID")
    private String ownerId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "HEADER_IMAGE", length = 1024)
    private String headerImage;

    @Column(name = "FORMAT")
    private String format;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DATA_PRODUCT_AUTHOR", joinColumns = @JoinColumn(name = "PRODUCT_URI"))
    @Column(name = "AUTHOR")
    private List<String> authors = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DATA_PRODUCT_TAG", joinColumns = @JoinColumn(name = "PRODUCT_URI"))
    @Column(name = "TAG")
    private List<String> tags = new ArrayList<>();

    /**
     * Product metadata stored in the unified METADATA table.
     * This field is transient and populated by the service layer from MetadataRepository.
     * Use MetadataRepository.findByDataProductUri(productUri) to manage metadata.
     */
    @Transient
    private Map<String, String> productMetadata = new HashMap<>();

    @OneToMany(
            targetEntity = DataReplicaLocationEntity.class,
            cascade = CascadeType.ALL,
            mappedBy = "dataProduct",
            fetch = FetchType.EAGER)
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

    public Map<String, String> getProductMetadata() {
        return productMetadata;
    }

    public void setProductMetadata(Map<String, String> productMetadata) {
        this.productMetadata = productMetadata;
    }

    public List<DataReplicaLocationEntity> getReplicaLocations() {
        return replicaLocations;
    }

    public void setReplicaLocations(List<DataReplicaLocationEntity> replicaLocations) {
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

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public ResourceScope getResourceScope() {
        return resourceScope;
    }

    public void setResourceScope(ResourceScope resourceScope) {
        this.resourceScope = resourceScope;
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors != null ? authors : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
}
