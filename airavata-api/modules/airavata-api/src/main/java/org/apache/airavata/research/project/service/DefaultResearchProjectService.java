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
package org.apache.airavata.research.project.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.repository.ResearchArtifactRepository;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;
import org.apache.airavata.research.project.mapper.ResearchProjectMapper;
import org.apache.airavata.research.project.model.CreateProjectRequest;
import org.apache.airavata.research.project.model.ResearchProject;
import org.apache.airavata.research.project.repository.ResearchProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultResearchProjectService implements ResearchProjectService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResearchProjectService.class);

    private final ResearchProjectRepository projectRepository;
    private final ResearchArtifactRepository artifactRepository;
    private final ResearchProjectMapper mapper;

    public DefaultResearchProjectService(
            @Qualifier("researchProjectRepository") ResearchProjectRepository projectRepository,
            ResearchArtifactRepository artifactRepository,
            ResearchProjectMapper mapper) {
        this.projectRepository = projectRepository;
        this.artifactRepository = artifactRepository;
        this.mapper = mapper;
    }

    @Override
    public ResearchProject findProject(String projectId) {
        var entity = projectRepository
                .findByIdAndState(projectId, ArtifactState.ACTIVE)
                .orElseThrow(() -> {
                    logger.error("Unable to find a Project with id: {}", projectId);
                    return new EntityNotFoundException("Unable to find a Project with id: " + projectId);
                });
        return mapper.toModel(entity);
    }

    /**
     * Internal entity lookup used by the session layer which requires a managed entity reference.
     */
    ResearchProjectEntity findProjectEntity(String projectId) {
        return projectRepository
                .findByIdAndState(projectId, ArtifactState.ACTIVE)
                .orElseThrow(() -> {
                    logger.error("Unable to find a Project with id: {}", projectId);
                    return new EntityNotFoundException("Unable to find a Project with id: " + projectId);
                });
    }

    @Override
    public ResearchProject createProject(CreateProjectRequest createProjectRequest) {
        var userId = UserContext.userId();
        if (!userId.equalsIgnoreCase(createProjectRequest.getOwnerId())) {
            throw new IllegalArgumentException("User is not owner of this project");
        }

        var project = new ResearchProjectEntity();
        project.setName(createProjectRequest.getName());
        project.setOwnerId(createProjectRequest.getOwnerId());

        var artifact = artifactRepository.findById(createProjectRequest.getRepositoryId());
        if (artifact.isEmpty()) {
            throw new EntityNotFoundException("Repository not found");
        } else if (!artifact.get().getType().equals(ArtifactType.REPOSITORY)) {
            throw new IllegalArgumentException(
                    "RepositoryId: " + createProjectRequest.getRepositoryId() + " is not a repository");
        }
        var repositoryArtifact = (RepositoryArtifactEntity) artifact.get();
        project.setRepositoryArtifact(repositoryArtifact);

        var artifacts = artifactRepository.findAllById(createProjectRequest.getDatasetIds());
        if (artifacts.size() != createProjectRequest.getDatasetIds().size()) {
            throw new IllegalArgumentException("At least one of the data set ids is not a valid artifact id");
        }
        for (ResearchArtifactEntity a : artifacts) {
            if (!a.getType().equals(ArtifactType.DATASET)) {
                throw new IllegalArgumentException("DatasetId: " + a.getId() + " is not a dataset");
            }
        }

        var datasetArtifactsList = artifacts.stream()
                .filter(a -> a instanceof DatasetArtifactEntity)
                .map(a -> (DatasetArtifactEntity) a)
                .toList();

        var datasetArtifactsSet = new HashSet<>(datasetArtifactsList);
        project.setDatasetArtifacts(datasetArtifactsSet);
        project.setState(ArtifactState.ACTIVE);
        projectRepository.save(project);
        return mapper.toModel(project);
    }

    @Override
    public List<ResearchProject> getAllProjects() {
        return mapper.toModelList(projectRepository.findALlByState(ArtifactState.ACTIVE));
    }

    @Override
    public List<ResearchProject> getAllProjectsByOwnerId(String ownerId) {
        return mapper.toModelList(
                projectRepository.findAllByOwnerIdAndStateOrderByCreatedAtDesc(ownerId, ArtifactState.ACTIVE));
    }

    @Override
    public boolean deleteProject(String projectId) {
        var optionalProject = projectRepository.findByIdAndState(projectId, ArtifactState.ACTIVE);
        if (optionalProject.isEmpty()
                || ArtifactState.DELETED.equals(optionalProject.get().getState())) {
            throw new EntityNotFoundException("Unable to find a Project with id: " + projectId);
        }

        var project = optionalProject.get();
        var userId = UserContext.userId();
        if (!project.getOwnerId().equalsIgnoreCase(userId)) {
            throw new IllegalArgumentException(
                    String.format("User %s is not authorized to delete project with id: %s", userId, projectId));
        }

        project.setState(ArtifactState.DELETED);
        projectRepository.save(project);
        return true;
    }

    @Override
    public List<ResearchProject> findProjectsWithRepository(String repositoryArtifactId) {
        var artifact = artifactRepository
                .findById(repositoryArtifactId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Repository artifact not found: " + repositoryArtifactId));
        if (!(artifact instanceof RepositoryArtifactEntity repositoryArtifact)) {
            throw new IllegalArgumentException("Artifact " + repositoryArtifactId + " is not a repository");
        }
        return mapper.toModelList(
                projectRepository.findProjectsByRepositoryArtifactAndState(repositoryArtifact, ArtifactState.ACTIVE));
    }

    @Override
    public List<ResearchProject> findProjectsContainingDataset(String datasetArtifactId) {
        var artifact = artifactRepository
                .findById(datasetArtifactId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset artifact not found: " + datasetArtifactId));
        if (!(artifact instanceof DatasetArtifactEntity datasetArtifact)) {
            throw new IllegalArgumentException("Artifact " + datasetArtifactId + " is not a dataset");
        }
        return mapper.toModelList(projectRepository.findProjectsByDatasetArtifactsContainingAndState(
                Set.of(datasetArtifact), ArtifactState.ACTIVE));
    }
}
