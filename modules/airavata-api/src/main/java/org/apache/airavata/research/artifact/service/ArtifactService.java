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
package org.apache.airavata.research.artifact.service;

import java.util.List;
import org.apache.airavata.research.artifact.dto.ArtifactResponse;
import org.apache.airavata.research.artifact.dto.CreateArtifactRequest;
import org.apache.airavata.research.artifact.dto.ModifyArtifactRequest;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.entity.TagEntity;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.springframework.data.domain.Page;

public interface ArtifactService {

    void initializeArtifact(ResearchArtifactEntity artifact);

    ArtifactResponse createArtifact(ResearchArtifactEntity artifact, ArtifactType type);

    void transferArtifactRequestFields(ResearchArtifactEntity artifact, CreateArtifactRequest createArtifactRequest);

    ArtifactResponse createRepositoryArtifact(CreateArtifactRequest artifactRequest, String repoUrl);

    ResearchArtifactEntity modifyArtifact(ModifyArtifactRequest artifactRequest);

    boolean starOrUnstarArtifact(String artifactId);

    boolean checkWhetherUserStarredArtifact(String artifactId);

    List<ResearchArtifactEntity> getAllStarredArtifacts(String userId);

    long getArtifactStarCount(String artifactId);

    ResearchArtifactEntity getArtifactById(String id);

    boolean deleteArtifactById(String id);

    Page<ResearchArtifactEntity> getAllArtifacts(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResearchArtifactEntity>> typeList,
            String[] tag,
            String nameSearch);

    List<TagEntity> getAllTags();

    List<TagEntity> getAllTagsByPopularity();

    List<TagEntity> getAllTagsByAlphabeticalOrder();

    List<ResearchArtifactEntity> getAllArtifactsByTypeAndName(
            Class<? extends ResearchArtifactEntity> type, String name);
}
