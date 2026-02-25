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
package org.apache.airavata.status.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.status.model.EventKind;
import org.apache.airavata.status.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified repository for parent-scoped event records (status and error) in the EVENT table.
 */
@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {

    @Query(
            value =
                    "SELECT COALESCE(MAX(e.sequence_num), 0) + 1 FROM event e WHERE e.parent_id = :parentId FOR UPDATE",
            nativeQuery = true)
    long getNextSequenceNum(@Param("parentId") String parentId);

    @Query(
            "SELECT e FROM EventEntity e WHERE e.parentId = :parentId AND e.eventKind = :eventKind ORDER BY e.sequenceNum DESC")
    List<EventEntity> findByParentIdAndEventKindOrderBySequenceNumDesc(
            @Param("parentId") String parentId,
            @Param("eventKind") EventKind eventKind);

    Optional<EventEntity> findFirstByParentIdAndEventKindOrderBySequenceNumDesc(
            String parentId, EventKind eventKind);

    @Modifying
    @Query("DELETE FROM EventEntity e WHERE e.parentId = :parentId")
    void deleteByParentId(@Param("parentId") String parentId);

    default List<EventEntity> findByParentIdAndEventKind(String parentId, EventKind eventKind) {
        return findByParentIdAndEventKindOrderBySequenceNumDesc(parentId, eventKind);
    }

    default Optional<EventEntity> findLatestByParentIdAndEventKind(String parentId, EventKind eventKind) {
        return findFirstByParentIdAndEventKindOrderBySequenceNumDesc(parentId, eventKind);
    }
}
