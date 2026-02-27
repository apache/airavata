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
package org.apache.airavata.research.session.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.research.session.entity.SessionEntity;
import org.apache.airavata.research.session.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    List<SessionEntity> findByUserId(String userId);

    @Query(
            value = "SELECT * FROM research_session WHERE project_id = :projectId AND user_id = :userId",
            nativeQuery = true)
    Optional<SessionEntity> findSessionByProjectIdAndUserId(
            @Param("projectId") String projectId, @Param("userId") String userId);

    List<SessionEntity> findByUserIdAndStatus(String userId, SessionStatus status);

    List<SessionEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<SessionEntity> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, SessionStatus status);

    int countSessionsByUserIdAndStatus(String userId, SessionStatus status);
}
