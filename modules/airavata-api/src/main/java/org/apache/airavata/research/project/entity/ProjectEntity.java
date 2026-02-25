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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Unified project entity — the primary organizational unit for research work.
 *
 * <p>A project groups experiments, artifacts (repository + datasets), and
 * allocation projects. Credential-based allocations are tied to projects
 * through the accounting layer.
 *
 * <p>Maps to the PROJECT table. Artifact associations (repository, datasets)
 * are managed via Hibernate join columns/tables.
 */
@Entity(name = "ProjectEntity")
@Table(name = "project")
@EntityListeners(AuditingEntityListener.class)
public class ProjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "project_id", nullable = false, length = 255)
    private String projectId;

    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectState state = ProjectState.ACTIVE;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "repository_artifact_id")
    private RepositoryArtifactEntity repositoryArtifact;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_dataset",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "dataset_artifact_id"))
    private Set<DatasetArtifactEntity> datasetArtifacts = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<ExperimentEntity> experiments;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public ProjectEntity() {}

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public ProjectState getState() {
        return state;
    }

    public void setState(ProjectState state) {
        this.state = state;
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

    public List<ExperimentEntity> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentEntity> experiments) {
        this.experiments = experiments;
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
