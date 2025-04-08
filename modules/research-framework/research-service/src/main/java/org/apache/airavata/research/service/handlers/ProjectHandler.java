/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.handlers;

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProjectHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectHandler.class);

    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;


    public ProjectHandler(ProjectRepository projectRepository, ResourceRepository resourceRepository) {
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
    }

    public Project findProject(String projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> {
            LOGGER.error("Unable to find a Project with id: " + projectId);
            return new EntityNotFoundException("Unable to find a Project with id: " + projectId);
        });
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> findProjectsWithRepository(RepositoryResource repositoryResource) {
        return projectRepository.findProjectsByRepositoryResource(repositoryResource);
    }

    public List<Project> findProjectsContainingDataset(DatasetResource datasetResource) {
        Set<DatasetResource> set = new HashSet<>();
        set.add(datasetResource);

        return projectRepository.findProjectsByDatasetResourcesContaining(set);
    }

}
