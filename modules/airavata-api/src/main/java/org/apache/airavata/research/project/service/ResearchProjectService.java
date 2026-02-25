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
import java.util.stream.Collectors;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.repository.ResearchArtifactRepository;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.research.project.dto.CreateProjectRequest;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;
import org.apache.airavata.research.project.repository.ResearchProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("researchProjectService")
public class ResearchProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchProjectService.class);

    private final ResearchProjectRepository projectRepository;
    private final ResearchArtifactRepository artifactRepository;

    public ResearchProjectService(
            @Qualifier("researchProjectRepository") ResearchProjectRepository projectRepository,
            ResearchArtifactRepository artifactRepository) {
        this.projectRepository = projectRepository;
        this.artifactRepository = artifactRepository;
    }

    public ResearchProjectEntity findProject(String projectId) {
        return projectRepository
                .findByIdAndState(projectId, ArtifactState.ACTIVE)
                .orElseThrow(() -> {
                    LOGGER.error("Unable to find a Project with id: {}", projectId);
                    return new EntityNotFoundException("Unable to find a Project with id: " + projectId);
                });
    }

    public ResearchProjectEntity createProject(CreateProjectRequest createProjectRequest) {
        var userId = UserContext.userId();
        if (!userId.equalsIgnoreCase(createProjectRequest.getOwnerId())) {
            throw new RuntimeException("User is not owner of this project");
        }

        var project = new ResearchProjectEntity();
        project.setName(createProjectRequest.getName());
        project.setOwnerId(createProjectRequest.getOwnerId());

        var artifact = artifactRepository.findById(createProjectRequest.getRepositoryId());
        if (artifact.isEmpty()) {
            throw new RuntimeException("Repository not found");
        } else if (!artifact.get().getType().equals(ArtifactType.REPOSITORY)) {
            throw new RuntimeException(
                    "RepositoryId: " + createProjectRequest.getRepositoryId() + " is not a repository");
        }
        var repositoryArtifact = (RepositoryArtifactEntity) artifact.get();
        project.setRepositoryArtifact(repositoryArtifact);

        var artifacts = artifactRepository.findAllById(createProjectRequest.getDatasetIds());
        if (artifacts.size() != createProjectRequest.getDatasetIds().size()) {
            throw new RuntimeException("At least one of the data set ids is not a valid artifact id");
        }
        for (ResearchArtifactEntity a : artifacts) {
            if (!a.getType().equals(ArtifactType.DATASET)) {
                throw new RuntimeException("DatasetId: " + a.getId() + " is not a dataset");
            }
        }

        var datasetArtifactsList = artifacts.stream()
                .filter(a -> a instanceof DatasetArtifactEntity)
                .map(a -> (DatasetArtifactEntity) a)
                .collect(Collectors.toList());

        var datasetArtifactsSet = new HashSet<>(datasetArtifactsList);
        project.setDatasetArtifacts(datasetArtifactsSet);
        project.setState(ArtifactState.ACTIVE);
        projectRepository.save(project);
        return project;
    }

    public List<ResearchProjectEntity> getAllProjects() {
        return projectRepository.findALlByState(ArtifactState.ACTIVE);
    }

    public List<ResearchProjectEntity> getAllProjectsByOwnerId(String ownerId) {
        return projectRepository.findAllByOwnerIdAndStateOrderByCreatedAtDesc(ownerId, ArtifactState.ACTIVE);
    }

    public boolean deleteProject(String projectId) {
        var optionalProject = projectRepository.findByIdAndState(projectId, ArtifactState.ACTIVE);
        if (optionalProject.isEmpty()
                || ArtifactState.DELETED.equals(optionalProject.get().getState())) {
            throw new EntityNotFoundException("Unable to find a Project with id: " + projectId);
        }

        var project = optionalProject.get();
        var userId = UserContext.userId();
        if (!project.getOwnerId().equalsIgnoreCase(userId)) {
            throw new RuntimeException(
                    String.format("User %s is not authorized to delete project with id: %s", userId, projectId));
        }

        project.setState(ArtifactState.DELETED);
        projectRepository.save(project);
        return true;
    }

    public List<ResearchProjectEntity> findProjectsWithRepository(RepositoryArtifactEntity repositoryArtifact) {
        return projectRepository.findProjectsByRepositoryArtifactAndState(repositoryArtifact, ArtifactState.ACTIVE);
    }

    public List<ResearchProjectEntity> findProjectsContainingDataset(DatasetArtifactEntity datasetArtifact) {
        var set = new HashSet<DatasetArtifactEntity>();
        set.add(datasetArtifact);

        return projectRepository.findProjectsByDatasetArtifactsContainingAndState(set, ArtifactState.ACTIVE);
    }
}
