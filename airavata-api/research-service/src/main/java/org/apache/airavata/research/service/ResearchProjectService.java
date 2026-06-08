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
package org.apache.airavata.research.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.research.model.DatasetResourceEntity;
import org.apache.airavata.research.model.RepositoryResourceEntity;
import org.apache.airavata.research.model.ResearchProjectEntity;
import org.apache.airavata.research.model.ResourceEntity;
import org.apache.airavata.research.model.ResourceTypeEnum;
import org.apache.airavata.research.model.StateEnum;
import org.apache.airavata.research.repository.ResearchProjectRepository;
import org.apache.airavata.research.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResearchProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchProjectService.class);

    private final ResearchProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;

    public ResearchProjectService(ResearchProjectRepository projectRepository, ResourceRepository resourceRepository) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
    }

    public ResearchProjectEntity findProject(String projectId) {
        return projectRepository.findByIdAndState(projectId, StateEnum.ACTIVE).orElseThrow(() -> {
            LOGGER.error("Unable to find a ResearchProjectEntity with id: {}", projectId);
            return new EntityNotFoundException("Unable to find a ResearchProjectEntity with id: " + projectId);
        });
    }

    public ResearchProjectEntity createProject(
            String name, String ownerId, String repositoryId, Set<String> datasetIds) {
        String userId = UserContext.userId();
        if (!userId.equalsIgnoreCase(ownerId)) {
            throw new RuntimeException("User is not owner of this project");
        }

        ResearchProjectEntity project = new ResearchProjectEntity();
        project.setName(name);
        project.setOwnerId(ownerId);

        Optional<ResourceEntity> resource = resourceRepository.findById(repositoryId);
        if (resource.isEmpty()) {
            throw new RuntimeException("Repository not found");
        } else if (!resource.get().getType().equals(ResourceTypeEnum.REPOSITORY)) {
            throw new RuntimeException("RepositoryId: " + repositoryId + " is not a repository");
        }
        RepositoryResourceEntity repositoryResource = (RepositoryResourceEntity) resource.get();
        project.setRepositoryResource(repositoryResource);

        List<ResourceEntity> resources = resourceRepository.findAllById(datasetIds);
        if (resources.size() != datasetIds.size()) {
            throw new RuntimeException("At least one of the data set ids is not a valid resource id");
        }
        for (ResourceEntity r : resources) {
            if (!r.getType().equals(ResourceTypeEnum.DATASET)) {
                throw new RuntimeException("DatasetId: " + r.getId() + " is not a dataset");
            }
        }

        List<DatasetResourceEntity> datasetResourcesList = resources.stream()
                .filter(r -> r instanceof DatasetResourceEntity)
                .map(r -> (DatasetResourceEntity) r)
                .collect(Collectors.toList());

        Set<DatasetResourceEntity> datasetResourcesSet = new HashSet<>(datasetResourcesList);
        project.setDatasetResources(datasetResourcesSet);
        project.setState(StateEnum.ACTIVE);
        projectRepository.save(project);
        return project;
    }

    public List<ResearchProjectEntity> getAllProjects() {
        return projectRepository.findALlByState(StateEnum.ACTIVE);
    }

    public List<ResearchProjectEntity> getAllProjectsByOwnerId(String ownerId) {
        return projectRepository.findAllByOwnerIdAndStateOrderByCreatedAtDesc(ownerId, StateEnum.ACTIVE);
    }

    public boolean deleteProject(String projectId) {
        Optional<ResearchProjectEntity> optionalProject =
                projectRepository.findByIdAndState(projectId, StateEnum.ACTIVE);
        if (optionalProject.isEmpty()
                || StateEnum.DELETED.equals(optionalProject.get().getState())) {
            throw new EntityNotFoundException("Unable to find a ResearchProjectEntity with id: " + projectId);
        }

        ResearchProjectEntity project = optionalProject.get();
        String userId = UserContext.userId();
        if (!project.getOwnerId().equalsIgnoreCase(userId)) {
            throw new RuntimeException(
                    String.format("User %s is not authorized to delete project with id: %s", userId, projectId));
        }

        project.setState(StateEnum.DELETED);
        projectRepository.save(project);
        return true;
    }

    public List<ResearchProjectEntity> findProjectsWithRepository(RepositoryResourceEntity repositoryResource) {
        return projectRepository.findProjectsByRepositoryResourceAndState(repositoryResource, StateEnum.ACTIVE);
    }

    public List<ResearchProjectEntity> findProjectsContainingDataset(DatasetResourceEntity datasetResource) {
        Set<DatasetResourceEntity> set = new HashSet<>();
        set.add(datasetResource);

        return projectRepository.findProjectsByDatasetResourcesContainingAndState(set, StateEnum.ACTIVE);
    }
}
