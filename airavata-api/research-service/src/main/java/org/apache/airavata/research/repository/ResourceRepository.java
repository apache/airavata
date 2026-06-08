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
import org.apache.airavata.research.model.ResourceEntity;
import org.apache.airavata.research.model.StateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, String> {

    @Query("""
                    SELECT r
                    FROM ResourceEntity r
                    WHERE TYPE(r) IN :types
                      AND r.name LIKE CONCAT('%', :nameSearch, '%')
                      AND r.state = 'ACTIVE'
                      AND r.privacy = 'PUBLIC'
                    ORDER BY r.name
                    """)
    Page<ResourceEntity> findAllByTypes(
            @Param("types") List<Class<? extends ResourceEntity>> types,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT r
            FROM ResourceEntity r
            JOIN r.authors a
            WHERE r.class IN :typeList
              AND LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))
              AND r.state = 'ACTIVE'
              AND (r.privacy = 'PUBLIC' or a = :userId)
            ORDER BY r.name
            """)
    Page<ResourceEntity> findAllByTypesForUser(
            @Param("typeList") List<Class<? extends ResourceEntity>> typeList,
            @Param("nameSearch") String nameSearch,
            @Param("userId") String userId,
            Pageable pageable);

    @Query("""
                    SELECT r
                    FROM ResourceEntity r
                    JOIN r.tags t
                    WHERE r.class IN :typeList
                      AND t.value IN :tags
                      AND LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))
                      AND r.state = 'ACTIVE'
                      AND r.privacy = 'PUBLIC'
                    GROUP BY r
                    HAVING COUNT(DISTINCT t.value) = :tagCount
                    ORDER BY r.name
                    """)
    Page<ResourceEntity> findAllByTypesAndAllTags(
            @Param("typeList") List<Class<? extends ResourceEntity>> typeList,
            @Param("tags") String[] tags,
            @Param("tagCount") long tagCount,
            @Param("nameSearch") String nameSearch,
            Pageable pageable);

    @Query("""
            SELECT r
            FROM ResourceEntity r
            JOIN r.tags t
            JOIN r.authors a
            WHERE r.class IN :typeList
              AND t.value IN :tags
              AND LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))
              AND r.state = 'ACTIVE'
              AND (r.privacy = 'PUBLIC' OR a = :userId)
            GROUP BY r
            HAVING COUNT(DISTINCT t.value) = :tagCount
            ORDER BY r.name
            """)
    Page<ResourceEntity> findAllByTypesAndAllTagsForUser(
            @Param("typeList") List<Class<? extends ResourceEntity>> typeList,
            @Param("tags") String[] tags,
            @Param("tagCount") Long tagCount,
            @Param("nameSearch") String nameSearch,
            @Param("userId") String userId,
            Pageable pageable);

    @Query("""
                    SELECT r
                    FROM ResourceEntity r
                    JOIN r.authors a
                    WHERE TYPE(r) = :type AND r.state = 'ACTIVE'
                    AND LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))
                    AND (r.privacy = "PUBLIC" OR a = :userId)
                    """)
    List<ResourceEntity> findByTypeAndNameContainingIgnoreCase(
            @Param("type") Class<? extends ResourceEntity> type,
            @Param("name") String name,
            @Param("userId") String userId);

    Optional<ResourceEntity> findByIdAndState(String id, StateEnum state);
}
