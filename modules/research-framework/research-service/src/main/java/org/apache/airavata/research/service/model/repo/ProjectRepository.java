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
package org.apache.airavata.research.service.model.repo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    boolean existsByOwnerId(String ownerId);

    List<Project> findProjectsByRepositoryResource(RepositoryResource repositoryResource);

    List<Project> findProjectsByDatasetResourcesContaining(Set<DatasetResource> datasetResources);

    List<Project> findAllByOwnerId(String ownerId);

    List<Project> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId);

    List<Project> findALlByState(StateEnum state);

    List<Project> findProjectsByRepositoryResourceAndState(RepositoryResource repositoryResource, StateEnum state);

    List<Project> findAllByOwnerIdAndStateOrderByCreatedAtDesc(String ownerId, StateEnum state);

    List<Project> findProjectsByDatasetResourcesContainingAndState(
            Set<DatasetResource> datasetResources, StateEnum state);

    StateEnum State(StateEnum state);

    Optional<Project> findByIdAndState(String id, StateEnum state);
}
