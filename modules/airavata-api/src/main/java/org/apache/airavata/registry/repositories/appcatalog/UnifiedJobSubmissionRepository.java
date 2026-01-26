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
package org.apache.airavata.registry.repositories.appcatalog;

import java.util.List;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.registry.entities.appcatalog.UnifiedJobSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for unified job submission entities.
 */
@Repository
public interface UnifiedJobSubmissionRepository extends JpaRepository<UnifiedJobSubmissionEntity, String> {

    /**
     * Find all job submissions for a compute resource.
     */
    List<UnifiedJobSubmissionEntity> findByComputeResourceId(String computeResourceId);

    /**
     * Find all job submissions for a compute resource, ordered by priority.
     */
    @Query("SELECT e FROM UnifiedJobSubmissionEntity e WHERE e.computeResourceId = :resourceId ORDER BY e.priorityOrder ASC NULLS LAST")
    List<UnifiedJobSubmissionEntity> findByComputeResourceIdOrderByPriority(@Param("resourceId") String computeResourceId);

    /**
     * Find all job submissions for a specific protocol.
     */
    List<UnifiedJobSubmissionEntity> findByProtocol(JobSubmissionProtocol protocol);

    /**
     * Find job submissions for a compute resource with a specific protocol.
     */
    List<UnifiedJobSubmissionEntity> findByComputeResourceIdAndProtocol(
            String computeResourceId, JobSubmissionProtocol protocol);

    /**
     * Find all job submissions that use a specific resource job manager.
     */
    List<UnifiedJobSubmissionEntity> findByResourceJobManagerId(String resourceJobManagerId);

    /**
     * Delete all job submissions for a compute resource.
     */
    void deleteByComputeResourceId(String computeResourceId);

    /**
     * Check if a compute resource has any job submission interfaces.
     */
    boolean existsByComputeResourceId(String computeResourceId);

    /**
     * Get the highest priority (lowest number) submission for a compute resource.
     */
    @Query("SELECT e FROM UnifiedJobSubmissionEntity e WHERE e.computeResourceId = :resourceId ORDER BY e.priorityOrder ASC NULLS LAST LIMIT 1")
    UnifiedJobSubmissionEntity findTopByComputeResourceIdOrderByPriority(@Param("resourceId") String computeResourceId);
}
