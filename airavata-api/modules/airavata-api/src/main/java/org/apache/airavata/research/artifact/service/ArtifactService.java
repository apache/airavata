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
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.model.CreateArtifactRequest;
import org.apache.airavata.research.artifact.model.ModifyArtifactRequest;
import org.apache.airavata.research.artifact.model.ResearchArtifact;
import org.apache.airavata.research.artifact.model.Tag;
import org.springframework.data.domain.Page;

public interface ArtifactService {

    ResearchArtifact createArtifact(CreateArtifactRequest request, ArtifactType type);

    ResearchArtifact modifyArtifact(ModifyArtifactRequest request);

    boolean starOrUnstarArtifact(String artifactId);

    boolean checkWhetherUserStarredArtifact(String artifactId);

    List<ResearchArtifact> getAllStarredArtifacts(String userId);

    long getArtifactStarCount(String artifactId);

    ResearchArtifact getArtifactById(String id);

    boolean deleteArtifactById(String id);

    Page<ResearchArtifact> getAllArtifacts(
            int pageNumber, int pageSize, List<ArtifactType> types, String[] tag, String nameSearch);

    List<Tag> getAllTags();

    List<Tag> getAllTagsByPopularity();

    List<Tag> getAllTagsByAlphabeticalOrder();

    List<ResearchArtifact> getAllArtifactsByTypeAndName(ArtifactType type, String name);
}
