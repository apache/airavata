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
package org.apache.airavata.research.artifact.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.ModelArtifactEntity;
import org.apache.airavata.research.artifact.entity.NotebookArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.entity.TagEntity;
import org.apache.airavata.research.artifact.model.ResearchArtifact;
import org.apache.airavata.research.artifact.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class ArtifactMapper {

    public ResearchArtifact toModel(ResearchArtifactEntity entity) {
        if (entity == null) return null;

        var model = new ResearchArtifact();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setHeaderImage(entity.getHeaderImage());
        model.setAuthors(entity.getAuthors());
        model.setTags(
                entity.getTags() != null
                        ? entity.getTags().stream().map(TagEntity::getValue).collect(Collectors.toSet())
                        : null);
        model.setStatus(entity.getStatus());
        model.setState(entity.getState());
        model.setPrivacy(entity.getPrivacy());
        model.setType(entity.getType());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        if (entity instanceof RepositoryArtifactEntity repo) {
            model.setRepositoryUrl(repo.getRepositoryUrl());
        } else if (entity instanceof DatasetArtifactEntity dataset) {
            model.setDatasetUrl(dataset.getDatasetUrl());
        } else if (entity instanceof ModelArtifactEntity modelArtifact) {
            model.setApplicationInterfaceId(modelArtifact.getApplicationInterfaceId());
            model.setVersion(modelArtifact.getVersion());
        } else if (entity instanceof NotebookArtifactEntity notebook) {
            model.setNotebookPath(notebook.getNotebookPath());
        }

        return model;
    }

    public List<ResearchArtifact> toModelList(List<ResearchArtifactEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toModel).toList();
    }

    public Tag toTag(TagEntity entity) {
        if (entity == null) return null;

        var tag = new Tag();
        tag.setId(entity.getId());
        tag.setValue(entity.getValue());
        return tag;
    }

    public List<Tag> toTagList(List<TagEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toTag).toList();
    }
}
