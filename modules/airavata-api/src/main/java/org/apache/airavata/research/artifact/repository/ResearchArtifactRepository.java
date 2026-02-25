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
import java.util.Optional;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("researchArtifactRepository")
public interface ResearchArtifactRepository extends JpaRepository<ResearchArtifactEntity, String> {

    @Query("""
                    SELECT a
                    FROM ResearchArtifactEntity a
                    WHERE TYPE(a) IN :types
                      AND a.name LIKE CONCAT('%', :nameSearch, '%')
                      AND a.state = 'ACTIVE'
                      AND a.privacy = 'PUBLIC'
                    ORDER BY a.name
                    """)
    Page<ResearchArtifactEntity> findAllByTypes(
            @Param("types") List<Class<? extends ResearchArtifactEntity>> types,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT a
            FROM ResearchArtifactEntity a
            JOIN a.authors au
            WHERE a.class IN :typeList
              AND LOWER(a.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))
              AND a.state = 'ACTIVE'
              AND (a.privacy = 'PUBLIC' or au = :userId)
            ORDER BY a.name
            """)
    Page<ResearchArtifactEntity> findAllByTypesForUser(
            @Param("typeList") List<Class<? extends ResearchArtifactEntity>> typeList,
            @Param("nameSearch") String nameSearch,
            @Param("userId") String userId,
            Pageable pageable);

    @Query("""
                    SELECT a
                    FROM ResearchArtifactEntity a
                    JOIN a.tags t
                    WHERE a.class IN :typeList
                      AND t.value IN :tags
                      AND LOWER(a.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))
                      AND a.state = 'ACTIVE'
                      AND a.privacy = 'PUBLIC'
                    GROUP BY a
                    HAVING COUNT(DISTINCT t.value) = :tagCount
                    ORDER BY a.name
                    """)
    Page<ResearchArtifactEntity> findAllByTypesAndAllTags(
            @Param("typeList") List<Class<? extends ResearchArtifactEntity>> typeList,
            @Param("tags") String[] tags,
            @Param("tagCount") long tagCount,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);

    @Query("""
            SELECT a
            FROM ResearchArtifactEntity a
            JOIN a.tags t
            JOIN a.authors au
            WHERE a.class IN :typeList
              AND t.value IN :tags
              AND LOWER(a.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))
              AND a.state = 'ACTIVE'
              AND (a.privacy = 'PUBLIC' OR au = :userId)
            GROUP BY a
            HAVING COUNT(DISTINCT t.value) = :tagCount
            ORDER BY a.name
            """)
    Page<ResearchArtifactEntity> findAllByTypesAndAllTagsForUser(
            @Param("typeList") List<Class<? extends ResearchArtifactEntity>> typeList,
            @Param("tags") String[] tags,
            @Param("tagCount") Long tagCount,
            @Param("nameSearch") String nameSearch,
            @Param("userId") String userId,
            Pageable pageable);

    @Query("""
                    SELECT a
                    FROM ResearchArtifactEntity a
                    JOIN a.authors au
                    WHERE TYPE(a) = :type AND a.state = 'ACTIVE'
                    AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))
                    AND (a.privacy = "PUBLIC" OR au = :userId)
                    """)
    List<ResearchArtifactEntity> findByTypeAndNameContainingIgnoreCase(
            @Param("type") Class<? extends ResearchArtifactEntity> type,
            @Param("name") String name,
            @Param("userId") String userId);

    Optional<ResearchArtifactEntity> findByIdAndState(String id, ArtifactState state);
}
