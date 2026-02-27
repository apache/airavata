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
package org.apache.airavata.research.artifact.repository;

import java.util.List;
import org.apache.airavata.research.artifact.entity.ArtifactStarEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtifactStarRepository extends JpaRepository<ArtifactStarEntity, String> {

    boolean existsByArtifactAndUserId(ResearchArtifactEntity artifact, String ownerId);

    List<ArtifactStarEntity> findByArtifactAndUserId(ResearchArtifactEntity artifact, String ownerId);

    List<ArtifactStarEntity> findByUserIdAndArtifactState(String loggedInUser, ArtifactState stateEnum);

    long countArtifactStarByArtifact(ResearchArtifactEntity artifact);
}
