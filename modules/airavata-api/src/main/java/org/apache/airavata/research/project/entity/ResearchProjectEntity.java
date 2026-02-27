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
package org.apache.airavata.research.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "ResearchProjectEntity")
@Table(name = "research_project")
@EntityListeners(AuditingEntityListener.class)
public class ResearchProjectEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, length = 48)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "repository_artifact_id")
    private RepositoryArtifactEntity repositoryArtifact;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "research_project_dataset",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "dataset_artifact_id"))
    private Set<DatasetArtifactEntity> datasetArtifacts = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArtifactState state;

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

    public RepositoryArtifactEntity getRepositoryArtifact() {
        return repositoryArtifact;
    }

    public void setRepositoryArtifact(RepositoryArtifactEntity repositoryArtifact) {
        this.repositoryArtifact = repositoryArtifact;
    }

    public Set<DatasetArtifactEntity> getDatasetArtifacts() {
        return datasetArtifacts;
    }

    public void setDatasetArtifacts(Set<DatasetArtifactEntity> datasetArtifacts) {
        this.datasetArtifacts = datasetArtifacts;
    }

    public void addDatasetArtifact(DatasetArtifactEntity datasetArtifact) {
        this.datasetArtifacts.add(datasetArtifact);
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

    public ArtifactState getState() {
        return state;
    }

    public void setState(ArtifactState state) {
        this.state = state;
    }
}
