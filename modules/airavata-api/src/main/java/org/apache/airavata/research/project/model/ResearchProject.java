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
package org.apache.airavata.research.project.model;

import java.time.Instant;
import java.util.Set;
import org.apache.airavata.research.artifact.model.ArtifactState;

public class ResearchProject {

    private String id;
    private String name;
    private String ownerId;
    private String repositoryArtifactId;
    private Set<String> datasetArtifactIds;
    private ArtifactState state;
    private Instant createdAt;
    private Instant updatedAt;

    public ResearchProject() {}

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRepositoryArtifactId() {
        return repositoryArtifactId;
    }

    public void setRepositoryArtifactId(String repositoryArtifactId) {
        this.repositoryArtifactId = repositoryArtifactId;
    }

    public Set<String> getDatasetArtifactIds() {
        return datasetArtifactIds;
    }

    public void setDatasetArtifactIds(Set<String> datasetArtifactIds) {
        this.datasetArtifactIds = datasetArtifactIds;
    }

    public ArtifactState getState() {
        return state;
    }

    public void setState(ArtifactState state) {
        this.state = state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
