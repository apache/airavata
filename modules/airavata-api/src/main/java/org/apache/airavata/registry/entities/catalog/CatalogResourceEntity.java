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
package org.apache.airavata.registry.entities.catalog;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for Research Catalog Resources (datasets and repositories).
 * 
 * <h2>Resource Scope Model</h2>
 * <p>Resources have a two-level scope model with inferred delegation:</p>
 * <ul>
 *   <li><b>USER</b>: Resource owned by a specific user (stored in DB)</li>
 *   <li><b>GATEWAY</b>: Resource owned at the gateway level (stored in DB)</li>
 *   <li><b>DELEGATED</b>: Resource accessible via group credentials but not directly owned (inferred, not stored)</li>
 * </ul>
 * 
 * <p>The {@code groupResourceProfileId} field is used to track which group resource profile
 * provides access to this resource for delegation purposes, but the scope remains USER or GATEWAY.</p>
 * 
 * <p>When a resource is returned via API, the service layer infers DELEGATED scope if:
 * <ul>
 *   <li>The resource is accessible via a group (groupResourceProfileId matches user's accessible groups)</li>
 *   <li>AND the resource is not directly owned by the user (scope != USER or ownerId != userId)</li>
 *   <li>AND the resource is not directly owned by the gateway (scope != GATEWAY or gatewayId doesn't match)</li>
 * </ul>
 */
@Entity
@Table(name = "CATALOG_RESOURCE")
public class CatalogResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Resource types: DATASET or REPOSITORY
     * REPOSITORY can contain notebooks, models, or general code repositories
     */
    public enum ResourceType {
        DATASET, REPOSITORY
    }

    public enum ResourceStatus {
        NONE, PENDING, VERIFIED, REJECTED
    }

    public enum Privacy {
        PUBLIC, PRIVATE
    }

    /**
     * Resource scope stored in database.
     * Only USER and GATEWAY are stored. DELEGATED is inferred at runtime.
     */
    public enum ResourceScope {
        USER, GATEWAY
    }

    @Id
    @Column(name = "RESOURCE_ID", nullable = false)
    private String id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION", length = 4096)
    private String description;

    @Column(name = "RESOURCE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private ResourceStatus status = ResourceStatus.NONE;

    @Column(name = "PRIVACY")
    @Enumerated(EnumType.STRING)
    private Privacy privacy = Privacy.PRIVATE;

    @Column(name = "RESOURCE_SCOPE")
    @Enumerated(EnumType.STRING)
    private ResourceScope scope = ResourceScope.USER;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "OWNER_ID")
    private String ownerId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "HEADER_IMAGE")
    private String headerImage;

    // Type-specific fields
    @Column(name = "NOTEBOOK_PATH")
    private String notebookPath;

    @Column(name = "JUPYTER_SERVER_URL")
    private String jupyterServerUrl;

    @Column(name = "DATASET_URL")
    private String datasetUrl;

    @Column(name = "DATASET_SIZE")
    private Long datasetSize;

    @Column(name = "DATASET_FORMAT")
    private String datasetFormat;

    @Column(name = "REPOSITORY_URL")
    private String repositoryUrl;

    @Column(name = "REPOSITORY_BRANCH")
    private String repositoryBranch;

    @Column(name = "REPOSITORY_COMMIT")
    private String repositoryCommit;

    @Column(name = "MODEL_URL")
    private String modelUrl;

    @Column(name = "APPLICATION_INTERFACE_ID")
    private String applicationInterfaceId;

    @Column(name = "MODEL_FRAMEWORK")
    private String modelFramework;

    @ElementCollection
    @CollectionTable(name = "CATALOG_RESOURCE_AUTHOR", joinColumns = @JoinColumn(name = "RESOURCE_ID"))
    @Column(name = "AUTHOR")
    private List<String> authors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "CATALOG_RESOURCE_TAG", joinColumns = @JoinColumn(name = "RESOURCE_ID"))
    @Column(name = "TAG")
    private List<String> tags = new ArrayList<>();

    @Column(name = "CREATED_AT", nullable = false)
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    public CatalogResourceEntity() {}

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
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

    public ResourceScope getScope() {
        return scope;
    }

    public void setScope(ResourceScope scope) {
        this.scope = scope;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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

    public String getNotebookPath() {
        return notebookPath;
    }

    public void setNotebookPath(String notebookPath) {
        this.notebookPath = notebookPath;
    }

    public String getJupyterServerUrl() {
        return jupyterServerUrl;
    }

    public void setJupyterServerUrl(String jupyterServerUrl) {
        this.jupyterServerUrl = jupyterServerUrl;
    }

    public String getDatasetUrl() {
        return datasetUrl;
    }

    public void setDatasetUrl(String datasetUrl) {
        this.datasetUrl = datasetUrl;
    }

    public Long getDatasetSize() {
        return datasetSize;
    }

    public void setDatasetSize(Long datasetSize) {
        this.datasetSize = datasetSize;
    }

    public String getDatasetFormat() {
        return datasetFormat;
    }

    public void setDatasetFormat(String datasetFormat) {
        this.datasetFormat = datasetFormat;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryBranch() {
        return repositoryBranch;
    }

    public void setRepositoryBranch(String repositoryBranch) {
        this.repositoryBranch = repositoryBranch;
    }

    public String getRepositoryCommit() {
        return repositoryCommit;
    }

    public void setRepositoryCommit(String repositoryCommit) {
        this.repositoryCommit = repositoryCommit;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getModelFramework() {
        return modelFramework;
    }

    public void setModelFramework(String modelFramework) {
        this.modelFramework = modelFramework;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
