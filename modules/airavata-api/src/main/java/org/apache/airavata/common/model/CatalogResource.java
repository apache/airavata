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
import java.util.List;

/**
 * Domain model for Research Catalog Resources.
 * 
 * <h2>Resource Scope</h2>
 * <p>The scope field can be one of:</p>
 * <ul>
 *   <li><b>USER</b>: Resource owned by a specific user (stored in DB)</li>
 *   <li><b>GATEWAY</b>: Resource owned at gateway level (stored in DB)</li>
 *   <li><b>DELEGATED</b>: Resource accessible via group credentials but not directly owned (inferred at runtime)</li>
 * </ul>
 * 
 * <p>Only USER and GATEWAY can be set when creating resources. DELEGATED is automatically
 * inferred by the service layer when returning resources that are accessible via groups.</p>
 */
public class CatalogResource {
    private String id;
    private String name;
    private String description;
    private String type; // DATASET, REPOSITORY
    private String status; // NONE, PENDING, VERIFIED, REJECTED
    private String privacy; // PUBLIC, PRIVATE
    private String scope; // USER, GATEWAY (stored) or DELEGATED (inferred)
    private String gatewayId;
    private String ownerId;
    private String groupResourceProfileId;
    private String headerImage;
    private List<String> authors = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();
    private long createdAt;
    private long updatedAt;

    // Notebook specific
    private String notebookPath;
    private String jupyterServerUrl;

    // Dataset specific
    private String datasetUrl;
    private Long size;
    private String format;

    // Repository specific
    private String repositoryUrl;
    private String branch;
    private String commit;

    // Model specific
    private String modelUrl;
    private String applicationInterfaceId;
    private String framework;

    public CatalogResource() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
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

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    /**
     * Tag model for catalog resources
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
