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
package org.apache.airavata.research.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.airavata.research.model.DatasetResourceEntity;
import org.apache.airavata.research.model.RepositoryResourceEntity;
import org.apache.airavata.research.model.ResearchProjectEntity;
import org.apache.airavata.sharing.model.StateEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearchProjectRepository extends JpaRepository<ResearchProjectEntity, String> {

    boolean existsByOwnerId(String ownerId);

    List<ResearchProjectEntity> findProjectsByRepositoryResource(RepositoryResourceEntity repositoryResource);

    List<ResearchProjectEntity> findProjectsByDatasetResourcesContaining(Set<DatasetResourceEntity> datasetResources);

    List<ResearchProjectEntity> findAllByOwnerId(String ownerId);

    List<ResearchProjectEntity> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId);

    List<ResearchProjectEntity> findALlByState(StateEnum state);

    List<ResearchProjectEntity> findProjectsByRepositoryResourceAndState(
            RepositoryResourceEntity repositoryResource, StateEnum state);

    List<ResearchProjectEntity> findAllByOwnerIdAndStateOrderByCreatedAtDesc(String ownerId, StateEnum state);

    List<ResearchProjectEntity> findProjectsByDatasetResourcesContainingAndState(
            Set<DatasetResourceEntity> datasetResources, StateEnum state);

    StateEnum State(StateEnum state);

    Optional<ResearchProjectEntity> findByIdAndState(String id, StateEnum state);
}
