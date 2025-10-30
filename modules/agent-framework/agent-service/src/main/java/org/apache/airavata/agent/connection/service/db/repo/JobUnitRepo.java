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
package org.apache.airavata.agent.connection.service.db.repo;

import org.apache.airavata.agent.connection.service.db.entity.JobUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobUnitRepo extends JpaRepository<JobUnitEntity, String> {

    @Query(
            value =
                    """
            SELECT ID
            FROM JOB_UNIT
            WHERE BATCH_ID = :batchId AND STATUS = 'PENDING'
            ORDER BY CREATED_AT, ID
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true)
    String lockNextPending(@Param("batchId") String batchId);

    @Modifying
    @Query(
            value =
                    """
            UPDATE JOB_UNIT SET STATUS='IN_PROGRESS', AGENT_ID=:agentId, STARTED_AT=CURRENT_TIMESTAMP(6) WHERE ID=:id
            """,
            nativeQuery = true)
    int markInProgress(@Param("id") String id, @Param("agentId") String agentId);

    @Query(value = "SELECT RESOLVED_COMMAND FROM JOB_UNIT WHERE ID=:id", nativeQuery = true)
    String getResolvedCommand(@Param("id") String id);

    @Modifying
    @Query(
            value =
                    """
            UPDATE JOB_UNIT SET STATUS='COMPLETED', COMPLETED_AT=CURRENT_TIMESTAMP(6) WHERE ID=:id
            """,
            nativeQuery = true)
    int markCompleted(@Param("id") String id);

    @Query(
            value =
                    """
            SELECT COUNT(*)
            FROM JOB_UNIT
            WHERE BATCH_ID = :batchId AND STATUS IN ('PENDING','IN_PROGRESS')
            """,
            nativeQuery = true)
    int countRemaining(@Param("batchId") String batchId);
}
