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
import org.apache.airavata.common.model.ErrorParentType;
import org.apache.airavata.registry.entities.ErrorEntity;
import org.apache.airavata.registry.entities.ErrorEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified repository for error records across all parent types.
 *
 * <p>This repository consolidates the functionality of:
 * <ul>
 *   <li>{@code ExperimentErrorRepository}</li>
 *   <li>{@code ProcessErrorRepository}</li>
 *   <li>{@code TaskErrorRepository}</li>
 * </ul>
 */
@Repository
public interface ErrorRepository extends JpaRepository<ErrorEntity, ErrorEntityPK> {

    /**
     * Find all errors for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return list of error entities
     */
    @Query("SELECT e FROM ErrorEntity e WHERE e.parentId = :parentId AND e.parentType = :parentType ORDER BY e.creationTime DESC")
    List<ErrorEntity> findByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") ErrorParentType parentType);

    /**
     * Find all errors for an experiment.
     *
     * @param experimentId the experiment ID
     * @return list of error entities
     */
    default List<ErrorEntity> findByExperimentId(String experimentId) {
        return findByParentIdAndParentType(experimentId, ErrorParentType.EXPERIMENT);
    }

    /**
     * Find all errors for a process.
     *
     * @param processId the process ID
     * @return list of error entities
     */
    default List<ErrorEntity> findByProcessId(String processId) {
        return findByParentIdAndParentType(processId, ErrorParentType.PROCESS);
    }

    /**
     * Find all errors for a task.
     *
     * @param taskId the task ID
     * @return list of error entities
     */
    default List<ErrorEntity> findByTaskId(String taskId) {
        return findByParentIdAndParentType(taskId, ErrorParentType.TASK);
    }

    /**
     * Find all errors for a workflow.
     *
     * @param workflowId the workflow ID
     * @return list of error entities
     */
    default List<ErrorEntity> findByWorkflowId(String workflowId) {
        return findByParentIdAndParentType(workflowId, ErrorParentType.WORKFLOW);
    }

    /**
     * Find all errors for an application.
     *
     * @param applicationId the application ID
     * @return list of error entities
     */
    default List<ErrorEntity> findByApplicationId(String applicationId) {
        return findByParentIdAndParentType(applicationId, ErrorParentType.APPLICATION);
    }

    /**
     * Find all errors for a handler.
     *
     * @param handlerId the handler ID
     * @return list of error entities
     */
    default List<ErrorEntity> findByHandlerId(String handlerId) {
        return findByParentIdAndParentType(handlerId, ErrorParentType.HANDLER);
    }

    /**
     * Delete all errors for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     */
    @Modifying
    @Query("DELETE FROM ErrorEntity e WHERE e.parentId = :parentId AND e.parentType = :parentType")
    void deleteByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") ErrorParentType parentType);

    /**
     * Count errors for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return count of errors
     */
    @Query("SELECT COUNT(e) FROM ErrorEntity e WHERE e.parentId = :parentId AND e.parentType = :parentType")
    long countByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") ErrorParentType parentType);

    /**
     * Check if any errors exist for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return true if errors exist
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ErrorEntity e WHERE e.parentId = :parentId AND e.parentType = :parentType")
    boolean existsByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") ErrorParentType parentType);
}
