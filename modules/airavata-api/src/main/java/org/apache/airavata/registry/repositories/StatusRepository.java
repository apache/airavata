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
package org.apache.airavata.registry.repositories;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.entities.StatusEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified repository for status records across all parent types.
 *
 * <p>This repository consolidates the functionality of:
 * <ul>
 *   <li>{@code ExperimentStatusRepository}</li>
 *   <li>{@code ProcessStatusRepository}</li>
 *   <li>{@code TaskStatusRepository}</li>
 *   <li>{@code JobStatusRepository}</li>
 *   <li>{@code QueueStatusRepository}</li>
 * </ul>
 */
@Repository
public interface StatusRepository extends JpaRepository<StatusEntity, StatusEntityPK> {

    /**
     * Find all statuses for a specific parent entity, ordered by creation (most recent first).
     * Uses sequenceNum as primary sort key for deterministic ordering (auto-increment guarantees creation order).
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return list of status entities ordered by sequence number descending
     */
    @Query("SELECT s FROM StatusEntity s WHERE s.parentId = :parentId AND s.parentType = :parentType ORDER BY s.sequenceNum DESC")
    List<StatusEntity> findByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") StatusParentType parentType);

    /**
     * Find the most recent status for a specific parent entity.
     * Uses sequenceNum for deterministic ordering (auto-increment guarantees creation order).
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return the most recent status entity, if any
     */
    @Query("SELECT s FROM StatusEntity s WHERE s.parentId = :parentId AND s.parentType = :parentType ORDER BY s.sequenceNum DESC LIMIT 1")
    Optional<StatusEntity> findLatestByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") StatusParentType parentType);

    /**
     * Find all statuses for an experiment.
     *
     * @param experimentId the experiment ID
     * @return list of status entities
     */
    default List<StatusEntity> findByExperimentId(String experimentId) {
        return findByParentIdAndParentType(experimentId, StatusParentType.EXPERIMENT);
    }

    /**
     * Find the latest status for an experiment.
     *
     * @param experimentId the experiment ID
     * @return the latest status entity, if any
     */
    default Optional<StatusEntity> findLatestByExperimentId(String experimentId) {
        return findLatestByParentIdAndParentType(experimentId, StatusParentType.EXPERIMENT);
    }

    /**
     * Find all statuses for a process.
     *
     * @param processId the process ID
     * @return list of status entities
     */
    default List<StatusEntity> findByProcessId(String processId) {
        return findByParentIdAndParentType(processId, StatusParentType.PROCESS);
    }

    /**
     * Find the latest status for a process.
     *
     * @param processId the process ID
     * @return the latest status entity, if any
     */
    default Optional<StatusEntity> findLatestByProcessId(String processId) {
        return findLatestByParentIdAndParentType(processId, StatusParentType.PROCESS);
    }

    /**
     * Find all statuses for a task.
     *
     * @param taskId the task ID
     * @return list of status entities
     */
    default List<StatusEntity> findByTaskId(String taskId) {
        return findByParentIdAndParentType(taskId, StatusParentType.TASK);
    }

    /**
     * Find the latest status for a task.
     *
     * @param taskId the task ID
     * @return the latest status entity, if any
     */
    default Optional<StatusEntity> findLatestByTaskId(String taskId) {
        return findLatestByParentIdAndParentType(taskId, StatusParentType.TASK);
    }

    /**
     * Find all statuses for a job.
     *
     * @param jobId the job ID
     * @return list of status entities
     */
    default List<StatusEntity> findByJobId(String jobId) {
        return findByParentIdAndParentType(jobId, StatusParentType.JOB);
    }

    /**
     * Find the latest status for a job.
     *
     * @param jobId the job ID
     * @return the latest status entity, if any
     */
    default Optional<StatusEntity> findLatestByJobId(String jobId) {
        return findLatestByParentIdAndParentType(jobId, StatusParentType.JOB);
    }

    /**
     * Find all statuses for a workflow.
     *
     * @param workflowId the workflow ID
     * @return list of status entities
     */
    default List<StatusEntity> findByWorkflowId(String workflowId) {
        return findByParentIdAndParentType(workflowId, StatusParentType.WORKFLOW);
    }

    /**
     * Delete all statuses for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     */
    @Modifying
    @Query("DELETE FROM StatusEntity s WHERE s.parentId = :parentId AND s.parentType = :parentType")
    void deleteByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") StatusParentType parentType);

    /**
     * Count statuses for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return count of statuses
     */
    @Query("SELECT COUNT(s) FROM StatusEntity s WHERE s.parentId = :parentId AND s.parentType = :parentType")
    long countByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") StatusParentType parentType);

    /**
     * Find all statuses by state value.
     *
     * @param state the state value
     * @param parentType the type of parent entity
     * @return list of status entities
     */
    @Query("SELECT s FROM StatusEntity s WHERE s.state = :state AND s.parentType = :parentType")
    List<StatusEntity> findByStateAndParentType(
            @Param("state") String state, @Param("parentType") StatusParentType parentType);

    /**
     * Find all queue statuses (latest for each queue).
     *
     * @return list of queue status entities
     */
    @Query("SELECT s FROM StatusEntity s WHERE s.parentType = 'QUEUE' ORDER BY s.timeOfStateChange DESC")
    List<StatusEntity> findAllQueueStatuses();
}
