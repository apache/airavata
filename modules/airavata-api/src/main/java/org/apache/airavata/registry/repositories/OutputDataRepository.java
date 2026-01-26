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
import org.apache.airavata.common.model.DataObjectParentType;
import org.apache.airavata.registry.entities.OutputDataEntity;
import org.apache.airavata.registry.entities.OutputDataEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified repository for output data records across all parent types.
 *
 * <p>This repository consolidates the functionality of:
 * <ul>
 *   <li>{@code ExperimentOutputRepository}</li>
 *   <li>{@code ProcessOutputRepository}</li>
 *   <li>{@code ApplicationOutputRepository}</li>
 * </ul>
 */
@Repository
public interface OutputDataRepository extends JpaRepository<OutputDataEntity, OutputDataEntityPK> {

    /**
     * Find all outputs for a specific parent entity, ordered by output order.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return list of output data entities ordered by output order
     */
    @Query("SELECT o FROM OutputDataEntity o WHERE o.parentId = :parentId AND o.parentType = :parentType ORDER BY o.outputOrder")
    List<OutputDataEntity> findByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") DataObjectParentType parentType);

    /**
     * Find all outputs for an experiment.
     *
     * @param experimentId the experiment ID
     * @return list of output data entities
     */
    default List<OutputDataEntity> findByExperimentId(String experimentId) {
        return findByParentIdAndParentType(experimentId, DataObjectParentType.EXPERIMENT);
    }

    /**
     * Find all outputs for a process.
     *
     * @param processId the process ID
     * @return list of output data entities
     */
    default List<OutputDataEntity> findByProcessId(String processId) {
        return findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
    }

    /**
     * Find all outputs for an application.
     *
     * @param applicationId the application interface ID
     * @return list of output data entities
     */
    default List<OutputDataEntity> findByApplicationId(String applicationId) {
        return findByParentIdAndParentType(applicationId, DataObjectParentType.APPLICATION);
    }

    /**
     * Find all outputs for a handler.
     *
     * @param handlerId the handler ID
     * @return list of output data entities
     */
    default List<OutputDataEntity> findByHandlerId(String handlerId) {
        return findByParentIdAndParentType(handlerId, DataObjectParentType.HANDLER);
    }

    /**
     * Delete all outputs for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM OutputDataEntity o WHERE o.parentId = :parentId AND o.parentType = :parentType")
    void deleteByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") DataObjectParentType parentType);

    /**
     * Count outputs for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return count of outputs
     */
    @Query("SELECT COUNT(o) FROM OutputDataEntity o WHERE o.parentId = :parentId AND o.parentType = :parentType")
    long countByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") DataObjectParentType parentType);
}
