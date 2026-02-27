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
package org.apache.airavata.research.project.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;
import org.apache.airavata.research.project.model.ResearchProject;
import org.springframework.stereotype.Component;

@Component
public class ResearchProjectMapper {

    public ResearchProject toModel(ResearchProjectEntity entity) {
        if (entity == null) return null;

        var model = new ResearchProject();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setOwnerId(entity.getOwnerId());
        model.setState(entity.getState());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        model.setRepositoryArtifactId(
                entity.getRepositoryArtifact() != null
                        ? entity.getRepositoryArtifact().getId()
                        : null);

        Set<DatasetArtifactEntity> datasets = entity.getDatasetArtifacts();
        model.setDatasetArtifactIds(
                datasets != null
                        ? datasets.stream().map(DatasetArtifactEntity::getId).collect(Collectors.toSet())
                        : Set.of());

        return model;
    }

    public List<ResearchProject> toModelList(List<ResearchProjectEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toModel).toList();
    }
}
