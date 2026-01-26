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
import org.apache.airavata.registry.entities.InputDataEntity;
import org.apache.airavata.registry.entities.InputDataEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified repository for input data records across all parent types.
 *
 * <p>This repository consolidates the functionality of:
 * <ul>
 *   <li>{@code ExperimentInputRepository}</li>
 *   <li>{@code ProcessInputRepository}</li>
 *   <li>{@code ApplicationInputRepository}</li>
 * </ul>
 */
@Repository
public interface InputDataRepository extends JpaRepository<InputDataEntity, InputDataEntityPK> {

    /**
     * Find all inputs for a specific parent entity, ordered by input order.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return list of input data entities ordered by input order
     */
    @Query("SELECT i FROM InputDataEntity i WHERE i.parentId = :parentId AND i.parentType = :parentType ORDER BY i.inputOrder")
    List<InputDataEntity> findByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") DataObjectParentType parentType);

    /**
     * Find all inputs for an experiment.
     *
     * @param experimentId the experiment ID
     * @return list of input data entities
     */
    default List<InputDataEntity> findByExperimentId(String experimentId) {
        return findByParentIdAndParentType(experimentId, DataObjectParentType.EXPERIMENT);
    }

    /**
     * Find all inputs for a process.
     *
     * @param processId the process ID
     * @return list of input data entities
     */
    default List<InputDataEntity> findByProcessId(String processId) {
        return findByParentIdAndParentType(processId, DataObjectParentType.PROCESS);
    }

    /**
     * Find all inputs for an application.
     *
     * @param applicationId the application interface ID
     * @return list of input data entities
     */
    default List<InputDataEntity> findByApplicationId(String applicationId) {
        return findByParentIdAndParentType(applicationId, DataObjectParentType.APPLICATION);
    }

    /**
     * Find all inputs for a handler.
     *
     * @param handlerId the handler ID
     * @return list of input data entities
     */
    default List<InputDataEntity> findByHandlerId(String handlerId) {
        return findByParentIdAndParentType(handlerId, DataObjectParentType.HANDLER);
    }

    /**
     * Delete all inputs for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM InputDataEntity i WHERE i.parentId = :parentId AND i.parentType = :parentType")
    void deleteByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") DataObjectParentType parentType);

    /**
     * Count inputs for a specific parent entity.
     *
     * @param parentId the parent entity ID
     * @param parentType the type of parent entity
     * @return count of inputs
     */
    @Query("SELECT COUNT(i) FROM InputDataEntity i WHERE i.parentId = :parentId AND i.parentType = :parentType")
    long countByParentIdAndParentType(
            @Param("parentId") String parentId, @Param("parentType") DataObjectParentType parentType);
}
