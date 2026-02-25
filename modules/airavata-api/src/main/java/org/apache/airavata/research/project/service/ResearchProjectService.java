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

import java.util.List;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.project.dto.CreateProjectRequest;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;

/**
 * Service contract for managing research projects.
 */
public interface ResearchProjectService {

    ResearchProjectEntity findProject(String projectId);

    ResearchProjectEntity createProject(CreateProjectRequest createProjectRequest);

    List<ResearchProjectEntity> getAllProjects();

    List<ResearchProjectEntity> getAllProjectsByOwnerId(String ownerId);

    boolean deleteProject(String projectId);

    List<ResearchProjectEntity> findProjectsWithRepository(RepositoryArtifactEntity repositoryArtifact);

    List<ResearchProjectEntity> findProjectsContainingDataset(DatasetArtifactEntity datasetArtifact);
}
